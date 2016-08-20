/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.toughcoder.opengl.opengl2s;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import net.toughcoder.opengl.oaqs.AirHockeyRenderer;

/**
 * A view container where OpenGL ES graphics can be drawn on screen.
 * This view can also be used to capture touch events, such as a user
 * interacting with drawn objects.
 */
public class OpenGLES2SurfaceView extends GLSurfaceView {

//    private final OpenGLES2Render mRenderer;
    private final AirHockeyRenderer mRenderer;

    public OpenGLES2SurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);
        // Set the Renderer for drawing on the GLSurfaceView
//        mRenderer = new JayWayRenderer();
//        mRenderer = new OpenGLES2Render();
//        setRenderer(new JayWayRenderer());
        mRenderer = new AirHockeyRenderer(context);
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float normalizedX = (event.getX() / (float) getWidth()) * 2 - 1;
        final float normalizedY = -((event.getY() / (float) getHeight()) * 2 -1);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    mRenderer.handleTouchPress(normalizedX, normalizedY);
                }
            });
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    mRenderer.handleTouchDrag(normalizedX, normalizedY);
                }
            });
        }
        return true;
    }

}
