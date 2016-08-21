package net.toughcoder.opengl;

import android.os.Bundle;

import net.toughcoder.miscellaneous.ExampleListActivity;
import net.toughcoder.opengl.opengl1s.StarActivity;
import net.toughcoder.opengl.opengl2s.OpenGLExampleActivity;
import net.toughcoder.opengl.starcamera.StarCameraActivity;
import net.toughcoder.widget.AlphaOpenGLActivity;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by alexhilton on 16/8/21.
 */
public class OpenGLESExampleListActivity extends ExampleListActivity {
    private static final String TAG = "OpenGl ES examples";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(TAG);
    }

    @Override
    protected List<Class> getActivityList() {
        List<Class> list = new LinkedList<>();
        list.add(AlphaOpenGLActivity.class);
        list.add(OpenGLExampleActivity.class);
        list.add(StarActivity.class);
        list.add(StarCameraActivity.class);
        return list;
    }
}
