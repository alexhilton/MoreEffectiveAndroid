package net.toughcoder.eos;

import android.view.Surface;

/**
 * Created by alex on 17-11-3.
 */

public interface Targetable {
    Surface getSurface();
    void setInputDimension(int width, int height);
    boolean isAlive();
}
