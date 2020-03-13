package org.reactnative.camera.events;

import androidx.core.util.Pools;

import org.reactnative.camera.CameraViewManager;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

public class RecordingEndEvent extends Event<RecordingEndEvent> {
    private static final Pools.SynchronizedPool<RecordingEndEvent> EVENTS_POOL = new Pools.SynchronizedPool<>(3);
    private RecordingEndEvent() {}

    public static RecordingEndEvent obtain(int viewTag) {
        RecordingEndEvent event = EVENTS_POOL.acquire();
        if (event == null) {
        event = new RecordingEndEvent();
        }
        event.init(viewTag);
        return event;
    }

    @Override
    public short getCoalescingKey() {
        return 0;
    }

    @Override
    public String getEventName() {
        return CameraViewManager.Events.EVENT_ON_RECORDING_END.toString();
    }

    @Override
    public void dispatch(RCTEventEmitter rctEventEmitter) {
        rctEventEmitter.receiveEvent(getViewTag(), getEventName(), serializeEventData());
    }

    private WritableMap serializeEventData() {
        return Arguments.createMap();
    }
}
