package org.reactnative.camera.tasks;

import android.util.SparseArray;

import org.reactnative.facedetector.ExpoFaceDetector;
import org.reactnative.facedetector.ExpoFrame;
import org.reactnative.facedetector.ExpoFrameFactory;
import com.google.android.gms.vision.face.Face;

public class FaceDetectorAsyncTask extends android.os.AsyncTask<Void, Void, SparseArray<Face>> {
  private byte[] mImageData;
  private int mWidth;
  private int mHeight;
  private int mRotation;
  private ExpoFaceDetector mFaceDetector;
  private FaceDetectorAsyncTaskDelegate mDelegate;

  public FaceDetectorAsyncTask(
      FaceDetectorAsyncTaskDelegate delegate,
      ExpoFaceDetector faceDetector,
      byte[] imageData,
      int width,
      int height,
      int rotation
  ) {
    mImageData = imageData;
    mWidth = width;
    mHeight = height;
    mRotation = rotation;
    mDelegate = delegate;
    mFaceDetector = faceDetector;
  }

  @Override
  protected SparseArray<Face> doInBackground(Void... ignored) {
    if (isCancelled() || mDelegate == null || mFaceDetector == null || !mFaceDetector.isOperational()) {
      return null;
    }

    ExpoFrame frame = ExpoFrameFactory.buildFrame(mImageData, mWidth, mHeight, mRotation);
    return mFaceDetector.detect(frame);
  }

  @Override
  protected void onPostExecute(SparseArray<Face> faces) {
    super.onPostExecute(faces);

    if (faces == null) {
      mDelegate.onFaceDetectionError(mFaceDetector);
    } else {
      mDelegate.onFacesDetected(faces, mWidth, mHeight, mRotation);
      mDelegate.onFaceDetectingTaskCompleted();
    }
  }
}
