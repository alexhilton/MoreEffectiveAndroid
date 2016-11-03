package net.toughcoder.libraries;

import android.os.Bundle;

import net.toughcoder.miscellaneous.ExampleListActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexhilton on 16/11/3.
 */

public class LibraryExampleList extends ExampleListActivity {
    private static final String TAG = "Open source library examples";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(TAG);
    }

    @Override
    protected List<Class> getActivityList() {
        List<Class> list = new ArrayList<>();
        list.add(RetrofitExampleActivity.class);
        return list;
    }
}
