package net.toughcoder.camera;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import net.toughcoder.effectiveandroid.R;

public class OpenGLPreviewActivity extends ActionBarActivity {
    private static String TAG = "OpenGL Preview";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(TAG);
        setContentView(R.layout.activity_open_glpreview);
    }
}
