package org.reactnative.camera.events;

import android.support.v4.util.Pools;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import org.reactnative.camera.CameraViewManager;

import com.dynamsoft.dbr.BarcodeReader;
import com.dynamsoft.dbr.EnumBarcodeFormat;
import com.dynamsoft.dbr.EnumConflictMode;
import com.dynamsoft.dbr.EnumImagePixelFormat;
import com.dynamsoft.dbr.PublicRuntimeSettings;
import com.dynamsoft.dbr.TextResult;

public class DynamsoftBarcodeEvent extends Event<DynamsoftBarcodeEvent> {

  private static final Pools.SynchronizedPool<DynamsoftBarcodeEvent> EVENTS_POOL =
      new Pools.SynchronizedPool<>(3);

  private TextResult[] mBarcodes;

  private DynamsoftBarcodeEvent() {
  }

  public static DynamsoftBarcodeEvent obtain(
      int viewTag,
      TextResult[] barcodes
  ) {
    DynamsoftBarcodeEvent event = EVENTS_POOL.acquire();
    if (event == null) {
      event = new DynamsoftBarcodeEvent();
    }
    event.init(viewTag, barcodes);
    return event;
  }

  private void init(
      int viewTag,
      TextResult[] barcodes
  ) {
    super.init(viewTag);
    mBarcodes = barcodes;
  }

  /**
   * note(@sjchmiela)
   * Should the events about detected barcodes coalesce, the best strategy will be
   * to ensure that events with different barcodes count are always being transmitted.
   */
  @Override
  public short getCoalescingKey() {
    if (mBarcodes.length > Short.MAX_VALUE) {
        return Short.MAX_VALUE;
    }

    return (short) mBarcodes.length;
  }

  @Override
  public String getEventName() {
    return CameraViewManager.Events.EVENT_ON_DYNAMSOFT_BARCODE_DETECTED.toString();
  }

  @Override
  public void dispatch(RCTEventEmitter rctEventEmitter) {
    rctEventEmitter.receiveEvent(getViewTag(), getEventName(), serializeEventData());
  }

  private WritableMap serializeEventData() {
    WritableArray barcodesList = Arguments.createArray();

    for (TextResult result: mBarcodes) {
      WritableMap serializedBarcode = Arguments.createMap();
      serializedBarcode.putString("data", result.barcodeText);
      serializedBarcode.putString("type", result.barcodeFormatString);
      barcodesList.pushMap(serializedBarcode);
    }

    WritableMap event = Arguments.createMap();
    event.putString("type", "barcode");
    event.putArray("barcodes", barcodesList);
    event.putInt("target", getViewTag());
    return event;
  }
}
