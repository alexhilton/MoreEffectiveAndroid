package net.toughcoder.widget;

import android.app.Activity;
import android.os.Bundle;

import net.toughcoder.effectiveandroid.R;

public class GridLayoutExampleActivity extends Activity {
    private static final String TAG = "GridLayout example";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(TAG);
        setContentView(R.layout.activity_grid_layout_example);
    }

}
