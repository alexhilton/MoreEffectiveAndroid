package net.toughcoder.starcamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import net.toughcoder.effectiveandroid.R;
import net.toughcoder.oaqs.ShaderHelper;
import net.toughcoder.oaqs.ShaderProgram;
import net.toughcoder.opengl2s.OpenGLES2SurfaceView;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by alexhilton on 15/8/1.
 */
public class StarCameraRender implements GLSurfaceView.Renderer, Camera.PreviewCallback {
    private static final String TAG = "StarCameraRender";

    private Context mContext;
    private List<Runnable> mRendererJob;
    private SurfaceTexture mCameraPreviewTexture;
    private ByteBuffer mYBuffer;
    private ByteBuffer mUVBuffer;
    private int mYTexture;
    private int mUVTexture;

    private CameraModel mModel;
    private Stars mStars;

    private Camera mCamera;

    public StarCameraRender(Context ctx) {
        mContext = ctx;
        mRendererJob = new LinkedList<>();
        mYTexture = -1;
        mUVTexture = -1;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0f, 0f, 0f, 1.0f);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        mModel = new CameraModel();
        mStars = new Stars(mContext, 50);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        runAllJobs();

        mModel.onDraw(mYTexture, mUVTexture);
        mStars.onDraw();

        if (mCameraPreviewTexture != null) {
            mCameraPreviewTexture.updateTexImage();
        }
    }

    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        if (!mRendererJob.isEmpty()) {
            return;
        }
        addRendererJob(new Runnable() {
            @Override
            public void run() {
                // create texture
                Camera.Size size = camera.getParameters().getPreviewSize();
                final int frameSize = size.width * size.height;
                if (mYBuffer == null) {
                    mYBuffer = ByteBuffer.allocateDirect(frameSize)
                            .order(ByteOrder.nativeOrder());
                }
                if (mUVBuffer == null) {
                    mUVBuffer = ByteBuffer.allocateDirect(frameSize / 2)
                            .order(ByteOrder.nativeOrder());
                }
                mYBuffer.put(data, 0, frameSize);
                mYBuffer.position(0);
                mUVBuffer.put(data, frameSize, frameSize / 2);
                mUVBuffer.position(0);

                mYTexture = loadTexture(mYBuffer, size.width, size.height, mYTexture, false);
                mUVTexture = loadTexture(mUVBuffer, size.width / 2, size.height / 2, mUVTexture, true);
                camera.addCallbackBuffer(data);
            }
        });
    }

    public void resume() {
        setupCamera();
    }

    public void pause() {
        if (mCamera == null) {
            return;
        }
        mCamera.setPreviewCallback(null);
        mCamera.release();
        mCamera = null;
    }

    private void runAllJobs() {
        synchronized (mRendererJob) {
            while (!mRendererJob.isEmpty()) {
                mRendererJob.remove(0).run();
            }
        }
    }

    private void setupCamera() {
        addRendererJob(new Runnable() {
            @Override
            public void run() {
                try {
                    int[] texture = new int[1];
                    GLES20.glGenTextures(1, texture, 0);
                    mCameraPreviewTexture = new SurfaceTexture(texture[0]);
                    mCamera = Camera.open();
                    configCamera(mCamera);
                    Camera.Parameters params = mCamera.getParameters();
                    params.setPreviewSize(params.getSupportedPreviewSizes().get(1).width,
                            params.getSupportedPreviewSizes().get(1).height);
                    mCamera.setPreviewTexture(mCameraPreviewTexture);
                    mCamera.setPreviewCallback(StarCameraRender.this);
                    mCamera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    Log.e(TAG, "exception ", e);
                }
            }
        });
    }

    private void configCamera(Camera camera) {
        Camera.Parameters params = camera.getParameters();
        if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        camera.setParameters(params);
    }

    private void addRendererJob(Runnable job) {
        synchronized (mRendererJob) {
            mRendererJob.add(job);
        }
    }

    static class CameraModel {
        public static final String VERTEX_SHADER = "" +
                "attribute vec4 position;\n" +
                "attribute vec4 inputTextureCoords;\n" +
                "varying vec2 textureCoords;\n" +
                "void main() {\n" +
                "  gl_Position = position;\n" +
                "  textureCoords = inputTextureCoords.xy;\n" +
                "}";

        public static final String YUV_FRAGMENT_SHADER = "" +
                "precision highp float;\n" +
                "const vec2 uvDelta = vec2(0.5 , 0.5);\n" +
                "const mat3 convertMatrix = mat3(1.0 , 1.0 , 1.0 , 0 , -0.39465 , 2.03211 , 1.13983 , -0.58060 , 0);\n" +
                "uniform sampler2D uYTextureSampler;\n" +
                "uniform sampler2D uUVTextureSampler;\n" +
                "varying vec2 textureCoords;\n" +
                "void main () {\n" +
                "  vec3 yuv = vec3(texture2D(uYTextureSampler, textureCoords).r, texture2D(uUVTextureSampler, textureCoords).ar - uvDelta);\n" +
                "  vec3 rgb = convertMatrix * yuv;\n" +
                "  gl_FragColor = vec4(rgb, 1.0);\n" +
                "}";
        private float[] model = {
                -1.0f, -1.0f,
                1.0f, -1.0f,
                -1.0f, 1.0f,
                1.0f, 1.0f,
        };

        private float[] textureCoords = {
                1, 1,
                1, 0,
                0, 1,
                0, 0,
        };

        private FloatBuffer vertexBuffer;
        private FloatBuffer textureBuffer;

        private int mGLProgram;
        private int mAttribPosition;
        private int mTextureCoords;
        private int mYUniformLocation;
        private int mUVUniformLocation;

        public CameraModel() {
            vertexBuffer = ByteBuffer.allocateDirect(model.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            vertexBuffer.position(0);
            vertexBuffer.put(model);

            textureBuffer = ByteBuffer.allocateDirect(textureCoords.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            textureBuffer.position(0);
            textureBuffer.put(textureCoords);

            mGLProgram = ShaderHelper.buildProgram(VERTEX_SHADER, YUV_FRAGMENT_SHADER);

            mAttribPosition = GLES20.glGetAttribLocation(mGLProgram, "position");
            mTextureCoords = GLES20.glGetAttribLocation(mGLProgram, "inputTextureCoords");
            mYUniformLocation = GLES20.glGetUniformLocation(mGLProgram, "uYTextureSampler");
            mUVUniformLocation = GLES20.glGetUniformLocation(mGLProgram, "uUVTextureSampler");
        }

        public void onDraw(int yTexture, int uvTexture) {
            GLES20.glUseProgram(mGLProgram);
            if (yTexture < 0 || uvTexture < 0) {
                return;
            }

            GLES20.glEnableVertexAttribArray(mAttribPosition);
            GLES20.glEnableVertexAttribArray(mTextureCoords);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glUniform1i(mYUniformLocation, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, yTexture);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glUniform1i(mUVUniformLocation, 1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, uvTexture);

            vertexBuffer.position(0);
            GLES20.glVertexAttribPointer(mAttribPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
            textureBuffer.position(0);
            GLES20.glVertexAttribPointer(mTextureCoords, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

            GLES20.glDisableVertexAttribArray(mAttribPosition);
            GLES20.glDisableVertexAttribArray(mTextureCoords);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        }
    }

    private static class Stars {
        private static final String VERTEX_SHADER =
                        "attribute vec4 modelPosition;\n" +
                        "attribute vec4 inputTextureCoords;\n" +
                        "uniform mat4 aMatrix;\n" +
                        "varying vec2 textureCoords;\n" +
                        "void main() {\n" +
                        "  gl_Position = aMatrix * modelPosition;\n" +
                        "  textureCoords = inputTextureCoords.xy;\n" +
                        "}";
        private static final String FRAGMENT_SHADER =
                        "varying highp vec2 textureCoords;\n" +
                        "uniform sampler2D uTexture;\n" +
                        "void main() {\n" +
                        "  gl_FragColor = texture2D(uTexture, textureCoords);\n" +
                        "}";
        private float starLen = 0.1f;
        private float[] star = {
                -starLen, -starLen,
                starLen, -starLen,
                -starLen, starLen,
                starLen, starLen,
        };
        private float[] coords = {
                0f, 1f,
                1f, 1f,
                0f, 0f,
                1f, 0f,
        };

        private float[] colors = {
                1f, 0f, 0f, 1f,
                0.5f, 0.5f, 0.5f, 1f,
                0f, 1f, 0f, 1f,
                0f, 0f, 1f, 1f,
        };

        private FloatBuffer mStar;
        private FloatBuffer mCoords;
        private FloatBuffer mColor;
        private int mCount;

        private int mGLProgram;
        private int mAttribPosition;
        private int mAttribTextureCoords;
        private int mAttribMatrix;
        private int mUniformTexture;
        private int mAttribColor;
        private int mAttribLight; // light position
        private int mUniformNormal; // normal of the plane
        private int mTextureId = -1;

        private Context mContext;

        private Random random;

        private float[] modelMatrix = new float[16];

        public Stars(Context ctx, int count) {
            mContext = ctx;
            mCount = count;
            mStar = ByteBuffer.allocateDirect(star.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            mStar.put(star);
            mCoords = ByteBuffer.allocateDirect(coords.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            mCoords.put(coords);
            mColor = ByteBuffer.allocateDirect(colors.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            mGLProgram = ShaderHelper.buildProgram(VERTEX_SHADER, FRAGMENT_SHADER);
            mAttribPosition = GLES20.glGetAttribLocation(mGLProgram, "modelPosition");
            mAttribTextureCoords = GLES20.glGetAttribLocation(mGLProgram, "inputTextureCoords");
            mUniformTexture = GLES20.glGetUniformLocation(mGLProgram, "uTexture");
            mAttribMatrix = GLES20.glGetUniformLocation(mGLProgram, "aMatrix");

            Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.star4);
            mTextureId = loadTexture(bitmap);
            bitmap.recycle();

            random = new Random();
        }

        public void onDraw() {
            GLES20.glUseProgram(mGLProgram);

            for (int i = 0; i < mCount; i++) {
                Matrix.setIdentityM(modelMatrix, 0);
                float x = random.nextFloat();
                float y = random.nextFloat();
                float z = random.nextFloat();
                Matrix.translateM(modelMatrix, 0, x, y, z);

                GLES20.glEnableVertexAttribArray(mAttribPosition);
                GLES20.glEnableVertexAttribArray(mAttribTextureCoords);
                mStar.position(0);
                GLES20.glVertexAttribPointer(mAttribPosition, 2, GLES20.GL_FLOAT, false, 0, mStar);
                mCoords.position(0);
                GLES20.glVertexAttribPointer(mAttribTextureCoords, 2, GLES20.GL_FLOAT, false, 0, mCoords);

//                GLES20.glVertexAttrib4f(mAttribColor, .91f, .97f, .91f, 1f);

                GLES20.glUniformMatrix4fv(mAttribMatrix, 1, false, modelMatrix, 0);

//                GLES20.glVertexAttrib3f(mAttribLight, x, y, z);
//                GLES20.glUniform3f(mUniformNormal, 0f, 0f, 1f);

                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glUniform1i(mUniformTexture, 0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);

                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

                GLES20.glDisableVertexAttribArray(mAttribPosition);
                GLES20.glDisableVertexAttribArray(mAttribTextureCoords);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            }
        }
    }

    private static int loadTexture(Bitmap bitmap) {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        return textures[0];
    }

    public static int loadTexture(final ByteBuffer data, final int width, int height, final int oldTextureId, boolean isUV) {
        int[] textures = new int[1];
        if (oldTextureId == -1) {
            GLES20.glGenTextures(1, textures, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, isUV ? GLES20.GL_LUMINANCE_ALPHA : GLES20.GL_LUMINANCE,
                    width, height, 0, isUV ? GLES20.GL_LUMINANCE_ALPHA : GLES20.GL_LUMINANCE,
                    GLES20.GL_UNSIGNED_BYTE, data);
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, oldTextureId);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, isUV ? GLES20.GL_LUMINANCE_ALPHA : GLES20.GL_LUMINANCE,
                    width, height, 0, isUV ? GLES20.GL_LUMINANCE_ALPHA : GLES20.GL_LUMINANCE,
                    GLES20.GL_UNSIGNED_BYTE, data);
            textures[0] = oldTextureId;
        }
        return textures[0];
    }
}
