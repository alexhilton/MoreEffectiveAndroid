package net.toughcoder.rs;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import net.toughcoder.effectiveandroid.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class RSYUV2RGBAActivity extends ActionBarActivity implements Camera.PreviewCallback {
    private static String TAG = "Convert YUV to RGBA";
    private ImageView mImageView;
    private Button mSnapshot;
    private Camera mCamera;
    private byte[] mBuffer;
    private int width = 640;
    private int height = 480;
    private SurfaceTexture mTexture;
    private RenderScript mRS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(TAG);
        setContentView(R.layout.activity_rsyuv2_rgba);
        mRS = RenderScript.create(this);
        mImageView = (ImageView) findViewById(R.id.imageview);
        mSnapshot = (Button) findViewById(R.id.snapshot);
        mSnapshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncTask<Void, Void, Bitmap>() {
                    @Override
                    protected void onPreExecute() {
                    }

                    @Override
                    protected Bitmap doInBackground(Void... params) {
                        long t1 = System.currentTimeMillis();
//                        Bitmap rotated = convert2Bitmap();
                        Bitmap rotated = yuv2BitmapWithRS();
                        long t2 = System.currentTimeMillis();
                        Log.e("yuv", "converting 640x480 yuv to bitmap takes " + (t2 - t1));
                        return rotated;
                    }

                    @Override
                    protected void onPostExecute(Bitmap bm) {
                        if (bm != null) {
                            mImageView.setImageBitmap(bm);
                        }
                    }
                }.execute();
            }
        });
    }

    private Bitmap convert2Bitmap() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        synchronized (RSYUV2RGBAActivity.this) {
            if (mBuffer == null) {
                return null;
            }
            YuvImage yuv = new YuvImage(mBuffer, ImageFormat.NV21, width, height, null);
            yuv.compressToJpeg(new Rect(0, 0, width, height), 100, baos);
        }
        final byte[] bytes = baos.toByteArray();
        Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Bitmap rotated = rotateBitmap(bm);
        return rotated;
    }

    private Bitmap rotateBitmap(Bitmap bm) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90, bm.getWidth() / 2, bm.getHeight() / 2);
        Bitmap rotated = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        if (rotated != bm) {
            bm.recycle();
        }
        return rotated;
    }

    private Bitmap yuv2BitmapWithRS() {
        if (mBuffer == null) {
            return null;
        }
        Type.Builder yuvType = new Type.Builder(mRS, Element.YUV(mRS));
        yuvType.setYuvFormat(ImageFormat.NV21);
        yuvType.setX(width);
        yuvType.setY(height);
        Allocation input = Allocation.createTyped(mRS, yuvType.create()); // By default, it is Allocation.USAGE_SCRIPT
        synchronized (this) {
            input.copyFrom(mBuffer);
        }

        Type.Builder rgbType = new Type.Builder(mRS, Element.RGBA_8888(mRS));
        rgbType.setX(width);
        rgbType.setY(height);
        Allocation output = Allocation.createTyped(mRS, rgbType.create());

        ScriptIntrinsicYuvToRGB script = ScriptIntrinsicYuvToRGB.create(mRS, Element.U8(mRS));
        script.setInput(input);
        script.forEach(output);
        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        output.copyTo(bm);

        Bitmap rotated = rotateBitmap(bm);
        return rotated;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCamera = Camera.open(0);
        Camera.Parameters param = mCamera.getParameters();
        param.setPreviewSize(width, height);
        mCamera.setParameters(param);
        int[] tex = new int[1];
        GLES20.glGenTextures(1, tex, 0);
        mTexture = new SurfaceTexture(tex[0]);
        try {
            mCamera.setPreviewTexture(mTexture);
            mCamera.setPreviewCallback(this);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCamera.stopPreview();
        mCamera.setPreviewCallback(null);
        mCamera.release();
        mTexture.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRS.destroy();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        synchronized (this) {
            if (mBuffer == null) {
                mBuffer = new byte[data.length];
            }
            System.arraycopy(data, 0, mBuffer, 0, data.length);
        }
    }
}
