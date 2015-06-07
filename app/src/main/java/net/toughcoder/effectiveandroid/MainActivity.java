package net.toughcoder.effectiveandroid;

import android.app.Fragment;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import net.toughcoder.effectiveandroid.R;
import net.toughcoder.opengl.OpenGLExampleActivity;


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

        View opengl = findViewById(R.id.opengl);
        opengl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getApplicationContext(), OpenGLExampleActivity.class);
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
