package org.reactnative.camera.events;

import androidx.core.util.Pools;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import org.reactnative.camera.CameraViewManager;

public class DocumentDetectedEvent extends Event<DocumentDetectedEvent> {
    private static final Pools.SynchronizedPool<DocumentDetectedEvent> EVENTS_POOL = new Pools.SynchronizedPool<>(3);
    private WritableMap mData;

    private DocumentDetectedEvent(){}

    public static DocumentDetectedEvent obtain(int viewTag, WritableMap data) {
        DocumentDetectedEvent event = EVENTS_POOL.acquire();
        if (event == null) {
            event = new DocumentDetectedEvent();
        }
        event.init(viewTag, data);
        return event;
    }

    private void init(int viewTag, WritableMap data) {
        super.init(viewTag);
        mData = data;
    }

    @Override
    public String getEventName() {
        return CameraViewManager.Events.EVENT_ON_DOCUMENT_DETECTED.toString();
    }

    @Override
    public void dispatch(RCTEventEmitter rctEventEmitter) {
        rctEventEmitter.receiveEvent(getViewTag(), getEventName(), serializeEventData());
    }

    private WritableMap serializeEventData() {
        WritableMap event = Arguments.createMap();
        event.putString("type", "document");
        event.putMap("document", mData);
        event.putInt("target", getViewTag());
        return event;
    }
}
