package net.toughcoder.camera;

import android.os.Bundle;

import net.toughcoder.effectiveandroid.ExampleListActivity;

import java.util.LinkedList;
import java.util.List;

public class CameraExampleListActivity extends ExampleListActivity {
    private static String TAG = "Camera Example List";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(TAG);
    }

    @Override
    protected List<Class> getActivityList() {
        List<Class> sActivityList = new LinkedList<>();
        sActivityList.add(CameraPreviewActivity.class);
        sActivityList.add(TextureViewActivity.class);
        sActivityList.add(OpenGLPreviewActivity.class);
        return sActivityList;
    }
}
