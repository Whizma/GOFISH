package com.example.gofish;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class FishingGame extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensor;

    Timer timer;

    private MediaPlayer castLinePlayer;
    private Vibrator vibrator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_game);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);


        castLinePlayer = MediaPlayer.create(this, R.raw.fishing_splash);

        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float z = event.values[0];

        if (x > 8 || z > 8) {
            castLinePlayer.start();
            waitForFish();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    void waitForFish() {
        Random rand = new Random();
        int minDelay = 5000;
        int maxDelay = 8000;
        int delay = rand.nextInt(maxDelay - minDelay) + minDelay;

        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                long[] timings = new long[] { 50, 50, 50, 50, 50, 100, 350, 25, 25, 25, 25, 200 };
                int[] amplitudes = new int[] { 33, 51, 75, 113, 170, 255, 0, 38, 62, 100, 160, 255 };
                int repeatIndex = 1; // Do not repeat.

                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, repeatIndex));
            }
        }, delay);
    }

}

