package net.toughcoder.effectiveandroid;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import net.toughcoder.effectiveandroid.R;

/**
 * Created by alexhilton on 15/4/29.
 */

public class FragmentTestActivity extends Activity {
    public static final String TAG = "FragmentTestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_test);
        Fragment frag = getFragmentManager().findFragmentById(R.id.blank_fragment);
        Log.e(TAG, "frag " + frag);

        View another = findViewById(R.id.another);
        another.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.e(TAG, "let do this");
                        Fragment frag = new BlankFragment();
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.add(R.id.content, frag);
                        ft.commit();
                    }
                }).start();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Fragment frag = getFragmentManager().findFragmentById(R.id.blank_fragment);
                Log.e(TAG, "with thread frag " + frag);
            }
        }).start();
    }
}
