package net.toughcoder.starcamera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import net.toughcoder.oaqs.ShaderHelper;
import net.toughcoder.oaqs.ShaderProgram;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by alexhilton on 15/8/1.
 */
public class StarCameraRender implements GLSurfaceView.Renderer, Camera.PreviewCallback {
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

    private Context mContext;
    private List<Runnable> mRendererJob;
    private SurfaceTexture mCameraPreviewTexture;
    private ByteBuffer mYBuffer;
    private ByteBuffer mUVBuffer;
    private int mYTexture;
    private int mUVTexture;
    private int mGLProgram;
    private int mAttribPosition;
    private int mTextureCoords;
    private int mYUniformLocation;
    private int mUVUniformLocation;

    public StarCameraRender(Context ctx) {
        mContext = ctx;
        mRendererJob = new LinkedList<>();
        mYTexture = -1;
        mUVTexture = -1;
        setupCamera();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0f, 0f, 0f, 1.0f);
        mGLProgram = ShaderHelper.buildProgram(VERTEX_SHADER, YUV_FRAGMENT_SHADER);

        mAttribPosition = GLES20.glGetAttribLocation(mGLProgram, "position");
        mTextureCoords = GLES20.glGetAttribLocation(mGLProgram, "inputTextureCoords");
        mYUniformLocation = GLES20.glGetUniformLocation(mGLProgram, "uYTextureSampler");
        mUVUniformLocation = GLES20.glGetUniformLocation(mGLProgram, "uUVTextureSampler");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(mGLProgram);
        if (mYTexture < 0 || mUVTexture < 0) {
            return;
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
            }
        });
    }

    private void setupCamera() {
        addRendererJob(new Runnable() {
            @Override
            public void run() {
                try {
                    int[] texture = new int[1];
                    GLES20.glGenTextures(1, texture, 0);
                    mCameraPreviewTexture = new SurfaceTexture(texture[0]);
                    Camera camera = Camera.open();
                    Camera.Parameters params = camera.getParameters();
                    params.setPreviewSize(params.getSupportedPreviewSizes().get(1).width,
                            params.getSupportedPreviewSizes().get(1).height);
                    camera.setPreviewTexture(mCameraPreviewTexture);
                    camera.setPreviewCallback(StarCameraRender.this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void addRendererJob(Runnable job) {
        synchronized (mRendererJob) {
            mRendererJob.add(job);
        }
    }

    static class CameraModel {
        private float[] model = {
                -1.0f, -1.0f,
                1.0f, -1.0f,
                -1.0f, 1.0f,
                1.0f, 1.0f,
        };

        private float[] textureCoords = {
                0, 1,
                1, 1,
                1, 0,
                0, 0,
        };

        private FloatBuffer vertexBuffer;
        private FloatBuffer textureBuffer;

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
        }

        public void onDraw(int position, int texturePosition) {
            //
        }
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
