package org.reactnative.camera.events;

import android.support.v4.util.Pools;

import org.reactnative.camera.CameraViewManager;
import org.reactnative.facedetector.RNFaceDetector;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.util.Date;

public class FaceDetectionErrorEvent extends Event<FaceDetectionErrorEvent> {
  private static final Pools.SynchronizedPool<FaceDetectionErrorEvent> EVENTS_POOL = new Pools.SynchronizedPool<>(3);
  private RNFaceDetector mFaceDetector;
  private FaceDetectionErrorEvent() {}

  public static FaceDetectionErrorEvent obtain(int viewTag, RNFaceDetector faceDetector) {
    FaceDetectionErrorEvent event = EVENTS_POOL.acquire();
    if (event == null) {
      event = new FaceDetectionErrorEvent();
    }
    event.init(viewTag);
    return event;
  }

  private void init(int viewTag, RNFaceDetector faceDetector) {
    super.init(viewTag);
    mFaceDetector = faceDetector;
  }

  @Override
  public short getCoalescingKey() {
    return 0;
  }

  @Override
  public String getEventName() {
    return CameraViewManager.Events.EVENT_ON_MOUNT_ERROR.toString();
  }

  @Override
  public void dispatch(RCTEventEmitter rctEventEmitter) {
    rctEventEmitter.receiveEvent(getViewTag(), getEventName(), serializeEventData());
  }

  private WritableMap serializeEventData() {
    WritableMap map = Arguments.createMap();
    map.putBoolean("isOperational", mFaceDetector != null ? mFaceDetector.isOperational() : false);
    return map;
  }
}
