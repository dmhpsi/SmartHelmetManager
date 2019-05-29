package com.darkha.smarthelmetmanager;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.triggertrap.seekarc.SeekArc;

import org.json.JSONException;
import org.json.JSONObject;

import static android.support.constraint.Constraints.TAG;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class HomeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    AudioManager audioManager;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    private BluetoothHandler bluetooth;
    private TinyDB tinyDB;
    private AudioContentObserver audioContentObserver;

    public HomeFragment() {
        // Required empty public constructor
    }

    public void setServices(BluetoothHandler bluetooth, TinyDB tinyDB) {
        this.bluetooth = bluetooth;
        this.tinyDB = tinyDB;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView: " + "init");
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        ImageButton button = view.findViewById(R.id.button_toggle_connection);

        if (bluetooth.isConnected()) {
            button.setImageResource(R.drawable.ic_bluetooth_disabled);
            setStatus(view, "Connected");
        } else {
            button.setImageResource(R.drawable.ic_bluetooth_connected);
            setStatus(view, "Disconnected");
        }
        bluetooth.setOnConnectError((device, message) -> {
            getActivity().runOnUiThread(() -> {
                button.setEnabled(true);
                setStatus(view, "Disconnected");
            });
        });
        bluetooth.setOnDeviceDisconnected((device, message) -> {
            getActivity().runOnUiThread(() -> {
                button.setImageResource(R.drawable.ic_bluetooth_connected);
                button.setEnabled(true);
                setStatus(view, "Disconnected");
            });
        });
        bluetooth.setOnDeviceAuthenticated((device) -> {
            getActivity().runOnUiThread(() -> {
                button.setImageResource(R.drawable.ic_bluetooth_disabled);
                button.setEnabled(true);
                setStatus(view, "Connected");
            });
        });
        button.setOnClickListener(v -> {
            button.setEnabled(false);
            if (bluetooth.isConnected()) {
                bluetooth.suppressDisconnectCallback();
                try {
                    bluetooth.disconnect();
                } catch (Exception ignored) {

                }
            } else {
                try {
                    JSONObject object = new JSONObject(tinyDB.getString(getString(R.string.key_last_connected_device)));
                    Log.e(TAG, "onCreateView: " + object.toString());
                    bluetooth.connectToDevice(object.getString("name"), object.getString("address"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

        SeekArc arcMusic = view.findViewById(R.id.arc_music);
        arcMusic.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        arcMusic.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        arcMusic.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int i, boolean b) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {

            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {

            }
        });
        SeekArc arcCall = view.findViewById(R.id.arc_call);
        arcCall.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL));
        arcCall.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL));
        arcCall.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int i, boolean b) {
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, i, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {

            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {

            }
        });

        audioContentObserver = new AudioContentObserver(new Handler());
        audioContentObserver.setOnAudioChange(() -> {
            arcMusic.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        });
        getContext().getContentResolver().registerContentObserver(
                android.provider.Settings.System.CONTENT_URI, true,
                audioContentObserver);

        view.findViewById(R.id.test_call).setOnClickListener(v -> {
//            String phone = "+84386648412";
//            SmsManager smsManager = SmsManager.getDefault();
//            smsManager.sendTextMessage(phone, null, "đm tuấn", null, null);
            DiagnosticFragment dialogFragment = new DiagnosticFragment();
            dialogFragment.setService(bluetooth);
            dialogFragment.show(getActivity().getSupportFragmentManager(), null);
        });

        bluetooth.addOnMessages(message -> {
            if (message.startsWith("warning")) {
                Intent intent = new Intent(getContext(), PreAlarmActivity.class);
                startActivity(intent);
                Log.e("message", "warning");
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    private void setStatus(View view, String status) {
        JSONObject object = null;
        try {
            object = new JSONObject(tinyDB.getString(getString(R.string.key_last_connected_device)));
            ((TextView) view.findViewById(R.id.text_info_name)).setText(object.getString("name"));
            ((TextView) view.findViewById(R.id.text_info_address)).setText(object.getString("address"));
            ((TextView) view.findViewById(R.id.text_info_status)).setText(status);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
                SeekArc seekArc = getActivity().findViewById(R.id.arc_music);
                seekArc.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
                seekArc = getActivity().findViewById(R.id.arc_music);
                seekArc.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            default:
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getContext().getContentResolver().unregisterContentObserver(audioContentObserver);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
