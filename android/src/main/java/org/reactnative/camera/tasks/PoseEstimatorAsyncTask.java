package org.reactnative.camera.tasks;

import android.graphics.Bitmap;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;

import org.reactnative.camera.PoseEstimator;
import org.reactnative.camera.BodyPoints;

import static org.reactnative.camera.utils.ImageFormatConverter.convertYUV420_NV21toRGB8888;

public class PoseEstimatorAsyncTask extends android.os.AsyncTask<Void, Void, BodyPoints> {
    private PoseEstimator mPoseEstimator;
    private PoseEstimatorAsyncTaskDelegate mDelegate;
    //private Bitmap mbitmap;
    private byte[] mImageData;
    private int mWidth;
    private int mHeight;
    private int mRotation;

    public PoseEstimatorAsyncTask(
            PoseEstimatorAsyncTaskDelegate delegate,
            PoseEstimator poseEstimator,
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
    protected BodyPoints doInBackground(Void... ignored) {
        if (isCancelled() || mDelegate == null || mPoseEstimator == null) {
            return null;
        }

        // Seems like Android's 'ScriptIntrinsicYuvToRGB' would be a preferred solution.
        int[] bytesInRGB = convertYUV420_NV21toRGB8888(mImageData, mWidth, mHeight);
        Bitmap bmp = Bitmap.createBitmap(bytesInRGB, 192, 192, Bitmap.Config.ARGB_8888);

        mPoseEstimator.classifyFrame(bmp);
        return new BodyPoints(mPoseEstimator.bodyPoints);
    }

    @Override
    protected void onPostExecute(BodyPoints bodyPoints) {
        super.onPostExecute(bodyPoints);

        if (bodyPoints != null) {
            mDelegate.onPoseEstimated(serializeEventData(bodyPoints));
        }
        mDelegate.onPoseEstimatorTaskCompleted();
    }

    private WritableArray serializeEventData(BodyPoints bodyPoints) {
        return Arguments.makeNativeArray(bodyPoints.getBodyPoints());
    }
}
