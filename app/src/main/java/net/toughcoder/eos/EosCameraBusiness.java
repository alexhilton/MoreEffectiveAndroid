package net.toughcoder.eos;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

/**
 * Created by alex on 17-11-1.
 * This is the interface between user interface and logic business.
 */

@RequiresApi(Build.VERSION_CODES.M)
public class EosCameraBusiness implements TargetReadyListener {
    private final CameraAgent mCameraAgent;
    private final Context mContext;
    private EosCameraView mPreview;

    public EosCameraBusiness(Context context) {
        mContext = context;
        mCameraAgent = new CameraAgent(context);
    }

    public void onStart() {
        mCameraAgent.startCameraThread();
    }

    public void onResume() {
        mCameraAgent.openCamera();
    }

    public void onPause() {
        mCameraAgent.closeCamera();
    }

    public void onStop() {
        mCameraAgent.stopCameraThread();
    }

    public void setPreviewTarget(EosCameraView view) {
        mPreview = view;
        mPreview.setReadyListener(this);
        mCameraAgent.setTarget(mPreview);
    }

    @Override
    public void onTargetReady(int targetWidth, int targetHeight) {
        mCameraAgent.startPreview(targetWidth, targetHeight);
    }
}
