package com.example.visit;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class BottomSheetManager {
    private final BottomSheetBehavior<View> behavior;
    private final TextView title, description;
    private final Button btnDetails;

    public BottomSheetManager(Activity activity, View bottomSheetView) {
        // Ustawienie wysokości na 2/3 ekranu
        int screenHeight = activity.getResources().getDisplayMetrics().heightPixels;
        bottomSheetView.getLayoutParams().height = (int) (screenHeight * 0.66);
        bottomSheetView.requestLayout();

        behavior = BottomSheetBehavior.from(bottomSheetView);
        behavior.setHideable(true);
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        title = bottomSheetView.findViewById(R.id.sheet_title);
        description = bottomSheetView.findViewById(R.id.sheet_description);
        btnDetails = bottomSheetView.findViewById(R.id.btn_details);
        Button btnVR = bottomSheetView.findViewById(R.id.btn_vr);

        setupListeners(btnVR, activity);
    }

    private void setupListeners(Button btnVR, Activity activity) {
        btnDetails.setOnClickListener(v -> {
            if (description.getVisibility() == View.GONE) {
                description.setVisibility(View.VISIBLE);
                btnDetails.setText("UKRYJ OPIS");
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                description.setVisibility(View.GONE);
                btnDetails.setText("OPIS");
            }
        });

        btnVR.setOnClickListener(v ->
                Toast.makeText(activity, "Tryb VR wkrótce...", Toast.LENGTH_SHORT).show());
    }

    public void show(Attraction attraction, boolean allowHide) {
        title.setText(attraction.name);
        description.setText(attraction.description);
        description.setVisibility(View.VISIBLE);
        btnDetails.setText("UKRYJ OPIS");

        behavior.setHideable(allowHide);
        if (behavior.getState() == BottomSheetBehavior.STATE_HIDDEN ||
                behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    public void hide() {
        behavior.setHideable(true);
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }
}