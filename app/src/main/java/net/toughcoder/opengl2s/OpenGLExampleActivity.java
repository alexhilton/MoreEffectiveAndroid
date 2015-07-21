package net.toughcoder.opengl2s;

import android.opengl.GLSurfaceView;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import net.toughcoder.effectiveandroid.R;
import net.toughcoder.oaqs.AirHockeyRenderer;

public class OpenGLExampleActivity extends ActionBarActivity {
    private GLSurfaceView mGLView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        StarRenderer renderer = new StarRenderer(this);
        mGLView.setEGLContextClientVersion(2);
        mGLView.setRenderer(renderer);
        setContentView(mGLView);
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
