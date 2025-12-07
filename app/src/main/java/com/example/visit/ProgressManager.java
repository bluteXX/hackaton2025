package com.example.visit; // Upewnij się, że pakiet jest zgodny z Twoim projektem

import android.view.View;
import android.widget.TextView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import java.util.ArrayList;

public class ProgressManager {

    private final CircularProgressIndicator progressIndicator;
    private final TextView progressText;

    // Konstruktor przyjmuje główny widok (root), żeby znaleźć w nim elementy paska postępu
    public ProgressManager(View rootView) {
        // Szukamy ID zdefiniowanych w pliku layout_progress.xml
        progressIndicator = rootView.findViewById(R.id.progress_indicator);
        progressText = rootView.findViewById(R.id.progress_text);
    }

    /**
     * Metoda oblicza postęp na podstawie listy atrakcji i aktualizuje UI.
     * @param attractions Lista wszystkich atrakcji (odwiedzonych i nieodwiedzonych)
     */
    public void updateProgress(ArrayList<Attraction> attractions) {
        if (attractions == null || attractions.isEmpty()) {
            if (progressText != null) progressText.setText("0/0");
            if (progressIndicator != null) progressIndicator.setProgress(0);
            return;
        }

        int total = attractions.size();
        int visitedCount = 0;

        // Zliczamy ile atrakcji ma flagę isVisited = true
        for (Attraction a : attractions) {
            if (a.isVisited) {
                visitedCount++;
            }
        }

        // Aktualizacja tekstu (np. "3/10")
        if (progressText != null) {
            progressText.setText(visitedCount + "/" + total);
        }

        // Aktualizacja paska graficznego (procenty)
        if (progressIndicator != null) {
            int percentage = (int) (((double) visitedCount / total) * 100);
            progressIndicator.setProgress(percentage);
        }
    }
}