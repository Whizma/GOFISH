package com.example.gofish;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
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
import android.view.View;
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

    private float currentRotation;

    Timer timer;


    private MediaPlayer castLinePlayer;
    private MediaPlayer ambientPlayer;
    private MediaPlayer lowBubblePlayer;
    private MediaPlayer loudBubblePlayer;
    private MediaPlayer exclamationsPlayer;

    private int[] exclamations = new int[] {R.raw.ohyeah, R.raw.thatsanicefish, R.raw.woohoo};

    private Vibrator vibrator;

    private Random random;

    private ImageView rod;
    private ImageView background;
    private ImageView fish;
    private ImageView ocean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_game);

        Intent intent = getIntent();
        if (intent != null) {
            location = intent.getStringExtra("location");
        }

        random = new Random();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        castLineSensorListener = new CastLineSensorListener();
        nibblingSensorListener = new FishNibblingSensorListener();

        castLinePlayer = MediaPlayer.create(this, R.raw.fishing_splash);
        lowBubblePlayer = MediaPlayer.create(this, R.raw.low_instensity_bubbles);
        loudBubblePlayer = MediaPlayer.create(this, R.raw.bubble);
        exclamationsPlayer = MediaPlayer.create(this, exclamations[new Random().nextInt(exclamations.length)]);

        background = findViewById(R.id.horizon);

        chosenLocation(location);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        rod = findViewById(R.id.rod);
        fish = findViewById(R.id.fish);
        fish.setAlpha(0f);

        timer = new Timer();

        currentRotation = 0;

        sensorManager.registerListener(castLineSensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ambientPlayer.release();
        castLinePlayer.release();
        lowBubblePlayer.release();
        loudBubblePlayer.release();
        vibrator.cancel();
    }

    private void waitForFish() {
        Random rand = new Random();
        int minDelay = 5000;
        int maxDelay = 10000;
        int delay = rand.nextInt(maxDelay - minDelay) + minDelay;

        lowBubblePlayer.start();

        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                //lowBubblePlayer.stop();
                fishStartsNibbling();
            }
        }, delay);
    }

    private void fishStartsNibbling() {

        sensorManager.unregisterListener(castLineSensorListener);
        sensorManager.registerListener(nibblingSensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        rod.setRotationX(50);

        loudBubblePlayer.start();


        long[] timings = new long[]{300, 800};
        int[] amplitudes = new int[]{255, 0};
        int repeatIndex = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, repeatIndex));
        }

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Failed to catch fish
                sensorManager.unregisterListener(nibblingSensorListener);
                sensorManager.registerListener(castLineSensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
                vibrator.cancel();
                rod.setRotationX(0);
            }
        }, 5000);
    }

    private void caughtFish() {
        ambientPlayer.stop();
        exclamationsPlayer.start();

        sensorManager.unregisterListener(nibblingSensorListener);
        sensorManager.registerListener(castLineSensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        vibrationGoesCrazy();
        startRotation();

        //ObjectAnimator fadeIn = ObjectAnimator.ofFloat(fish, "alpha", 0f, 1f);

        //fadeIn.setDuration(1000); // Adjust the duration as per your preference


        // Start the animation
       // fadeIn.start();

        //vibrator.cancel();
    }

    private void startRotation() {
        // Increment the rotation angle by 1 degree

        int increment = random.nextInt(31) - 15;

        float amplitude = 10f; // Adjust the amplitude for the sway effect
        float frequency = 0.1f; // Adjust the frequency for the sway effect

        currentRotation = amplitude * (float) Math.sin(frequency) + increment;

        // Create an ObjectAnimator to animate the rotation
        ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(rod, View.ROTATION, currentRotation);
        rotationAnimator.setDuration(random.nextInt(1400) + 300); // Duration in milliseconds (approximately 60 frames per second)
        rotationAnimator.start();

        // Repeat the animation by recursively calling startRotation()
        rotationAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                startRotation();
            }


            @Override
            public void onAnimationCancel(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {

            }
        });
    }

    private void vibrationGoesCrazy() {
        long[] timings = new long[]{50, 50, 50, 50, 50, 100, 350, 25, 25, 25, 25, 200};
        int[] amplitudes = new int[]{33, 51, 75, 113, 170, 255, 0, 38, 62, 100, 160, 255};
        int repeatIndex = 1;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, repeatIndex));
        }
    }

    private void chosenLocation(String location) {

        switch (location) {
            case "lake":
                ambientPlayer = MediaPlayer.create(this, R.raw.ambient_lake);
                background.setImageResource(R.drawable.lake);
                break;
            case "beach":
                ambientPlayer = MediaPlayer.create(this, R.raw.beach);
                break;
            case "dock":
                ambientPlayer = MediaPlayer.create(this, R.raw.dock);
                background.setImageResource(R.drawable.dockbg);
                break;
        }

        ambientPlayer.start();
        ambientPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                ambientPlayer.start();
            }
        });
    }

    class CastLineSensorListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float z = event.values[2];

            if (z > 5) {
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
            float z = event.values[2];
            if (z < -2) {
                caughtFish();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
}

