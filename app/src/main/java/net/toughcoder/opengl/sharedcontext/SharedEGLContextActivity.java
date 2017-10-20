package net.toughcoder.opengl.sharedcontext;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.widget.Toast;

import net.toughcoder.effectiveandroid.R;
import net.toughcoder.opengl.miniglview.OpenGLESView;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.M)
public class SharedEGLContextActivity extends Activity implements SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = "SharedEGLContext";
    private static final int REQ_PERMISSION = 0x01;

    private static final int MSG_START_PREVIEW = 0x100;

    private SurfaceTexture mSurfaceTexture;
    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private static final String CAMERA = "0";
    private int mPreviewTexture;
    private CameraCaptureSession mSession;
    private CaptureRequest.Builder mPreviewRequestBuilder;

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
    private void startPreview(int targetWidth, int targetHeight) {
        Log.d(TAG, "setuppreview device + " + mCameraDevice + ", surface " + mSurfaceTexture);
        mCameraHandler.removeMessages(MSG_START_PREVIEW);
        configPreviewSize(targetWidth, targetHeight);
        mSurfaceTexture.setOnFrameAvailableListener(this);
        if (mCameraDevice == null) {
            Message msg = Message.obtain();
            msg.what = MSG_START_PREVIEW;
            msg.obj = new Size(targetWidth, targetHeight);
            mCameraHandler.sendMessageDelayed(msg, 100);
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

    private void configPreviewSize(int targetWidth, int targetHeight) {
        try {
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(CAMERA);
            StreamConfigurationMap configMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            // Find out whether need to swap dimensions
            final int displayRotation = getWindowManager().getDefaultDisplay().getRotation();
            final int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            boolean needSwap = false;
            switch (displayRotation) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    needSwap = sensorOrientation == 90 || sensorOrientation == 270;
                    break;
                case Surface.ROTATION_90:
                case Surface.ROTATION_270:
                    needSwap = sensorOrientation == 0 || sensorOrientation == 180;
                    break;
                default:
                    Log.d(TAG, "Invalid display rotation, something is really wrong.");
            }
            final int rotatedWidth = needSwap ? targetHeight : targetWidth;
            final int rotatedHeight = needSwap ? targetWidth : targetHeight;
            final Size bestPreviewSize = chooseOptimalPreviewSize(configMap.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);
            Log.d(TAG, "preview size w -> " + bestPreviewSize.getWidth() + ", height -> " + bestPreviewSize.getHeight());
            mSurfaceTexture.setDefaultBufferSize(bestPreviewSize.getWidth(), bestPreviewSize.getHeight());
            configRenderers(bestPreviewSize.getWidth(), bestPreviewSize.getHeight());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void configRenderers(int width, int height) {
        mPreviewRenderer.setInputDimension(width, height);
        for (Filter filter : mFilters) {
            Log.d(TAG, "config renderer filter -> " + filter);
            filter.setInputDimension(width, height);
        }
    }

    private Size chooseOptimalPreviewSize(Size[] choices, int targetWidth, int targetHeight) {
        final float desiredRatio = (float) targetWidth / (float) targetHeight;
        for (Size option : choices) {
            float ratio = (float) option.getWidth() / (float) option.getHeight();
            if (Math.abs(ratio - desiredRatio) < 0.02) {
                return option;
            }
        }
        return choices[0];
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

    private OpenGLESView mPreview;
    private SurfaceTextureRenderer mPreviewRenderer;

    private List<Filter> mFilters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_eglcontext);
        setTitle(TAG);

        initViews();
        mFilters = new ArrayList<>();

        // init camera manager
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
    }

    private void initViews() {
        // Initialize GLSurfaceView
        mPreviewRenderer = new PreviewRenderer();
        mPreview = (OpenGLESView) findViewById(R.id.preview);
        mPreview.shareEGLContext();
        mPreview.setRenderMode(OpenGLESView.RenderMode.WHEN_DIRTY);
        mPreview.setRenderer(mPreviewRenderer);
        mPreview.setZOrderOnTop(false);
    }

    private void addFilters() {
        mFilters.add(new Filter(this, R.id.grayscale, mSurfaceTexture, mPreviewTexture));
        mFilters.add(new Filter(this, R.id.swirl, mSurfaceTexture, mPreviewTexture));
        mFilters.add(new Filter(this, R.id.sphere, mSurfaceTexture, mPreviewTexture));
    }

    private void removeFilters() {
        mFilters.clear();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // start camera handler
        // init thread
        startCameraThread();
    }

    private void startCameraThread() {
        mCameraThread = new HandlerThread("Camera Handler Thread");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_START_PREVIEW: {
                        Size p = (Size) msg.obj;
                        startPreview(p.getWidth(), p.getHeight());
                        break;
                    }
                }
            }
        };
    }

    private void stopCameraThread() {
        mCameraHandler.removeCallbacksAndMessages(null);
        mCameraThread.quitSafely();
        try {
            mCameraThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mCameraThread = null;
        mCameraHandler = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume mSurfaceTexture -> " + mSurfaceTexture);
        openCamera();
        // For not creation case
        if (mSurfaceTexture != null && !mCameraHandler.hasMessages(MSG_START_PREVIEW)) {
            Message msg = Message.obtain();
            msg.what = MSG_START_PREVIEW;
            msg.obj = new Size(mPreview.getWidth(), mPreview.getHeight());
            mCameraHandler.sendMessage(msg);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        closeCamera();
    }

    @Override
    public void onStop() {
        super.onStop();
        // stop camera thread.
        stopCameraThread();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult requestCode = " + requestCode + " permissions " + permissions + ", results " + grantResults);
        if (requestCode == REQ_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Cannot work without camera", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void openCamera() {
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

    private void closeCamera() {
        if (mSession != null) {
            mSession.close();
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        Log.d(TAG, "onFrameAvailable ");
        mPreview.requestRender();

        for (Filter filter : mFilters) {
            filter.requestRender();
        }
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

    private class PreviewRenderer extends SurfaceTextureRenderer {
        private static final String TAG = "PreviewRenderer";

        @Override
        public void onContextCreate() {
            Log.d(TAG, "onContextCreate");
            super.onContextCreate();
            initializeSurfaceTexture();

            addFilters();
        }

        @Override
        public void onContextChange(int width, int height) {
            Log.d(TAG, "onContextChange width -> " + width + ", height -> " + height);
            super.onContextChange(width, height);
            // Able to start preview now.
            // When back from HOME, onResume will start preview first, no need second one.
            if (mSession == null) {
                Message msg = Message.obtain();
                msg.what = MSG_START_PREVIEW;
                msg.obj = new Size(width, height);
                mCameraHandler.sendMessage(msg);
            }
        }

        @Override
        public void onDrawFrame() {
            mSurfaceTexture.updateTexImage();
            super.onDrawFrame();
        }

        @Override
        public void onContextDestroy() {
            Log.d(TAG, "onContextDestroy -->");
            super.onContextDestroy();
            removeFilters();
            GLES20.glDeleteTextures(1, new int[] {mPreviewTexture}, 0);
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }

        @Override
        protected SurfaceTexture getSurfaceTexture() {
            return mSurfaceTexture;
        }

        @Override
        protected int getPreviewTexture() {
            return mPreviewTexture;
        }
    }
}
