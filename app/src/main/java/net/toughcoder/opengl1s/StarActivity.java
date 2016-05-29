package net.toughcoder.opengl1s;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.util.Log;

import net.toughcoder.effectiveandroid.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by alexhilton on 15/7/16.
 */
public class StarActivity extends Activity {
    private static final String TAG = "OpenGL Stars example";

    private GLSurfaceView glSurfaceView;
    private StarRenderer renderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(TAG);
        Load.loadBitmap(getResources());
        renderer = new StarRenderer();
        glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(1);
        glSurfaceView.setRenderer(renderer);
        setContentView(glSurfaceView);
    }
}

class Load {
    public static Bitmap bitmap;

    public static void loadBitmap(Resources res) {
        bitmap = BitmapFactory.decodeResource(res, R.drawable.star);
    }
}

class StarRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "StarRenderer";
    private int one = 0x10000 / 2;
    private IntBuffer vertexBuffer;
    private int[] vertex = new int[] { -one, -one, 0, one, -one, 0, -one, one,
            0, one, one, 0 };
    private IntBuffer coordBuffer;
    private int[] coord = { 0, 0, one, 0, 0, one, one, one };
    private int[] textures = new int[1];
    private Random random = new Random();
    // 闪烁的星星
    boolean twinkle = true;
    // star数目
    int num = 50;
    // star数目数组
    NiceStar[] star = new NiceStar[num];
    // star 倾角
    float tilt = 90.0f;
    // star 距人的dist
    float zoom = -10.0f;
    float spin; // 闪烁星星的自转

    // init数据
    public void initData() {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertex.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuffer.asIntBuffer();
        vertexBuffer.put(vertex);
        vertexBuffer.position(0);

        ByteBuffer coordByteBuffer = ByteBuffer
                .allocateDirect(coord.length * 4);
        coordByteBuffer.order(ByteOrder.nativeOrder());
        coordBuffer = coordByteBuffer.asIntBuffer();
        coordBuffer.put(coord);
        coordBuffer.position(0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // 清除屏幕和深度缓存
        gl.glClear(GL10.GL_DEPTH_BUFFER_BIT | GL10.GL_COLOR_BUFFER_BIT);
        gl.glFrontFace(GL10.GL_CCW);
        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glCullFace(GL10.GL_BACK);
        // 初始化数据
        initData();

        for (int i = 0; i < num; i++) {
            gl.glLoadIdentity();
            // 向屏幕里移入zoom
            gl.glTranslatef(0.0f, 0.0f, zoom);

            gl.glRotatef(tilt, 1.0f, 0.0f, 0.0f); // 倾斜视角
            gl.glRotatef(star[i].angle, 0.0f, 1.0f, 0.0f); // 旋转至当前所画星星的角度
            gl.glTranslatef(star[i].dist, 0.0f, 0.0f); // 沿X轴正向移动
            gl.glRotatef(-star[i].angle, 0.0f, 1.0f, 0.0f); // 取消当前星星的角度
            gl.glRotatef(-tilt, 1.0f, 0.0f, 0.0f); // 取消屏幕倾斜

            // 开启顶点、颜色和纹理
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
//            gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
            gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

            if (twinkle) // 启用闪烁效果
            {
                // 使用byte型数值指定一个颜色
                gl.glColor4f((float) star[(num - i) - 1].r / 255.0f,
                        (float) star[(num - i) - 1].g / 255.0f,
                        (float) star[(num - i) - 1].b / 255.0f, 1.0f);
                gl.glVertexPointer(3, GL10.GL_FIXED, 0, vertexBuffer);
                gl.glTexCoordPointer(2, GL10.GL_FIXED, 0, coordBuffer);
                // 绘制
                gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
                Log.e(TAG, String.format("first gldraw aray 0x%X", gl.glGetError()));
                // 绘制结束
//                gl.glFinish();
            }

//            gl.glRotatef(spin, 0.0f, 0.0f, 1.0f); // 绕z轴旋转星星
//
//            // 使用byte型数值指定一个颜色
//            gl.glColor4f((float) star[(num - i) - 1].r / 255.0f,
//                    (float) star[(num - i) - 1].g / 255.0f,
//                    (float) star[(num - i) - 1].b / 255.0f, 1.0f);
//
//            gl.glVertexPointer(3, GL10.GL_FIXED, 0, vertexBuffer);
//            gl.glTexCoordPointer(2, GL10.GL_FIXED, 0, coordBuffer);
//            gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
//            // 绘制
//            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
//            Log.e(TAG, String.format("second gldraw aray 0x%X", gl.glGetError()));
            // 关闭顶点、颜色和纹理
            gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
//            gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

            spin += 0.01f; // 星星的公转
            star[i].angle += (float) (i) / (float) num; // 改变星星的自转角度
            star[i].dist -= 0.01f; // 改变星星离中心的距离

            if (star[i].dist < 0.0f) // 星星到达中心了么
            {
                // 往外移5个单位
                star[i].dist += 5.0f;
                // 赋一个新红色分量
                star[i].r = random.nextInt(256);
                // 赋一个新绿色分量
                star[i].g = random.nextInt(256);
                // 赋一个新蓝色分量
                star[i].b = random.nextInt(256);
            }
        }
        gl.glEnable(GL10.GL_CULL_FACE);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
        // 设置观察模型
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
        // 黑色背景色
        gl.glClearColorx(0, 0, 0, 0);
        // 启用阴影平滑
        gl.glShadeModel(GL10.GL_SMOOTH);

//         启用深度测试
        gl.glEnable(GL10.GL_DEPTH_TEST);
        // 深度测试类型
        gl.glDepthFunc(GL10.GL_LEQUAL);
        // 设置深度缓存
        gl.glClearDepthf(1.0f);

        // 启用纹理
        gl.glEnable(GL10.GL_TEXTURE_2D);
        // 生成纹理
        gl.glGenTextures(1, textures, 0);
        // 绑定纹理
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, Load.bitmap, 0);
        Log.e(TAG, String.format("load texture 0x%x", gl.glGetError()));

        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE); // 设置混色函数取得半透明效果
        gl.glEnable(GL10.GL_BLEND); // 启用混色

        // loop all star
        for (int i = 0; i < num; i++) {
            NiceStar starTmp = new NiceStar();
            // all star from 0 angle
            starTmp.angle = 0.0f;
            // calc star 距离
            starTmp.dist = ((float) (i) / (float) num) * 5.0f;
            // set red
            starTmp.r = random.nextInt(256);
            // set green
            starTmp.g = random.nextInt(256);
            // set blue
            starTmp.b = random.nextInt(256);

            star[i] = starTmp;
        }
    }
}

class NiceStar {
    // star颜色
    int r, g, b;
    // star距中心距离
    float dist;
    // current star angle
    float angle = 0.0f;
}

