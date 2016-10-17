package net.toughcoder.camera;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import net.toughcoder.effectiveandroid.R;
import net.toughcoder.widget.RecyclerViewExampleActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SimpleCameraActivity extends Activity implements SurfaceHolder.Callback {
    private static final String TAG = "simpleCamera";
    private SurfaceView mSurfaceView; //预览SurfaceView
    private SurfaceHolder mSurfaceHolder;
    private ProgressDialog mProgressDialog;
    private Camera mCamera;
    private boolean mPreviewing;
    private static final int REQ_PERM = 0x01;
    private FrameLayout mContainer;

    private final Handler mMainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case 0:
                    mProgressDialog.dismiss();
                    break;
                case 1:
                    mProgressDialog.show();
                    break;
                case 2:
                    mProgressDialog.setProgress(msg.arg1);
                    break;
                case 3:
                    mProgressDialog.dismiss();
                    break;
            }
        }
    };

    private final Camera.PictureCallback mJpegCallback = new Camera.PictureCallback() {
        //对jpeg图像数据的回调,最重要的一个回调
        public void onPictureTaken(final byte[] data, Camera camera) {
            if (data == null) {
                return;
            }
            new Thread() {
                @Override
                public void run() {
                    mMainHandler.sendEmptyMessage(1);
                    publishProgress(0);
                    //data是字节数据，将其解析成位图
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    if (bitmap == null) {
                        return;
                    }
                    publishProgress(30);
                    Matrix matrix = new Matrix();
                    matrix.postRotate((float) 90.0);
                    Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                            bitmap.getHeight(), matrix, false);
                    //保存图片到sdcard
                    if (rotated != null) {
                        saveJpeg(rotated);
                    }
                }
            }.start();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        turnonFullscreen();
        setContentView(R.layout.activity_simple_camera);
        initViews();
        initProgressDialog();
    }

    private void initViews() {
        mContainer = (FrameLayout) findViewById(R.id.container);
        View button = findViewById(R.id.photoImgBtn);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mPreviewing) {
                    return;
                }
                mCamera.takePicture(new Camera.ShutterCallback() {
                    public void onShutter() {
                        Log.d(TAG, "onShutter");
                    }
                }, null, mJpegCallback);
            }
        });
    }

    private void initProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setTitle("处理图片中...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMax(100);
    }

    private void turnonFullscreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        Window myWindow = this.getWindow();
        myWindow.setFlags(flag, flag);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        tearDownCamera();
        mSurfaceHolder = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_PERM);
            } else {
                setupCamera();
            }
        } else {
            setupCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != REQ_PERM) {
            return;
        }
        if (grantResults == null || grantResults.length < 1) {
            return;
        }
        boolean granted = true;
        for (int res : grantResults) {
            granted &= (res == PackageManager.PERMISSION_GRANTED);
        }
        if (granted) {
            Log.e("shit", "request result, granted normal start");
            setupCamera();
        } else {
            AlertDialog ad = new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("温馨提示")
                    .setMessage("亲，您拒绝了授予权限，拍立淘将无法正常使用。\n" + "您可以通过以下操作开启权限以恢复：\n" + "设置/应用/权限/相机/存储空间")
                    .setCancelable(false)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .create();
            ad.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        tearDownCamera();
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e("shit", "surface changed");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e("shit", "surface created ");
        mSurfaceHolder = holder;
        try {
            startPreview(holder);
        } catch (Exception e) {
            Log.e(TAG, "surfaceCreated", e);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e("shit", "surface destroyed");
        mSurfaceHolder = null;
    }

    private void saveJpeg(Bitmap bm) {
        String savePath = "/mnt/sdcard/rectPhoto/";
        File folder = new File(savePath);
        if (!folder.exists()) {
            folder.mkdir();
        }
        long dataTake = System.currentTimeMillis();
        String jpegName = savePath + dataTake + ".jpg";
        publishProgress(50);
        try {
            bm.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(jpegName));
            searchImage(jpegName);
        } catch (Throwable t) {
            Log.e(TAG, "saveJpg", t);
        }
    }

    private void searchImage(final String fileName) {
        publishProgress(100);
        mMainHandler.sendEmptyMessage(3);
        Log.i(TAG, "searchImage[" + fileName + "]");
        RecyclerViewExampleActivity.jumpToPailitao(this, fileName);
    }

    private void publishProgress(int prog) {
        Message msg = Message.obtain();
        msg.what = 2;
        msg.obj = prog;
        mMainHandler.sendMessage(msg);
    }

    private void setupCamera() {
        Log.e("shit", "setup camera, holder is " + mSurfaceHolder + ", camera " + mCamera);
        mCamera = Camera.open(0);
        setCameraDisplayOrientation(this, 0, mCamera);
        if (mSurfaceHolder != null) {
            // start preview directly
            startPreview(mSurfaceHolder);
        } else {
            addSurfaceView();
        }
    }

    private void startPreview(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
        }
        mCamera.startPreview();
        mPreviewing = true;
    }

    // 设置摄像头的旋转角度
    private static void setCameraDisplayOrientation(Activity activity, int cameraId,
                                                   android.hardware.Camera camera) {
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

    private void tearDownCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private void addSurfaceView() {
        if (mSurfaceView != null) {
            mContainer.removeView(mSurfaceView);
        }
        mSurfaceView = new SurfaceView(this);
        mSurfaceView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mContainer.addView(mSurfaceView, 0);
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
}
