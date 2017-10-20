package net.toughcoder.opengl.sharedcontext;

import android.app.Activity;
import android.graphics.SurfaceTexture;

import net.toughcoder.opengl.miniglview.OpenGLESView;

/**
 * Created by alex on 17-10-20.
 */

public class Filter {
    private final OpenGLESView mView;
    private final FilterRenderer mRenderer;

    public Filter(Activity host, int viewId, SurfaceTexture surfaceTexture, int texture) {
        mView = (OpenGLESView) host.findViewById(viewId);
        mRenderer = FilterRenderer.createRenderer(viewId, surfaceTexture, texture);
        mView.shareEGLContext();
        mView.setRenderMode(OpenGLESView.RenderMode.WHEN_DIRTY);
        mView.setRenderer(mRenderer);
        mView.setZOrderOnTop(true);
    }

    public void requestRender() {
        mView.requestRender();
    }

    public void setInputDimension(int width, int height) {
        mRenderer.setInputDimension(width, height);
    }
}
