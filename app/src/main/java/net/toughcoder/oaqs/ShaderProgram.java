package net.toughcoder.oaqs;

import android.content.Context;
import android.opengl.GLES20;

/**
 * Created by alexhilton on 15/7/1.
 */
public class ShaderProgram {
    public static final String U_COLOR = "u_Color";
    public static final String A_COLOR = "a_Color";
    public static final String A_POSITION = "a_Position";
    public static final String U_MATRIX = "u_Matrix";
    public static final String U_TEXTURE_UNIT = "u_TextureUnit";
    public static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";

    protected final int program;

    public ShaderProgram(Context context, int vshid, int fshid) {
        program = ShaderHelper.buildProgram(Utils.readTextFileFromResource(context, vshid),
                Utils.readTextFileFromResource(context, fshid));
    }

    public void useProgram() {
        GLES20.glUseProgram(program);
    }
}
