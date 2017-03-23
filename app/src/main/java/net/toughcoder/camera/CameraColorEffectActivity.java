package net.toughcoder.camera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;

import net.toughcoder.effectiveandroid.R;

import java.io.IOException;
import java.util.List;

public class CameraColorEffectActivity extends Activity
        implements TextureView.SurfaceTextureListener {
    private static final String LOG_TAG = "CameraColorEffect";
    private static final String TAG = "Camera Color Effect";
    private TextureView mTextureView;
    private Camera mCamera;
    private SurfaceTexture mSurfaceTexture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(TAG);
        setContentView(R.layout.activity_camera_color_effect);
        mTextureView = (TextureView) findViewById(R.id.preview_display);
        mTextureView.setSurfaceTextureListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCamera = Camera.open();
        if (mSurfaceTexture != null) {
            startPreview(mSurfaceTexture);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // View event handler
    public void onTakePicture(View view) {
        if (mCamera == null) {
            return;
        }
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                processPicture(data, camera);
            }
        });
    }

    private void processPicture(final byte[] data, final Camera camera) {
        if (data == null) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                doProcessPicture(data, camera);
            }
        }).start();
    }

    private void doProcessPicture(byte[] data, Camera camera) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inSampleSize = 8;
        final Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length, opt);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView preview = (ImageView) findViewById(R.id.result_preview);
                preview.setVisibility(View.VISIBLE);
                preview.setImageBitmap(bm);
            }
        });
    }

    public void resultPreviewClick(View view) {
        view.setVisibility(View.GONE);
    }

    // SurfaceTexture listener
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mCamera != null) {
            startPreview(surface);
        } else {
            mSurfaceTexture = surface;
        }
    }

    private void startPreview(SurfaceTexture surface) {
        Camera.Parameters parameters = mCamera.getParameters();
        List<String> effects = parameters.getSupportedColorEffects();
        Log.e(LOG_TAG, "supported color effects " + effects);
        parameters.setColorEffect("mono");
        mCamera.setParameters(parameters);
        try {
            CameraPreviewActivity.setCameraDisplayOrientation(this, 0, mCamera);
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
