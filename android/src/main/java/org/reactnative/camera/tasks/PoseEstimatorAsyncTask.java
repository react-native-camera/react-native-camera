package org.reactnative.camera.tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.vision.Frame;

import org.reactnative.camera.PoseEstimatorModule;
import org.reactnative.camera.PoseHeatmap;
import org.reactnative.frame.RNFrame;
import org.reactnative.frame.RNFrameFactory;

import static org.reactnative.camera.utils.ImageFormatConverter.convertYUV420_NV21toRGB8888;

public class PoseEstimatorAsyncTask extends android.os.AsyncTask<Void, Void, PoseHeatmap> {
    private PoseEstimatorModule mPoseEstimator;
    private PoseEstimatorAsyncTaskDelegate mDelegate;
    //private Bitmap mbitmap;
    private byte[] mImageData;
    private int mWidth;
    private int mHeight;
    private int mRotation;

    public PoseEstimatorAsyncTask(
            PoseEstimatorAsyncTaskDelegate delegate,
            PoseEstimatorModule poseEstimator,
            byte[] imageData,
            int width,
            int height,
            int rotation
    ) {
        mDelegate = delegate;
        mPoseEstimator = poseEstimator;
        mImageData = imageData;
        mWidth = width;
        mHeight = height;
        mRotation = rotation;
    }

    @Override
    protected PoseHeatmap doInBackground(Void... ignored) {
        if (isCancelled() || mDelegate == null || mPoseEstimator == null) {
            return null;
        }

        // Seems like Android's 'ScriptIntrinsicYuvToRGB' would be a preferred solution.
        int[] bytesInRGB = convertYUV420_NV21toRGB8888(mImageData, mWidth, mHeight);
        Bitmap bmp = Bitmap.createBitmap(bytesInRGB, mWidth, mHeight, Bitmap.Config.ARGB_8888);

        mPoseEstimator.run(bmp);
        return new PoseHeatmap(mPoseEstimator.Output);
    }

    @Override
    protected void onPostExecute(PoseHeatmap poseHeatmap) {
        super.onPostExecute(poseHeatmap);

        if (poseHeatmap != null) {
            mDelegate.onPoseEstimated(serializeEventData(poseHeatmap));
        }
        mDelegate.onPoseEstimatorTaskCompleted();
    }

    private WritableArray serializeEventData(PoseHeatmap poseHeatmap) {
        return Arguments.makeNativeArray(poseHeatmap.getHeatmap());
    }
}
