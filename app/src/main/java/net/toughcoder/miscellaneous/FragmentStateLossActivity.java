package net.toughcoder.miscellaneous;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;

import net.toughcoder.effectiveandroid.R;

/*
 * State loss exception will be thrown if onBackPressed executed after onSaveInstanceState or onStop only for
 * FragmentActivity in support-v4. Standard Activity does not have such issue.
 */
public class FragmentStateLossActivity extends Activity {
    private static final String TAG = "Fragment state loss";
    private boolean mStateSaved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_state_loss);
        setTitle(TAG);
        mStateSaved = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Not call super won't help us, still get crash
        super.onSaveInstanceState(outState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            mStateSaved = true;
        }
        Log.e(TAG, "onSaveInstanceState. finishing " + isFinishing());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mStateSaved = false;
        Log.e(TAG, "onResume. finishing " + isFinishing());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause. finishing " + isFinishing());
    }

    @Override
    protected void onStop() {
        super.onStop();
//        mStateSaved = true;
        Log.e(TAG, "onStop. finishing " + isFinishing());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onBackPressed();
            }
        }, 5000);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mStateSaved = false;
        Log.e(TAG, "onStart. finishing " + isFinishing());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy. finishing " + isFinishing());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e(TAG, "keycode " + keyCode + ", event " + event);
        return super.onKeyDown(keyCode, event);
//        if (!mStateSaved) {
//        } else {
//            // State already saved, so ignore the event
//            return true;
//        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!mStateSaved) {
        }
        Log.e(TAG, "onBackPressed. finishing " + isFinishing());
    }
}
