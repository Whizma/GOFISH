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
import android.util.Log;
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
    private Timer innerTimer; // Declare innerTimer as a member variable

    private MediaPlayer castLinePlayer;
    private MediaPlayer ambientPlayer;
    private MediaPlayer lowBubblePlayer;
    private MediaPlayer loudBubblePlayer;
    private MediaPlayer reelPlayer;
    private MediaPlayer exclamationsPlayer;
    private MediaPlayer linebreakPlayer;
    private boolean failed;
    private int reelDistance;
    private boolean warningVibrationOn;

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
    private ImageView stopGesture;
    private Button restart;
    private Boolean escapingFish;

    private Fish fish;

    private Button map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_game);

        Intent intent = getIntent();
        if (intent != null) {
            location = intent.getStringExtra("location");
        }

        failed = false;
        random = new Random();
        escapingFish = false;
        warningVibrationOn = false;

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        castLineSensorListener = new CastLineSensorListener();
        nibblingSensorListener = new FishNibblingSensorListener();
        reelingSensorListener = new ReelingSensorListener();

        initializeMediaPlayers();

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
        stopGesture = findViewById(R.id.stopGesture);
        stopGesture.setAlpha(0f);
        restart = findViewById(R.id.restart);
        restart.setVisibility(View.INVISIBLE);
        restart.setEnabled(false);

        //timer = new Timer(); //hade problem med denna

        Button map = findViewById(R.id.map);
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to navigate back to the map activity
                Intent intent = new Intent(FishingGame.this, Map.class);
                startActivity(intent);
            }
        });

        currentRotation = 0;

        sensorManager.registerListener(castLineSensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void initializeMediaPlayers() {
        castLinePlayer = MediaPlayer.create(this, R.raw.fishing_splash);
        lowBubblePlayer = MediaPlayer.create(this, R.raw.low_instensity_bubbles);
        loudBubblePlayer = MediaPlayer.create(this, R.raw.bubble);
        reelPlayer = MediaPlayer.create(this, R.raw.reel);
        exclamationsPlayer = MediaPlayer.create(this, exclamations[new Random().nextInt(exclamations.length)]);
        linebreakPlayer = MediaPlayer.create(this, R.raw.fail);
    }

    private void onReset() {
        cancelFishTimer();
        castLinePlayer.release();
        lowBubblePlayer.release();
        loudBubblePlayer.release();
        reelPlayer.release();
        exclamationsPlayer.release();
        linebreakPlayer.release();
        vibrator.cancel();


        initializeMediaPlayers();

        gesture.setAlpha(0);
        if (failed) {
            linebreakPlayer.start();
        }
        escapingFish = false;
        rod.setRotationX(0);
        sensorManager.registerListener(castLineSensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        failed = false;
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
        linebreakPlayer.release();
        vibrator.cancel();
        cancelFishTimer();
    }

    private void waitForFish() {
        Random rand = new Random();
        int minDelay = 5000;
        int maxDelay = 10000;
        int delay = rand.nextInt(maxDelay - minDelay) + minDelay;
        lowBubblePlayer.start();
        cancelFishTimer();
        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                lowBubblePlayer.stop();
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
                failed = true;
                sensorManager.unregisterListener(nibblingSensorListener);
                onReset();
            }
        }, 5000);
    }

    private void caughtFish() {

        cancelFishTimer();
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

                restart.setVisibility(View.INVISIBLE);
                restart.setEnabled(false);
                fishInfo.setText("");
                fishInfo.setAlpha(0f);
                fadeOut.start();
                fish = new Fish(location);
                onReset();
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
            case "Lake":
                ambientPlayer = MediaPlayer.create(this, R.raw.ambient_lake);
                background.setImageResource(R.drawable.lake2);
                break;
            case "Beach":
                ambientPlayer = MediaPlayer.create(this, R.raw.beach);
                ambientPlayer.setVolume(0.5f, 0.5f);
                break;
            case "Dock":
                ambientPlayer = MediaPlayer.create(this, R.raw.dock);
                background.setImageResource(R.drawable.dock2);
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
        reelDistance = (int)(fish.getWeight()*30) + rand.nextInt(maxDistance - minDistance) + minDistance;
        gesture.setAlpha(1f);
        reeling();
    }




    private void startFishTimer() {
        Random rand = new Random();
        int minTime = 500;
        int maxTime = 8000;
        int randomTime = rand.nextInt(maxTime - minTime) + minTime;

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // timer to randomly introduce fish escape
                //sensorManager.registerListener(castLineSensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
                //soundplayer med rod tension, och vibration

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.cancel();
                    vibrator.vibrate(VibrationEffect.createOneShot(8000-(int)(150*fish.getWeight()), VibrationEffect.DEFAULT_AMPLITUDE)); //användare har 1 sek på sig att reagera, kan höja o sänka beroende på fisk
                }
                warningVibrationOn = true;
                Timer secondTimer = new Timer();
                secondTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        escapingFish = true;
                        stopGesture.setAlpha(1f);
                        innerTimer = new Timer();

                        innerTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                escapingFish = false;
                                stopGesture.setAlpha(0f);
                                // You might want to add any additional logic here if needed
                                warningVibrationOn = false;
                                startFishTimer();

                            }
                        }, 3000); // 3000 milliseconds = 3 seconds
                    }
                }, 5000-(int)(150*fish.getWeight()));

            }
        }, randomTime);
    }

    // Method to cancel the fish timer
    private void cancelFishTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null; // Reset the timer reference
        }
        if (innerTimer != null) {
            innerTimer.cancel();
            escapingFish = false;
            stopGesture.setAlpha(0f);
            innerTimer = null; // Reset the timer reference
        }
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !warningVibrationOn) {
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
                timer.cancel();
                startFishTimer();
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
            if (!isReeling && event.values[0] >= 0 && event.values[0] <= 9 && !escapingFish) {
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
            } else if (!isReeling && event.values[0] >= 0 && event.values[0] <= 9 && escapingFish) {
                cancelFishTimer();
                failed = true;
                sensorManager.unregisterListener(reelingSensorListener);
                onReset();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Not needed for this example, but you may need to implement it depending on your requirements
        }
    }

}

