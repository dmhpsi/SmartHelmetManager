package com.darkha.smarthelmetmanager;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DiagnosticFragment extends DialogFragment {
    private BluetoothHandler bluetooth;
    private Handler handler;
    private Runnable runnable;

    void setService(BluetoothHandler bluetooth) {
        this.bluetooth = bluetooth;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_diagnostic, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if (bluetooth.isConnected()) {
                        bluetooth.send("get ang\n");
                    }
                } catch (Exception ignored) {

                }
                handler.postDelayed(this, 50);
            }
        };
        handler.post(runnable);

        if (bluetooth != null) {
            bluetooth.addOnMessages(message -> {
                if (message.startsWith("ang")) {
                    try {
                        float angle = -Float.valueOf(message.split("\\s+")[1]);
                        TextView textView = view.findViewById(R.id.text_angle);
                        getActivity().runOnUiThread(() -> {
                            textView.setText(String.valueOf(angle));
                            view.findViewById(R.id.image_compass).setRotation(angle);
                        });
                    } catch (Exception ignored) {

                    }
                }
            });
        } else {
            dismiss();
        }

        view.findViewById(R.id.button_dismiss).setOnClickListener(v -> {
            dismiss();

        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(runnable);
    }
}
