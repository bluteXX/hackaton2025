package com.example.visit;

import org.osmdroid.views.overlay.Marker; // <-- DODAJ TEN IMPORT NA GÓRZE

public class Attraction {
    public String name;
    public double lat;
    public double lon;
    public String description;
    public String imageFileName;
    public boolean isVisited;

    // NOWE POLE:
    // To pozwoli nam odnaleźć pineskę na mapie, gdy będziemy chcieli zmienić jej wygląd
    public Marker markerRef = null;

    public Attraction(String name, double lat, double lon, String description, String imageFileName, boolean isVisited) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.description = description;
        this.imageFileName = imageFileName;
        this.isVisited = isVisited;
    }
}