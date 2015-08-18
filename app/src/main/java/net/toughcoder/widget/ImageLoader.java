package net.toughcoder.widget;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ImageLoader {
    @SuppressWarnings("unused")
    private static final String TAG = "ImageLoader";
    private static final int CACHE_SIZE = 100;

    private static ImageLoader sInstance;

    // Queue of work to do in the worker thread. The work is done in order.
    private final ArrayList<WorkItem> mQueue = new ArrayList<WorkItem>();

    // the worker thread and a done flag so we know when to exit
    private boolean mDone;
    private Thread mDecodeThread;
    private Context mContext;
    private Handler mMainHandler;
    private ContentResolver mCr;
    private LruCache<String, WeakReference<Bitmap>> mCache;

    public interface LoadedCallback {
        /**
         * This method is guaranteed to get called in main thread.
         * @param image
         * @param result
         */
        public void run(ImageItem image, Bitmap result);
    }

    private ImageLoader() {
        //
    }

    public static synchronized ImageLoader getInstance() {
        if (sInstance == null) {
            sInstance = new ImageLoader();
        }

        return sInstance;
    }


    public void init(Context ctx) {
        mContext = ctx;
        mCr = ctx.getContentResolver();
        if (mMainHandler == null) {
            mMainHandler = new Handler(Looper.getMainLooper());
        }
        if (mCache == null) {
            mCache = new LruCache<String, WeakReference<Bitmap>>(CACHE_SIZE);
        }
        start();
    }

    public void getBitmap(ImageView view, ImageItem image, LoadedCallback imageLoadedRunnable) {
        if (mDecodeThread == null) {
            start();
        }
        WeakReference<Bitmap> value = mCache.get(image.getIdentity());
        if (value != null && value.get() != null) {
            // Cache hit
            imageLoadedRunnable.run(image, value.get());
            return;
        }
        // cache miss, load
        synchronized (mQueue) {
            WorkItem w = new WorkItem(view, image, imageLoadedRunnable);
            mQueue.add(w);
            mQueue.notifyAll();
        }
    }

    public boolean cancel(final ImageItem image) {
        synchronized (mQueue) {
            int index = findItem(image);
            if (index >= 0) {
                mQueue.remove(index);
                return true;
            } else {
                return false;
            }
        }
    }

    // The caller should hold mQueue lock.
    private int findItem(ImageItem image) {
        for (int i = 0; i < mQueue.size(); i++) {
            if (mQueue.get(i).mImage == image) {
                return i;
            }
        }
        return -1;
    }

    // Clear the queue. Returns an array of tags that were in the queue.
    public void clearQueue() {
        synchronized (mQueue) {
            mQueue.clear();
        }
    }

    private static class WorkItem {
        ImageView mView;
        ImageItem mImage;
        LoadedCallback mOnLoadedRunnable;

        WorkItem(ImageView view, ImageItem image, LoadedCallback onLoadedRunnable) {
            mView = view;
            mImage = image;
            mOnLoadedRunnable = onLoadedRunnable;
        }
    }

    private class WorkerThread implements Runnable {

        // Pick off items on the queue, one by one, and compute their bitmap.
        // Place the resulting bitmap in the cache, then call back by executing
        // the given runnable so things can get updated appropriately.
        public void run() {
            while (true) {
                WorkItem workItem = null;
                synchronized (mQueue) {
                    if (mDone) {
                        break;
                    }
                    if (!mQueue.isEmpty()) {
                        workItem = mQueue.remove(0);
                    } else {
                        try {
                            mQueue.wait();
                        } catch (InterruptedException ex) {
                            // ignore the exception
                        }
                        continue;
                    }
                }

                if (workItem.mView.getTag() != workItem.mImage) {
                    continue;
                }
                final Bitmap b = workItem.mImage.loadThumbnail();
                if (b != null) {
                    mCache.put(workItem.mImage.getIdentity(), new WeakReference<Bitmap>(b));
                }

                if (workItem.mOnLoadedRunnable != null) {
                    final WorkItem finalWorkItem = workItem;
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            finalWorkItem.mOnLoadedRunnable.run(finalWorkItem.mImage, b);
                        }
                    });
                }
            }
        }
    }

    private void start() {
        if (mDecodeThread != null) {
            return;
        }

        mDone = false;
        Thread t = new Thread(new WorkerThread());
        t.setName("image-loader");
        mDecodeThread = t;
        t.start();
    }

    public void stop() {
        synchronized (mQueue) {
            mDone = true;
            mQueue.notifyAll();
        }
        mDecodeThread = null;
    }

    public void clearCache() {
        mCache.evictAll();
    }
}
