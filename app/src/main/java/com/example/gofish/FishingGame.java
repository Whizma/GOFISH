package com.example.gofish;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.ImageView;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class FishingGame extends AppCompatActivity {

    private String location;
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener castLineSensorListener;
    private SensorEventListener nibblingSensorListener;

    Timer timer;

    private MediaPlayer castLinePlayer;
    private MediaPlayer ambientLakePlayer;

    private MediaPlayer fishOnHookPlayer;

    private Vibrator vibrator;

    private ImageView rod;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_game);

        Intent intent = getIntent();
        if (intent != null) {
            location = intent.getStringExtra("location");
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        castLineSensorListener = new CastLineSensorListener();
        nibblingSensorListener = new FishNibblingSensorListener();


        castLinePlayer = MediaPlayer.create(this, R.raw.fishing_splash);
        fishOnHookPlayer = MediaPlayer.create(this, R.raw.bubble);
        ambientLakePlayer = MediaPlayer.create(this, R.raw.ambient_lake);
        ambientLakePlayer.start();

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        rod = findViewById(R.id.rod);

        timer = new Timer();

        ambientLakePlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                ambientLakePlayer.start();
            }
        });

        sensorManager.registerListener(castLineSensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ambientLakePlayer.release();
        castLinePlayer.release();
        fishOnHookPlayer.release();
        vibrator.cancel();
    }

    private void waitForFish() {
        Random rand = new Random();
        int minDelay = 5000;
        int maxDelay = 10000;
        int delay = rand.nextInt(maxDelay - minDelay) + minDelay;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fishStartsNibbling();
            }
        }, delay);
    }

    private void fishStartsNibbling() {

        sensorManager.unregisterListener(castLineSensorListener);
        sensorManager.registerListener(nibblingSensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        rod.setRotationX(50);

        fishOnHookPlayer.start();


        long[] timings = new long[] { 300, 800 };
        int[] amplitudes = new int[] { 255, 0 };
        int repeatIndex = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, repeatIndex));
        }

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Failed to catch fish
                fishOnHookPlayer.stop();
                sensorManager.unregisterListener(nibblingSensorListener);
                sensorManager.registerListener(castLineSensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
                vibrator.cancel();
                rod.setRotationX(0);
            }
        }, 4000);
    }

    private void vibrationGoesCrazy() {
        long[] timings = new long[] { 50, 50, 50, 50, 50, 100, 350, 25, 25, 25, 25, 200 };
        int[] amplitudes = new int[] { 33, 51, 75, 113, 170, 255, 0, 38, 62, 100, 160, 255 };
        int repeatIndex = 1;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, repeatIndex));
        }
    }

    class CastLineSensorListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float z = event.values[2];

            if (x > 8 || z > 8) {
                castLinePlayer.start();
                waitForFish();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    class FishNibblingSensorListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float z = event.values[2];
            if (x < 5 || z < 5) {

            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
}

