package org.reactnative.facedetector;

import android.content.Context;

import org.reactnative.camera.utils.ImageDimensions;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import org.reactnative.frame.RNFrame;

import java.util.List;

public class RNFaceDetector {
  public static int ALL_CLASSIFICATIONS = FaceDetectorOptions.CLASSIFICATION_MODE_ALL;
  public static int NO_CLASSIFICATIONS = FaceDetectorOptions.CLASSIFICATION_MODE_NONE;
  public static int ALL_LANDMARKS = FaceDetectorOptions.LANDMARK_MODE_ALL;
  public static int NO_LANDMARKS = FaceDetectorOptions.LANDMARK_MODE_NONE;
  public static int ACCURATE_MODE = FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE;
  public static int FAST_MODE = FaceDetectorOptions.PERFORMANCE_MODE_FAST;

  private FaceDetector mFaceDetector = null;
  private ImageDimensions mPreviousDimensions;
  private FaceDetectorOptions.Builder mBuilder = null;

  private int mClassificationType = NO_CLASSIFICATIONS;
  private int mLandmarkType = NO_LANDMARKS;
  private float mMinFaceSize = 0.15f;
  private int mMode = FAST_MODE;

  public RNFaceDetector(Context context) {
    mBuilder = new FaceDetectorOptions.Builder();
    mBuilder.setMinFaceSize(mMinFaceSize);
    mBuilder.setPerformanceMode(mMode);
    mBuilder.setLandmarkMode(mLandmarkType);
    mBuilder.setClassificationMode(mClassificationType);
  }

  // Public API

  public boolean isOperational() {
    if (mFaceDetector == null) {
      createFaceDetector();
    }

    return true;
  }

  public List<Face> detect(RNFrame frame) {
    // If the frame has different dimensions, create another face detector.
    // Otherwise we will get nasty "inconsistent image dimensions" error from detector
    // and no face will be detected.
    if (!frame.getDimensions().equals(mPreviousDimensions)) {
      releaseFaceDetector();
    }

    if (mFaceDetector == null) {
      createFaceDetector();
      mPreviousDimensions = frame.getDimensions();
    }

    return mFaceDetector.process(frame.getFrame()).getResult();
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

  public void release() {
    releaseFaceDetector();
    mPreviousDimensions = null;
  }

  // Lifecycle methods

  private void releaseFaceDetector() {
    if (mFaceDetector != null) {
      mFaceDetector.close();
      mFaceDetector = null;
    }
  }

  private void createFaceDetector() {
    mFaceDetector = FaceDetection.getClient(mBuilder.build());
  }
}
