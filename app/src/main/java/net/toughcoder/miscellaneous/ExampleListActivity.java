package net.toughcoder.miscellaneous;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import net.toughcoder.effectiveandroid.R;

import java.lang.reflect.Field;
import java.util.List;

public abstract class ExampleListActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example_list);
        LinearLayout list = (LinearLayout) findViewById(R.id.activity_list);
        final LayoutInflater factory = LayoutInflater.from(this);
        List<Class> activities = getActivityList();
        for (final Class clazz : activities) {
            Button btn = (Button) factory.inflate(R.layout.activity_list_item, list, false);
            btn.setText(extractTag(clazz));
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getApplicationContext(), clazz);
                    startActivity(i);
                }
            });
            list.addView(btn);
        }
    }

    private static String extractTag(Class activity) {
        try {
            Field tag = activity.getDeclaredField("TAG");
            tag.setAccessible(true);
            String tagValue = (String) tag.get(activity);
            return tagValue;
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }
        return activity.getSimpleName();
    }

    protected abstract List<Class> getActivityList();
}
