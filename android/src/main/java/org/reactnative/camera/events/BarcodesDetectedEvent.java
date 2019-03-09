package org.reactnative.camera.events;

import android.graphics.Rect;
import android.support.v4.util.Pools;
import android.util.SparseArray;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.google.android.gms.vision.barcode.Barcode;

import org.reactnative.barcodedetector.BarcodeFormatUtils;
import org.reactnative.camera.CameraViewManager;

public class BarcodesDetectedEvent extends Event<BarcodesDetectedEvent> {

  private static final Pools.SynchronizedPool<BarcodesDetectedEvent> EVENTS_POOL =
      new Pools.SynchronizedPool<>(3);

  private SparseArray<Barcode> mBarcodes;
  private int mSourceWidth;
  private int mSourceHeight;
  private int mSourceRotation;

  private BarcodesDetectedEvent() {
  }

  public static BarcodesDetectedEvent obtain(
      int viewTag,
      SparseArray<Barcode> barcodes,
      int sourceWidth,
      int sourceHeight,
      int sourceRotation
  ) {
    BarcodesDetectedEvent event = EVENTS_POOL.acquire();
    if (event == null) {
      event = new BarcodesDetectedEvent();
    }
    event.init(viewTag, barcodes, sourceWidth, sourceHeight, sourceRotation);
    return event;
  }

  private void init(
      int viewTag,
      SparseArray<Barcode> barcodes,
      int sourceWidth,
      int sourceHeight,
      int sourceRotation
  ) {
    super.init(viewTag);
    mBarcodes = barcodes;
    mSourceWidth = sourceWidth;
    mSourceHeight= sourceHeight;
    mSourceRotation = sourceRotation;
  }

  /**
   * note(@sjchmiela)
   * Should the events about detected barcodes coalesce, the best strategy will be
   * to ensure that events with different barcodes count are always being transmitted.
   */
  @Override
  public short getCoalescingKey() {
    if (mBarcodes.size() > Short.MAX_VALUE) {
      return Short.MAX_VALUE;
    }

    return (short) mBarcodes.size();
  }

  @Override
  public String getEventName() {
    return CameraViewManager.Events.EVENT_ON_BARCODES_DETECTED.toString();
  }

  @Override
  public void dispatch(RCTEventEmitter rctEventEmitter) {
    rctEventEmitter.receiveEvent(getViewTag(), getEventName(), serializeEventData());
  }

  private WritableMap serializeEventData() {
    WritableArray barcodesList = Arguments.createArray();

    for (int i = 0; i < mBarcodes.size(); i++) {
      Barcode barcode = mBarcodes.valueAt(i);
      WritableMap serializedBarcode = Arguments.createMap();
      serializedBarcode.putString("data", barcode.displayValue);
      serializedBarcode.putString("rawData", barcode.rawValue);
      serializedBarcode.putString("type", BarcodeFormatUtils.get(barcode.format));

      Rect barcodeBoundingBox = barcode.getBoundingBox();
      if (barcodeBoundingBox != null) {
        WritableMap serializedBounds = Arguments.createMap();

        WritableMap serializedSize = Arguments.createMap();
        serializedSize.putString("width", String.valueOf(barcodeBoundingBox.width()));
        serializedSize.putString("height", String.valueOf(barcodeBoundingBox.height()));
        serializedBounds.putMap("size", serializedSize);
        WritableMap serializedOrigin = Arguments.createMap();
        serializedOrigin.putString("x", String.valueOf(barcodeBoundingBox.left));
        serializedOrigin.putString("y", String.valueOf(barcodeBoundingBox.top));
        serializedBounds.putMap("origin", serializedOrigin);
        serializedBarcode.putMap("bounds", serializedBounds);

        barcodesList.pushMap(serializedBarcode);
      }
    }

    WritableMap event = Arguments.createMap();
    event.putString("type", "barcode");
    event.putInt("sourceWidth", mSourceWidth);
    event.putInt("sourceHeight", mSourceHeight);
    event.putInt("sourceRotation", mSourceRotation);
    event.putArray("barcodes", barcodesList);
    event.putInt("target", getViewTag());
    return event;
  }
}
