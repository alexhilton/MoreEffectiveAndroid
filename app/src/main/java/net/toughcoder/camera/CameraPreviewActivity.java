package net.toughcoder.camera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import net.toughcoder.effectiveandroid.R;

import java.io.ByteArrayOutputStream;

public class CameraPreviewActivity extends Activity {
    private static String TAG = "Preview with SurfaceView";
    private Camera mCamera;
    private int mIndex;
    private SurfaceView mSurfaceView;
    private ImageView mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(TAG);
        setContentView(R.layout.activity_camera_preview);
        mSurfaceView = (SurfaceView) findViewById(R.id.preview_display);
        SurfaceHolder holder = mSurfaceView.getHolder();
        /// Try to make surfaceview transparent, but not working
        mSurfaceView.setZOrderOnTop(true);
        mSurfaceView.setAlpha(0.4f);
        holder.setFormat(PixelFormat.TRANSPARENT);

        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                startPreview(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                stopPreview();
            }
        });
        View toggle = findViewById(R.id.camera_toggle);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIndex++;
                mIndex %= 2;
                openCamera();
                startPreview(mSurfaceView.getHolder());
            }
        });
        View snapshot = findViewById(R.id.snapshot);
        snapshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        Camera.Parameters param = camera.getParameters();
                        int width = param.getPreviewSize().width;
                        int height = param.getPreviewSize().height;
                        processPreviewFrame(data, width, height);
                    }
                });
            }
        });
        mPreview = (ImageView) findViewById(R.id.snapshot_preview);
    }

    private void startPreview(SurfaceHolder holder) {
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(640, 480);
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (Exception e) {

        }
    }

    private void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    private void processPreviewFrame(final byte[] data, final int width, final int height) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                final YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, width, height, null);
                yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, out);
                final byte[] imageBytes = out.toByteArray();

                Bitmap bmp = null;
                try {
                    bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                } catch (OutOfMemoryError e) {
                    BitmapFactory.Options opt = new BitmapFactory.Options();
                    opt.inSampleSize = 2;
                    try {
                        bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, opt);
                    } catch (OutOfMemoryError er) {}
                }
                final Bitmap finalBmp = bmp;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPreview.setVisibility(View.VISIBLE);
                        mPreview.setImageBitmap(finalBmp);
                        mCamera.setPreviewCallback(null);
                    }
                });
            }
        }).start();
    }
    @Override
    protected void onResume() {
        super.onResume();
        openCamera();
    }

    private void openCamera() {
        try {
            closeCamera();
            mCamera = Camera.open(mIndex);
            setCameraDisplayOrientation(this, mIndex, mCamera);
        } catch (Exception e) {
            //
        }
    }

    private static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
    }

    private void closeCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
}
