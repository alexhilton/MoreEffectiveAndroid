package net.toughcoder.miscellaneous;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import net.toughcoder.effectiveandroid.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexhilton on 15/7/21.
 */
public class ShareWeixinActivity extends Activity {
    private static final String TAG = "Share to weixin example";
    private Uri mUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(TAG);
        setContentView(R.layout.share_weixin_activity);
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String filepath = "1234";
        File image = new File(dir, filepath);
        File file = new File(image, "IMG_20150720_180942.jpg");
        mUri = Uri.fromFile(file);
        ImageView iv = (ImageView) findViewById(R.id.image);
        iv.setImageURI(mUri);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_weixin, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private List<ComponentName> cn;
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mWeixinComponents = findWeixinComponents();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.friend) {
            shareToWeixin(mUri, false);
        } else if (item.getItemId() == R.id.stream) {
            shareToWeixin(mUri, true);
        }
        return true;
    }
    private List<ComponentName> mWeixinComponents;
    private void shareToWeixin(Uri uri, boolean timeline) {
        Intent i = new Intent();
        i.setAction(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_STREAM, uri);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setType("image/*");

        i.setComponent(mWeixinComponents.get(timeline ? 1 : 0));

        try {
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "亲, 分享失败了, 请稍后再试", Toast.LENGTH_SHORT).show();
        }
    }

    private List<ComponentName> findWeixinComponents() {
        Intent i = new Intent();
        i.setAction(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/Pictures/"));
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setType("image/*");

        PackageManager pm = getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(i, PackageManager.GET_INTENT_FILTERS);
        List<ComponentName> componentNames = new ArrayList<ComponentName>();
        for (ResolveInfo info : list) {
            if (info == null || info.activityInfo == null) {
                continue;
            }
            if (TextUtils.equals(info.activityInfo.packageName, "com.tencent.mm")) {
                if (info.activityInfo.name.contains("ShareImgUI")) {
                    componentNames.add(0, new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
                }
                if (info.activityInfo.name.contains("ShareToTimeLineUI")) {
                    componentNames.add(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
                }
            }
        }

        return componentNames;
    }
}
