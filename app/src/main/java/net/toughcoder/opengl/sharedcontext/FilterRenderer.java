package net.toughcoder.opengl.sharedcontext;

import android.graphics.SurfaceTexture;

/**
 * Created by alex on 17-9-30.
 */

public class FilterRenderer extends SurfaceTextureRenderer {
    private SurfaceTexture mSurfaceTexture;
    private int mPreviewTexture;

    public FilterRenderer(SurfaceTexture surfaceTexture, int texture) {
        mSurfaceTexture = surfaceTexture;
        mPreviewTexture = texture;
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
