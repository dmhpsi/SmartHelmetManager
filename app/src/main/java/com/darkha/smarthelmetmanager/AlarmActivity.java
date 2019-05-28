package com.darkha.smarthelmetmanager;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class AlarmActivity extends AppCompatActivity {
    MediaPlayer alarmPlayer;
    AudioManager audioManager;
    int preVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        TinyDB tinyDB = new TinyDB(this);

        findViewById(R.id.button_rescue).setOnLongClickListener(v -> {
            finish();
            return false;
        });
        TextView textContact = findViewById(R.id.text_numbers);
        String message = "Contacted to:";
        String number1, number2, number3;
        try {
            JSONObject info = new JSONObject(tinyDB.getString(getString(R.string.key_phone_numbers)));
            try {
                number1 = info.getString("number1");
                if (!TextUtils.isEmpty(number1)) {
                    message += "\n" + number1;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                number2 = info.getString("number2");
                if (!TextUtils.isEmpty(number2)) {
                    message += "\n" + number2;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                number3 = info.getString("number3");
                if (!TextUtils.isEmpty(number3)) {
                    message += "\n" + number3;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        textContact.setText(message);

        message = "User Info:";

        String name, address, allergy, blood;
        try {
            JSONObject info = new JSONObject(tinyDB.getString(getString(R.string.key_user_info)));
            try {
                name = info.getString("name");
                if (!TextUtils.isEmpty(name)) {
                    message += "\nName: " + name;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                address = info.getString("address");
                if (!TextUtils.isEmpty(address)) {
                    message += "\nAddress: " + address;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                allergy = info.getString("allergy");
                message += "\nAllergy: " + allergy;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                blood = info.getString("blood-type");
                if (!TextUtils.isEmpty(blood)) {
                    message += "\nBlood type: " + blood;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        TextView userInfo = findViewById(R.id.text_info);
        userInfo.setText(message);

        alarmPlayer = MediaPlayer.create(AlarmActivity.this, R.raw.alarm);
        alarmPlayer.setLooping(true);
        alarmPlayer.start();

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (alarmPlayer != null) {
            alarmPlayer.stop();
            alarmPlayer.reset();
            alarmPlayer.release();
        }
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, preVolume, 0);
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setStreamSolo(AudioManager.STREAM_MUSIC, false);
    }
}
