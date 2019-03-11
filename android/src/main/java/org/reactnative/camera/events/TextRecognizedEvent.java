package org.reactnative.camera.events;

import android.support.v4.util.Pools;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import org.reactnative.camera.CameraViewManager;


public class TextRecognizedEvent extends Event<TextRecognizedEvent> {

  private static final Pools.SynchronizedPool<TextRecognizedEvent> EVENTS_POOL =
      new Pools.SynchronizedPool<>(3);

  private WritableArray mData;

  private TextRecognizedEvent() {}

  public static TextRecognizedEvent obtain(int viewTag, WritableArray data) {
    TextRecognizedEvent event = EVENTS_POOL.acquire();
    if (event == null) {
      event = new TextRecognizedEvent();
    }
    event.init(viewTag, data);
    return event;
  }

  private void init(int viewTag, WritableArray data) {
    super.init(viewTag);
    mData = data;
  }

  @Override
  public String getEventName() {
    return CameraViewManager.Events.EVENT_ON_TEXT_RECOGNIZED.toString();
  }

  @Override
  public void dispatch(RCTEventEmitter rctEventEmitter) {
    rctEventEmitter.receiveEvent(getViewTag(), getEventName(), createEvent());
  }

  private WritableMap createEvent() {
    WritableMap event = Arguments.createMap();
    event.putString("type", "textBlock");
    event.putArray("textBlocks", mData);
    event.putInt("target", getViewTag());
    return event;
  }
}
