package net.toughcoder.opengl.oaqs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

/**
 * Created by alexhilton on 15/7/1.
 */
public class TextureHelper {

    private static final String TAG = "TextureHelper";

    public static int loadTexture(Context context, int resourceId) {
        final int[] obj = new int[1];
        GLES20.glGenTextures(1, obj, 0);
        if (obj[0] == 0) {
            Log.e(TAG, "failed to generate texture objects");
            return 0;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
        if (bitmap == null) {
            Log.e(TAG, "cannot load resource");
            GLES20.glDeleteTextures(1, obj, 0);
            return 0;
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, obj[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR_MIPMAP_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        return obj[0];
    }
}
