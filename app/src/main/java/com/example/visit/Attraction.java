package com.example.visit;

public class Attraction {
    public String name;
    public double lat;
    public double lon;
    public String description;
    public String imageFileName;
    public boolean isVisited;

    public Attraction(String name, double lat, double lon, String description, String imageFileName, boolean isVisited) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.description = description;
        this.imageFileName = imageFileName;
        this.isVisited = isVisited;
    }
}