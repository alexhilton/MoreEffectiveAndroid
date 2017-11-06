package net.toughcoder.eos;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
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
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by alex on 17-11-3.
 * Encapsulation of  Camera API to separate business from Android Camera API.
 */

@TargetApi(Build.VERSION_CODES.M)
public class CameraAgent {
    private static final String TAG = "CameraAgent";
    private static final int MSG_START_PREVIEW = 0x100;

    private final CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private HandlerThread mCameraThread;
    private Handler mCameraHandler;
    private final Context mContext;
    private Targetable mPreview;

    private PictureReadyListener mPictureReadyListener;
    private ImageReader mImageReader;
    private ImageReader.OnImageAvailableListener mImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.d(TAG, "image available tid: " + Thread.currentThread().getName());
            Image image = reader.acquireLatestImage();
            if (mPictureReadyListener == null) {
                image.close();
                return;
            }
            ByteBuffer buf = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buf.remaining()];
            buf.get(bytes);
            // Yeah, up to this point listener should not be null.
            mPictureReadyListener.onPictureReady(bytes);
            image.close();
            mPictureReadyListener = null;
        }
    };

    private static final String CAMERA = "0";
    private CameraCaptureSession mSession;
    private CaptureRequest.Builder mPreviewRequestBuilder;
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
    private void doStartPreview(int targetWidth, int targetHeight) {
        Log.d(TAG, "setuppreview device + " + mCameraDevice);
        mCameraHandler.removeMessages(MSG_START_PREVIEW);
        configPreviewSize(targetWidth, targetHeight);
        if (mCameraDevice == null) {
            Message msg = Message.obtain();
            msg.what = MSG_START_PREVIEW;
            msg.obj = new Size(targetWidth, targetHeight);
            mCameraHandler.sendMessageDelayed(msg, 100);
            return;
        }
        configCaptureTarget();
        List<Surface> target = new ArrayList<>();
        // Preview target surface.
        Surface targetSurface = mPreview.getSurface();
        target.add(targetSurface);
        // Capture target surface
        target.add(mImageReader.getSurface());
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

    // TODO: Requiring check access exception is a stupid design, devise a way to remove it.
    private void configCaptureTarget() {
        // Select largest picture size
        try {
            CameraCharacteristics cc = mCameraManager.getCameraCharacteristics(CAMERA);
            StreamConfigurationMap scmap = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size largest =
                    Collections.max(Arrays.asList(scmap.getOutputSizes(ImageFormat.JPEG)), new CompareSizeByAreas());
            Log.d(TAG, "configCaptureTarget size w -> " + largest.getWidth() + ", h -> " + largest.getHeight());
            if (mImageReader == null) {
                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                        ImageFormat.JPEG, 1);
                mImageReader.setOnImageAvailableListener(mImageAvailableListener, mCameraHandler);
            }
        } catch (CameraAccessException e) {
        }
    }

    private void configPreviewSize(int targetWidth, int targetHeight) {
        try {
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(CAMERA);
            StreamConfigurationMap configMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            // Find out whether need to swap dimensions
            final WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            final int displayRotation = windowManager.getDefaultDisplay().getRotation();
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
            configRenderers(bestPreviewSize.getWidth(), bestPreviewSize.getHeight());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void configRenderers(int width, int height) {
        mPreview.setInputDimension(width, height);
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
                mSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, mCameraHandler);
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
            Log.d(TAG, "onCaptureStarted");
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            Log.d(TAG, "onCaptureProgressed request " + request);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            captureStillPicture(result);
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            Log.d(TAG, "onCaptureFailed failure->" + failure);
        }
    };

    private void captureStillPicture(TotalCaptureResult result) {
        final Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
        Log.d(TAG, "captureStillPicture result =" + result + ", afstate -> " + afState);
        doCapture();
    }

    private void doCapture() {
        try {
            final CaptureRequest.Builder captureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            // Orientation
            final WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            final int rotation = wm.getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));
//            mSession.stopRepeating();
            mSession.capture(captureBuilder.build(), null, null);
        } catch (CameraAccessException e) {
        }
    }

    private int getOrientation(int rotation) {
        int orientation = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                orientation = 90;
                break;
            case Surface.ROTATION_90:
                orientation = 0;
                break;
            case Surface.ROTATION_180:
                orientation = 180;
                break;
            case Surface.ROTATION_270:
                orientation = 270;
                break;
        }
        // Assume sensor orientation is 90
        final int sensorOrientation = 90;
        return (orientation + sensorOrientation + 270) % 360;
    }

    public CameraAgent(Context context) {
        mContext = context;
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    public void setTarget(Targetable target) {
        mPreview = target;
    }

    public void startCameraThread() {
        mCameraThread = new HandlerThread("Camera Handler Thread");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_START_PREVIEW: {
                        Size p = (Size) msg.obj;
                        doStartPreview(p.getWidth(), p.getHeight());
                        break;
                    }
                }
            }
        };
    }

    public void stopCameraThread() {
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

    public void openCamera() {
        if (mContext.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(mContext, "Permission denied, need camera access permission", Toast.LENGTH_LONG).show();
            return;
        } else {
            try {
                mCameraManager.openCamera(CAMERA, mSetupCallback, mCameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void closeCamera() {
        if (mSession != null) {
            mSession.close();
            mSession = null;
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
        }
    }

    public void startPreview(int targetWidth, int targetHeight) {
        if (mSession == null) {
            Message msg = Message.obtain();
            msg.what = MSG_START_PREVIEW;
            msg.obj = new Size(targetWidth, targetHeight);
            mCameraHandler.sendMessage(msg);
        }
    }

    public void takePicture(PictureReadyListener listener) {
        if (listener == null) {
            return;
        }
        // This is a oneshot listener
        // should clear it once take picture finished.
        // also need to prevent overriding: second listener would clear first one.
        // I hate field stateful listener.
        mPictureReadyListener = listener;
        // lock the focus first
        try {
            mSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    static class CompareSizeByAreas implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    interface PictureReadyListener {
        void onPictureReady(byte[] jpeg);
    }
}
