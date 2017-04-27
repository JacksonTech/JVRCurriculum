/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 * Modifications Copyright 2017 Cody Jackson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.csus.ecs.minimaldemo2;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * A stripped-down version of the Google VR "TreasureHunt" activity, with contributions from
 * Cody Jackson
 */
public class VRMinimalDemoActivity extends GvrActivity implements GvrView.StereoRenderer {

    private static final String TAG = "VRMinimalDemoActivity";

    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100.0f;
    private static final float CAMERA_Z = 0.01f;
    private static final float TIME_DELTA = 0.3f;
    private static final int COORDS_PER_VERTEX = 3;
    private static final float MAX_MODEL_DISTANCE = 7.0f;
    private static final float RETICULE_DISTANCE = 7f;
    private static final float RETICULE_SIZE = 0.06f;

    // VBO ids
    private int cubeVerticesVbo, cubeColorsVbo, retColorsVbo;

    private int shaderProgram;      // shader program ID
    private int positionLoc;        // location of position shader attr
    private int colorLoc;           // location of color shader attr
    private int mvpLoc;             // location of mvp uniform

    private float cameraPosition[];
    private float[] cameraMatrix;   // matrix to move camera around
    private float[] viewMatrix;     // view matrix from each eye

    private float[] cubeMatrix;     // Model matrix for cube
    private float[] cubePosition;   // Position (vector)

    private float[] modelViewProj;
    private float[] modelView;

    private float[] reticuleMatrix, reticuleRotMatrix, reticuleTransMatrix, reticuleScaleMatrix;
    private float[] reticulePosition;
    private float[] reticuleAngles; // stores the rotational angles from the headTransform
    private float[] tempMatrix;

    /**
     * Converts a raw text file, saved as a resource, into an OpenGL ES shader.
     *
     * Code provided by Google
     * @param type  The type of shader we will be creating.
     * @param resId The resource ID of the raw text file about to be turned into a shader.
     * @return The shader object handler.
     */
    private int loadGLShader(int type, int resId) {
        String code = readRawTextFile(resId);
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);
        // Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        if (shader == 0) {
            throw new RuntimeException("Error creating shader.");
        }
        return shader;
    }

    /**
     * Checks if we've had an error inside of OpenGL ES, and if so what that error is.
     *
     * Code provided by Google
     * @param label Label to report in case of error.
     */
    private static void checkGLError(String label) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, label + ": glError " + error);
            throw new RuntimeException(label + ": glError " + error);
        }
    }

    /**
     * Sets the viewMatrix to our GvrView and initializes the transformation matrices we will use
     * to render our scene.
     *
     * Code provided by Google
     * Heavy modifications by Cody Jackson
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeGvrView();

        cubeMatrix = new float[16];
        cameraMatrix = new float[16];
        viewMatrix = new float[16];
        modelViewProj = new float[16];
        modelView = new float[16];
        // cube first appears directly in front of user.
        cubePosition = new float[]{0.0f, 0.0f, -MAX_MODEL_DISTANCE};
        // cameraMatrix
        cameraPosition = new float[4];
        cameraPosition[0] = 0;
        cameraPosition[1] = 0;
        cameraPosition[2] = CAMERA_Z;
        // reticule
        reticuleMatrix = new float[16];
        reticuleScaleMatrix = new float[16];
        reticuleTransMatrix = new float[16];
        reticuleRotMatrix = new float[16];
        reticulePosition = new float[]{0f, 0f, -RETICULE_DISTANCE}; // directly in front of user
        reticuleAngles = new float[3];
        tempMatrix = new float[16];
    }

    /**
     * Code provided by Google
     */
    public void initializeGvrView() {
        setContentView(R.layout.common_ui);
        GvrView gvrView = (GvrView) findViewById(R.id.gvr_view);
        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);
        gvrView.setRenderer(this);
        gvrView.setTransitionViewEnabled(true);
        // lowers CPU/GPU temp if possible
        if (gvrView.setAsyncReprojectionEnabled(true)) {
            AndroidCompat.setSustainedPerformanceMode(this, true);
        }
        setGvrView(gvrView);
    }

    // required method
    @Override
    public void onRendererShutdown() {
        Log.i(TAG, "onRendererShutdown");
    }

    // required method
    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.i(TAG, "onSurfaceChanged");
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Creates the buffers we use to store information about the 3D world.
     *
     *
     * Some code provided by Google
     * Mostly by Cody Jackson
     * @param config The EGL configuration used when creating the surface.
     */
    @Override
    public void onSurfaceCreated(EGLConfig config) {
        Log.i(TAG, "onSurfaceCreated");
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f); // Dark background so text shows up well.

        FloatBuffer cubeVertices, cubeColors;

        // google stuff
        ByteBuffer bbVertices = ByteBuffer.allocateDirect(WorldLayoutData.CUBE_COORDS.length * 4);
        bbVertices.order(ByteOrder.nativeOrder()); // don't forget this--won't render if you do!
        cubeVertices = bbVertices.asFloatBuffer();
        cubeVertices.put(WorldLayoutData.CUBE_COORDS);
        cubeVertices.position(0); // prep for reading later

        ByteBuffer bbColors = ByteBuffer.allocateDirect(WorldLayoutData.CUBE_COLORS.length * 4);
        bbColors.order(ByteOrder.nativeOrder()); // don't forget this--won't render if you do!
        cubeColors = bbColors.asFloatBuffer();
        cubeColors.put(WorldLayoutData.CUBE_COLORS);
        cubeColors.position(0); // prep for reading later
        // end google stuff

        // set up VBOs for cube AND reticule (they share geometry)
        int vbo[] = new int[3];
        GLES20.glGenBuffers(3, vbo, 0);
        cubeVerticesVbo = vbo[0];
        cubeColorsVbo = vbo[1];
        retColorsVbo = vbo[2];

        int vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.light_vertex);
        int passthroughShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.passthrough_fragment);

        shaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(shaderProgram, vertexShader);
        GLES20.glAttachShader(shaderProgram, passthroughShader);
        GLES20.glLinkProgram(shaderProgram);
        GLES20.glUseProgram(shaderProgram);

        checkGLError("Cube program");

        positionLoc = GLES20.glGetAttribLocation(shaderProgram, "a_Position");
        colorLoc = GLES20.glGetAttribLocation(shaderProgram, "a_Color");
        mvpLoc = GLES20.glGetUniformLocation(shaderProgram, "u_MVP");
        checkGLError("Cube program params");

        // buffer the data
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, cubeVerticesVbo);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, WorldLayoutData.CUBE_COORDS.length * 4, cubeVertices, GLES20.GL_STATIC_DRAW);

        // do the same for the colors
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, cubeColorsVbo);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, WorldLayoutData.CUBE_COLORS.length * 4, cubeColors, GLES20.GL_STATIC_DRAW);

        // do it again for the reticule (uses same model) but use white instead
        cubeColors.clear(); // reset to beginning for write
        cubeColors.put(WorldLayoutData.CUBE_COLORS_CROSSHAIR);
        cubeColors.position(0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, retColorsVbo);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, WorldLayoutData.CUBE_COLORS.length * 4, cubeColors, GLES20.GL_STATIC_DRAW);

        // reset
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        checkGLError("Buffering data");

        // move the cube
        Matrix.setIdentityM(cubeMatrix, 0);
        Matrix.translateM(cubeMatrix, 0, cubePosition[0], cubePosition[1], cubePosition[2]);

        checkGLError("onSurfaceCreated");
    }

    /**
     * Converts a raw text file into a string.
     *
     * Code provided by Google
     * @param resId The resource ID of the raw text file about to be turned into a shader.
     * @return The context of the text file, or null in case of error.
     */
    private String readRawTextFile(int resId) {
        InputStream inputStream = getResources().openRawResource(resId);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Prepares OpenGL ES before we draw a frame.
     *
     * Code by Cody Jackson
     * @param headTransform The head transformation in the new frame.
     */
    @Override
    public void onNewFrame(HeadTransform headTransform) {
        // rotate cube
        Matrix.rotateM(cubeMatrix, 0, TIME_DELTA, 0.5f, 0.5f, 1.0f);

        // set up reticule transforms
        Matrix.setIdentityM(reticuleMatrix, 0);
        Matrix.setIdentityM(reticuleTransMatrix, 0);
        Matrix.setIdentityM(reticuleRotMatrix, 0);
        Matrix.setIdentityM(reticuleScaleMatrix, 0);
        Matrix.setIdentityM(tempMatrix, 0);

        // scale it (this happens first, note careful order of LHS/RHS)
        Matrix.scaleM(reticuleScaleMatrix, 0, RETICULE_SIZE, RETICULE_SIZE, RETICULE_SIZE); // make it smaller
        Matrix.multiplyMM(reticuleMatrix, 0, reticuleScaleMatrix, 0, tempMatrix, 0);

        // translate it
        Matrix.translateM(reticuleTransMatrix, 0, reticulePosition[0], reticulePosition[1], reticulePosition[2]);
        Matrix.multiplyMM(tempMatrix, 0, reticuleTransMatrix, 0, reticuleMatrix, 0);

        // rotate it (this happens last)
        headTransform.getEulerAngles(reticuleAngles, 0);
        reticuleAngles[0] = (float)(reticuleAngles[0] * 180 / Math.PI); // convert to degrees
        reticuleAngles[1] = (float)(reticuleAngles[1] * 180 / Math.PI);
        reticuleAngles[2] = (float)(reticuleAngles[2] * 180 / Math.PI);
        Matrix.rotateM(reticuleRotMatrix, 0, reticuleAngles[1], 0f, 1f, 0f); // apply yaw 1st as per GVR docs
        Matrix.rotateM(reticuleRotMatrix, 0, reticuleAngles[0], 1f, 0f, 0f); // apply pitch 2nd
        Matrix.rotateM(reticuleRotMatrix, 0, reticuleAngles[2], 0f, 0f, 1f); // roll last

        Matrix.multiplyMM(reticuleMatrix, 0, reticuleRotMatrix, 0, tempMatrix, 0);

        // Build the cameraMatrix matrix and apply it to the ModelView for cube
        // You can apply position here to move cameraMatrix
        // note that we're always looking at the point in front of the camera (camera z - 1)
        // (rotation is handled by the HeadTransform/EyeView)
        Matrix.setLookAtM(cameraMatrix, 0, cameraPosition[0], cameraPosition[1], cameraPosition[2],
                cameraPosition[0], cameraPosition[1], cameraPosition[2] - 1, 0.0f, 1.0f, 0.0f);
        checkGLError("onReadyToDraw");
    }

    /**
     * Draws a frame for an eye.
     *
     * Code provided by Google
     * Modified by Cody Jackson to draw second cube
     * @param eye The eye to render. Includes all required transformations.
     */
    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        checkGLError("colorParam");

        // Apply the eye transformation to the cameraMatrix (this rotates it to match HMD)
        Matrix.multiplyMM(viewMatrix, 0, eye.getEyeView(), 0, cameraMatrix, 0);
        float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);

        // set up MVP for cube
        Matrix.multiplyMM(modelView, 0, viewMatrix, 0, cubeMatrix, 0);
        Matrix.multiplyMM(modelViewProj, 0, perspective, 0, modelView, 0);
        drawCube(false); // draw cube

        // reticule goes on top of everything else
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
        // set up MVP for reticule
        Matrix.multiplyMM(modelView, 0, viewMatrix, 0, reticuleMatrix, 0);
        Matrix.multiplyMM(modelViewProj, 0, perspective, 0, modelView, 0);
        drawCube(true); // draw crosshair
    }

    // required method
    @Override
    public void onFinishFrame(Viewport viewport) {}

    /**
     * Draw the cube.
     *
     * Code provided Cody Jackson
     * @param crosshair should we draw the crosshair or the cube? true is for crosshair
     */
    public void drawCube(boolean crosshair) {
        GLES20.glUseProgram(shaderProgram);
        // Set the ModelViewProjection matrix in the shader.
        GLES20.glUniformMatrix4fv(mvpLoc, 1, false, modelViewProj, 0);

        // bind the buffers
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, cubeVerticesVbo);
        GLES20.glVertexAttribPointer(positionLoc, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, 0);

        if (crosshair) { // choose which VBO to use for vert colors
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, retColorsVbo);
        } else {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, cubeColorsVbo);
        }
        GLES20.glVertexAttribPointer(colorLoc, 4, GLES20.GL_FLOAT, false, 0, 0);
        // enable the two VBOs
        GLES20.glEnableVertexAttribArray(positionLoc);
        GLES20.glEnableVertexAttribArray(colorLoc);
        // draw cube
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
        // unbind VBO (good habit)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glDisableVertexAttribArray(positionLoc);
        GLES20.glDisableVertexAttribArray(positionLoc);
        checkGLError("Drawing cube");
    }
}
