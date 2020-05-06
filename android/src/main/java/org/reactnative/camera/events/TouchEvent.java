package org.reactnative.camera.events;

import androidx.core.util.Pools;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;


import org.reactnative.camera.CameraViewManager;


public class TouchEvent extends Event<TouchEvent> {
  private static final Pools.SynchronizedPool<TouchEvent> EVENTS_POOL =
      new Pools.SynchronizedPool<>(3);

  private int mX;
  private int mY;
  private boolean mIsDoubleTap;

  private TouchEvent() {}

  public static TouchEvent obtain(int viewTag, boolean isDoubleTap, int x, int y) {
    TouchEvent event = EVENTS_POOL.acquire();
    if (event == null) {
      event = new TouchEvent();
    }
    event.init(viewTag, isDoubleTap, x, y);
    return event;
  }

  private void init(int viewTag, boolean isDoubleTap, int x, int y) {
    super.init(viewTag);
    mX = x;
    mY = y;
    mIsDoubleTap=isDoubleTap;
  }


  @Override
  public short getCoalescingKey() {
    return 0;
  }

  @Override
  public String getEventName() {
    return CameraViewManager.Events.EVENT_ON_TOUCH.toString();
  }

  @Override
  public void dispatch(RCTEventEmitter rctEventEmitter) {
    rctEventEmitter.receiveEvent(getViewTag(), getEventName(), serializeEventData());
  }

  private WritableMap serializeEventData() {
    WritableMap event = Arguments.createMap();

    event.putInt("target", getViewTag());

    WritableMap touchOrigin = Arguments.createMap();
    touchOrigin.putInt("x", mX);
    touchOrigin.putInt("y",mY);

    event.putBoolean("isDoubleTap", mIsDoubleTap);
    event.putMap("touchOrigin", touchOrigin);
    return event;
  }
}
