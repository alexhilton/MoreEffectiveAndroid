package net.toughcoder.opengl.sharedcontext;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;

import net.toughcoder.effectiveandroid.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

@TargetApi(Build.VERSION_CODES.M)
public class SharedEGLContextActivity extends Activity implements SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = "SharedEGLContext";
    private static final int REQ_PERMISSION = 0x01;

    private static final int MSG_START_PREVIEW = 0x100;

    private GLSurfaceView mPreview;
    private GLSurfaceView mFilter;
    private SurfaceTexture mSurfaceTexture;
    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private static final String CAMERA = "0";
    private int mPreviewTexture;
    private CameraCaptureSession mSession;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private SharedEGLContextFactory mEGLContextFactory;

    private Handler mMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //
        }
    };

    private CameraDevice.StateCallback mSetupCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.d(TAG, "setup onOpened");
            mCameraDevice = camera;
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            mCameraDevice = null;
        }
    };


    // Need to wait for SurfaceTexture creation.
    private void setupPreview() {
        Log.d(TAG, "setuppreview device + " + mCameraDevice + ", surface " + mSurfaceTexture);
        mSurfaceTexture.setOnFrameAvailableListener(this);
        mCameraHandler.removeMessages(MSG_START_PREVIEW);
        if (mCameraDevice == null) {
            mCameraHandler.sendEmptyMessageDelayed(MSG_START_PREVIEW, 100);
            return;
        }
        List<Surface> target = new ArrayList<>();
        Surface targetSurface = new Surface(mSurfaceTexture);
        target.add(targetSurface);
        try {
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        mPreviewRequestBuilder.addTarget(targetSurface);
        try {
            mCameraDevice.createCaptureSession(target, mSessionCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraCaptureSession.StateCallback mSessionCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            Log.d(TAG, "session callback onConfigure session + " + session);
            mSession = session;
            try {
                mSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback, mCameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            mSession = null;
        }
    };

    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
        }
    };

    private HandlerThread mCameraThread;
    private Handler mCameraHandler;

    private GLSurfaceView.Renderer mPreviewRenderer;
    private GLSurfaceView.Renderer mFilterRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_eglcontext);
        setTitle(TAG);

        // init thread
        mCameraThread = new HandlerThread("Camera Handler Thread");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_START_PREVIEW: {
                        setupPreview();
                        break;
                    }
                }
            }
        };

        mEGLContextFactory = new SharedEGLContextFactory();
        mPreviewRenderer = new PreviewRenderer();
        mPreview = initGLSurfaceView(R.id.preview);
        mPreview.setEGLContextFactory(mEGLContextFactory);
        mPreview.setRenderer(mPreviewRenderer);
        mPreview.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mPreview.setZOrderOnTop(false);
        mFilterRenderer = new FilterRenderer();
        mFilter = initGLSurfaceView(R.id.filter);
        mFilter.setEGLContextFactory(mEGLContextFactory);
        mFilter.setRenderer(mFilterRenderer);
        mFilter.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mFilter.setZOrderOnTop(true);
        // init camera
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.CAMERA}, REQ_PERMISSION);
            return;
        } else {
            try {
                mCameraManager.openCamera(CAMERA, mSetupCallback, mCameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private GLSurfaceView initGLSurfaceView(int resId) {
        GLSurfaceView view = (GLSurfaceView) findViewById(resId);
        view.setEGLContextClientVersion(2);
        view.setPreserveEGLContextOnPause(true);
        view.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        return view;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mPreview.requestRender();
        mFilter.requestRender();
    }

    // Must be called in GLContext thread
    private void initializeSurfaceTexture() {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        mPreviewTexture = textures[0];
        mSurfaceTexture = new SurfaceTexture(mPreviewTexture);
    }

    public static int loadProgram(final String vsh, final String fsh) {
        int vshader = loadShader(vsh, GLES20.GL_VERTEX_SHADER);
        if (vshader == 0) {
            Log.e(TAG, "failed to load vertex shader " + vsh);
            return 0;
        }

        int fshader = loadShader(fsh, GLES20.GL_FRAGMENT_SHADER);
        if (fshader == 0) {
            Log.e(TAG, "failed to load fragment shader " + fsh);
            return 0;
        }

        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vshader);
        GLES20.glAttachShader(program, fshader);
        GLES20.glLinkProgram(program);

        int[] status = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0);
        if (status[0] <= 0) {
            Log.e(TAG, "failed to link program");
        }

        GLES20.glDeleteShader(vshader);
        GLES20.glDeleteShader(fshader);

        return program;
    }

    public static int loadShader(String source, int type) {
        int[] status = new int[1];
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] == 0) {
            Log.e(TAG, "failed to compile shader " + source + ", type " + type);
            return 0;
        }
        return shader;
    }

    private class PreviewRenderer implements GLSurfaceView.Renderer {
        private static final String TAG = "PreviewRenderer";
        public final float[] CUBE = {
                -1.0f, -1.0f,
                1.0f, -1.0f,
                -1.0f, 1.0f,
                1.0f, 1.0f,
        };
        public final float[] TEXTURE_NO_ROTATION = {
                0.0f, 0.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
        };
        private final FloatBuffer mCubeBuffer;
        private final FloatBuffer mTextureBuffer;

        public static final String VERTEX_SHADER =
                "attribute vec4 position;\n" +
                        "uniform mat4 uSTMatrix;\n" +
                        "attribute vec4 inputTextureCoords;\n" +
                        "varying vec2 textureCoords;\n" +
                        "void main() {\n" +
                        "  gl_Position = position;\n" +
                        "  textureCoords = (uSTMatrix * inputTextureCoords).xy;\n" +
                        "}";

        public static final String FRAGMENT_SHADER =
                "#extension GL_OES_EGL_image_external : require\n" +
                        "precision highp float;\n" +
                        "uniform samplerExternalOES uTextureSampler;\n" +
                        "varying vec2 textureCoords;\n" +
                        "void main () {\n" +
                        "  vec4 tex = texture2D(uTextureSampler, textureCoords);\n" +
                        "  gl_FragColor = vec4(tex.rgb, 1);\n" +
                        "}";

        private int mProgram;
        private int mAttributePosition;
        protected int mTextureCoords;
        private int mUniformLocation;
        private int mUniformMatrix;

        public PreviewRenderer() {
            mCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            mCubeBuffer.put(CUBE).position(0);

            mTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_NO_ROTATION.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            mTextureBuffer.put(TEXTURE_NO_ROTATION).position(0);
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            Log.d(TAG, "onSurfaceCreated");
            initializeSurfaceTexture();
            // Able to start preview now.
            mCameraHandler.sendEmptyMessage(MSG_START_PREVIEW);

            // Initialize GL stuff
            GLES20.glDisable(GLES20.GL_DITHER);
            GLES20.glClearColor(0, 0, 0, 0);
            GLES20.glEnable(GLES20.GL_CULL_FACE);
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);

            init();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            Log.d(TAG, "onSurfaceChanged");
            GLES20.glViewport(0, 0, width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            mSurfaceTexture.updateTexImage();


            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glUseProgram(mProgram);

            mCubeBuffer.position(0);
            GLES20.glVertexAttribPointer(mAttributePosition, 2, GLES20.GL_FLOAT, false, 0, mCubeBuffer);
            GLES20.glEnableVertexAttribArray(mAttributePosition);

            mTextureBuffer.position(0);
            GLES20.glVertexAttribPointer(mTextureCoords, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer);
            GLES20.glEnableVertexAttribArray(mTextureCoords);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glUniform1i(mUniformLocation, 0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mPreviewTexture);

            float[] matrix = new float[16];
            mSurfaceTexture.getTransformMatrix(matrix);
            GLES20.glUniformMatrix4fv(mUniformMatrix, 1, false, matrix, 0);
            final int error = GLES20.glGetError();
            if (error != 0) {
                Log.e("render", "render error " + String.format("0x%x", error));
            }

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            GLES20.glDisableVertexAttribArray(mAttributePosition);
            GLES20.glDisableVertexAttribArray(mTextureCoords);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        }

        public final void init() {
            mProgram = loadProgram(VERTEX_SHADER, FRAGMENT_SHADER);
            mAttributePosition = GLES20.glGetAttribLocation(mProgram, "position");
            mTextureCoords = GLES20.glGetAttribLocation(mProgram, "inputTextureCoords");
            mUniformLocation = GLES20.glGetUniformLocation(mProgram, "uTextureSampler");
            mUniformMatrix = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
        }
    }

    private class FilterRenderer implements GLSurfaceView.Renderer {
        private static final String TAG = "FilterRenderer";
        public final float[] CUBE = {
                -1.0f, -1.0f,
                1.0f, -1.0f,
                -1.0f, 1.0f,
                1.0f, 1.0f,
        };
        public final float[] TEXTURE_NO_ROTATION = {
                0.0f, 0.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
        };
        private final FloatBuffer mCubeBuffer;
        private final FloatBuffer mTextureBuffer;

        public static final String VERTEX_SHADER =
                "attribute vec4 position;\n" +
                        "uniform mat4 uSTMatrix;\n" +
                        "attribute vec4 inputTextureCoords;\n" +
                        "varying vec2 textureCoords;\n" +
                        "void main() {\n" +
                        "  gl_Position = position;\n" +
                        "  textureCoords = (uSTMatrix * inputTextureCoords).xy;\n" +
                        "}";

        public static final String GRAYSCALE_FRAGMENT_SHADER =
                "#extension GL_OES_EGL_image_external : require\n" +
                        "precision highp float;\n" +
                        "uniform samplerExternalOES uTextureSampler;\n" +
                        "varying vec2 textureCoords;\n" +
                        "void main () {\n" +
                        "  vec4 tex = texture2D(uTextureSampler, textureCoords);\n" +
                        "  vec3 factor = vec3(0.299, 0.587, 0.114); \n" +
                        "  float gray = dot(tex.rgb, factor); \n" +
                        "  gl_FragColor = vec4(gray, gray, gray, 1);\n" +
                        "}";

        private int mProgram;
        private int mAttributePosition;
        protected int mTextureCoords;
        private int mUniformLocation;
        private int mUniformMatrix;

        public FilterRenderer() {
            mCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            mCubeBuffer.put(CUBE).position(0);

            mTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_NO_ROTATION.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            mTextureBuffer.put(TEXTURE_NO_ROTATION).position(0);
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            Log.d(TAG, "onSurfaceCreated");
            // Initialize GL stuff
            GLES20.glDisable(GLES20.GL_DITHER);
            GLES20.glClearColor(0, 0, 0, 0);
            GLES20.glEnable(GLES20.GL_CULL_FACE);
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);

            init();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            Log.d(TAG, "onSurfaceChanged");
            GLES20.glViewport(0, 0, width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glUseProgram(mProgram);

            mCubeBuffer.position(0);
            GLES20.glVertexAttribPointer(mAttributePosition, 2, GLES20.GL_FLOAT, false, 0, mCubeBuffer);
            GLES20.glEnableVertexAttribArray(mAttributePosition);

            mTextureBuffer.position(0);
            GLES20.glVertexAttribPointer(mTextureCoords, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer);
            GLES20.glEnableVertexAttribArray(mTextureCoords);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glUniform1i(mUniformLocation, 0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mPreviewTexture);

            float[] matrix = new float[16];
            mSurfaceTexture.getTransformMatrix(matrix);
            GLES20.glUniformMatrix4fv(mUniformMatrix, 1, false, matrix, 0);
            final int error = GLES20.glGetError();
            if (error != 0) {
                Log.e("render", "render error " + String.format("0x%x", error));
            }

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            GLES20.glDisableVertexAttribArray(mAttributePosition);
            GLES20.glDisableVertexAttribArray(mTextureCoords);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        }

        public final void init() {
            mProgram = loadProgram(VERTEX_SHADER, GRAYSCALE_FRAGMENT_SHADER);
            mAttributePosition = GLES20.glGetAttribLocation(mProgram, "position");
            mTextureCoords = GLES20.glGetAttribLocation(mProgram, "inputTextureCoords");
            mUniformLocation = GLES20.glGetUniformLocation(mProgram, "uTextureSampler");
            mUniformMatrix = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
        }
    }

    public class SharedEGLContextFactory implements GLSurfaceView.EGLContextFactory {
        private int CLIENT_VERSION = 0x3098;

        public EGLContext mSharedContext;

        @Override
        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
            int[] attrib_list = {CLIENT_VERSION, 2, EGL10.EGL_NONE};
            mSharedContext = egl.eglCreateContext(display,
                    eglConfig, mSharedContext == null ? EGL10.EGL_NO_CONTEXT : mSharedContext, attrib_list);
            return mSharedContext;
        }

        @Override
        public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
            if (!egl.eglDestroyContext(display, context)) {
                Log.d(TAG, "Failed to destory context");
            }
        }
    }
}
