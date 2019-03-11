package org.reactnative.facedetector;

import android.content.Context;
import android.util.Log;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;


public class RNFaceDetector {
  public static int ALL_CLASSIFICATIONS = FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS;
  public static int NO_CLASSIFICATIONS = FirebaseVisionFaceDetectorOptions.NO_CLASSIFICATIONS;
  public static int ALL_LANDMARKS = FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS;
  public static int NO_LANDMARKS = FirebaseVisionFaceDetectorOptions.NO_LANDMARKS;
  public static int ACCURATE_MODE = FirebaseVisionFaceDetectorOptions.ACCURATE;
  public static int FAST_MODE = FirebaseVisionFaceDetectorOptions.FAST;
  // TODO contours detection is possible for MLKit-based face detector, implement this feature
  public static int ALL_CONTOURS = FirebaseVisionFaceDetectorOptions.ALL_CONTOURS;
  public static int NO_CONTOURS = FirebaseVisionFaceDetectorOptions.NO_CONTOURS;

  private FirebaseVisionFaceDetector mFaceDetector = null;
  private FirebaseVisionFaceDetectorOptions.Builder mBuilder;

  private int mClassificationType = NO_CLASSIFICATIONS;
  private int mLandmarkType = NO_LANDMARKS;
  private float mMinFaceSize = 0.15f;
  private int mMode = FAST_MODE;

  public RNFaceDetector(Context context) {
    mBuilder = new FirebaseVisionFaceDetectorOptions.Builder()
            .setPerformanceMode(mMode)
            .setLandmarkMode(mLandmarkType)
            .setClassificationMode(mClassificationType)
            .setMinFaceSize(mMinFaceSize);
  }

  public boolean isOperational() {
    // Legacy api from GMV
    return true;
  }

  public FirebaseVisionFaceDetector getDetector() {

    if (mFaceDetector == null) {
      createFaceDetector();
    }
    return mFaceDetector;
  }

  public void setTracking(boolean trackingEnabled) {
    release();
    if (trackingEnabled) {
      mBuilder.enableTracking();
    }
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

  public void setTrackingEnabled(boolean tracking) {
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
    FirebaseVisionFaceDetectorOptions options = mBuilder.build();
    mFaceDetector = FirebaseVision.getInstance().getVisionFaceDetector(options);
  }
}
