package org.reactnative.camera.tasks;

//import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.common.InputImage;

import org.reactnative.imagelabeler.RNImageLabeler;
import org.reactnative.camera.utils.ImageDimensions;

import java.util.List;

public class ImageLabelerAsyncTask extends android.os.AsyncTask<Void, Void, Void> {

  private byte[] mImageData;
  private int mWidth;
  private int mHeight;
  private int mRotation;
  private RNImageLabeler mImageLabeler;
  private ImageLabelerAsyncTaskDelegate mDelegate;
  private double mScaleX;
  private double mScaleY;
  private ImageDimensions mImageDimensions;
  private int mPaddingLeft;
  private int mPaddingTop;
  private String TAG = "RNCamera";

  public ImageLabelerAsyncTask(
      ImageLabelerAsyncTaskDelegate delegate,
      RNImageLabeler imageLabeler,
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
    mImageLabeler = imageLabeler;
    mImageDimensions = new ImageDimensions(width, height, rotation, facing);
    mScaleX = (double) (viewWidth) / (mImageDimensions.getWidth() * density);
    mScaleY = 1 / density;
    mPaddingLeft = viewPaddingLeft;
    mPaddingTop = viewPaddingTop;
  }

  @Override
  protected Void doInBackground(Void... ignored) {
    if (isCancelled() || mDelegate == null || mImageLabeler == null) {
      return null;
    }

    InputImage image = InputImage.fromByteArray(mImageData,
            mWidth,
            mHeight,
            mRotation,
            InputImage.IMAGE_FORMAT_YV12
    );

    ImageLabeler labeler = mImageLabeler.getDetector();
    labeler.process(image)
            .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
              @Override
              public void onSuccess(List<ImageLabel> labels) {
                WritableArray serializedLabels = serializeEventData(labels);
                mDelegate.onLabelsDetected(serializedLabels);
                mDelegate.onImageLabelingTaskCompleted();
              }
            })
            .addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(Exception e) {
                Log.e(TAG, "Text recognition task failed" + e);
                mDelegate.onImageLabelingTaskCompleted();
              }
            });
    return null;
  }

  private WritableArray serializeEventData(List<ImageLabel> labels) {
    WritableArray labelsList = Arguments.createArray();

    for (ImageLabel label: labels) {
      String text = label.getText();
      double confidence = (double) label.getConfidence();

      WritableMap serializedImageLabel = Arguments.createMap();
      serializedImageLabel.putString("text", text);
      serializedImageLabel.putDouble("confidence", confidence);
      labelsList.pushMap(serializedImageLabel);
    }

    return labelsList;
  }
}
