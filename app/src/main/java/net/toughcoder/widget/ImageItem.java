package net.toughcoder.widget;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by alexhilton on 15/8/17.
 */
class ImageItem {
    private String mPath;

    private int mTargetWidth;
    private int mTargetHeight;

    public ImageItem(long id, String path, int orientation) {
        mPath = path;
        mTargetHeight = 0;
        mTargetWidth = 0;
    }

    public Bitmap loadThumbnail() {
        Bitmap thumb = null;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mPath, opts);
        opts.inSampleSize = (int) Math.ceil(opts.outWidth / mTargetWidth);
        opts.inJustDecodeBounds = false;
        thumb = BitmapFactory.decodeFile(mPath, opts);
        return thumb;
    }

    public String getIdentity() {
        return mPath + mTargetWidth + mTargetHeight;
    }

    public String getPath() {
        return mPath;
    }

    public void setMetrics(int width, int height) {
        mTargetHeight = height;
        mTargetWidth = width;
    }
}