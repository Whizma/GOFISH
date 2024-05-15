package com.example.gofish;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class Map extends AppCompatActivity {
    private ImageView fishMapImage;

    private ImageButton beachButton;
    private ImageButton dockButton;
    private ImageButton lakeButton;


    private ImageView dockStar1;
    private ImageView dockStar2;
    private ImageView lakeStar1;
    private ImageView lakeStar2;
    private ImageView lakeStar3;
    private TextView dockText;
    private TextView lakeText;




    private String beachInfo;
    private String dockInfo;
    private String lakeInfo;

    private int beach = 0;
    private int dock = 1;
    private int lake = 2;

    public boolean[] currentState = {true, false, false};

    protected void onResume() {
        super.onResume();
        MyBroadcastReceiver localBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter("locationUnlocked");
        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, intentFilter);
    }
    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            currentState = (boolean[]) extras.get("currentState");
            for (int i = 0; i < currentState.length; i++) {
                if (currentState[i] && i == 1) {
                    dockButton.setClickable(true);
                    dockButton.setAlpha(1.0f);
                    dockStar1.setAlpha(1.0f);
                    dockStar2.setAlpha(1.0f);
                    dockText.setAlpha(1.0f);
                    dockButton.setOnClickListener(new OnLocationClickListener("Dock", dockInfo, R.drawable.dock_popup2));
                }
                if (currentState[i] && i == 2) {
                    lakeButton.setClickable(true);
                    lakeButton.setAlpha(1.0f);
                    lakeStar1.setAlpha(1.0f);
                    lakeStar2.setAlpha(1.0f);
                    lakeStar3.setAlpha(1.0f);
                    lakeText.setAlpha(1.0f);
                    lakeButton.setOnClickListener(new OnLocationClickListener("Lake", lakeInfo, R.drawable.lake_popup));
                }
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        beachButton = (ImageButton) findViewById(R.id.beach);
        dockButton = (ImageButton) findViewById(R.id.dock);
        lakeButton = (ImageButton) findViewById(R.id.lake);

        dockStar1 = (ImageView) findViewById(R.id.dock_star1);
        dockStar2 = (ImageView) findViewById(R.id.dock_star2);

        lakeStar1 = (ImageView) findViewById(R.id.lake_star1);
        lakeStar2 = (ImageView) findViewById(R.id.lake_star2);
        lakeStar3 = (ImageView) findViewById(R.id.lake_star3);

        dockText = (TextView) findViewById(R.id.dockText);
        lakeText = (TextView) findViewById(R.id.lakeText);


        dockButton.setOnClickListener(null);
        dockButton.setClickable(false);
        dockStar1.setAlpha(0.3f);
        dockStar2.setAlpha(0.3f);
        dockButton.setAlpha(0.3f);
        dockText.setAlpha(0.3f);

        lakeButton.setOnClickListener(null);
        lakeButton.setClickable(false);
        lakeStar1.setAlpha(0.3f);
        lakeStar2.setAlpha(0.3f);
        lakeStar3.setAlpha(0.3f);
        lakeButton.setAlpha(0.3f);
        lakeText.setAlpha(0.3f);

        beachInfo = "The beach is a great starting point for catching your first fish, the friendly Perch";
        dockInfo = "Continue your fishing journey at the dock, and try to catch the elusive Brown Trout";
        lakeInfo = "Let any fish who meets your gaze learn the true meaning of fear; for you are the harbinger of death. The bane of creatures sub-aqueous, your rod is true and unwavering as you cast into the aquatic abyss. You, scorned by this uncaring Earth, finds solace in the sea. Your only friend, the worm upon your hook. Wriggling, writhing, struggling to surmount the mortal pointlessness that permeates this barren world. You are alone. You are empty. And yet, You fish. Beware of the lake, and beware of the ole mighty GammelGäddan";

        beachButton.setOnClickListener(new OnLocationClickListener("Beach", beachInfo, R.drawable.beach_popup2));
    }

    final Map map = this;

    private void showPopupDialog(final String location, String information, int locationImageId) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.custom_dialog_layout, null); // Inflate custom dialog layout
        builder.setView(dialogView); // Set custom layout to dialog

        TextView titleTextView = dialogView.findViewById(R.id.dialog_title); // Reference to title TextView
        titleTextView.setText(location); // Set the location name as title

        TextView infoTextView = dialogView.findViewById(R.id.dialog_information); // Reference to information TextView
        infoTextView.setText(information); // Set the detailed information text

        ImageView imageView = dialogView.findViewById(R.id.dialog_image); // Reference to ImageView in custom dialog layout
        imageView.setImageResource(locationImageId); // Set the image resource

        builder.
                setPositiveButton("GOFISH!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle OK button click
                        dialog.dismiss(); // Close the dialog
                        Intent intent = new Intent(Map.this, FishingGame.class);
                        intent.putExtra("location", location);
                        intent.putExtra("currentState", currentState);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // Close the dialog
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.white));
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.white));
                        dialog.getWindow().setBackgroundDrawableResource(R.drawable.popup_background);
                    }
                });
                dialog.show(); // Display the dialog
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