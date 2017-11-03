package net.toughcoder.eos;

/**
 * Created by alex on 17-11-1.
 * This is the interface between user interface and logic business.
 */

public interface EosCamera {
    void onStart();
    void onResume();
    void onPause();
    void onStop();
}
