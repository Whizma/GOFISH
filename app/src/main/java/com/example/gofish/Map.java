package com.example.gofish;

import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        beachButton = (ImageButton) findViewById(R.id.beach);
        dockButton = (ImageButton) findViewById(R.id.dock);
        lakeButton = (ImageButton) findViewById(R.id.lake);

        beachButton.setOnClickListener(new OnLocationClickListener("beach"));
        dockButton.setOnClickListener(new OnLocationClickListener("dock"));
        lakeButton.setOnClickListener(new OnLocationClickListener("lake"));
    }

    class OnLocationClickListener implements View.OnClickListener {
        private String location;
        public OnLocationClickListener(String location) {
            this.location = location;
        }
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Map.this, FishingGame.class);
            intent.putExtra("location", location);
            startActivity(intent);
        }
    }
}