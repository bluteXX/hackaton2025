package com.example.visit;

import android.content.Context;
import android.widget.Toast;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

public class FamousPathManager {

    private final MapView mapView;
    private final Context context;
    private boolean isPathActive = false;
    private List<Marker> hiddenMarkers = new ArrayList<>();

    public FamousPathManager(MapView mapView, Context context) {
        this.mapView = mapView;
        this.context = context;
    }

    public void toggleFamousPath() {
        if (!isPathActive) {
            activateFamousPath();
        } else {
            deactivateFamousPath();
        }
    }

    private void activateFamousPath() {
        hideAttractionMarkers();
        Toast.makeText(context, "Aktywowano Ścieżkę Znanej Osoby!", Toast.LENGTH_LONG).show();
        isPathActive = true;
    }

    private void deactivateFamousPath() {
        showAttractionMarkers();
        Toast.makeText(context, "Powrócono do standardowego widoku atrakcji.", Toast.LENGTH_SHORT).show();
        isPathActive = false;
    }

    private void hideAttractionMarkers() {
        hiddenMarkers.clear();

        // Przechodzimy przez wszystkie overlay na mapie
        for (int i = 0; i < mapView.getOverlays().size(); i++) {
            if (mapView.getOverlays().get(i) instanceof Marker) {
                Marker marker = (Marker) mapView.getOverlays().get(i);
                // Ukrywamy tylko znaczniki atrakcji (te z tytułem)
                if (marker.getTitle() != null && !marker.getTitle().isEmpty()) {
                    hiddenMarkers.add(marker);
                    marker.setVisible(false);
                }
            }
        }
        mapView.invalidate();
    }

    private void showAttractionMarkers() {
        for (Marker marker : hiddenMarkers) {
            marker.setVisible(true);
        }
        hiddenMarkers.clear();
        mapView.invalidate();
    }

    public boolean isPathActive() {
        return isPathActive;
    }
}