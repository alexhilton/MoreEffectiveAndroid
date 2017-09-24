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
import android.opengl.GLSurfaceView;
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
    private void setupPreview(int targetWidth, int targetHeight) {
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
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        mSurfaceTexture.setDefaultBufferSize(1920, 1080);
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

    private GLSurfaceView mPreview;
    private GLSurfaceView mGrayscale;
    private GLSurfaceView mSwirl;
    private GLSurfaceView mSphere;

    private SurfaceTextureRenderer mPreviewRenderer;
    private SurfaceTextureRenderer mFilterRenderer;
    private SurfaceTextureRenderer mSwirlRenderer;
    private SurfaceTextureRenderer mSphereRenderer;

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
                        Size p = (Size) msg.obj;
                        setupPreview(p.getWidth(), p.getHeight());
                        break;
                    }
                }
            }
        };

        initViews();

        // init camera manager
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
    }

    private void initViews() {
        // Initialize GLSurfaceView
        mEGLContextFactory = new SharedEGLContextFactory();
        mPreviewRenderer = new PreviewRenderer();
        mPreview = initGLSurfaceView(R.id.preview);
        mPreview.setEGLContextFactory(mEGLContextFactory);
        mPreview.setRenderer(mPreviewRenderer);
        mPreview.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mPreview.setZOrderOnTop(false);
        mFilterRenderer = new GrayscaleRenderer();
        mGrayscale = initGLSurfaceView(R.id.grayscale);
        mGrayscale.setEGLContextFactory(mEGLContextFactory);
        mGrayscale.setRenderer(mFilterRenderer);
        mGrayscale.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mGrayscale.setZOrderOnTop(true);
        mSwirlRenderer = new SwirlRenderer();
        mSwirl = initGLSurfaceView(R.id.swirl);
        mSwirl.setEGLContextFactory(mEGLContextFactory);
        mSwirl.setRenderer(mSwirlRenderer);
        mSwirl.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mSwirl.setZOrderOnTop(true);
        mSphereRenderer = new SphereRenderer();
        mSphere = initGLSurfaceView(R.id.sphere);
        mSphere.setEGLContextFactory(mEGLContextFactory);
        mSphere.setRenderer(mSphereRenderer);
        mSphere.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mSphere.setZOrderOnTop(true);
    }

    private GLSurfaceView initGLSurfaceView(int resId) {
        GLSurfaceView view = (GLSurfaceView) findViewById(resId);
        view.setEGLContextClientVersion(2);
        view.setPreserveEGLContextOnPause(true);
        view.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        return view;
    }

    @Override
    protected void onResume() {
        super.onResume();
        openCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
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
    protected void onDestroy() {
        releaseGLResources();
        super.onDestroy();
    }

    /*
     * How to release GL resources is a hard question.
     * There are three things to break:
     * 1. all should be called in GL Thread, which generates these resources.
     * 2. There is no clean up callback from GL thread, even we got instantiation callback.
     * 3. need to make sure release happen after all draw.
     * Currently, no definite solution, unfortunately.
     */
    private void releaseGLResources() {
        mPreview.queueEvent(new Runnable() {
            @Override
            public void run() {
//                GLES20.glDeleteTextures(1, new int[] {mPreviewTexture}, 0);
                mPreviewRenderer.destroy();
                Log.d(TAG, "preview queue " + Thread.currentThread().getName() + ":" + Thread.currentThread().getId());
            }
        });
        mGrayscale.queueEvent(new Runnable() {
            @Override
            public void run() {
//                mFilterRenderer.destroy();
                Log.d(TAG, "grayscale queue " + Thread.currentThread().getName() + ":" + Thread.currentThread().getId());
            }
        });
        mSwirl.queueEvent(new Runnable() {
            @Override
            public void run() {
//                mSwirlRenderer.destroy();
                Log.d(TAG, "swirl queue " + Thread.currentThread().getName() + ":" + Thread.currentThread().getId());
            }
        });
        mSphere.queueEvent(new Runnable() {
            @Override
            public void run() {
//                mSphereRenderer.destroy();
                Log.d(TAG, "sphere queue " + Thread.currentThread().getName() + ":" + Thread.currentThread().getId());
            }
        });
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mPreview.requestRender();
        mGrayscale.requestRender();
        mSwirl.requestRender();
        mSphere.requestRender();
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
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            Log.d(TAG, "onSurfaceCreated");
            initializeSurfaceTexture();
            super.onSurfaceCreated(gl, config);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            Log.d(TAG, "onSurfaceChanged");
            super.onSurfaceChanged(gl, width, height);
            // Able to start preview now.
            Message msg = Message.obtain();
            msg.what = MSG_START_PREVIEW;
            msg.obj = new Size(width, height);
            mCameraHandler.sendMessage(msg);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            mSurfaceTexture.updateTexImage();
            super.onDrawFrame(gl);
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

    private class GrayscaleRenderer extends SurfaceTextureRenderer {
        private static final String TAG = "GrayscaleRenderer";

        public static final String GRAYSCALE_FRAGMENT_SHADER =
                "#extension GL_OES_EGL_image_external : require\n" +
                        "precision highp float;\n" +
                        "uniform samplerExternalOES uTextureSampler;\n" +
                        "varying vec2 textureCoords;\n" +
                        "void main () {\n" +
                        "  vec4 tex = texture2D(uTextureSampler, textureCoords);\n" +
                        "  vec3 factor = vec3(0.2125, 0.7154, 0.0721); \n" +
                        "  float luma = dot(tex.rgb, factor); \n" +
                        "  gl_FragColor = vec4(vec3(luma), tex.a);\n" +
                        "}";

        @Override
        protected SurfaceTexture getSurfaceTexture() {
            return mSurfaceTexture;
        }

        @Override
        protected int getPreviewTexture() {
            return mPreviewTexture;
        }

        @Override
        protected String getFragmentShader() {
            return GRAYSCALE_FRAGMENT_SHADER;
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
            Log.d(TAG, "destroyContext " + Thread.currentThread().getName() + ":" + Thread.currentThread().getId());
        }
    }

    private class SwirlRenderer extends SurfaceTextureRenderer {
        private static final String TAG = "SwirlRenderer";

        private static final String SWIRL_FRAGMENT_SHADER =
                "#extension GL_OES_EGL_image_external : require\n" +
                "precision highp float;\n" +
                "uniform samplerExternalOES uTextureSampler;\n" +
                "varying highp vec2 textureCoords;\n" +
                "\n" +
                "highp vec2 center = vec2(.5, .5);\n" +
                "highp float radius = .5;\n" +
                "highp float angle = 1.;\n" +
                "\n" +
                "void main()\n" +
                "{\n" +
                "  highp vec2 textureCoordinateToUse = textureCoords;\n" +
                "  highp float dist = distance(center, textureCoords);\n" +
                "  if (dist < radius)\n" +
                "  {\n" +
                "    textureCoordinateToUse -= center;\n" +
                "    highp float percent = (radius - dist) / radius;\n" +
                "    highp float theta = percent * percent * angle * 8.0;\n" +
                "    highp float s = sin(theta);\n" +
                "    highp float c = cos(theta);\n" +
                "    textureCoordinateToUse = vec2(dot(textureCoordinateToUse, vec2(c, -s)), dot(textureCoordinateToUse, vec2(s, c)));\n" +
                "    textureCoordinateToUse += center;\n" +
                "  }\n" +
                "  gl_FragColor = texture2D(uTextureSampler, textureCoordinateToUse);\n" +
                "}\n";

        @Override
        protected SurfaceTexture getSurfaceTexture() {
            return mSurfaceTexture;
        }

        @Override
        protected int getPreviewTexture() {
            return mPreviewTexture;
        }

        @Override
        protected String getFragmentShader() {
            return SWIRL_FRAGMENT_SHADER;
        }
    }

    private class SphereRenderer extends SurfaceTextureRenderer {
        private static final String SPHERE_FRAGMENT_SHADER =
                "#extension GL_OES_EGL_image_external : require\n" +
                "varying highp vec2 textureCoords;\n" +
                "\n" +
                "uniform samplerExternalOES uTextureSampler;\n" +
                "\n" +
                "highp vec2 center = vec2(.5, .5);\n" +
                "highp float radius = .5;\n" +
                "highp float aspectRatio = 1.;\n" +
                "highp float refractiveIndex = 0.71;\n" +
                "const highp vec3 lightPosition = vec3(-0.5, 0.5, 1.0);\n" +
                "const highp vec3 ambientLightPosition = vec3(0.0, 0.0, 1.0);\n" +
                "\n" +
                "void main()\n" +
                "{\n" +
                "  highp vec2 textureCoordinateToUse = vec2(textureCoords.x, (textureCoords.y * aspectRatio + 0.5 - 0.5 * aspectRatio));\n" +
                "  highp float distanceFromCenter = distance(center, textureCoordinateToUse);\n" +
                "  lowp float checkForPresenceWithinSphere = step(distanceFromCenter, radius);\n" +
                "\n" +
                "  distanceFromCenter = distanceFromCenter / radius;\n" +
                "\n" +
                "  highp float normalizedDepth = radius * sqrt(1.0 - distanceFromCenter * distanceFromCenter);\n" +
                "  highp vec3 sphereNormal = normalize(vec3(textureCoordinateToUse - center, normalizedDepth));\n" +
                "\n" +
                "  highp vec3 refractedVector = 2.0 * refract(vec3(0.0, 0.0, -1.0), sphereNormal, refractiveIndex);\n" +
                "  refractedVector.xy = -refractedVector.xy;\n" +
                "\n" +
                "  highp vec3 finalSphereColor = texture2D(uTextureSampler, (refractedVector.xy + 1.0) * 0.5).rgb;\n" +
                "\n" +
                "  // Grazing angle lighting\n" +
                "  highp float lightingIntensity = 2.5 * (1.0 - pow(clamp(dot(ambientLightPosition, sphereNormal), 0.0, 1.0), 0.25));\n" +
                "  finalSphereColor += lightingIntensity;\n" +
                "\n" +
                "  // Specular lighting\n" +
                "  lightingIntensity  = clamp(dot(normalize(lightPosition), sphereNormal), 0.0, 1.0);\n" +
                "  lightingIntensity  = pow(lightingIntensity, 15.0);\n" +
                "  finalSphereColor += vec3(0.8, 0.8, 0.8) * lightingIntensity;\n" +
                "\n" +
                "  gl_FragColor = vec4(finalSphereColor, 1.0) * checkForPresenceWithinSphere;\n" +
                "}\n";

        @Override
        protected SurfaceTexture getSurfaceTexture() {
            return mSurfaceTexture;
        }

        @Override
        protected int getPreviewTexture() {
            return mPreviewTexture;
        }

        @Override
        protected String getFragmentShader() {
            return SPHERE_FRAGMENT_SHADER;
        }
    }
}
