package org.reactnative.camera.tasks;

import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.google.android.cameraview.CameraView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetector;

import org.reactnative.camera.utils.ImageDimensions;
import org.reactnative.facedetector.FaceDetectorUtils;
import org.reactnative.facedetector.RNFaceDetector;

import java.util.List;

public class FaceDetectorAsyncTask extends android.os.AsyncTask<Void, Void, Void> {
  private byte[] mImageData;
  private int mWidth;
  private int mHeight;
  private int mRotation;
  private RNFaceDetector mFaceDetector;
  private FaceDetectorAsyncTaskDelegate mDelegate;
  private double mScaleX;
  private double mScaleY;
  private ImageDimensions mImageDimensions;
  private int mPaddingLeft;
  private int mPaddingTop;
  private String TAG = "RNCamera";

  public FaceDetectorAsyncTask(
      FaceDetectorAsyncTaskDelegate delegate,
      RNFaceDetector faceDetector,
      byte[] imageData,
      int width,
      int height,
      int rotation,
      float density,
      int facing,
      int viewWidth,
      int viewHeight,
      int viewPaddingLeft,
      int viewPaddingTop
  ) {
    mImageData = imageData;
    mWidth = width;
    mHeight = height;
    mRotation = rotation;
    mDelegate = delegate;
    mFaceDetector = faceDetector;
    mImageDimensions = new ImageDimensions(width, height, rotation, facing);
    mScaleX = (double) (viewWidth) / (mImageDimensions.getWidth() * density);
    mScaleY = (double) (viewHeight) / (mImageDimensions.getHeight() * density);
    mPaddingLeft = viewPaddingLeft;
    mPaddingTop = viewPaddingTop;
  }

  @Override
  protected Void doInBackground(Void... ignored) {
    if (isCancelled() || mDelegate == null || mFaceDetector == null) {
      return null;
    }
    InputImage image = InputImage.fromByteArray(mImageData, mWidth, mHeight, getFirebaseRotation(), InputImage.IMAGE_FORMAT_YV12);

    FaceDetector detector = mFaceDetector.getDetector();
    detector.process(image)
            .addOnSuccessListener(
                    new OnSuccessListener<List<Face>>() {
                      @Override
                      public void onSuccess(List<Face> faces) {
                        WritableArray facesList = serializeEventData(faces);
                        mDelegate.onFacesDetected(facesList);
                        mDelegate.onFaceDetectingTaskCompleted();
                      }
                    })
            .addOnFailureListener(
                    new OnFailureListener() {
                      @Override
                      public void onFailure(Exception e) {
                        Log.e(TAG, "Text recognition task failed" + e);
                        mDelegate.onFaceDetectingTaskCompleted();
                      }
                    });
    return null;
  }

  private int getFirebaseRotation(){
    int result;
    switch (mRotation) {
      case 0:
        result = 0;
        break;
      case 90:
        result = 90;
        break;
      case 180:
        result = 180;
        break;
      case 270:
      case -90:
        result = 270;
        break;
      default:
        result = 0;
        Log.e(TAG, "Bad rotation value: " + mRotation);
    }
    return result;
  }

  private WritableArray serializeEventData(List<Face> faces) {
    WritableArray facesList = Arguments.createArray();

    for (Face face : faces) {
      WritableMap serializedFace = FaceDetectorUtils.serializeFace(face, mScaleX, mScaleY, mWidth, mHeight, mPaddingLeft, mPaddingTop);
      if (mImageDimensions.getFacing() == CameraView.FACING_FRONT) {
        serializedFace = FaceDetectorUtils.rotateFaceX(serializedFace, mImageDimensions.getWidth(), mScaleX);
      } else {
        serializedFace = FaceDetectorUtils.changeAnglesDirection(serializedFace);
      }
      facesList.pushMap(serializedFace);
    }

    return facesList;
  }

}
