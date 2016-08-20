package net.toughcoder.opengl.oaqs;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import net.toughcoder.effectiveandroid.R;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by alexhilton on 15/6/29.
 */
public class AirHockeyRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "AirHockeyRenderer";

    private final float leftBound = -0.5f;
    private final float rightBound = 0.5f;
    private final float farBound = -0.8f;
    private final float nearBound = 0.8f;

    private final float[] projectionMatrix = new float[16];
    private final float[] modelMatrix = new float[16];

    private final float[] viewMatrix = new float[16];
    private final float[] viewProjectionMatrix = new float[16];
    private final float[] modelViewProjectionMatrix = new float[16];
    private final float[] invertedViewProjectionMatrix = new float[16];

    private Table table;
    private Mallet mallet;
    private Puck puck;

    private TextureShaderProgram textureProgram;
    private ColorShaderProgram colorProgram;

    private Context context;

    private int texture;

    // for touch events
    private boolean malletPressed;
    private Point blueMalletPosition;
    private Point previousBlueMalletPosition;

    private Point puckPosition;
    private Geometry.Vector puckVector;

    public AirHockeyRenderer(Context ctx) {
        context = ctx;
        malletPressed = false;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(.0f, .0f, .0f, .0f);
        table = new Table();
        mallet = new Mallet(0.08f, 0.15f, 32);
        puck = new Puck(0.06f, 0.02f, 32);
        puckPosition = new Point(0f, puck.height / 2.0f, 0f);
        puckVector = new Geometry.Vector(0f, 0f, 0f);

        textureProgram = new TextureShaderProgram(context);
        colorProgram = new ColorShaderProgram(context);

        texture = TextureHelper.loadTexture(context, R.drawable.air_hockey_surface);

        blueMalletPosition = new Point(0f, mallet.height / 2f, 0.4f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        MatrixHelper.perspectiveM(projectionMatrix, 45, (float) width / (float) height, 1f, 10f);

        Matrix.setLookAtM(viewMatrix, 0, // offset
                0f, 1.2f, 2.2f, // eye
                0f, 0f, 0f, // center
                0f, 1f, 0f); // head orientation
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        puckPosition = puckPosition.translate(puckVector);
        if (puckPosition.x < leftBound + puck.radius || puckPosition.x > rightBound - puck.radius) {
            puckVector = new Geometry.Vector(-puckVector.x, puckVector.y, puckVector.z);
            puckVector = puckVector.scale(0.9f);
        }
        if (puckPosition.z < farBound + puck.radius || puckPosition.z > nearBound - puck.radius) {
            puckVector = new Geometry.Vector(puckVector.x, puckVector.y, -puckVector.z);
            puckVector = puckVector.scale(0.9f);
        }
        puckPosition = new Point(
                clamp(puckPosition.x, leftBound + puck.radius, rightBound - puck.radius),
                puckPosition.y,
                clamp(puckPosition.z, farBound + puck.radius, nearBound - puck.radius));

        puckVector = puckVector.scale(0.99f);

        Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        Matrix.invertM(invertedViewProjectionMatrix, 0, viewProjectionMatrix, 0);

        // draw the table
        positionTableInScene();
        textureProgram.useProgram();
        textureProgram.setUniforms(modelViewProjectionMatrix, texture);
        table.bindData(textureProgram);
        table.draw();

        // draw the mallets
        positionObjectInScene(0f, mallet.height / 2f, -0.4f);
        colorProgram.useProgram();
        colorProgram.setUniforms(modelViewProjectionMatrix, .9f, .2f, 2f);
        mallet.bindData(colorProgram);
        mallet.draw();

        positionObjectInScene(blueMalletPosition.x, blueMalletPosition.y, blueMalletPosition.z);
        colorProgram.setUniforms(modelViewProjectionMatrix, .2f, .2f, .9f);
        mallet.draw();

        // draw the puck


        positionObjectInScene(puckPosition.x, puckPosition.y, puckPosition.z);
        colorProgram.setUniforms(modelViewProjectionMatrix, 0.8f, 0.8f, 0.4f);
        puck.bindData(colorProgram);
        puck.draw();
    }

    public void handleTouchPress(float x, float y) {
        Geometry.Ray ray = convertNormalized2DPointToRay(x, y);

        Geometry.Sphere malletBoundingSphere = new Geometry.Sphere(new Point(blueMalletPosition.x, blueMalletPosition.y, blueMalletPosition.z),
                mallet.height / 2f);
        malletPressed = Geometry.intersects(malletBoundingSphere, ray);
    }

    public void handleTouchDrag(float x, float y) {
        if (malletPressed) {
            Geometry.Ray ray = convertNormalized2DPointToRay(x, y);
            Geometry.Plane plane = new Geometry.Plane(new Point(0, 0, 0), new Geometry.Vector(0, 1, 0));
            Point touchedPoint = Geometry.intersectionPoint(ray, plane);

            previousBlueMalletPosition = blueMalletPosition;
            blueMalletPosition = new Point(
                    clamp(touchedPoint.x, leftBound + mallet.radius, rightBound - mallet.radius),
                    mallet.height / 2.f,
                    clamp(touchedPoint.z, 0f + mallet.radius, nearBound - mallet.radius));

            float distance = Geometry.vectorBetween(blueMalletPosition, puckPosition).length();
            if (distance < (puck.radius + mallet.radius)) {
                puckVector = Geometry.vectorBetween(previousBlueMalletPosition, blueMalletPosition);
            }
        }
    }

    private float clamp(float value, float min, float max) {
        return Math.min(max, Math.max(value, min));
    }

    private Geometry.Ray convertNormalized2DPointToRay(float x, float y) {
        final float[] nearPointNdc = {x, y, -1, 1};
        final float[] farPointNdc = {x, y, 1, 1};
        final float[] nearPointWorld = new float[4];
        final float[] farPointWorld = new float[4];

        Matrix.multiplyMV(nearPointWorld, 0, invertedViewProjectionMatrix, 0, nearPointNdc, 0);
        Matrix.multiplyMV(farPointWorld, 0, invertedViewProjectionMatrix, 0, farPointNdc, 0);

        divideByW(nearPointWorld);
        divideByW(farPointWorld);

        Point nearPointRay = new Point(nearPointWorld[0], nearPointWorld[1], nearPointWorld[2]);
        Point farPointRay = new Point(farPointWorld[0], farPointWorld[1], farPointWorld[2]);

        return new Geometry.Ray(nearPointRay, Geometry.vectorBetween(nearPointRay, farPointRay));
    }

    private void divideByW(float[] vector) {
        vector[0] /= vector[3];
        vector[1] /= vector[3];
        vector[2] /= vector[3];
    }

    private void positionTableInScene() {
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.rotateM(modelMatrix, 0, -90f, 1f, 0f, 0f);
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0);
    }

    private void positionObjectInScene(float x, float y, float z) {
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, x, y, z);
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0);
    }
}
