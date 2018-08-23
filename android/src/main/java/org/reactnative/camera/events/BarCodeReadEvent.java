package org.reactnative.camera.events;

import android.support.v4.util.Pools;

import org.reactnative.camera.CameraViewManager;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;

import java.util.Date;

public class BarCodeReadEvent extends Event<BarCodeReadEvent> {
  private static final Pools.SynchronizedPool<BarCodeReadEvent> EVENTS_POOL =
      new Pools.SynchronizedPool<>(3);

  private Result mBarCode;

  private BarCodeReadEvent() {}

  public static BarCodeReadEvent obtain(int viewTag, Result barCode) {
    BarCodeReadEvent event = EVENTS_POOL.acquire();
    if (event == null) {
      event = new BarCodeReadEvent();
    }
    event.init(viewTag, barCode);
    return event;
  }

  private void init(int viewTag, Result barCode) {
    super.init(viewTag);
    mBarCode = barCode;
  }

  /**
   * We want every distinct barcode to be reported to the JS listener.
   * If we return some static value as a coalescing key there may be two barcode events
   * containing two different barcodes waiting to be transmitted to JS
   * that would get coalesced (because both of them would have the same coalescing key).
   * So let's differentiate them with a hash of the contents (mod short's max value).
   */
  @Override
  public short getCoalescingKey() {
    int hashCode = mBarCode.getText().hashCode() % Short.MAX_VALUE;
    return (short) hashCode;
  }

  @Override
  public String getEventName() {
    return CameraViewManager.Events.EVENT_ON_BAR_CODE_READ.toString();
  }

  @Override
  public void dispatch(RCTEventEmitter rctEventEmitter) {
    rctEventEmitter.receiveEvent(getViewTag(), getEventName(), serializeEventData());
  }

  private WritableMap serializeEventData() {
    WritableMap event = Arguments.createMap();

    event.putInt("target", getViewTag());
    event.putString("data", mBarCode.getText());
    event.putString("type", mBarCode.getBarcodeFormat().toString());
    WritableArray resultPoints = Arguments.createArray();
    ResultPoint[] points = mBarCode.getResultPoints();
    for (ResultPoint point: points) {
      if(point!=null) {
        WritableMap newPoint = Arguments.createMap();
        newPoint.putString("x", String.valueOf(point.getX()));
        newPoint.putString("y", String.valueOf(point.getY()));
        resultPoints.pushMap(newPoint);
      }
    }
    event.putArray("bounds",resultPoints);
    return event;
  }
}
