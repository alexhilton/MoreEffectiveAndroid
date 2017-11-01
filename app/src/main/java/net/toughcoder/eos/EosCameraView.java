package net.toughcoder.eos;

import android.content.Context;
import android.util.AttributeSet;

import net.toughcoder.opengl.miniglview.OpenGLESView;

/**
 * Created by alex on 17-11-1.
 */

public class EosCameraView extends OpenGLESView implements EosCamera {
    public EosCameraView(Context context) {
        super(context);
    }

    public EosCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EosCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
