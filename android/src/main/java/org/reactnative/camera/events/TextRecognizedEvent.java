package org.reactnative.camera.events;

import android.support.v4.util.Pools;
import android.util.SparseArray;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.google.android.cameraview.CameraView;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import org.reactnative.camera.CameraViewManager;
import org.reactnative.camera.utils.ImageDimensions;
import org.reactnative.facedetector.FaceDetectorUtils;


public class TextRecognizedEvent extends Event<TextRecognizedEvent> {

  private static final Pools.SynchronizedPool<TextRecognizedEvent> EVENTS_POOL =
      new Pools.SynchronizedPool<>(3);


  private double mScaleX;
  private double mScaleY;
  private SparseArray<TextBlock> mTextBlocks;
  private ImageDimensions mImageDimensions;

  private TextRecognizedEvent() {}

  public static TextRecognizedEvent obtain(
      int viewTag,
      SparseArray<TextBlock> textBlocks,
      ImageDimensions dimensions,
      double scaleX,
      double scaleY) {
    TextRecognizedEvent event = EVENTS_POOL.acquire();
    if (event == null) {
      event = new TextRecognizedEvent();
    }
    event.init(viewTag, textBlocks, dimensions, scaleX, scaleY);
    return event;
  }

  private void init(
      int viewTag,
      SparseArray<TextBlock> textBlocks,
      ImageDimensions dimensions,
      double scaleX,
      double scaleY) {
    super.init(viewTag);
    mTextBlocks = textBlocks;
    mImageDimensions = dimensions;
    mScaleX = scaleX;
    mScaleY = scaleY;
  }

  @Override
  public String getEventName() {
    return CameraViewManager.Events.EVENT_ON_TEXT_RECOGNIZED.toString();
  }

  @Override
  public void dispatch(RCTEventEmitter rctEventEmitter) {
    rctEventEmitter.receiveEvent(getViewTag(), getEventName(), serializeEventData());
  }

  private WritableMap serializeEventData() {
    WritableArray textBlocksList = Arguments.createArray();
    for (int i = 0; i < mTextBlocks.size(); ++i) {
      TextBlock textBlock = mTextBlocks.valueAt(i);
      WritableMap serializedTextBlock = serializeText(textBlock);
      if (mImageDimensions.getFacing() == CameraView.FACING_FRONT) {
        serializedTextBlock = rotateTextX(serializedTextBlock);
      }
      textBlocksList.pushMap(serializedTextBlock);
    }

    WritableMap event = Arguments.createMap();
    event.putString("type", "textBlock");
    event.putArray("textBlocks", textBlocksList);
    event.putInt("target", getViewTag());
    return event;
  }

  private WritableMap serializeText(Text text) {
    WritableMap encodedText = Arguments.createMap();

    WritableArray components = Arguments.createArray();
    for (Text component : text.getComponents()) {
      components.pushMap(serializeText(component));
    }
    encodedText.putArray("components", components);

    encodedText.putString("value", text.getValue());

    WritableMap origin = Arguments.createMap();
    origin.putDouble("x", text.getBoundingBox().left * this.mScaleX);
    origin.putDouble("y", text.getBoundingBox().top * this.mScaleY);

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
