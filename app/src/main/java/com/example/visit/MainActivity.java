package com.example.visit;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ImageButton; // Dodany import dla ImageButton
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView; // Dodany import dla MapView

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainApp";

    // Moduły (klasy pomocnicze)
    private AttractionsRepository repository;
    private MapManager mapManager;
    private BottomSheetManager bottomSheetManager;
    private ProgressManager progressManager;

    // Dodana referencja do MapView (użyteczna w setupZoomButtons)
    private MapView map;

    private ArrayList<Attraction> attractions;
    private Attraction currentZoneAttraction = null;

    // Uprawnienia
    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> {
                if (granted) initLocationLogic();
                else Toast.makeText(this, "Brak zgody na lokalizację!", Toast.LENGTH_SHORT).show();
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Konfiguracja OSM
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_main);

        // 1. Inicjalizacja komponentów
        repository = new AttractionsRepository(this);
        progressManager = new ProgressManager(getWindow().getDecorView());
        bottomSheetManager = new BottomSheetManager(this, findViewById(R.id.bottom_sheet));

        // Inicjalizacja MapView
        map = findViewById(R.id.map);
        mapManager = new MapManager(this, map);

        // 2. Wczytanie danych
        attractions = repository.loadAttractionsFromXml("attractions.xml");
        progressManager.updateProgress(attractions);

        // 3. Konfiguracja Mapy i Markerów
        mapManager.addMarkers(attractions, this::onMarkerClicked);

        // Konfiguracja przycisków zoomu (wyłączenie wbudowanych w MapManager)
        mapManager.configureZoomControls();

        // ⭐ KLUCZOWA ZMIANA: Podłączenie niestandardowych przycisków
        setupZoomButtons();

        // 4. Sprawdzenie uprawnień i start
        checkPermissionAndStart();
    }

    /**
     * Ustawia słuchaczy dla niestandardowych przycisków zoomu
     * umieszczonych w prawym górnym rogu w pliku activity_main.xml.
     */
    private void setupZoomButtons() {
        ImageButton zoomInButton = findViewById(R.id.zoomInButton);
        ImageButton zoomOutButton = findViewById(R.id.zoomOutButton);

        // Obsługa kliknięcia dla Powiększ
        if (zoomInButton != null) {
            zoomInButton.setOnClickListener(v -> {
                if (map != null) {
                    map.getController().zoomIn();
                }
            });
        }

        // Obsługa kliknięcia dla Pomniejsz
        if (zoomOutButton != null) {
            zoomOutButton.setOnClickListener(v -> {
                if (map != null) {
                    map.getController().zoomOut();
                }
            });
        }
    }

    private void onMarkerClicked(Attraction attraction) {
        if (attraction.isVisited) {
            bottomSheetManager.show(attraction, true);
        } else {
            Toast.makeText(this, "Podejdź bliżej, aby odkryć to miejsce!", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            initLocationLogic();
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void initLocationLogic() {
        // Uruchamiamy warstwę lokalizacji na mapie
        mapManager.initLocationOverlay(() -> {
            // KLUCZOWE: Używamy runOnUiThread, aby obsłużyć MapView z głównego wątku.
            runOnUiThread(() -> {
                // Po złapaniu fixa (pierwszej lokalizacji) przybliżamy mapę
                GeoPoint userLoc = mapManager.getUserLocation();
                if (userLoc != null) {
                    mapManager.zoomToLocation(userLoc, 18.0);
                    Toast.makeText(this, "Lokalizacja ustalona!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Uruchamiamy wątek sprawdzania odległości
        startDistanceCheckThread();
    }

    private void startDistanceCheckThread() {
        new Thread(() -> {
            while (true) {
                GeoPoint current = mapManager.getUserLocation();
                if (current != null) {
                    checkDistances(current);
                }
                try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
            }
        }).start();
    }

    private void checkDistances(GeoPoint current) {
        Attraction nearby = null;
        double minDistance = Double.MAX_VALUE;

        for (Attraction a : attractions) {
            double dist = distance(current.getLatitude(), current.getLongitude(), a.lat, a.lon);

            // Logika "jesteś w pobliżu" (< 50m)
            if (dist < 50 && dist < minDistance) {
                minDistance = dist;
                nearby = a;
            }
        }

        handleNearbyAttraction(nearby);
    }

    private void handleNearbyAttraction(Attraction nearby) {
        if (nearby != null) {
            // Jeśli weszliśmy w nową strefę
            if (currentZoneAttraction != nearby) {
                currentZoneAttraction = nearby;

                // Jeśli nieodkryte -> odkryj
                if (!nearby.isVisited) {
                    unlockAttraction(nearby);
                }

                // Pokaż panel (false = nie można zamknąć dopóki tam jesteś)
                runOnUiThread(() -> bottomSheetManager.show(nearby, false));
            }
        } else {
            // Jeśli wyszliśmy ze strefy
            if (currentZoneAttraction != null) {
                currentZoneAttraction = null;
                runOnUiThread(() -> bottomSheetManager.hide());
            }
        }
    }

    private void unlockAttraction(Attraction attraction) {
        attraction.isVisited = true;
        repository.saveVisited(attraction.name); // Zapis do pamięci telefonu

        // Działania na UI muszą być w głównym wątku!
        runOnUiThread(() -> {
            Toast.makeText(this, "ODKRYTO: " + attraction.name + "!", Toast.LENGTH_LONG).show();
            mapManager.refreshMarker(attraction); // Odśwież ikonę na mapie
            progressManager.updateProgress(attractions); // Odśwież pasek postępu
        });
    }

    // Pomocnicza metoda obliczania odległości
    private double distance(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }

    @Override
    protected void onResume() { super.onResume(); mapManager.onResume(); }
    @Override
    protected void onPause() { super.onPause(); mapManager.onPause(); }
}