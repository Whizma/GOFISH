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
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class FishingGame extends AppCompatActivity {

    private String location;
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener castLineSensorListener;
    private SensorEventListener nibblingSensorListener;
    private SensorEventListener reelingSensorListener;
    private Sensor lightSensor;
    private SensorEventListener sensorEventListener;

    private float currentRotation;

    private Timer timer;

    private MediaPlayer castLinePlayer;
    private MediaPlayer ambientPlayer;
    private MediaPlayer lowBubblePlayer;
    private MediaPlayer loudBubblePlayer;
    private MediaPlayer reelPlayer;
    private MediaPlayer exclamationsPlayer;
    private int reelDistance;

    private int[] exclamations = new int[] {R.raw.ohyeah, R.raw.thatsanicefish, R.raw.woohoo};

    private Vibrator vibrator;

    private Random random;

    private ImageView rod;
    private ImageView background;
    private ImageView fishImage;
    private TextView fishInfo;
    private ImageView redBorder;
    private ImageView ocean;
    private ImageView gesture;
    private Button restart;

    private Fish fish;

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

        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        castLineSensorListener = new CastLineSensorListener();
        nibblingSensorListener = new FishNibblingSensorListener();
        reelingSensorListener = new ReelingSensorListener();


        castLinePlayer = MediaPlayer.create(this, R.raw.fishing_splash);
        lowBubblePlayer = MediaPlayer.create(this, R.raw.low_instensity_bubbles);
        loudBubblePlayer = MediaPlayer.create(this, R.raw.bubble);
        reelPlayer = MediaPlayer.create(this, R.raw.reel);
        exclamationsPlayer = MediaPlayer.create(this, exclamations[new Random().nextInt(exclamations.length)]);

        background = findViewById(R.id.horizon);

        chosenLocation(location);

        fish = new Fish(location);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //connecting xml elements
        redBorder = findViewById(R.id.redBorder);
        redBorder.setAlpha(0f);
        rod = findViewById(R.id.rod);
        fishImage = findViewById(R.id.fish);
        fishImage.setImageResource(fish.getImageResource());
        fishImage.setAlpha(0f);
        fishInfo = findViewById(R.id.fishInfo);
        fishInfo.setAlpha(0f);
        gesture = findViewById(R.id.gesture);
        gesture.setAlpha(0f);
        restart = findViewById(R.id.restart);
        restart.setVisibility(View.INVISIBLE);
        restart.setEnabled(false);

        //timer = new Timer(); //hade problem med denna

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
        reelPlayer.release();
        exclamationsPlayer.release();
        vibrator.cancel();
    }

    private void waitForFish() {
        Random rand = new Random();
        int minDelay = 5000;
        int maxDelay = 10000;
        int delay = rand.nextInt(maxDelay - minDelay) + minDelay;

        lowBubblePlayer.start();
        timer = new Timer();
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
        timer = new Timer();
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
        //ambientPlayer.stop(); //varför?
        exclamationsPlayer.start();

        sensorManager.unregisterListener(reelingSensorListener);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(fishImage, "alpha", 0f, 1f);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(fishImage, "alpha", 1f, 0f);

        //wip
        //vibrationGoesCrazy();
        //startRotation();

        fadeIn.setDuration(1000); // Adjust the duration as per your preference
        fadeOut.setDuration(500); // Adjust the duration as per your preference


        // Start the animation
       fadeIn.start();

        vibrator.cancel();

        fishInfo.setAlpha(1f);
        fishInfo.setText(String.format(Locale.getDefault(),"Congratulations! You caught a %s.\n It weighs %.1f kg!", fish.getName(), fish.getWeight()));

        //knapp som startar om
        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rod.setRotationX(0);
                sensorManager.registerListener(castLineSensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
                restart.setVisibility(View.INVISIBLE);
                restart.setEnabled(false);
                fishInfo.setText("");
                fishInfo.setAlpha(0f);
                fadeOut.start();
                fish = new Fish(location);
            }
        });
        restart.setVisibility(View.VISIBLE);
        restart.setEnabled(true);

    }

    private void reeling() {
        sensorManager.unregisterListener(nibblingSensorListener);
        sensorManager.registerListener(reelingSensorListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
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
                ambientPlayer.setVolume(0.5f, 0.5f);
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

    public void startReeling(){
        reelPlayer.start(); //markera att fisken ska reelas in - wip - ska ändras
        Random rand = new Random();
        int minDistance = 150;
        int maxDistance = 250;
        reelDistance = (int)(fish.getWeight()*10) + rand.nextInt(maxDistance - minDistance) + minDistance;
        gesture.setAlpha(1f);
        reeling();
    }

    private void reelSound(){
        if (reelPlayer.isPlaying()) {
            // If reelPlayer is already playing, stop it and play again
            reelPlayer.stop();
            reelPlayer.prepareAsync(); // Prepare the player asynchronously
            reelPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start(); // Start playing again once prepared
                }
            });
        } else {
            reelPlayer.start();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.cancel();
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
        }

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
                timer.cancel(); // Cancel the TimerTask if it's not null
                //wip - ändras efter testning
                startReeling();

            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    class ReelingSensorListener implements SensorEventListener {
        private boolean isReeling = false;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (!isReeling && event.values[0] >= 0 && event.values[0] <= 9) {
                isReeling = true;
                reelDistance -= 50;
                reelSound();
                if (reelDistance > 0) {

                } else {
                    gesture.setAlpha(0);
                    caughtFish();
                }

                // Reset the flag after a short delay to allow the method to be triggered again
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isReeling = false;
                    }
                }, 500); // Adjust the delay time (in milliseconds) as needed
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Not needed for this example, but you may need to implement it depending on your requirements
        }
    }

}

