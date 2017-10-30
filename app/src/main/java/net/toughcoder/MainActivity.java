package net.toughcoder;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import net.toughcoder.animation.AnimationExampleListActivity;
import net.toughcoder.camera.CameraExampleListActivity;
import net.toughcoder.effectiveandroid.R;
import net.toughcoder.eos.EosCameraActivity;
import net.toughcoder.libraries.LibraryExampleList;
import net.toughcoder.miscellaneous.DrawableLabelActivity;
import net.toughcoder.miscellaneous.ExampleListActivity;
import net.toughcoder.miscellaneous.FragmentStateLossActivity;
import net.toughcoder.miscellaneous.FragmentTestActivity;
import net.toughcoder.miscellaneous.KeyboardAwareActivity;
import net.toughcoder.miscellaneous.ScrollLinearLayoutActivity;
import net.toughcoder.opengl.OpenGLESExampleListActivity;
import net.toughcoder.rs.RSExampleListActivity;
import net.toughcoder.widget.GridLayoutExampleActivity;
import net.toughcoder.widget.RecyclerViewExampleActivity;
import net.toughcoder.widget.SurfaceExampleActivity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class MainActivity extends ExampleListActivity {
    private static final String TAG = "Effective Android";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate inst -> " + savedInstanceState);
        setTitle(TAG);

        explainContext();

//        threadLimit();
    }

    @Override
    protected List<Class> getActivityList() {
        List<Class> list = new LinkedList<>();
        list.add(FragmentTestActivity.class);
        list.add(ScrollLinearLayoutActivity.class);
        list.add(DrawableLabelActivity.class);
        list.add(RecyclerViewExampleActivity.class);
        list.add(KeyboardAwareActivity.class);
        list.add(GridLayoutExampleActivity.class);
        list.add(SurfaceExampleActivity.class);
        list.add(OpenGLESExampleListActivity.class);
        list.add(RSExampleListActivity.class);
        list.add(CameraExampleListActivity.class);
        list.add(AnimationExampleListActivity.class);
        list.add(FragmentStateLossActivity.class);
        list.add(LibraryExampleList.class);
        list.add(EosCameraActivity.class);
        return list;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
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

    private void explainContext() {
        Context ctx = getApplicationContext();
        Log.e(TAG, "explainContext getApplicationContext->" + ctx);
        Application app = getApplication();
        Log.e(TAG, "explainContext getApplication->" + app);
    }

    // Try to find how many threads an app can start at most?
    // terminated by OOM, when reach 1759 on Nexus 6
    private void threadLimit() {
        int count = 0;
        while (true) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            count++;
            Log.e(TAG, "current " + count);
        }
    }
}
