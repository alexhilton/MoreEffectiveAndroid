package net.toughcoder.effectiveandroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import net.toughcoder.opengl1s.StarActivity;
import net.toughcoder.opengl2s.OpenGLExampleActivity;
import net.toughcoder.starcamera.StarCameraActivity;
import net.toughcoder.widget.RecyclerViewExampleActivity;


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
                Intent it = new Intent(Intent.ACTION_VIEW);
//                it.setClass(getApplicationContext(), ShareWeixinActivity.class);
                it.setData(Uri.parse("http://h5.m.taobao.com/tusou/album/index.html?pssource=effective&search=1"));
                startActivity(it);
            }
        });

        View sc = findViewById(R.id.star_camera);
        sc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(Intent.ACTION_VIEW);
                it.setClass(getApplicationContext(), StarCameraActivity.class);
                startActivity(it);
            }
        });
        View rv = findViewById(R.id.recyclerview);
        rv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(Intent.ACTION_VIEW);
                it.setClass(getApplicationContext(), RecyclerViewExampleActivity.class);
                startActivity(it);
            }
        });
        View ka = findViewById(R.id.keyboard_aware);
        ka.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(Intent.ACTION_VIEW);
                it.setClass(getApplicationContext(), KeyboardAwareActivity.class);
                startActivity(it);
            }
        });
//      try {
//        Thread.sleep(10 * 1000);
//      } catch (InterruptedException e) {
//        e.printStackTrace();
//      }
//      block();
        new Handler().post(new Runnable() {
          @Override
          public void run() {
//               showADialog("shit man");
//            showProgressDialog();
          }
        });
    }

  private void showADialog(String msg) {
    Dialog dialog = new AlertDialog.Builder(this)
        .setTitle("Alert")
        .setMessage(msg)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .create();
    dialog.show();
  }

  private void showProgressDialog() {
    Dialog dialog = ProgressDialog.show(this, "welcome", "Hello, world");
  }

  private void block() {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {
        block();
      }
    }, 10);
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
