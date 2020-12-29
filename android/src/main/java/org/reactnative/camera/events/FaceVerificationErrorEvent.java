package org.reactnative.camera.events;

import androidx.core.util.Pools;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import org.reactnative.camera.CameraViewManager;
import org.reactnative.facedetector.RNFaceDetector;

public class FaceVerificationErrorEvent extends Event<FaceVerificationErrorEvent> {
    private static final Pools.SynchronizedPool<FaceVerificationErrorEvent> EVENTS_POOL = new Pools.SynchronizedPool<>(3);


    private FaceVerificationErrorEvent() {
    }

    public static FaceVerificationErrorEvent obtain(int viewTag) {
        FaceVerificationErrorEvent event = EVENTS_POOL.acquire();
        if (event == null) {
            event = new FaceVerificationErrorEvent();
        }
//        event.init(viewTag);
        return event;
    }

//    private void init(int viewTag) {
//        super.init();
//    }

//    @Override
//    public short getCoalescingKey() {
//        return 0;
//    }

    @Override
    public String getEventName() {
        return CameraViewManager.Events.EVENT_ON_FACE_VERIFICATION_ERROR.toString();
    }

    @Override
    public void dispatch(RCTEventEmitter rctEventEmitter) {
        rctEventEmitter.receiveEvent(getViewTag(), getEventName(), serializeEventData());
    }

    private WritableMap serializeEventData() {
        WritableMap map = Arguments.createMap();
        map.putString("error", "verification error...");

        return map;
    }
}
