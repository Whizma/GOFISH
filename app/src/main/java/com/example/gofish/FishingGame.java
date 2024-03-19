package com.example.gofish;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;

public class FishingGame extends AppCompatActivity {

    // Interactions
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener sensorEventListener;

    // feedback
    private MediaPlayer castLinePlayer;
    private Vibrator vibrator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_game);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);


        castLinePlayer = MediaPlayer.create(this, R.raw.castline);

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float x = event.values[0]; // forward/backward
                float y = event.values[1]; // up/down
                float z = event.values[2]; // left/right

                float acceleration = y;
                if (acceleration > .1) {
                    castLinePlayer.start();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };
    }
}