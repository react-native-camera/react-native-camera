package org.reactnative.camera.tasks;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;

import org.reactnative.camera.utils.ImageDimensions;

import java.util.List;


public class TextRecognizerAsyncTask {

  private TextRecognizerAsyncTaskDelegate mDelegate;
  private ThemedReactContext mThemedReactContext;
  private byte[] mImageData;
  private int mWidth;
  private int mHeight;
  private int mRotation;
  private ImageDimensions mImageDimensions;
  private double mScaleX;
  private double mScaleY;
  private int mPaddingLeft;
  private int mPaddingTop;

  public TextRecognizerAsyncTask(
          TextRecognizerAsyncTaskDelegate delegate,
          ThemedReactContext themedReactContext,
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
    mDelegate = delegate;
    mThemedReactContext = themedReactContext;
    mImageData = imageData;
    mWidth = width;
    mHeight = height;
    mRotation = rotation;
    mImageDimensions = new ImageDimensions(width, height, rotation, facing);
    mScaleX = (double) (viewWidth) / (mImageDimensions.getWidth() * density);
    mScaleY = (double) (viewHeight) / (mImageDimensions.getHeight() * density);
    mPaddingLeft = viewPaddingLeft;
    mPaddingTop = viewPaddingTop;
  }

  private WritableMap serializeText(String value) {
    WritableMap encodedText = Arguments.createMap();
    encodedText.putString("value", value);
    return encodedText;
  }

  public void recognizedText() {
    com.google.mlkit.vision.text.TextRecognizer mTextRecognizer;
    mTextRecognizer = TextRecognition.getClient();

    InputImage image = InputImage.fromByteArray(
            mImageData,
            /* image width */mWidth,
            /* image height */mHeight,
            mRotation,
            InputImage.IMAGE_FORMAT_NV21 // or IMAGE_FORMAT_YV12
    );

    Task<com.google.mlkit.vision.text.Text> result =
            mTextRecognizer.process(image)
                    .addOnSuccessListener(new OnSuccessListener<com.google.mlkit.vision.text.Text>() {
                      @Override
                      public void onSuccess(com.google.mlkit.vision.text.Text visionText) {
                        // Task completed successfully
                        // ...
                        List<com.google.mlkit.vision.text.Text.TextBlock> textBlocks = visionText.getTextBlocks();
                        if (textBlocks != null) {
                          WritableArray textBlocksList = Arguments.createArray();
                          for (int i = 0; i < textBlocks.size(); ++i) {
                            com.google.mlkit.vision.text.Text.TextBlock textBlock = textBlocks.get(i);
                            WritableMap serializedTextBlock = serializeText(textBlock.getText());
                            textBlocksList.pushMap(serializedTextBlock);
                          }
                          mDelegate.onTextRecognized(textBlocksList);
                        }
                        mDelegate.onTextRecognizerTaskCompleted();
                      }
                    })
                    .addOnFailureListener(
                            new OnFailureListener() {
                              @Override
                              public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                // ...
                                mDelegate.onTextRecognizerTaskCompleted();
                              }
                            });
  }


}
