package com.example.gofish;

import static android.app.PendingIntent.getActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class FishingGame extends AppCompatActivity {

    private boolean[] currentState;
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
    private Timer secondTimer;

    private MediaPlayer castLinePlayer;
    private MediaPlayer ambientPlayer;
    private MediaPlayer lowBubblePlayer;
    private MediaPlayer loudBubblePlayer;
    private MediaPlayer reelPlayer;
    private MediaPlayer exclamationsPlayer;
    private MediaPlayer linebreakPlayer;
    private MediaPlayer mothuggPlayer;
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
    private ImageView reelGestureSpot;
    private ImageView castGestureSpot;
    private Button restart;
    private Boolean escapingFish;

    private Fish fish;


    private Map mapActivity;

    private void changeLocationState(String location) {
        System.out.println(location);
        if (location.equals("Beach")) {
            currentState[1] = true;
        }
        if (location.equals("Dock")) {
            currentState[2] = true;
        }
        Intent intent = new Intent("locationUnlocked");
        intent.putExtra("currentState", currentState);
        for(int i = 0; i < currentState.length; i++) {
            System.out.println(currentState[i]);
        }
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.sendBroadcast(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_game);

        System.out.println("OnCreate Called");

        Intent intent = getIntent();
        if (intent != null) {
            location = intent.getStringExtra("location");
            currentState = intent.getBooleanArrayExtra("currentState");
            for(int i = 0; i < currentState.length; i++) {
                System.out.println(currentState[i]);
            }
        }

        failed = false;
        random = new Random();
        escapingFish = false;
        warningVibrationOn = false;

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        castLineSensorListener = new CastLineSensorListener(this);
        nibblingSensorListener = new FishNibblingSensorListener(this);
        reelingSensorListener = new ReelingSensorListener();

        initializeMediaPlayers();

        background = findViewById(R.id.horizon);

        reelGestureSpot = findViewById(R.id.reelGesture);
        reelGestureSpot.setAlpha(0f);
        castGestureSpot = findViewById(R.id.castGesture);
        castGestureSpot.setAlpha(1f);

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

        restart = findViewById(R.id.restart);
        restart.setAlpha(0f);
        restart.setEnabled(false);

        //timer = new Timer(); //hade problem med denna

        currentRotation = 0;

        sensorManager.registerListener(castLineSensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onStart() {
        super.onStart();
        initializeMediaPlayers();
        sensorManager.registerListener(castLineSensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        System.out.println("OnStart Called");
    }

    private void initializeMediaPlayers() {
        castLinePlayer = MediaPlayer.create(this, R.raw.fishing_splash);
        lowBubblePlayer = MediaPlayer.create(this, R.raw.low_instensity_bubble);
        loudBubblePlayer = MediaPlayer.create(this, R.raw.bubble);
        reelPlayer = MediaPlayer.create(this, R.raw.reel);
        mothuggPlayer = MediaPlayer.create(this, R.raw.whoosh);
        linebreakPlayer = MediaPlayer.create(this, R.raw.fail);
        exclamationsPlayer = MediaPlayer.create(this, exclamations[new Random().nextInt(exclamations.length)]);
    }

    private void onReset() {
        cancelFishTimer();
        if (secondTimer != null) {
            secondTimer.cancel();
            secondTimer = null;
        }
        if (castLinePlayer != null) castLinePlayer.release();
        if (lowBubblePlayer != null) lowBubblePlayer.release();
        if (loudBubblePlayer != null)  loudBubblePlayer.release();
        if (reelPlayer != null) reelPlayer.release();
        if (exclamationsPlayer != null) exclamationsPlayer.release();
        if (linebreakPlayer != null)  linebreakPlayer.release();
        vibrator.cancel();

        initializeMediaPlayers();

        reelGestureSpot.setImageResource(R.drawable.reel);
        reelGestureSpot.setAlpha(0f);

        if (failed) {
            linebreakPlayer.start();
        }
        escapingFish = false;
        rod.setRotationX(0);
        castGestureSpot.setImageResource(R.drawable.cast2);
        castGestureSpot.setAlpha(1f);
        sensorManager.registerListener(castLineSensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        failed = false;
        castGestureSpot.setImageResource(R.drawable.cast2);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (ambientPlayer != null) ambientPlayer.release();
        if (castLinePlayer != null) castLinePlayer.release();
        if (lowBubblePlayer != null) lowBubblePlayer.release();
        if (loudBubblePlayer != null) loudBubblePlayer.release();
        if (reelPlayer != null) reelPlayer.release();
        if (linebreakPlayer != null) linebreakPlayer.release();
        if (exclamationsPlayer != null) exclamationsPlayer.release();

        sensorManager.unregisterListener(castLineSensorListener);
        sensorManager.unregisterListener(nibblingSensorListener);
        sensorManager.unregisterListener(reelingSensorListener);

        vibrator.cancel();
        cancelFishTimer();
        System.out.println("OnDestroy Called");
    }



    private void waitForFish() {
        Random rand = new Random();
        int minDelay = 5000;
        int maxDelay = 10000;
        int delay = rand.nextInt(maxDelay - minDelay) + minDelay;
        if (lowBubblePlayer!= null) {
            lowBubblePlayer.start();
        } else {
            lowBubblePlayer = MediaPlayer.create(this, R.raw.low_instensity_bubble);
        }

        cancelFishTimer();
        timer = new Timer();
        sensorManager.unregisterListener(castLineSensorListener);

        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                lowBubblePlayer.stop();
                fishStartsNibbling();
            }
        }, delay);
    }

    private void fishStartsNibbling() {
        sensorManager.registerListener(nibblingSensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        rod.setRotationX(50);
        castGestureSpot.setAlpha(1f);
        castGestureSpot.setImageResource(R.drawable.strike);

        if (lowBubblePlayer!= null) {
            lowBubblePlayer.start();
        } else {
            lowBubblePlayer = MediaPlayer.create(this, R.raw.low_instensity_bubble);
            lowBubblePlayer.start();
        }


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

        redBorder.setAlpha(0f);
        cancelFishTimer();

        if (exclamationsPlayer != null) {
            exclamationsPlayer.start();
        }

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
        changeLocationState(location);
        System.out.println("fångad fisk");
        for (int i = 0; i < currentState.length; i++) {
            System.out.println(currentState[i]);
        }
        //knapp som startar om
        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                restart.setVisibility(View.INVISIBLE);
                restart.setAlpha(0f);
                restart.setEnabled(false);
                fishInfo.setText("");
                fishInfo.setAlpha(0f);
                fadeOut.start();
                fish = new Fish(location);
                onReset();
            }
        });
//        restart.setVisibility(View.VISIBLE);
        restart.setAlpha(1f);
        restart.setEnabled(true);

    }

    private void reeling() {
        sensorManager.unregisterListener(nibblingSensorListener);
        sensorManager.registerListener(reelingSensorListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
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
                castGestureSpot.setVisibility(View.INVISIBLE);
                reelGestureSpot.setVisibility(View.INVISIBLE);
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
        Random rand = new Random();
        int minDistance = 150;
        int maxDistance = 250;
        reelDistance = (int)(fish.getWeight()*30) + rand.nextInt(maxDistance - minDistance) + minDistance;

        castGestureSpot.setAlpha(0f);
        reelGestureSpot.setAlpha(1f);

        reeling();
    }

    private void startFishTimer() {
        Random rand = new Random();
        int minTime = 500;
        int maxTime = 8000;
        int randomTime = rand.nextInt(maxTime - minTime) + minTime;
        //castGestureSpot.setAlpha(1f);

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
                redBorder.setAlpha(0.2f);

                secondTimer = new Timer();
                secondTimer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        escapingFish = true;

                        reelGestureSpot.setImageResource(R.drawable.stop_reel);
                        innerTimer = new Timer();

                        redBorder.setAlpha(0.5f);

                        innerTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                escapingFish = false;

                                reelGestureSpot.setImageResource(R.drawable.reel);

                                // You might want to add any additional logic here if needed
                                warningVibrationOn = false;
                                startFishTimer();

                                redBorder.setAlpha(0f);
                            }
                        }, 3000); // 3000 milliseconds = 3 seconds
                    }
                }, 5000-(int)(150*fish.getWeight()));

            }
        }, randomTime);
        //castGestureSpot.setImageResource(R.drawable.strike);
    }

    // Method to cancel the fish timer
    private void cancelFishTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null; // Reset the timer reference
        }
        if (secondTimer != null) {
            secondTimer.cancel();
            secondTimer = null;
        }
        if (innerTimer != null) {
            innerTimer.cancel();
            escapingFish = false;

            reelGestureSpot.setAlpha(0f);
            innerTimer = null; // Reset the timer reference
            redBorder.setAlpha(0f);
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

        Context context;
        public CastLineSensorListener(Context t) {
            context = t;
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            float z = event.values[2];

            if (z > 5 || z < -5) {
                System.out.println(castLinePlayer);
                if (castLinePlayer != null) {
                    castLinePlayer.start();
                } else {
                    castLinePlayer = MediaPlayer.create(context, R.raw.fishing_splash);
                    castLinePlayer.start();
                }
                castGestureSpot.setAlpha(0f);
                waitForFish();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    class FishNibblingSensorListener implements SensorEventListener {

        Context t;

        public FishNibblingSensorListener(Context t) {
            this.t = t;
        }
        @Override
        public void onSensorChanged(SensorEvent event) {
            float z = event.values[2];
            if (z < -2) {
                if (mothuggPlayer != null) {
                    mothuggPlayer.start();
                } else {
                    mothuggPlayer = MediaPlayer.create(t, R.raw.whoosh);
                    mothuggPlayer.start();
                }
                cancelFishTimer();
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
                    reelGestureSpot.setAlpha(0f);
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

