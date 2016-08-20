package net.toughcoder.opengl.oaqs;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by alexhilton on 15/6/29.
 */
public class Utils {

    private static final String TAG = "Utils";

    public static String readTextFileFromResource(Context context, int resourceId) {
        StringBuilder body = new StringBuilder();

        try {
            InputStream is = context.getResources().openRawResource(resourceId);
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                body.append(line);
                body.append("\n");
            }
        } catch (IOException e) {
            Log.e(TAG, "cannot read text from res " + resourceId);
        }

        return body.toString();
    }
}
