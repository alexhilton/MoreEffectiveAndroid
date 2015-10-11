package net.toughcoder.opengl2s;

import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.toughcoder.effectiveandroid.R;
import net.toughcoder.oaqs.AirHockeyRenderer;

public class OpenGLExampleActivity extends ActionBarActivity {
    private GLSurfaceView mGLView;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
//        mGLView = new OpenGLES2SurfaceView(this);
//        mGLView = new GLSurfaceView(this);
//        final AirHockeyRenderer render = new AirHockeyRenderer(this);
//        mGLView.setEGLContextClientVersion(2);
//        mGLView.setRenderer(render);
//        mGLView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                final float normalizedX = (event.getX() / (float) v.getWidth()) * 2 - 1;
//                final float normalizedY = -((event.getY() / (float) v.getHeight()) * 2 -1);
//                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    mGLView.queueEvent(new Runnable() {
//                        @Override
//                        public void run() {
//                            render.handleTouchPress(normalizedX, normalizedY);
//                        }
//                    });
//                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
//                    mGLView.queueEvent(new Runnable() {
//                        @Override
//                        public void run() {
//                            render.handleTouchDrag(normalizedX, normalizedY);
//                        }
//                    });
//                }
//                return true;
//            }
//        });
        mGLView = new GLSurfaceView(this);
        int width = getResources().getDisplayMetrics().widthPixels;
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width, width);
        mGLView.setLayoutParams(params);
        StarRenderer renderer = new StarRenderer(this);
//        GLSurfaceView.Renderer renderer = new SunRenderer();
        mGLView.setEGLContextClientVersion(2);
        mGLView.setRenderer(renderer);
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.addView(mGLView);

        TextView label = new TextView(this);
        label.setText("Welcome");
        label.setTextSize(16);
        label.setTextColor(Color.BLACK);
        content.addView(label);
        setContentView(content);
        refresh();
    }

    private void refresh() {
        mGLView.requestRender();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        }, 300);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_open_glexample, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
