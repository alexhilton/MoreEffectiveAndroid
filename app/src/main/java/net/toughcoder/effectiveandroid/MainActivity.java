package net.toughcoder.effectiveandroid;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import net.toughcoder.opengl1s.StarActivity;
import net.toughcoder.opengl2s.OpenGLExampleActivity;


public class MainActivity extends ActionBarActivity {
    public static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View frag = findViewById(R.id.fragment_test);
        frag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getApplication(), FragmentTestActivity.class);
                startActivity(it);
            }
        });
        View scroll = findViewById(R.id.scroll_linearlayout);
        scroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getApplication(), ScrollLinearLayoutActivity.class);
                startActivity(it);
            }
        });

        View drawableLabel = findViewById(R.id.drawable_label);
        drawableLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getApplicationContext(), DrawableLabelActivity.class);
                startActivity(it);
            }
        });

        View opengl = findViewById(R.id.opengl);
        opengl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getApplicationContext(), OpenGLExampleActivity.class);
                startActivity(it);
            }
        });

        View lm = findViewById(R.id.launch_mirror);
        lm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setClass(getApplicationContext(), StarActivity.class);
                startActivity(i);
            }
        });

        View lh = findViewById(R.id.launch_mirror_h5);
        lh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent();
                it.setData(Uri.parse("https://magicmirror.m.taobao.com/magicmirror/index.htm?mode=1&url=http://10.2.19.155:82/src/p/beauty/beauty.html&height=0.4313&origin=PhotoSearch"));
                startActivity(it);
            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Fragment frag = getFragmentManager().findFragmentById(R.id.blank_fragment);
        Log.e(TAG, "frag is " + frag);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
