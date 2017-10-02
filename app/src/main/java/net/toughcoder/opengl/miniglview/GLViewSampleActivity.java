package net.toughcoder.opengl.miniglview;

import android.app.Activity;
import android.os.Bundle;

import net.toughcoder.effectiveandroid.R;

public class GLViewSampleActivity extends Activity {
    private static final String TAG = "GLViewSample";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(TAG);
        setContentView(R.layout.activity_glview_sample);
    }
}
