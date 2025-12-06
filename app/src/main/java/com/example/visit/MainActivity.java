package com.example.visit;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DEBUG_MAPA";

    // Menedżer postępu (deklaracja)
    private ProgressManager progressManager;

    private MapView map;
    private MyLocationNewOverlay locationOverlay;
    private ActivityResultLauncher<String> permissionLauncher;

    private ArrayList<Attraction> attractions;
    private HashMap<String, Marker> markersMap = new HashMap<>();

    private SharedPreferences prefs;

    private View bottomSheet;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private TextView sheetTitle, sheetDescription;
    private Button btnDetails, btnVR;

    private Attraction currentZoneAttraction = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        // Ważne: setContentView musi być przed inicjalizacją widoków
        setContentView(R.layout.activity_main);

        // 1. Inicjalizacja ProgressManager (teraz bezpiecznie, bo widok istnieje)
        progressManager = new ProgressManager(getWindow().getDecorView());

        Log.d(TAG, "=== Uruchamianie aplikacji ===");

        prefs = getSharedPreferences("visited_places", Context.MODE_PRIVATE);

        map = findViewById(R.id.map);
        map.setMultiTouchControls(true);

        // Wczytanie danych
        attractions = loadAttractions();

        // 2. Pierwsze odświeżenie paska postępu na starcie
        progressManager.updateProgress(attractions);

        setupBottomSheet();
        addMarkersToMap();

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        Log.d(TAG, "Uprawnienia lokalizacji: PRZYZNANE");
                        initMapWithLocation();
                    } else {
                        Log.e(TAG, "Uprawnienia lokalizacji: ODMOWA");
                        Toast.makeText(this, "Brak zgody na lokalizację!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        checkPermission();
    }

    // --- GŁÓWNA LOGIKA MARKERÓW Z DEBUGOWANIEM ---

    private void addMarkersToMap() {
        markersMap.clear();
        Log.d(TAG, "Dodawanie markerów na mapę. Ilość atrakcji: " + attractions.size());

        for (Attraction attraction : attractions) {
            Marker marker = new Marker(map);
            marker.setPosition(new GeoPoint(attraction.lat, attraction.lon));
            marker.setTitle(attraction.name);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            updateMarkerIcon(marker, attraction);

            marker.setOnMarkerClickListener((m, mapView) -> {
                Log.d(TAG, "Kliknięto marker: " + attraction.name + " | Odwiedzone: " + attraction.isVisited);
                if (attraction.isVisited) {
                    showBottomSheet(attraction, true);
                } else {
                    Toast.makeText(MainActivity.this, "Podejdź bliżej, aby odkryć to miejsce!", Toast.LENGTH_SHORT).show();
                }
                return true;
            });

            map.getOverlays().add(marker);
            markersMap.put(attraction.name, marker);
        }
        map.invalidate();
    }

    private void updateMarkerIcon(Marker marker, Attraction attraction) {
        if (attraction.isVisited) {
            String folderName = "zdjecia";
            String fileName = attraction.imageFileName;
            String fullPath = folderName + "/" + fileName;

            Log.d(TAG, ">>> Próba wczytania zdjęcia dla: " + attraction.name);

            try {
                // --- DEBUG: SPRAWDZENIE CZY PLIK ISTNIEJE W ASSETS ---
                String[] filesInFolder = getAssets().list(folderName);
                boolean exists = false;
                if (filesInFolder != null) {
                    for (String f : filesInFolder) {
                        if (f.equals(fileName)) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        Log.e(TAG, "❌ BŁĄD KRYTYCZNY: Pliku '" + fileName + "' NIE MA w folderze 'assets/" + folderName + "'!");
                    } else {
                        Log.d(TAG, "✅ Plik '" + fileName + "' znaleziony na liście plików assets.");
                    }
                }
                // -----------------------------------------------------

                InputStream is = getAssets().open(fullPath);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                is.close();

                if (bitmap != null) {
                    Bitmap circularBitmap = getCircularBitmap(bitmap);
                    Drawable bigIcon = new BitmapDrawable(getResources(),
                            Bitmap.createScaledBitmap(circularBitmap, 150, 150, true));

                    marker.setIcon(bigIcon);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                    Log.d(TAG, "🎉 SUKCES: Ikonka podmieniona dla " + attraction.name);
                } else {
                    Log.e(TAG, "❌ BŁĄD: InputStream otwarty, ale Bitmapa jest null (uszkodzony plik?): " + fullPath);
                }

            } catch (IOException e) {
                Log.e(TAG, "💥 WYJĄTEK (IO) przy ładowaniu zdjęcia: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private Bitmap getCircularBitmap(Bitmap bitmap) {
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, size, size);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(0xFFFFFFFF);

        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, null, rect, paint);

        paint.setXfermode(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(android.graphics.Color.WHITE);
        paint.setStrokeWidth(10);
        canvas.drawCircle(size / 2f, size / 2f, (size / 2f) - 5, paint);

        return output;
    }

    // --- LOGIKA ODKRYWANIA MIEJSC ---

    private void unlockAttraction(Attraction attraction) {
        if (!attraction.isVisited) {
            Log.d(TAG, "🔓 ODKRYWANIE MIEJSCA: " + attraction.name);
            attraction.isVisited = true;
            prefs.edit().putBoolean(attraction.name, true).apply();

            runOnUiThread(() -> {
                Toast.makeText(this, "ODKRYTO: " + attraction.name + "!", Toast.LENGTH_LONG).show();

                // Aktualizacja markera
                Marker m = markersMap.get(attraction.name);
                if (m != null) {
                    updateMarkerIcon(m, attraction);
                    map.invalidate();
                }

                // 3. Aktualizacja paska postępu po odkryciu
                progressManager.updateProgress(attractions);
            });
        }
    }

    // --- SETUP UI ---

    private void setupBottomSheet() {
        bottomSheet = findViewById(R.id.bottom_sheet);

        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int targetHeight = (int) (screenHeight * 0.66);

        bottomSheet.getLayoutParams().height = targetHeight;
        bottomSheet.requestLayout();

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        sheetTitle = bottomSheet.findViewById(R.id.sheet_title);
        sheetDescription = bottomSheet.findViewById(R.id.sheet_description);
        btnDetails = bottomSheet.findViewById(R.id.btn_details);
        btnVR = bottomSheet.findViewById(R.id.btn_vr);

        btnDetails.setOnClickListener(v -> {
            if (sheetDescription.getVisibility() == View.GONE) {
                sheetDescription.setVisibility(View.VISIBLE);
                btnDetails.setText("UKRYJ OPIS");
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                sheetDescription.setVisibility(View.GONE);
                btnDetails.setText("OPIS");
            }
        });

        btnVR.setOnClickListener(v -> Toast.makeText(this, "Tryb VR wkrótce...", Toast.LENGTH_SHORT).show());
    }

    private void showBottomSheet(Attraction attraction, boolean allowHide) {
        runOnUiThread(() -> {
            sheetTitle.setText(attraction.name);
            sheetDescription.setText(attraction.description);

            sheetDescription.setVisibility(View.VISIBLE);
            btnDetails.setText("UKRYJ OPIS");

            bottomSheetBehavior.setHideable(allowHide);

            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN ||
                    bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
    }

    private void hideBottomSheet() {
        runOnUiThread(() -> {
            bottomSheetBehavior.setHideable(true);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        });
    }

    // --- LOKALIZACJA ---

    private void initMapWithLocation() {
        locationOverlay = new MyLocationNewOverlay(map);
        locationOverlay.enableMyLocation();
        map.getOverlays().add(locationOverlay);

        locationOverlay.runOnFirstFix(() -> runOnUiThread(() -> {
            GeoPoint user = locationOverlay.getMyLocation();
            if (user != null) {
                Log.d(TAG, "Ustalono pierwszą lokalizację: " + user.toDoubleString());
                map.getController().setZoom(18.0);
                map.getController().animateTo(user);
            }
        }));

        new Thread(() -> {
            while (true) {
                GeoPoint current = locationOverlay.getMyLocation();
                if (current != null) {
                    Attraction nearby = null;
                    double minDistance = Double.MAX_VALUE;

                    for (Attraction a : attractions) {
                        double d = distance(current.getLatitude(), current.getLongitude(), a.lat, a.lon);

                        if (d < 100) {
                            Log.d(TAG, "Jesteś blisko: " + a.name + " (" + (int)d + "m)");
                        }

                        if (d < 50) {
                            if (d < minDistance) {
                                minDistance = d;
                                nearby = a;
                            }
                        }
                    }

                    if (nearby != null) {
                        if (currentZoneAttraction != nearby) {
                            currentZoneAttraction = nearby;
                            unlockAttraction(nearby);
                            showBottomSheet(nearby, false);
                        }
                    } else {
                        if (currentZoneAttraction != null) {
                            currentZoneAttraction = null;
                            hideBottomSheet();
                        }
                    }
                }
                try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
            }
        }).start();
    }

    // --- METODY POMOCNICZE I XML ---

    private ArrayList<Attraction> loadAttractions() {
        ArrayList<Attraction> list = new ArrayList<>();
        Log.d(TAG, "Rozpoczynam wczytywanie XML...");

        try {
            InputStream is = getAssets().open("attractions.xml");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);

            NodeList items = doc.getElementsByTagName("attraction");

            for (int i = 0; i < items.getLength(); i++) {
                Element e = (Element) items.item(i);

                String name = e.getElementsByTagName("name").item(0).getTextContent();
                double lat = Double.parseDouble(e.getElementsByTagName("lat").item(0).getTextContent());
                double lon = Double.parseDouble(e.getElementsByTagName("lon").item(0).getTextContent());

                String description = "";
                NodeList descNodes = e.getElementsByTagName("description");
                if (descNodes.getLength() > 0) description = descNodes.item(0).getTextContent();

                String imageFile = "";
                NodeList imgNodes = e.getElementsByTagName("photo");
                if (imgNodes.getLength() > 0) imageFile = imgNodes.item(0).getTextContent();

                boolean visited = prefs.getBoolean(name, false);

                Log.d(TAG, "Wczytano: " + name + " | Plik: " + imageFile + " | Odwiedzone: " + visited);
                list.add(new Attraction(name, lat, lon, description, imageFile, visited));
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ BŁĄD XML: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            initMapWithLocation();
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @Override
    protected void onResume() { super.onResume(); if (map != null) map.onResume(); }
    @Override
    protected void onPause() { super.onPause(); if (map != null) map.onPause(); }
}