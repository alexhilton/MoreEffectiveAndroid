package net.toughcoder.animation;

import android.os.Bundle;

import net.toughcoder.miscellaneous.ExampleListActivity;

import java.util.ArrayList;
import java.util.List;

public class AnimationExampleListActivity extends ExampleListActivity {
    private static final String TAG = "Animation examples";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(TAG);
    }

    @Override
    protected List<Class> getActivityList() {
        List<Class> clazzes = new ArrayList<>();
        return clazzes;
    }
}
