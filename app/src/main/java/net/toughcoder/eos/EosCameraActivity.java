package net.toughcoder.eos;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import net.toughcoder.effectiveandroid.R;

import java.io.File;

@RequiresApi(api = Build.VERSION_CODES.M)
public class EosCameraActivity extends Activity
        implements View.OnClickListener,
        EosCameraBusiness.NewPictureListener {
    private static final String TAG = "EosCamera";
    private static final int REQ_PERMISSION = 0x01;
    private Handler mMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //
        }
    };

    private ImageView mThumbnailView;
    private EosCameraBusiness mCameraBusiness;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eos_camera);
        setTitle(TAG);

        initViews();
    }

    private void initViews() {
        // Initialize GLSurfaceView
        final EosCameraView cameraView = (EosCameraView) findViewById(R.id.eos_preview);
        mCameraBusiness = new EosCameraBusiness(this);
        mCameraBusiness.setPreviewTarget(cameraView);
        mThumbnailView = (ImageView) findViewById(R.id.eos_thumbnail);
        mThumbnailView.setOnClickListener(this);
        mCameraBusiness.addPictureListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // start camera handler
        // init thread
        mCameraBusiness.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume ");
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQ_PERMISSION);
            return;
        } else {
            mCameraBusiness.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        mCameraBusiness.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        // stop camera thread.
        mCameraBusiness.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraBusiness.removePictureListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult requestCode = " + requestCode + " permissions " + permissions + ", results " + grantResults);
        if (requestCode == REQ_PERMISSION) {
            if (grantResults.length != 2 ||
                    grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Cannot work without camera and storage permissions", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // UI event listener
    public void onTakePicture(View view) {
        Log.d(TAG, "onTakePicture");
        mCameraBusiness.takePicture();
    }

    public void onFlashToggle(final View view) {
        // show a radio dialog
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.eos_list_option);
        dialog.show();
        final RadioGroup list = (RadioGroup) dialog.findViewById(R.id.eos_flash_list);
        list.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                EosCameraBusiness.FlashMode newMode;
                String modeLabel;
                if (checkedId == R.id.eos_flash_off) {
                    newMode = EosCameraBusiness.FlashMode.OFF;
                    modeLabel = "Off";
                } else if (checkedId == R.id.eos_flash_auto) {
                    newMode = EosCameraBusiness.FlashMode.AUTO;
                    modeLabel = "Auto";
                } else if (checkedId == R.id.eos_flash_on) {
                    newMode = EosCameraBusiness.FlashMode.ON;
                    modeLabel = "On";
                } else {
                    newMode = EosCameraBusiness.FlashMode.TORCH;
                    modeLabel = "Torch";
                }
                mCameraBusiness.setFlashMode(newMode);
                ((TextView) view).setText(modeLabel);
            }
        });
        int id = R.id.eos_flash_off;
        final EosCameraBusiness.FlashMode mode = mCameraBusiness.getFlashMode();
        switch (mode) {
            case OFF:
               id = R.id.eos_flash_off;
               break;
            case ON:
                id = R.id.eos_flash_on;
                break;
            case AUTO:
                id = R.id.eos_flash_auto;
                break;
            case TORCH:
                id = R.id.eos_flash_torch;
                break;
        }
        final RadioButton button = (RadioButton) dialog.findViewById(id);
        button.setChecked(true);
    }

    @Override
    public void onClick(View v) {
        // to review the image.
        final String path = (String) v.getTag();
        if (TextUtils.isEmpty(path)) {
            return;
        }
        Uri uri = Uri.fromFile(new File(path));
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(uri, "image/jpeg");
        startActivity(i);
    }

    @Override
    public void onNewPicture(final String path) {
        // show the thumbnail
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);
        opts.inSampleSize = Math.min(opts.outWidth, opts.outHeight) / mThumbnailView.getWidth();
        opts.inJustDecodeBounds = false;
        final Bitmap bm = BitmapFactory.decodeFile(path, opts);
        final Matrix matrix = new Matrix();
        matrix.postRotate(90);
        final Bitmap rotated = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        if (rotated != bm) {
            bm.recycle();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mThumbnailView.setImageBitmap(rotated);
                mThumbnailView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                mThumbnailView.setTag(path);
            }
        });

    }
}
