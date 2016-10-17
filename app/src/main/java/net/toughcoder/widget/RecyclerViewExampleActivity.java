package net.toughcoder.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import net.toughcoder.effectiveandroid.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexhilton on 15/8/15.
 */
public class RecyclerViewExampleActivity extends Activity {
    private static final String TAG = "RecyclerView example";
    private RecyclerView mRecylerView;

    private ShowType mShowType;
    private List<ImageItem> mImages;
    private GallaryAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(TAG);
        setContentView(R.layout.recycler_view_example);
        ImageLoader.getInstance().init(getApplication());
        mImages = null;
        mRecylerView = (RecyclerView) findViewById(R.id.recycler_view);

        mShowType = ShowType.LIST;

        setupRecyclerView(mRecylerView);

        loadImages();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.recycler_view_example, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ShowType type;
        switch (item.getItemId()) {
            case R.id.list:
                type = ShowType.LIST;
                break;
            case R.id.grid:
                type = ShowType.GRID;
                break;
            case R.id.waterfall:
                type = ShowType.WATERFALL;
                break;
            default:
                type = ShowType.LIST;
                break;
        }
        switchToType(type);
        return true;
    }

    private void setupRecyclerView(RecyclerView rv) {
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.addItemDecoration(new GeneralDividerDcecoration(this));
        rv.setItemAnimator(new DefaultItemAnimator());
    }

    private void switchToType(ShowType type) {
        if (type == mShowType) {
            return;
        }
        mShowType = type;
        mRecylerView.setLayoutManager(createLayoutManager(type));
        mAdapter.refresh(mShowType);
    }

    private RecyclerView.LayoutManager createLayoutManager(ShowType type) {
        switch (type) {
            case LIST:
                return new LinearLayoutManager(this);
            case GRID:
                return new GridLayoutManager(this, 3);
            case WATERFALL:
                return new StaggeredGridLayoutManager(3, 1);
            default:
                return new LinearLayoutManager(this);
        }
    }


    private void loadImages() {
        new AsyncTask<Void, Void, List<ImageItem>>() {
            @Override
            protected List<ImageItem> doInBackground(Void... params) {
                final String[] projection = new String[]{
                        MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.DATA,
                        MediaStore.Images.Media.DATE_ADDED,
                        MediaStore.Images.Media.ORIENTATION,
                };
                List<ImageItem> images = new ArrayList<>();
                final Cursor cursor = MediaStore.Images.Media.query(getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");
                if (cursor == null) {
                    return images;
                }
                if (!cursor.moveToFirst()) {
                    cursor.close();
                    return images;
                }
                do {
                    if (isCancelled()) {
                        break;
                    }
                    long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    if (TextUtils.isEmpty(path)) {
                        continue;
                    }
                    int orientation = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));

                    if (path.contains("/DCIM/")) {
                        images.add(new ImageItem(id, path, orientation));
                    }
                } while (cursor.moveToNext());
                cursor.close();
                return images;
            }

            @Override
            protected void onPostExecute(List<ImageItem> imageItems) {
                mAdapter = new GallaryAdapter(getApplication(), imageItems, mShowType);
                mRecylerView.setAdapter(mAdapter);
            }
        }.execute();
    }

    private enum ShowType {
        LIST,
        GRID,
        WATERFALL,
    }

    private static class GallaryAdapter extends RecyclerView.Adapter<GallaryViewHolder> {
        private Context mContext;
        private LayoutInflater mFactory;
        private List<ImageItem> mImages;
        private ShowType mShowType;

        public GallaryAdapter(Context ctx, List<ImageItem> images, ShowType type) {
            mContext = ctx;
            mFactory = LayoutInflater.from(ctx);
            mImages = images;
            mShowType = type;
        }

        public void refresh(ShowType type) {
            mShowType = type;
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }

        @Override
        public GallaryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new GallaryViewHolder(mFactory.inflate(R.layout.gallery_item, null, false));
        }

        @Override
        public void onBindViewHolder(final GallaryViewHolder holder, int position) {
            ViewGroup.LayoutParams lp = holder.mThumbnail.getLayoutParams();
            lp.width = getTargetWidth();
            lp.height = getTargetHeight();
            holder.mThumbnail.setLayoutParams(lp);
            final ImageItem item = mImages.get(position);
            holder.mThumbnail.setImageBitmap(null);
            holder.mThumbnail.setTag(item);
            holder.mThumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    jumpToPailitao(mContext, item.getPath());
                }
            });
            item.setMetrics(lp.width, lp.height);
            ImageLoader.getInstance().getBitmap(holder.mThumbnail, item, new ImageLoader.LoadedCallback() {
                @Override
                public void run(ImageItem image, Bitmap result) {
                    holder.mThumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    holder.mThumbnail.setImageBitmap(result);
                    holder.mThumbnail.setTag(null);
                }
            });
        }


        @Override
        public int getItemCount() {
            return mImages.size();
        }

        private int getTargetWidth() {
            if (mShowType == ShowType.LIST) {
                return mContext.getResources().getDisplayMetrics().widthPixels;
            } else {
                return mContext.getResources().getDisplayMetrics().widthPixels / 3;
            }
        }

        private int getTargetHeight() {
            if (mShowType == ShowType.LIST) {
                return mContext.getResources().getDisplayMetrics().widthPixels * 2 / 3;
            } else if (mShowType == ShowType.GRID) {
                return mContext.getResources().getDisplayMetrics().widthPixels / 3;
            } else {
                final int h = mContext.getResources().getDisplayMetrics().widthPixels / 3;
                return (int) (Math.random() * h + Math.random() * h);
            }
        }
    }

    private static class GallaryViewHolder extends RecyclerView.ViewHolder {
        private ImageView mThumbnail;
        public GallaryViewHolder(View itemView) {
            super(itemView);
            mThumbnail = (ImageView) itemView.findViewById(R.id.thumb);
        }
    }

    private static class GeneralDividerDcecoration extends RecyclerView.ItemDecoration {
        private Drawable mDivider;
        private int mDividerWidth;
        public GeneralDividerDcecoration(Context ctx) {
            mDivider = ctx.getResources().getDrawable(R.drawable.recycler_view_divider);
            mDividerWidth = ctx.getResources().getDimensionPixelSize(R.dimen.divider_width);
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            drawHorizontal(c, parent);
            drawVertical(c, parent);
        }

        @Override
        public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
            int spanCount = getSpanCount(parent);
            int childCount = parent.getAdapter().getItemCount();
            RecyclerView.LayoutManager lm = parent.getLayoutManager();
            if (isLastRow(parent, itemPosition, spanCount, childCount) && isLastCol(parent, itemPosition, spanCount, childCount)) {
                // left offset, top offset, right offset, bottom offset
                // the same to margin left, margin top, margin right and margin bottom
                outRect.set(0, 0, 0, 0);
            } else if (isLastRow(parent, itemPosition, spanCount, childCount)) {
                if (lm instanceof LinearLayoutManager || spanCount == 1) {
                    outRect.set(0, 0, 0, 0);
                } else {
                    outRect.set(0, 0, mDividerWidth, 0);
                }
            } else if (isLastCol(parent, itemPosition, spanCount, childCount)) {
                if (lm instanceof LinearLayoutManager || spanCount == 1) {
                    outRect.set(0, 0, 0, mDividerWidth);
                } else {
                    outRect.set(0, 0, 0, 0);
                }
            } else {
                outRect.set(0, 0, mDividerWidth, mDividerWidth);
            }
        }

        private int getSpanCount(RecyclerView parent) {
            int spanCount = -1;
            RecyclerView.LayoutManager lm = parent.getLayoutManager();
            if (lm instanceof GridLayoutManager) {
                spanCount = ((GridLayoutManager) lm).getSpanCount();
            } else if (lm instanceof StaggeredGridLayoutManager) {
                spanCount = ((StaggeredGridLayoutManager) lm).getSpanCount();
            } else {
                spanCount = 1;
            }

            return spanCount;
        }

        private void drawHorizontal(Canvas c, RecyclerView parent) {
            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int left = child.getLeft() - lp.leftMargin;
                final int right = child.getRight() + lp.rightMargin;
                final int top = child.getBottom() + lp.bottomMargin;
                final int bottom = top + mDividerWidth;
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        private void drawVertical(Canvas c, RecyclerView parent) {
            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int left = child.getRight() + lp.rightMargin;
                final int right = left + mDividerWidth;
                final int top = child.getTop() - lp.topMargin;
                final int bottom = child.getBottom() + lp.bottomMargin;
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        private boolean isLastCol(RecyclerView parent, int pos, int spanCount, int childCount) {
            RecyclerView.LayoutManager lm = parent.getLayoutManager();
            if (lm instanceof GridLayoutManager) {
                if ((pos + 1) % spanCount == 0) {
                    return true;
                }
            } else if (lm instanceof StaggeredGridLayoutManager) {
                int orientation = ((StaggeredGridLayoutManager) lm).getOrientation();
                if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                    if ((pos + 1) % spanCount == 0) {
                        return true;
                    }
                } else {
                    childCount = childCount - childCount % spanCount;
                    if (pos >= childCount) {
                        return true;
                    }
                }
            } else if (lm instanceof LinearLayoutManager) {
                int orientation = ((LinearLayoutManager) lm).getOrientation();
                if (orientation == LinearLayoutManager.VERTICAL) {
                    return true;
                } else {
                    return pos == childCount - 1;
                }
            }

            return false;
        }

        private boolean isLastRow(RecyclerView parent, int pos, int spanCount, int childCount) {
            RecyclerView.LayoutManager lm = parent.getLayoutManager();
            if (lm instanceof GridLayoutManager) {
                childCount = childCount - childCount % spanCount;
                if (pos >= childCount) {
                    return true;
                }
            } else if (lm instanceof StaggeredGridLayoutManager) {
                int orientation = ((StaggeredGridLayoutManager) lm).getOrientation();
                if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                    childCount = childCount - childCount % spanCount;
                    if (pos >= childCount) {
                        return true;
                    }
                } else {
                    if ((pos + 1) % spanCount == 0) {
                        return true;
                    }
                }
            } else if (lm instanceof LinearLayoutManager) {
                int orientation = ((LinearLayoutManager) lm).getOrientation();
                if (orientation == LinearLayoutManager.HORIZONTAL) {
                    return true;
                } else {
                    return pos == childCount - 1;
                }
            }

            return false;
        }
    }

    public static void jumpToPailitao(Context ctx, String path) {
        Intent i = new Intent("productsearch");
        i.setDataAndType(Uri.parse(path), "image/jpeg");
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }
}
