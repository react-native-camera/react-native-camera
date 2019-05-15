package org.reactnative.camera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.TextureView;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;

import java.io.IOException;
import java.util.function.Consumer;

public class PoseEstimatorRunner {

    private boolean runClassifier = false;
    private PoseEstimator poseEstimator;
    private final String TAG = "PoseEstimatorRunner";
    private static final String HANDLE_THREAD_NAME = "CameraBackground";
    private final Object lock = new Object();

    /** An additional thread for running tasks that shouldn't block the UI. */
    private HandlerThread backgroundThread;

    /** A {@link Handler} for running tasks in the background. */
    private Handler backgroundHandler;

    private TextureView textureView;

    private Consumer<WritableArray> onPoseEstimated;

    public PoseEstimatorRunner(Activity activity, TextureView textureView, Consumer<WritableArray> onPoseEstimated){

        assert textureView != null;
        this.textureView = textureView;

        try {
            poseEstimator = new PoseEstimatorTFLite(activity);
        } catch (IOException e){
            Log.e(TAG, "PoseEstimatorTFLite could not be initialized: " + e);
        }
        this.onPoseEstimated = onPoseEstimated;
    }

    public void startPoseEstimationInBackground(){
        startBackgroundThread();
        backgroundHandler.post(() -> poseEstimator.useGpu());
    }

    public void stopPoseEstimationInBackground(){

        stopBackgroundThread();

        // Close TFLite to free up resources. Might be better to keep it, so we do not need to reload the model.
        poseEstimator.close();
    }

    /** Starts a background thread and its {@link Handler}. */
    private void startBackgroundThread() {
        backgroundThread = new HandlerThread(HANDLE_THREAD_NAME);
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
        // Start the classification train & load an initial model.
        synchronized (lock) {
            runClassifier = true;
        }
        backgroundHandler.post(periodicClassify);
    }

    /** Stops the background thread and its {@link Handler}. */
    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
            synchronized (lock) {
                runClassifier = false;
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted when stopping background thread", e);
        }
    }

    /** Takes photos and classify them periodically. */
    private Runnable periodicClassify =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier) {
                            classifyFrame();
                        }
                    }
                    backgroundHandler.post(periodicClassify);
                }
            };

    /** Classifies a frame from the preview stream and sends an event to RN */
    private void classifyFrame() {
        if (poseEstimator == null || textureView == null) {
            return;
        }
        Bitmap bitmap = textureView.getBitmap(poseEstimator.getImageSizeX(), poseEstimator.getImageSizeY());
        poseEstimator.classifyFrame(bitmap);
        sendPoseEstimatedEvent(new BodyPoints(poseEstimator.getHeatmap()));
        bitmap.recycle();
    }

    private void sendPoseEstimatedEvent(BodyPoints bodyPoints){
        WritableArray serializedEventData = Arguments.makeNativeArray(bodyPoints.getBodyPoints());
        onPoseEstimated.accept(serializedEventData);
    }
}
