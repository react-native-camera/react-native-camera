package org.reactnative.camera.tasks;

import android.util.SparseArray;

import org.reactnative.facedetector.RNFaceDetector;
import org.reactnative.facedetector.RNFrame;
import org.reactnative.facedetector.RNFrameFactory;
import com.google.android.gms.vision.face.Face;

public class FaceDetectorAsyncTask extends android.os.AsyncTask<Void, Void, SparseArray<Face>> {
  private byte[] mImageData;
  private int mWidth;
  private int mHeight;
  private int mRotation;
  private RNFaceDetector mFaceDetector;
  private FaceDetectorAsyncTaskDelegate mDelegate;

  public FaceDetectorAsyncTask(
      FaceDetectorAsyncTaskDelegate delegate,
      RNFaceDetector faceDetector,
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

    RNFrame frame = RNFrameFactory.buildFrame(mImageData, mWidth, mHeight, mRotation);
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
