package org.reactnative.camera.events;

import android.support.v4.util.Pools;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import org.reactnative.camera.CameraViewManager;

public class PoseEstimatedEvent extends Event<PoseEstimatedEvent> {

    private static final Pools.SynchronizedPool<PoseEstimatedEvent> EVENTS_POOL =
            new Pools.SynchronizedPool<>(3);

    private WritableArray mData;

    private PoseEstimatedEvent() {}

    public static PoseEstimatedEvent obtain(int viewTag, WritableArray data) {
        PoseEstimatedEvent event = EVENTS_POOL.acquire();
        if (event == null) {
            event = new PoseEstimatedEvent();
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
        return CameraViewManager.Events.EVENT_ON_POSE_ESTIMATED.toString();
    }

    @Override
    public void dispatch(RCTEventEmitter rctEventEmitter) {
        rctEventEmitter.receiveEvent(getViewTag(), getEventName(), createEvent());
    }

    private WritableMap createEvent() {
        WritableMap event = Arguments.createMap();
        event.putString("type", "heatmap");
        event.putArray("heatmap", mData);
        event.putInt("target", getViewTag());
        return event;
    }
}
