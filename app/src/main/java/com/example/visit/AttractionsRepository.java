package com.example.visit;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class AttractionsRepository {
    private static final String TAG = "AttractionsRepo";
    private static final String PREFS_NAME = "visited_places";
    private final Context context;
    private final SharedPreferences prefs;

    public AttractionsRepository(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public ArrayList<Attraction> loadAttractionsFromXml(String fileName) {
        ArrayList<Attraction> list = new ArrayList<>();
        try {
            InputStream is = context.getAssets().open(fileName);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);

            NodeList items = doc.getElementsByTagName("attraction");

            for (int i = 0; i < items.getLength(); i++) {
                Element e = (Element) items.item(i);
                String name = getTagValue(e, "name");
                double lat = Double.parseDouble(getTagValue(e, "lat"));
                double lon = Double.parseDouble(getTagValue(e, "lon"));
                String desc = getTagValue(e, "description");
                String photo = getTagValue(e, "photo");

                boolean visited = prefs.getBoolean(name, false);
                list.add(new Attraction(name, lat, lon, desc, photo, visited));
            }
        } catch (Exception e) {
            Log.e(TAG, "Błąd wczytywania XML: " + e.getMessage());
        }
        return list;
    }

    private String getTagValue(Element e, String tagName) {
        NodeList list = e.getElementsByTagName(tagName);
        if (list != null && list.getLength() > 0) {
            return list.item(0).getTextContent();
        }
        return "";
    }

    public void saveVisited(String attractionName) {
        prefs.edit().putBoolean(attractionName, true).apply();
    }
}