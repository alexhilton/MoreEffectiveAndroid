package net.toughcoder.rs;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.toughcoder.effectiveandroid.R;
import net.toughcoder.effectiveandroid.ScriptC_press;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImagePressActivity extends ActionBarActivity {
    // Input bitmap object, serves as a single source for the image-processing kernel.
    private Bitmap inputBitmap;
    // Output bitmap object, serves as a destination for the image-processing kernel.
    private Bitmap outputBitmap;
    // Second bitmap object is required for the simple double-buffering scheme.
    // For example, the kernel outputs first frame to the outputBitmap, second to the outputBitmapStage,
    // then to outputBitmap again, and so on.
    private Bitmap outputBitmapStage;

    // Single ImageView that is used to output the resulting bitmap object.
    private ImageView outputImageView;
    // This is a dedicated (worker) thread for computing needs, required as
    // potentially long operations should be avoided in the GUI thread.
    private Thread backgroundThread;
    // A conditional that signals the worker thread that the application is active, which means it is
    // not minimized.
    private ConditionVariable isGoing;
    // A flag that signals the worker thread that the application is exiting, so it should wrap up all
    // the activities as well.
    private volatile boolean isShuttingDown = false;

    // A conditional that guards the compute and rendering stages of the pipeline so that the bitmap object
    // being processed by the image-processing kernel is not displayed until done.
    private ConditionVariable isRendering;
    // A counter that is responsible for expansion of the "circle of effect" that appears when you
    // touch the screen.
    private int stepCount;

    // These are the "snapshot" values for the touch event used in the GUI thread.
    private int xTouchUI;
    private int yTouchUI;
    private int stepTouchUI;

    // These are the touch coordinates to be applied in the image-processing filter.
    private int xTouchApply;
    private int yTouchApply;
    private int stepTouchApply;

    // RenderScript-specific properties:
    // RS context
    private RenderScript mRS;
    // "Glue" class that wraps access to the mScript.
    // The IDE generates the class automatically based on the mRS file, the class is located in the 'gen'
    // folder.
    private ScriptC_press mScript;
    // Allocations - memory abstractions that RenderScript kernels operate on.
    private Allocation allocationIn;
    private Allocation allocationOut;

    // Image file (when you take a shot with the camera) and it's path.
    private File image;
    private String newBitmapPath;

    // Pre-defined value to distinguish the camera-shot event, used in the Intents handler.
    private static final int CAMERA_SHOT = 1;

    // Time values in tickmarks, used for performance statistics.
    private long stepStart;
    private long stepEnd;
    private long prevFrameTimestamp;

    // Number of iterations over which the specific metrics are collected.
    private int itersAccum;
    // Frame time, accumulated over the number of iterations. Used for averaging the value.
    private long frameDurationAccum;
    // Image-processing kernel execution time, accumulated over the number of iterations. Used for
    // averaging the value.
    private long effectDurationAccum;
    // Threshold for the accumulated frames time after which the averaged FPS values are calculated
    // and reported.
    // Consider this approach rather than relying on the threshold for the number of the elapsed
    // frames (for example, 100 frames). The problem with relying on the number of frames
    // rather than time is that on a slow device getting 100 frames might take significant time.
    // Yet collecting statistics over smaller number (for example, 10 frames) might produce
    // volatile FPS values on fast devices. From this perspective, accumulating performance
    // statistics with the time threshold is more reliable.
    private static final long maxFrameDurationAccum = 500000000;

    // A few simple text views to output performance statistics.
    private TextView FPSLabel;
    private TextView frameDurationLabel;
    private TextView effectDurationLabel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_press);
        outputImageView = (ImageView) findViewById(R.id.outputImageView);
        FPSLabel = (TextView) findViewById(R.id.FPS);
        frameDurationLabel = (TextView) findViewById(R.id.FrameDuration);
        effectDurationLabel = (TextView) findViewById(R.id.EffectDuration);
        isGoing = new ConditionVariable(false);
        isRendering = new ConditionVariable(true);
        image = null;
        newBitmapPath = null;


        initRS();

        Button buttonPhoto = (Button) findViewById(R.id.PhotoButton);
        int anyCamera = Camera.getNumberOfCameras();
        if (anyCamera == 0)
            buttonPhoto.setVisibility(View.INVISIBLE);

        // Reset the touch state as if you have not touched the screen yet.
        resetTouch();
    }

    @Override
    protected void onDestroy() {
        Log.i("AndroidBasic", "onDestroy");

        // Tell the background thread that the application is closing.
        // You communicate with the thread via the isShuttingDown variable.
        isShuttingDown = true;
        isGoing.open();
        super.onDestroy();
        destroyRS();
    }

    protected void initRS() {
        // Initialize the RenderScript context.
        mRS = RenderScript.create(this);
        // Create the specific mScript, actually the bitcode of the mScript itself is located in the
        // resources (raw folder).
        mScript = new ScriptC_press(mRS);
    }

    private void destroyRS() {
        if (mScript != null) {
            mScript.destroy();
            mScript = null;
        }
        if (mRS != null) {
            mRS.destroy();
            mRS = null;
        }
    }

    public void photoOnClick(View v) {
        Intent cameraShotIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        mediaFolder.mkdir();
        // Notice that for the sake of simplicity the activity orientation is set to landscape.
        // Operate the camera accordingly - capture photos also in the plain landscape mode only.

        image = new File(mediaFolder, timeStamp + ".jpg");
        Log.i("AndroidBasic", "file name for the intent: " + Uri.fromFile(mediaFolder) + "/" + timeStamp + ".jpg");
        try {
            if (image.createNewFile()) {
                cameraShotIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
                if (cameraShotIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(cameraShotIntent, CAMERA_SHOT);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_SHOT) {
            if (resultCode != RESULT_OK) {
                image.delete();
                return;
            }
            // Reset the previous touches.
            resetTouch();

            newBitmapPath = image.getAbsolutePath();
            Log.i("AndroidBasic", "newBitmapPath: " + newBitmapPath);

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(image);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Unleash the "worker" thread.
        isGoing.open();
        Log.i("AndroidBasic", "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Stop the background filtering thread.
        isGoing.close();
        Log.i("AndroidBasic", "onStop");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        Log.i("AndroidBasic", "onWindowFocusChanged");
        // When the application is restarted, which means the inputBitmap doesn't exist,
        // load inputBitmap first and (re)start the worker thread.
        // If the inputBitmap already exists, the application is likely to resume from the minimized state.
        if (inputBitmap == null) {
            String fromResources = null;
            loadInputImage(fromResources);
            startBackgroundThread();
        }
    }

    private void loadInputImage(String path) {
        // To avoid potential issues with loading big images, scale the input image to fit the output view.
        // Obtain the actual dimensions from the outputImageView.
        int displayWidth = outputImageView.getWidth();
        int displayHeight = outputImageView.getHeight();
        Log.i("AndroidBasic", "display dimensions: " + displayWidth + ", " + displayHeight);

        // Obtain the original dimensions of the input picture from resources.
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;    // This avoids the decoding itself and reads the image statistics.

        if (path == null) {
            BitmapFactory.decodeResource(getResources(), R.drawable.picture, options);
        } else {
            BitmapFactory.decodeFile(path, options);
        }

        int origWidth = options.outWidth;
        int origHeight = options.outHeight;

        // According to the display and the original dimensions, calculate the scale factor that reduces
        // the amount of memory needed to store an image, and, at the same time, is not too high to avoid
        // significant image quality loss.
        options.inSampleSize = Math.min(origWidth / displayWidth, origHeight / displayHeight);

        // Now decode the real picture content with scaling.
        options.inJustDecodeBounds = false;
        if (path == null) {
            inputBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.picture, options);
        } else {
            inputBitmap = BitmapFactory.decodeFile(path, options);
        }

        inputBitmap = Bitmap.createScaledBitmap(inputBitmap, displayWidth, displayHeight, false);

        // Create an allocation (which is memory abstraction in the RenderScript)
        // that corresponds to the imputBitmap.
        allocationIn = Allocation.createFromBitmap(
                mRS,
                inputBitmap,
                Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT
        );
        // Create another allocation (with the same type and dimensions as allocationIn) to be used
        // as a source to update the outputBitmap.
        allocationOut = Allocation.createTyped(mRS, allocationIn.getType());
        // Starting Android API level 18 and higher, you can create the allocationOut directly from the
        // Bitmap via CreateFromBitmap()
        // Use the dedicated USAGE_SHARED flag, as with this flag copying to or from the bitmap
        // causes a synchronization rather than a full copy.
        // Also use syncAll(USAGE_SHARED) to synchronize the Allocation and the source Bitmap
        // rather than the current copyTo, refer to the stepRenderScript method
        // Notice that you would need to use a second allocation (and swap them similarly to bitmaps)

        int imageWidth = inputBitmap.getWidth();
        int imageHeight = inputBitmap.getHeight();
        // Two bitmap objects for the simple double-buffering scheme, where first bitmap object is rendered,
        // while the second one is being updated, then vice versa.
        outputBitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
        outputBitmapStage = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
    }

    private void startBackgroundThread() {
        // Create a thread that periodically calls the filter method of this activity.
        backgroundThread = new Thread(new Runnable() {
            public void run() {
                while (!isShuttingDown) {
                    // Check that the application is not in the minimized state.
                    isGoing.block();
                    // Guard for flip-flopping the buffers so that the GUI thread does not try displaying a
                    // not updated bitmap object.
                    isRendering.block();
                    // If you need to update the image, it is a good place (in a dedicated thread) -
                    // in a synchronized way.
                    if (newBitmapPath != null) {
                        loadInputImage(newBitmapPath);
                        newBitmapPath = null;

                    }

                    Log.i("AndroidBasic", "beforeStep");
                    // Swap target and staging bitmap objects.
                    Bitmap t = outputBitmap;
                    outputBitmap = outputBitmapStage;
                    outputBitmapStage = t;

                    stepStart = System.nanoTime();
                    step();
                    stepEnd = System.nanoTime();

                    Log.i("AndroidBasic", "afterStep");
                    // Prevent the background thread from computing next frame to the same bitmap object.
                    isRendering.close();
                    outputImageView.post(new Runnable() {
                                             public void run() {
                                                 // Snapshot of the touch-event parameters that should be applied
                                                 // until a new touch event happens.
                                                 // In the GUI thread copy parameters into a separated variables
                                                 // to guarantee the parameters are changed "atomically"
                                                 // (simultaneously) for the worker thread.
                                                 xTouchApply = xTouchUI;
                                                 yTouchApply = yTouchUI;
                                                 stepTouchApply = stepTouchUI;

                                                 // Update the performance statistics.
                                                 updatePerformanceStats();


                                                 Log.i("AndroidBasic", "setImageBitmap and invalidate");
                                                 outputImageView.setImageBitmap(outputBitmap);
                                                 outputImageView.invalidate();
                                                 // Enable the background thread to compute the next frame.
                                                 isRendering.open();
                                             }
                                         }
                    );
                }
                Log.i("AndroidBasic", "Exiting backgroundThread");
            }
        });

        backgroundThread.start();
    }

    private void updatePerformanceStats() {
        long curFrameTimestamp = System.nanoTime();

        if (prevFrameTimestamp != -1) {
            // Calculate the current frame duration value.
            long frameDuration = curFrameTimestamp - prevFrameTimestamp;
            long effectDuration = stepEnd - stepStart;
            frameDurationAccum += frameDuration;
            effectDurationAccum += effectDuration;
            itersAccum++;

            if (frameDurationAccum > maxFrameDurationAccum) {
                frameDuration = frameDurationAccum / itersAccum;
                effectDuration = effectDurationAccum / itersAccum;
                frameDurationAccum = 0;
                effectDurationAccum = 0;
                itersAccum = 0;

                FPSLabel.setText((float) (int) ((1e9f / frameDuration) * 10) / 10 + " FPS");
                frameDurationLabel.setText("Frame: " + frameDuration / 1000000 + " ms");
                effectDurationLabel.setText("Effect:  " + effectDuration / 1000000 + " ms");
            }
        }

        prevFrameTimestamp = curFrameTimestamp;
    }

    // This method runs in a separate working thread.
    private void step() {
        Log.i("AndroidBasic", "step");

        stepRenderScript();

        stepCount++;
    }

    private void stepRenderScript() {
        if (mScript == null || mRS == null) {
            return;
        }
        // Compute the parameters (for example, the "circle of effect") depending on the number of the
        // elapsed steps.
        int radius = (stepTouchApply == -1 ? -1 : 10 * (stepCount - stepTouchApply));
        int radiusHi = (radius + 2) * (radius + 2);
        int radiusLo = (radius - 2) * (radius - 2);
        // Setting parameters for the mScript.
        mScript.set_radiusHi(radiusHi);
        mScript.set_radiusLo(radiusLo);
        mScript.set_xTouchApply(xTouchApply);
        mScript.set_yTouchApply(yTouchApply);

        // Run the mScript.
        mScript.forEach_root(allocationIn, allocationOut);
        // For the API level 17 and earlier: explicit copy of results to the output bitmap for displaying
        allocationOut.copyTo(outputBitmap);
        // For the API level 18 and higher notice that you would
        // need another allocation to match outBitmapStage and swap the allocations similarly to bitmaps
        // (search for the "beforeStep" string in the code)
        //Wait for completion
        //mRS.finish();
        // Let the bitmap know the results
        //allocationOut.syncAll(Allocation.USAGE_SHARED);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            xTouchUI = (int) (event.getX());
            yTouchUI = (int) (event.getY());

            stepTouchUI = stepCount;
            Log.i("AndroidBasic", "x = " + event.getX() + ", y = " + event.getY());
        }

        return super.onTouchEvent(event);
    }

    private void resetTouch() {
        stepTouchUI = stepTouchApply = -1;
    }
}
