package net.toughcoder.camera;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import net.toughcoder.effectiveandroid.CameraPreviewActivity;
import net.toughcoder.effectiveandroid.R;
import net.toughcoder.widget.TextureViewActivity;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class CameraExampleListActivity extends ActionBarActivity {
    private static HashMap<String, Class> sActivityList = new LinkedHashMap<>();
    static {
        sActivityList.put("Camera Preview", CameraPreviewActivity.class);
        sActivityList.put("TextureView", TextureViewActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_example_list);
        LinearLayout list = (LinearLayout) findViewById(R.id.activity_list);
        final LayoutInflater factory = LayoutInflater.from(this);
        for (Map.Entry<String, Class> entry : sActivityList.entrySet()) {
            final String label = entry.getKey();
            final Class activity = entry.getValue();
            Button btn = (Button) factory.inflate(R.layout.activity_list_item, list, false);
            btn.setText(label);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getApplicationContext(), activity);
                    startActivity(i);
                }
            });
            list.addView(btn);
        }
    }
}
