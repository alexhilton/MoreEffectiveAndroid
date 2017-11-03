package net.toughcoder.eos;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import net.toughcoder.effectiveandroid.R;

@RequiresApi(api = Build.VERSION_CODES.M)
public class EosCameraActivity extends Activity {
    private static final String TAG = "EosCamera";
    private static final int REQ_PERMISSION = 0x01;
    private Handler mMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //
        }
    };

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
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.CAMERA}, REQ_PERMISSION);
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
}
