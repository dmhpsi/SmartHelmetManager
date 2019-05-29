package com.darkha.smarthelmetmanager;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class PreAlarmActivity extends AppCompatActivity {
    final Handler handler = new Handler();
    AudioManager audioManager;
    MediaPlayer preAlarmPlayer;
    int preVolume;
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_alarm);

        preAlarmPlayer = MediaPlayer.create(PreAlarmActivity.this, R.raw.pre_alarm);

        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setSpeakerphoneOn(true);
        audioManager.requestAudioFocus(focusChange -> {
                },
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);

        preVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

        runnable = new Runnable() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    int secondsRemaining = 5;
                    TextView textTimeout = findViewById(R.id.text_timeout);
                    try {
                        secondsRemaining = Integer.parseInt(textTimeout.getText().toString());
                    } catch (Exception e) {
                        // TODO: handle exception
                    } finally {
                        //also call the same runnable to call it at regular interval
                        if (secondsRemaining > 0) {
                            textTimeout.setText(String.valueOf(secondsRemaining - 1));
                            preAlarmPlayer.start();
                            vibrate();
                            handler.postDelayed(this, 1000);
                        } else {
                            Intent intent = new Intent(PreAlarmActivity.this, AlarmActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
            }
        };
        handler.post(runnable);

        findViewById(R.id.button_cancel).setOnClickListener(v -> {
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (preAlarmPlayer != null) {
            preAlarmPlayer.stop();
            preAlarmPlayer.reset();
            preAlarmPlayer.release();
        }
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, preVolume, 0);
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setStreamSolo(AudioManager.STREAM_MUSIC, false);
        handler.removeCallbacks(runnable);
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(500);
        }
    }
}

