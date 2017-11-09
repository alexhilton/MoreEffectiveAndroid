package net.toughcoder.eos;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by alex on 17-11-1.
 * This is the interface between user interface and logic business.
 */

@RequiresApi(Build.VERSION_CODES.M)
public class EosCameraBusiness implements TargetReadyListener {
    private static final String TAG = "CameraBusiness";
    private final CameraAgent mCameraAgent;
    private final Context mContext;
    private EosCameraView mPreview;

    // Camera Parameters
    private FlashMode mFlashMode = FlashMode.OFF;

    private List<NewPictureListener> mPictureListeners;

    public EosCameraBusiness(Context context) {
        mContext = context;
        mCameraAgent = new CameraAgent(context);
        mPictureListeners = new ArrayList<>();
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

    public void takePicture() {
        // take the picture, get the byte data.
        // apply post process
        // save the data to file.
        // create record to media provider.
        mCameraAgent.takePicture(new CameraAgent.PictureReadyListener() {
            @Override
            public void onPictureReady(byte[] jpeg) {
                // save it to a file
                // review it
                final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                final String stamp = sdf.format(new Date());
                final String filename = String.format("Eos_%s.jpg", stamp);
                File out = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        filename);
                if (out.exists()) {
                    out.delete();
                }
                Log.d(TAG, "onPictureTaken file -> " + filename);
                saveImage(jpeg, out);
            }
        });
    }

    private void saveImage(byte[] jpeg, File out) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(out));
            try {
                bos.write(jpeg, 0, jpeg.length);
            } finally {
                if (bos != null) {
                    bos.close();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTargetReady(int targetWidth, int targetHeight) {
        mCameraAgent.startPreview(targetWidth, targetHeight);
    }

    public void setFlashMode(FlashMode mode) {
        if (mFlashMode == mode) {
            return;
        }
        mFlashMode = mode;
        mCameraAgent.setFlashMode(mode);
    }

    public FlashMode getFlashMode() {
        return mFlashMode;
    }

    public void addPictureListener(NewPictureListener listener) {
        if (!mPictureListeners.contains(listener)) {
            mPictureListeners.add(listener);
        }
    }

    public void removePictureListener(NewPictureListener listener) {
        mPictureListeners.remove(listener);
    }

    public enum FlashMode {
        OFF,
        AUTO,
        ON,
        TORCH,
        RED_EYE,
    }

    public interface NewPictureListener {
        void onNewPicture(String path);
    }
}
