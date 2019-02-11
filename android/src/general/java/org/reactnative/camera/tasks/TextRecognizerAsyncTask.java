package org.reactnative.camera.tasks;

import android.util.SparseArray;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.google.android.cameraview.CameraView;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import org.reactnative.camera.utils.ImageDimensions;
import org.reactnative.facedetector.FaceDetectorUtils;
import org.reactnative.frame.RNFrame;
import org.reactnative.frame.RNFrameFactory;


public class TextRecognizerAsyncTask extends android.os.AsyncTask<Void, Void, SparseArray<TextBlock>> {

  private TextRecognizerAsyncTaskDelegate mDelegate;
  private ThemedReactContext mThemedReactContext;
  private TextRecognizer mTextRecognizer;
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

  @Override
  protected SparseArray<TextBlock> doInBackground(Void... ignored) {
    if (isCancelled() || mDelegate == null) {
      return null;
    }
    mTextRecognizer = new TextRecognizer.Builder(mThemedReactContext).build();
    RNFrame frame = RNFrameFactory.buildFrame(mImageData, mWidth, mHeight, mRotation);
    return mTextRecognizer.detect(frame.getFrame());
  }

  @Override
  protected void onPostExecute(SparseArray<TextBlock> textBlocks) {
    super.onPostExecute(textBlocks);
    if (mTextRecognizer != null) {
      mTextRecognizer.release();
    }
    if (textBlocks != null) {
      WritableArray textBlocksList = Arguments.createArray();
      for (int i = 0; i < textBlocks.size(); ++i) {
        TextBlock textBlock = textBlocks.valueAt(i);
        WritableMap serializedTextBlock = serializeText(textBlock);
        if (mImageDimensions.getFacing() == CameraView.FACING_FRONT) {
          serializedTextBlock = rotateTextX(serializedTextBlock);
        }
        textBlocksList.pushMap(serializedTextBlock);
      }
      mDelegate.onTextRecognized(textBlocksList);
    }
    mDelegate.onTextRecognizerTaskCompleted();
  }

  private WritableMap serializeText(Text text) {
    WritableMap encodedText = Arguments.createMap();

    WritableArray components = Arguments.createArray();
    for (Text component : text.getComponents()) {
      components.pushMap(serializeText(component));
    }
    encodedText.putArray("components", components);

    encodedText.putString("value", text.getValue());

    int x = text.getBoundingBox().left;
    int y = text.getBoundingBox().top;

    if (text.getBoundingBox().left < mWidth / 2) {
      x = x + mPaddingLeft / 2;
    } else if (text.getBoundingBox().left > mWidth /2) {
      x = x - mPaddingLeft / 2;
    }

    if (text.getBoundingBox().height() < mHeight / 2) {
      y = y + mPaddingTop / 2;
    } else if (text.getBoundingBox().height() > mHeight / 2) {
      y = y - mPaddingTop / 2;
    }

    WritableMap origin = Arguments.createMap();
    origin.putDouble("x", x * this.mScaleX);
    origin.putDouble("y", y * this.mScaleY);

    WritableMap size = Arguments.createMap();
    size.putDouble("width", text.getBoundingBox().width() * this.mScaleX);
    size.putDouble("height", text.getBoundingBox().height() * this.mScaleY);

    WritableMap bounds = Arguments.createMap();
    bounds.putMap("origin", origin);
    bounds.putMap("size", size);

    encodedText.putMap("bounds", bounds);

    String type_;
    if (text instanceof TextBlock) {
      type_ = "block";
    } else if (text instanceof Line) {
      type_ = "line";
    } else /*if (text instanceof Element)*/ {
      type_ = "element";
    }
    encodedText.putString("type", type_);

    return encodedText;
  }

  private WritableMap rotateTextX(WritableMap text) {
    ReadableMap faceBounds = text.getMap("bounds");

    ReadableMap oldOrigin = faceBounds.getMap("origin");
    WritableMap mirroredOrigin = FaceDetectorUtils.positionMirroredHorizontally(
            oldOrigin, mImageDimensions.getWidth(), mScaleX);

    double translateX = -faceBounds.getMap("size").getDouble("width");
    WritableMap translatedMirroredOrigin = FaceDetectorUtils.positionTranslatedHorizontally(mirroredOrigin, translateX);

    WritableMap newBounds = Arguments.createMap();
    newBounds.merge(faceBounds);
    newBounds.putMap("origin", translatedMirroredOrigin);

    text.putMap("bounds", newBounds);

    ReadableArray oldComponents = text.getArray("components");
    WritableArray newComponents = Arguments.createArray();
    for (int i = 0; i < oldComponents.size(); ++i) {
      WritableMap component = Arguments.createMap();
      component.merge(oldComponents.getMap(i));
      rotateTextX(component);
      newComponents.pushMap(component);
    }
    text.putArray("components", newComponents);

    return text;
  }

}
