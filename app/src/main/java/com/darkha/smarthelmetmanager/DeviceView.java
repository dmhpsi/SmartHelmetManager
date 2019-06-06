package com.darkha.smarthelmetmanager;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

public class DeviceView extends GLSurfaceView {
    private final DeviceRenderer renderer;

    public DeviceView(Context context, AttributeSet attributeSet) {
        super(context);
        setEGLContextClientVersion(2);
        renderer = new DeviceRenderer();
        setRenderer(renderer);
        Log.e("GLDRAW", "default");
    }

    public DeviceView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
        renderer = new DeviceRenderer();

        setRenderer(renderer);
        Log.e("GLDRAW", "Second");
    }

    public void setAngles(float accelerationX, float accelerationY, float accelerationZ) {
        renderer.setAngles(accelerationX, accelerationY, accelerationZ);

    }
}
