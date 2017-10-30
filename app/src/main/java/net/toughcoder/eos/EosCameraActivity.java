package net.toughcoder.eos;

import android.app.Activity;
import android.os.Bundle;

import net.toughcoder.effectiveandroid.R;

public class EosCameraActivity extends Activity {
    private static final String TAG = "EosCamera";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(TAG);
        setContentView(R.layout.activity_eos_camera);
    }
}
