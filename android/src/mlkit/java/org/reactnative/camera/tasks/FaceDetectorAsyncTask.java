package org.reactnative.camera.tasks;

import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.google.android.cameraview.CameraView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;

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
    FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
            .setWidth(mWidth)
            .setHeight(mHeight)
            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_YV12)
            .setRotation(getFirebaseRotation())
            .build();
    FirebaseVisionImage image = FirebaseVisionImage.fromByteArray(mImageData, metadata);

    FirebaseVisionFaceDetector detector = mFaceDetector.getDetector();
    detector.detectInImage(image)
            .addOnSuccessListener(
                    new OnSuccessListener<List<FirebaseVisionFace>>() {
                      @Override
                      public void onSuccess(List<FirebaseVisionFace> faces) {
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
        result = FirebaseVisionImageMetadata.ROTATION_0;
        break;
      case 90:
        result = FirebaseVisionImageMetadata.ROTATION_90;
        break;
      case 180:
        result = FirebaseVisionImageMetadata.ROTATION_180;
        break;
      case 270:
        result = FirebaseVisionImageMetadata.ROTATION_270;
        break;
      case -90:
        result = FirebaseVisionImageMetadata.ROTATION_270;
        break;
      default:
        result = FirebaseVisionImageMetadata.ROTATION_0;
        Log.e(TAG, "Bad rotation value: " + mRotation);
    }
    return result;
  }

  private WritableArray serializeEventData(List<FirebaseVisionFace> faces) {
    WritableArray facesList = Arguments.createArray();

    for (FirebaseVisionFace face : faces) {
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
