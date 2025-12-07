package com.example.visit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class MapManager {
    private final Context context;
    private final MapView map;
    private MyLocationNewOverlay locationOverlay;
    private final HashMap<String, Marker> markersMap = new HashMap<>();
    private OnMarkerClickListener listener;

    public interface OnMarkerClickListener {
        void onMarkerClick(Attraction attraction);
    }

    public MapManager(Context context, MapView map) {
        this.context = context;
        this.map = map;
        this.map.setMultiTouchControls(true);
    }

    /**
     * Konfiguruje widoczność przycisków zoomu.
     */
    public void configureZoomControls() {
        // WYŁĄCZ wbudowane kontrolki zoomu
        map.setBuiltInZoomControls(false);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
    }

    public void initLocationOverlay(Runnable onFirstFix) {
        locationOverlay = new MyLocationNewOverlay(map);
        locationOverlay.enableMyLocation();
        map.getOverlays().add(locationOverlay);

        if (onFirstFix != null) {
            locationOverlay.runOnFirstFix(onFirstFix);
        }
    }

    public GeoPoint getUserLocation() {
        if (locationOverlay != null) return locationOverlay.getMyLocation();
        return null;
    }

    public void zoomToLocation(GeoPoint point, double zoom) {
        if (point != null) {
            map.getController().setZoom(zoom);
            map.getController().animateTo(point);
        }
    }

    public void addMarkers(ArrayList<Attraction> attractions, OnMarkerClickListener listener) {
        this.listener = listener;

        // Czyścimy tylko markery atrakcji, zostawiając inne overlays
        markersMap.clear();

        for (Attraction a : attractions) {
            Marker marker = new Marker(map);
            marker.setPosition(new GeoPoint(a.lat, a.lon));
            marker.setTitle(a.name);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            marker.setOnMarkerClickListener((m, mapView) -> {
                if (this.listener != null) this.listener.onMarkerClick(a);
                return true;
            });

            updateMarkerIcon(marker, a);
            map.getOverlays().add(marker);
            markersMap.put(a.name, marker);
        }
        map.invalidate();
    }

    public void refreshMarker(Attraction attraction) {
        Marker m = markersMap.get(attraction.name);
        if (m != null) {
            updateMarkerIcon(m, attraction);
            map.invalidate();
        }
    }

    private void updateMarkerIcon(Marker marker, Attraction attraction) {
        if (attraction.isVisited) {
            try {
                InputStream is = context.getAssets().open("zdjecia/" + attraction.imageFileName);
                Bitmap b = BitmapFactory.decodeStream(is);
                if (b != null) {
                    Bitmap circle = BitmapHelper.getCircularBitmap(b);
                    Drawable icon = new BitmapDrawable(context.getResources(),
                            Bitmap.createScaledBitmap(circle, 150, 150, true));
                    marker.setIcon(icon);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                }
                is.close();
            } catch (IOException e) {
                Log.e("MapManager", "Błąd ładowania ikony: " + e.getMessage());
            }
        } else {
            marker.setIcon(null);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        }
    }

    public void onResume() {
        if (map != null) map.onResume();
    }

    public void onPause() {
        if (map != null) map.onPause();
    }
}