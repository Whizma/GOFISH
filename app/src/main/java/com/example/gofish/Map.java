package com.example.gofish;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

public class Map extends AppCompatActivity {
    private ImageView fishMapImage;

    private ImageButton beachButton;
    private ImageButton dockButton;
    private ImageButton lakeButton;

    private String beachInfo;
    private String dockInfo;
    private String lakeInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        beachButton = (ImageButton) findViewById(R.id.beach);
        dockButton = (ImageButton) findViewById(R.id.dock);
        lakeButton = (ImageButton) findViewById(R.id.lake);

        beachInfo = "Continue your fishing journey at the beach, and try to catch the elusive Mahi Mahi";
        dockInfo = "The dock is a great starting point for catching your first fish, the friendly Nemo!";
        lakeInfo = "Let any fish who meets your gaze learn the true meaning of fear; for you are the harbinger of death. The bane of creatures sub-aqueous, your rod is true and unwavering as you cast into the aquatic abyss. A man, scorned by this uncaring Earth, finds solace in the sea. Your only friend, the worm upon my hook. Wriggling, writhing, struggling to surmount the mortal pointlessness that permeates this barren world. You am alone. You  am empty. And yet, You fish. Beware of the lake, and beware of the ole mighty GammelGäddan";


        beachButton.setOnClickListener(new OnLocationClickListener("beach", beachInfo, R.drawable.beach_popup));
        dockButton.setOnClickListener(new OnLocationClickListener("dock", dockInfo, R.drawable.dock_popup));
        lakeButton.setOnClickListener(new OnLocationClickListener("lake", lakeInfo, R.drawable.lake_popup));
    }

    private void showPopupDialog(final String location, String information, int locationImageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.custom_dialog_layout, null); // Inflate custom dialog layout
        builder.setView(dialogView); // Set custom layout to dialog

        ImageView imageView = dialogView.findViewById(R.id.dialog_image); // Reference to ImageView in custom dialog layout
        imageView.setImageResource(locationImageId); // Set the image resource

        builder.setTitle(location.toUpperCase())
                .setMessage(information)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle OK button click
                        dialog.dismiss(); // Close the dialog
                        Intent intent = new Intent(Map.this, FishingGame.class);
                        intent.putExtra("location", location);
                        startActivity(intent);

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle Cancel button click
                        dialog.dismiss(); // Close the dialog
                    }
                })
                .show(); // Display the dialog
    }
    class OnLocationClickListener implements View.OnClickListener {
        private String location;
        private String information;
        private int locationImageId;
        public OnLocationClickListener(String location, String information, int locationImageId) {
            this.location = location;
            this.information = information;
            this.locationImageId = locationImageId;
        }
        @Override
        public void onClick(View v) {
            showPopupDialog(location, information, locationImageId);

        }
    }
}