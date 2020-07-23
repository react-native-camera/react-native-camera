package org.reactnative.camera.events;

import android.util.Base64;

import androidx.core.util.Pools;

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
  private byte[] mCompressedImage;

  private BarcodesDetectedEvent() {
  }

  public static BarcodesDetectedEvent obtain(
          int viewTag,
          WritableArray barcodes,
          byte[] compressedImage) {
    BarcodesDetectedEvent event = EVENTS_POOL.acquire();
    if (event == null) {
      event = new BarcodesDetectedEvent();
    }
    event.init(viewTag, barcodes, compressedImage);
    return event;
  }

  private void init(
          int viewTag,
          WritableArray barcodes,
          byte[] compressedImage) {
    super.init(viewTag);
    mBarcodes = barcodes;
    mCompressedImage = compressedImage;
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
    if (mCompressedImage != null) {
      event.putString("image", Base64.encodeToString(mCompressedImage, Base64.NO_WRAP));
    }
    return event;
  }
}
