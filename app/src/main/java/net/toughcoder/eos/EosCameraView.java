package net.toughcoder.eos;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import net.toughcoder.opengl.miniglview.OpenGLESView;
import net.toughcoder.opengl.sharedcontext.SurfaceTextureRenderer;

/**
 * Created by alex on 17-11-1.
 */

@RequiresApi(api = Build.VERSION_CODES.M)
public final class EosCameraView extends FrameLayout implements Targetable {
    private static final String TAG = "EosCameraView";

    private OpenGLESView mPreview;
    private PreviewRenderer mPreviewRenderer;
    private TargetReadyListener mListener;

    public EosCameraView(Context context) {
        super(context);
    }

    public EosCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EosCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mPreview = new OpenGLESView(getContext());
        addView(mPreview, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mPreviewRenderer = new PreviewRenderer();
        mPreview.setRenderer(mPreviewRenderer);
    }

    public void setReadyListener(TargetReadyListener listener) {
        mListener = listener;
    }

    @Override
    public Surface getSurface() {
        return mPreviewRenderer.getSurface();
    }

    @Override
    public void setInputDimension(int width, int height) {
        mPreviewRenderer.setInputDimension(width, height);
    }

    private class PreviewRenderer extends SurfaceTextureRenderer {
        private static final String TAG = "PreviewRenderer";
        private int mPreviewTexture;
        private SurfaceTexture mSurfaceTexture;

        @Override
        public void onContextCreate() {
            Log.d(TAG, "onContextCreate");
            super.onContextCreate();
            initializeSurfaceTexture();
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

        @Override
        public void onContextChange(int width, int height) {
            Log.d(TAG, "onContextChange width -> " + width + ", height -> " + height);
            super.onContextChange(width, height);
            // Able to start preview now.
            // When back from HOME, onResume will start preview first, no need second one.
            if (mListener != null) {
                mListener.onTargetReady(width, height);
            }
        }

        @Override
        public void onDrawFrame() {
            mSurfaceTexture.updateTexImage();
            super.onDrawFrame();
        }

        @Override
        public void onContextDestroy() {
            Log.d(TAG, "onContextDestroy -->");
            super.onContextDestroy();
            GLES20.glDeleteTextures(1, new int[] {mPreviewTexture}, 0);
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }

        @Override
        public void setInputDimension(int inputWidth, int inputHeight) {
            super.setInputDimension(inputWidth, inputHeight);
            if (mSurfaceTexture != null) {
                mSurfaceTexture.setDefaultBufferSize(inputWidth, inputHeight);
            }
        }

        @Override
        protected SurfaceTexture getSurfaceTexture() {
            return mSurfaceTexture;
        }

        @Override
        protected int getPreviewTexture() {
            return mPreviewTexture;
        }

        private Surface getSurface() {
            return new Surface(mSurfaceTexture);
        }
    }
}
