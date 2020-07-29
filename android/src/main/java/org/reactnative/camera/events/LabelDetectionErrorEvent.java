package org.reactnative.camera.events;

import androidx.core.util.Pools;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import org.reactnative.camera.CameraViewManager;
import org.reactnative.imagelabeler.RNImageLabeler;

public class LabelDetectionErrorEvent extends Event<LabelDetectionErrorEvent> {
  private static final Pools.SynchronizedPool<LabelDetectionErrorEvent> EVENTS_POOL = new Pools.SynchronizedPool<>(3);
  private RNImageLabeler mImageLabeler;

  private LabelDetectionErrorEvent() {
  }

  public static LabelDetectionErrorEvent obtain(int viewTag, RNImageLabeler imageLabeler) {
    LabelDetectionErrorEvent event = EVENTS_POOL.acquire();
    if (event == null) {
      event = new LabelDetectionErrorEvent();
    }
    event.init(viewTag, imageLabeler);
    return event;
  }

  private void init(int viewTag, RNImageLabeler imageLabeler) {
    super.init(viewTag);
    mImageLabeler = imageLabeler;
  }

  @Override
  public short getCoalescingKey() {
    return 0;
  }

  @Override
  public String getEventName() {
    return CameraViewManager.Events.EVENT_ON_LABEL_DETECTION_ERROR.toString();
  }

  @Override
  public void dispatch(RCTEventEmitter rctEventEmitter) {
    rctEventEmitter.receiveEvent(getViewTag(), getEventName(), serializeEventData());
  }

  private WritableMap serializeEventData() {
    WritableMap map = Arguments.createMap();
    map.putBoolean("isOperational", mImageLabeler != null && mImageLabeler.isOperational());
    return map;
  }
}
