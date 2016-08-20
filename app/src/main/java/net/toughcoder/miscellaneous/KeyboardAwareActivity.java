package net.toughcoder.miscellaneous;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.Toast;

import net.toughcoder.effectiveandroid.R;

/**
 * Created by alexhilton on 15/10/9.
 */
public class KeyboardAwareActivity extends Activity {
    private static final String TAG = "Keyboard aware example";
    private boolean mKeyboardUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(TAG);
        mKeyboardUp = false;
        setContentView(R.layout.keyboard_aware_activity);

        setListenerToRootView();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(getApplicationContext(), "Toast from worker thread", Toast.LENGTH_SHORT).show();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        final EditText edit = (EditText) findViewById(R.id.edittext);
        edit.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (isKeyboardShown(edit.getRootView())) {
                    Log.e("keyboard", "bottom method keyboard UP");
                    edit.setCursorVisible(true);
                } else {
                    Log.e("keyboard", "bottom method keyboard Down");
                    edit.setCursorVisible(false);
                }
            }
        });
    }

    private void setListenerToRootView() {
        final View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final int headerHeight = getActionBarHeight() + getStatusBarHeight();
                int heightDiff = rootView.getRootView().getHeight() - rootView.getHeight();
                Log.e("keyboard", "the rootview and rootview height diff " + heightDiff + ", headerHeight " + headerHeight);
                Log.e("keyboard", "rootView is " + rootView + ", its height " + rootView.getHeight());
                Log.e("keyboard", "rootof rootView is " + rootView.getRootView() + ", its height " + rootView.getRootView().getHeight());
                View contentView = ((ViewGroup) rootView).getChildAt(0);
                Log.e("keyboard", "rootView first child " + contentView + ", its height "+ contentView.getHeight());
                Log.e("keyboard", "screen height is " +
                        rootView.getResources().getDisplayMetrics().heightPixels +
                        ", status bar height " + getStatusBarHeight() +
                        ", action bar height " + getActionBarHeight());
                if (heightDiff > headerHeight) {
                    Log.e("keyboard", "keyboard is up");
                    if (!mKeyboardUp) {
                        mKeyboardUp = true;
                    }
                } else if (mKeyboardUp) {
                    Log.e("keyboard", "keyboard is hidden");
                    mKeyboardUp = false;
                }
            }
        });
    }

    private boolean isKeyboardShown(View rootView) {
        Log.e("keyboard", "isKeyboardShown rootView is " + rootView);
        final int softKeyboardHeight = 100;
        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        Log.e("keyboard", "rootView window visible display frame is " + r);
        DisplayMetrics dm = rootView.getResources().getDisplayMetrics();
        Log.e("keyboard", "rootView top " + rootView.getTop() + ", bottom " + rootView.getBottom());
        int heightDiff = rootView.getBottom() - r.bottom;
        String a = "";
        return heightDiff > softKeyboardHeight * dm.densityDpi;
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private int getActionBarHeight() {
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
            int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            return actionBarHeight;
        } else {
            return getActionBar().getHeight();
        }
    }
}
