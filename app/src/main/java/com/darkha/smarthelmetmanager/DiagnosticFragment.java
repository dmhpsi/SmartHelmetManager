package com.darkha.smarthelmetmanager;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import static java.lang.Math.PI;

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
                        bluetooth.send("get acc\n");
                    }
                } catch (Exception ignored) {

                }
                handler.postDelayed(this, 100);
            }
        };
        handler.post(runnable);

        ImageView drop = view.findViewById(R.id.image_drop);
        drop.bringToFront();
        int[] width = new int[1];

        RelativeLayout container = view.findViewById(R.id.frame_drop_container);
        ViewTreeObserver vto = container.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                width[0] = container.getHeight() / 2;
                Log.e("WIDTH", width[0] + " ");
                ViewTreeObserver obs = container.getViewTreeObserver();
                obs.removeOnGlobalLayoutListener(this);
            }

        });

        final double[] angles = {0, 0};
        final ImageView compass = view.findViewById(R.id.image_compass);
        TextView textAngle = view.findViewById(R.id.text_angle);
        TextView textBalance = view.findViewById(R.id.text_balance);
        if (bluetooth != null) {
            bluetooth.addOnMessages(message -> {
                if (message.startsWith("ang")) {
                    try {
                        double angle = -Float.valueOf(message.split("\\s+")[1]);
                        getActivity().runOnUiThread(() -> {
                            textAngle.setText(String.format("%.0f", angle));
                            angles[0] = Math.floor(angles[0] / 360) * 360 + angle;
                            if (angles[0] - angles[1] > 180) {
                                angles[0] -= 360;
                            }
                            if (angles[0] - angles[1] < -180) {
                                angles[0] += 360;
                            }
                            angles[1] = angles[0];
                            compass.animate().setDuration(100).rotation((float) angles[0]);
                        });
                    } catch (Exception ignored) {

                    }
                } else if (message.startsWith("acc")) {
                    try {
                        String vals = message.split("\\s+")[1];
                        float x = -Float.valueOf(vals.split(",")[0]);
                        float y = -Float.valueOf(vals.split(",")[1]);
                        float z = -Float.valueOf(vals.split(",")[2]);
                        if (x > 1) {
                            x = 1;
                        } else if (x < -1) {
                            x = -1;
                        }
                        if (y > 1) {
                            y = 1;
                        } else if (y < -1) {
                            y = -1;
                        }
                        if (z > 1) {
                            z = 1;
                        } else if (z < -1) {
                            z = -1;
                        }

                        float finalX = -x;
                        float finalY = -y;
                        float finalZ = (float) (Math.acos(-z) * 180 / PI);
                        getActivity().runOnUiThread(() -> {
                            drop.animate().setDuration(100).translationX(finalX * width[0]);
                            drop.animate().setDuration(100).translationY(finalY * width[0]);
                            textBalance.setText(String.format("%.0f", finalZ));
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
