package org.reactnative.camera.events;

import androidx.core.util.Pools;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import org.reactnative.camera.CameraViewManager;

public class LabelsDetectedEvent extends Event<LabelsDetectedEvent> {
  private static final Pools.SynchronizedPool<LabelsDetectedEvent> EVENTS_POOL =
      new Pools.SynchronizedPool<>(3);

  private WritableArray mData;

  private LabelsDetectedEvent() {}

  public static LabelsDetectedEvent obtain(int viewTag, WritableArray data) {
    LabelsDetectedEvent event = EVENTS_POOL.acquire();
    if (event == null) {
      event = new LabelsDetectedEvent();
    }
    event.init(viewTag, data);
    return event;
  }

  private void init(int viewTag, WritableArray data) {
    super.init(viewTag);
    mData = data;
  }

  @Override
  public short getCoalescingKey() {
    if (mData.size() > Short.MAX_VALUE) {
      return Short.MAX_VALUE;
    }

    return (short) mData.size();
  }

  @Override
  public String getEventName() {
    return CameraViewManager.Events.EVENT_ON_LABELS_DETECTED.toString();
  }

  @Override
  public void dispatch(RCTEventEmitter rctEventEmitter) {
    rctEventEmitter.receiveEvent(getViewTag(), getEventName(), serializeEventData());
  }

  private WritableMap serializeEventData() {
    WritableMap event = Arguments.createMap();
    event.putString("type", "label");
    event.putArray("labels", mData);
    event.putInt("target", getViewTag());
    return event;
  }
}
