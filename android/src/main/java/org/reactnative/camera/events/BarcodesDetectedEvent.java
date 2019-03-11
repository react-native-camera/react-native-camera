package org.reactnative.camera.events;

import android.support.v4.util.Pools;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import org.reactnative.camera.CameraViewManager;

public class BarcodesDetectedEvent extends Event<BarcodesDetectedEvent> {

  private static final Pools.SynchronizedPool<BarcodesDetectedEvent> EVENTS_POOL =
      new Pools.SynchronizedPool<>(3);

  private WritableArray mBarcodes;

  private BarcodesDetectedEvent() {
  }

  public static BarcodesDetectedEvent obtain(
      int viewTag,
      WritableArray barcodes
  ) {
    BarcodesDetectedEvent event = EVENTS_POOL.acquire();
    if (event == null) {
      event = new BarcodesDetectedEvent();
    }
    event.init(viewTag, barcodes);
    return event;
  }

  private void init(
      int viewTag,
      WritableArray barcodes
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
    WritableMap event = Arguments.createMap();
    event.putString("type", "barcode");
    event.putArray("barcodes", mBarcodes);
    event.putInt("target", getViewTag());
    return event;
  }
}
