package com.darkha.smarthelmetmanager;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class DeviceRenderer implements GLSurfaceView.Renderer {

    // vPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] vPMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private DeviceModel deviceModel;
    private float angleX, angleY, angleZ;
    private float[] rotationMatrix = new float[16];
    private float[] rotationMatrixX = new float[16];
    private float[] rotationMatrixY = new float[16];
    private float[] rotationMatrixZ = new float[16];

    public static int loadShader(int type, String shaderCode) {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        deviceModel = new DeviceModel();
        GLES20.glClearColor(0f, 0f, 0f, 0f);
//        GLES20.glDepthFunc( GLES20.GL_LEQUAL );
//        GLES20.glDepthMask( true );
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        float[] scratch = new float[16];

        Matrix.setLookAtM(viewMatrix, 0, 0, 0, -5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        long time = SystemClock.uptimeMillis() % 4000L;
        float angle = 0.090f * ((int) time);

//        Matrix.setRotateM(rotationMatrixZ, 0, angle, 1, 0, 0);
//        Matrix.setRotateM(rotationMatrixY, 0, angle, 0, 1, 0);
//        Matrix.setRotateM(rotationMatrixX, 0, angle, 0, 0, 1);
//
//        Matrix.multiplyMM(rotationMatrix, 0,  rotationMatrixZ, 0,  rotationMatrixY, 0);
//        Matrix.multiplyMM(rotationMatrix, 0,  rotationMatrix, 0,  rotationMatrixX, 0);

        Matrix.setRotateM(rotationMatrix, 0, angle, angle, 0, -1.0f);

        // Combine the rotation matrix with the projection and camera view
        // Note that the vPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrix, 0);

        // Draw triangle
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        deviceModel.draw(scratch);
    }

    public void setAngles(float angleX, float angleY, float angleZ) {
        this.angleX = angleX;
        this.angleY = angleY;
        this.angleZ = angleZ;
    }
}
