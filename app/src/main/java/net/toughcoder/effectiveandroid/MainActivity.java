package net.toughcoder.effectiveandroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import net.toughcoder.ViewServer;
import net.toughcoder.opengl1s.StarActivity;
import net.toughcoder.opengl2s.OpenGLExampleActivity;
import net.toughcoder.rs.ImagePressActivity;
import net.toughcoder.rs.GrayScalifyImageActivity;
import net.toughcoder.rs.RSYUV2RGBAActivity;
import net.toughcoder.starcamera.StarCameraActivity;
import net.toughcoder.widget.AlphaOpenGLActivity;
import net.toughcoder.widget.BitmapBlurTestActivity;
import net.toughcoder.widget.GridLayoutExampleActivity;
import net.toughcoder.widget.RecyclerViewExampleActivity;
import net.toughcoder.widget.SurfaceExampleActivity;
import net.toughcoder.widget.TextureViewActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends ActionBarActivity {
    public static final String TAG = "MainActivity";
    private static HashMap<String, Class> sActivityList = new LinkedHashMap<>();
    static {
        sActivityList.put("Fragment Test", FragmentTestActivity.class);
        sActivityList.put("Scroll LinearLayout", ScrollLinearLayoutActivity.class);
        sActivityList.put("Drawable Label", DrawableLabelActivity.class);
        sActivityList.put("OpenGL Example", OpenGLExampleActivity.class);
        sActivityList.put("Stars Example", StarActivity.class);
        sActivityList.put("Camera with Star", StarCameraActivity.class);
        sActivityList.put("Recycler View", RecyclerViewExampleActivity.class);
        sActivityList.put("Keyboard Aware", KeyboardAwareActivity.class);
        sActivityList.put("GridLayout Example", GridLayoutExampleActivity.class);
        sActivityList.put("Camera Preview", CameraPreviewActivity.class);
        sActivityList.put("TextureView", TextureViewActivity.class);
        sActivityList.put("Surface Example", SurfaceExampleActivity.class);
        sActivityList.put("Alpha OpenGL", AlphaOpenGLActivity.class);
        sActivityList.put("Bitmap Blur", BitmapBlurTestActivity.class);
        sActivityList.put("RS YUV to RGBA", RSYUV2RGBAActivity.class);
        sActivityList.put("RS Image Press", ImagePressActivity.class);
        sActivityList.put("RS Grayscale", GrayScalifyImageActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        ViewServer.get(this).addWindow(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ViewServer.get(this).setFocusedWindow(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ViewServer.get(this).removeWindow(this);
    }

    private void testList() {
        List<String> names = new ArrayList<String>();
        names.add("NewYork");
        names.add("Boston");
        names.add("Sanfransisco");
        names.add("Atalantics");
        names.add("Seatle");
        Log.e("list", "full list is " + names);
        names.subList(0, 2);
        Log.e("list", "sub of 0, 2 " + names);
    }

    private void asyncShowDialog(final String msg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finish();
            }
        }).start();
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
