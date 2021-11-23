package org.reactnative.facedetector;

import android.content.Context;
import android.util.Log;

import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetectorOptions;


public class RNFaceDetector {
  public static int ALL_CLASSIFICATIONS = FaceDetectorOptions.CLASSIFICATION_MODE_ALL;
  public static int NO_CLASSIFICATIONS = FaceDetectorOptions.CLASSIFICATION_MODE_NONE;
  public static int ALL_LANDMARKS = FaceDetectorOptions.LANDMARK_MODE_ALL;
  public static int NO_LANDMARKS = FaceDetectorOptions.LANDMARK_MODE_NONE;
  public static int ACCURATE_MODE = FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE;
  public static int FAST_MODE = FaceDetectorOptions.PERFORMANCE_MODE_FAST;
  // TODO contours detection is possible for MLKit-based face detector, implement this feature
  public static int ALL_CONTOURS = FaceDetectorOptions.CONTOUR_MODE_ALL;
  public static int NO_CONTOURS = FaceDetectorOptions.CONTOUR_MODE_NONE;

  private FaceDetector mFaceDetector = null;
  private FaceDetectorOptions.Builder mBuilder;

  private int mClassificationType = NO_CLASSIFICATIONS;
  private int mLandmarkType = NO_LANDMARKS;
  private float mMinFaceSize = 0.15f;
  private int mMode = FAST_MODE;

  public RNFaceDetector(Context context) {
    mBuilder = new FaceDetectorOptions.Builder()
            .setPerformanceMode(mMode)
            .setLandmarkMode(mLandmarkType)
            .setClassificationMode(mClassificationType)
            .setMinFaceSize(mMinFaceSize);
  }

  public boolean isOperational() {
    // Legacy api from GMV
    return true;
  }

  public FaceDetector getDetector() {

    if (mFaceDetector == null) {
      createFaceDetector();
    }
    return mFaceDetector;
  }

  public void setClassificationType(int classificationType) {
    if (classificationType != mClassificationType) {
      release();
      mBuilder.setClassificationMode(classificationType);
      mClassificationType = classificationType;
    }
  }

  public void setLandmarkType(int landmarkType) {
    if (landmarkType != mLandmarkType) {
      release();
      mBuilder.setLandmarkMode(landmarkType);
      mLandmarkType = landmarkType;
    }
  }

  public void setMode(int mode) {
    if (mode != mMode) {
      release();
      mBuilder.setPerformanceMode(mode);
      mMode = mode;
    }
  }

  public void setTracking(boolean tracking) {
    release();
    if (tracking) {
      mBuilder.enableTracking();
    }
  }

  public void release() {
    if (mFaceDetector != null) {
      try {
        mFaceDetector.close();
      } catch (Exception e) {
        Log.e("RNCamera", "Attempt to close FaceDetector failed");
      }
      mFaceDetector = null;
    }
  }

  private void createFaceDetector() {
    FaceDetectorOptions options = mBuilder.build();
    mFaceDetector = FaceDetection.getClient(options);
  }
}
