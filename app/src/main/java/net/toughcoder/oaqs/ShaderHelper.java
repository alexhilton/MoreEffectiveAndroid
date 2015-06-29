package net.toughcoder.oaqs;

import android.opengl.GLES20;
import android.util.Log;

/**
 * Created by alexhilton on 15/6/29.
 */
public class ShaderHelper {
    private static String TAG = "ShaderHelper";

    public static int compileVertexShader(String code) {
        return compileShader(GLES20.GL_VERTEX_SHADER, code);
    }

    public static int compibleFragmentShader(String code) {
        return compileShader(GLES20.GL_FRAGMENT_SHADER, code);
    }

    public static int compileShader(int type, String code) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);
        final int[] status = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);
        Log.e(TAG, "shader log " + GLES20.glGetShaderInfoLog(shader));
        if (status[0] == 0) {
            GLES20.glDeleteShader(shader);
            return 0;
        }
        return shader;
    }

    public static int linkProgram(int vsh, int fsh) {
        if (vsh == 0 || fsh == 0) {
            Log.e(TAG, "Invalid vertex shader and fragment shader");
            return 0;
        }
        int prog = GLES20.glCreateProgram();
        GLES20.glAttachShader(prog, vsh);
        GLES20.glAttachShader(prog, fsh);
        GLES20.glLinkProgram(prog);
        int[] status = new int[1];
        GLES20.glGetProgramiv(prog, GLES20.GL_LINK_STATUS, status, 0);
        Log.e(TAG, "link program " + GLES20.glGetProgramInfoLog(prog));
        if (status[0] == 0) {
            GLES20.glDeleteProgram(prog);
            return 0;
        }
        return prog;
    }

    public static boolean validateProgram(int prog) {
        if (prog == 0) {
            return false;
        }
        GLES20.glValidateProgram(prog);

        int[] status = new int[1];
        GLES20.glGetProgramiv(prog, GLES20.GL_VALIDATE_STATUS, status, 0);
        Log.e(TAG, "validate progra " + GLES20.glGetProgramInfoLog(prog));
        return status[0] != 0;
    }
}
