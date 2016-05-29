package net.toughcoder.rs;

import android.os.Bundle;

import net.toughcoder.effectiveandroid.ExampleListActivity;

import java.util.LinkedList;
import java.util.List;

public class RSExampleListActivity extends ExampleListActivity {
    private static String TAG = "RenderScript Examples";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(TAG);
    }

    @Override
    protected List<Class> getActivityList() {
        List<Class> list = new LinkedList<>();
        list.add(BitmapBlurTestActivity.class);
        list.add(RSYUV2RGBAActivity.class);
        list.add(ImagePressActivity.class);
        list.add(GrayScalifyImageActivity.class);
        return list;
    }
}
