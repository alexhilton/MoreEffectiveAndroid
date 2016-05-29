package net.toughcoder.effectiveandroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import net.toughcoder.ViewServer;
import net.toughcoder.camera.CameraExampleListActivity;
import net.toughcoder.opengl1s.StarActivity;
import net.toughcoder.opengl2s.OpenGLExampleActivity;
import net.toughcoder.rs.RSExampleListActivity;
import net.toughcoder.starcamera.StarCameraActivity;
import net.toughcoder.widget.AlphaOpenGLActivity;
import net.toughcoder.widget.GridLayoutExampleActivity;
import net.toughcoder.widget.RecyclerViewExampleActivity;
import net.toughcoder.widget.SurfaceExampleActivity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class MainActivity extends ExampleListActivity {
    private static final String TAG = "Effective Android";

    @Override
    protected List<Class> getActivityList() {
        List<Class> list = new LinkedList<>();
        list.add(FragmentTestActivity.class);
        list.add(ScrollLinearLayoutActivity.class);
        list.add(DrawableLabelActivity.class);
        list.add(OpenGLExampleActivity.class);
        list.add(StarActivity.class);
        list.add(StarCameraActivity.class);
        list.add(RecyclerViewExampleActivity.class);
        list.add(KeyboardAwareActivity.class);
        list.add(GridLayoutExampleActivity.class);
        list.add(SurfaceExampleActivity.class);
        list.add(AlphaOpenGLActivity.class);
        list.add(RSExampleListActivity.class);
        list.add(CameraExampleListActivity.class);
        return list;
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
