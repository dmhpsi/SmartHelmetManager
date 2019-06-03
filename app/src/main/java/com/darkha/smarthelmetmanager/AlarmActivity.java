package com.darkha.smarthelmetmanager;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.nlopez.smartlocation.SmartLocation;

public class AlarmActivity extends AppCompatActivity {
    private static final long LOCATION_REFRESH_TIME = 100;
    private static final float LOCATION_REFRESH_DISTANCE = 1f;
    MediaPlayer alarmPlayer;
    AudioManager audioManager;
    Double longitude, latitude;
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            //your code here
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            Log.e("LONGLAT", " " + longitude + " " + latitude);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };


    int preVolume;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        TinyDB tinyDB = new TinyDB(this);

        SmartLocation.with(this).location()
                .oneFix()
                .start(location -> {
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                });

        findViewById(R.id.button_rescue).setOnLongClickListener(v -> {
            finish();
            return false;
        });
        TextView textContact = findViewById(R.id.text_numbers);
        String message = "Contacted to:\n";

        ArrayList<String> savedContacts = tinyDB.getListString(getString(R.string.KEY_EMERGENCY_NUMBERS));
        ArrayList<String> infoList = new ArrayList<>();
        ArrayList<String> numberList = new ArrayList<>();

        for (String contact : savedContacts) {
            try {
                JSONObject object = new JSONObject(contact);
                String name = object.getString("name");
                String number = object.getString("number");
                String infoContact = name + " - " + number;
                numberList.add(number);
                infoList.add(infoContact);
            } catch (Exception ignored) {

            }
        }

        message += TextUtils.join("\n", infoList);

        textContact.setText(message);

        message = "User Info:";

        String name = "user", address, allergy, blood;
        try {
            JSONObject info = new JSONObject(tinyDB.getString(getString(R.string.KEY_USER_INFO)));
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

        final Handler handler = new Handler();
        String finalName = name;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Log.e("Alarm", "Run");
                boolean messageSent = false;
                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    String textMessage = "Emergency happens with " + finalName + " at location http://maps.google.com/?q=" + latitude + "," + longitude;
                    ArrayList<String> parts = smsManager.divideMessage(textMessage);

                    if (longitude != null && latitude != null) {
                        for (String number : numberList) {
                            smsManager.sendMultipartTextMessage(number, null, parts, null, null);
                            Log.e("Alarm", "Messages to " + number + " :" + textMessage);
                        }
                        messageSent = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (!messageSent) {
                        handler.postDelayed(this, 1000);
                    }
                }
            }
        };
        handler.post(runnable);


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
