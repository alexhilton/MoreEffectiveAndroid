package net.toughcoder.miscellaneous;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import net.toughcoder.effectiveandroid.R;

public class FragmentStateLossActivity extends ActionBarActivity {
    private static final String TAG = "Fragment state loss";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_state_loss);
        setTitle(TAG);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.e(TAG, "onSaveInstanceState. finishing " + isFinishing());
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        Log.e(TAG, "onStop. finishing " + isFinishing());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart. finishing " + isFinishing());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy. finishing " + isFinishing());
    }
}
