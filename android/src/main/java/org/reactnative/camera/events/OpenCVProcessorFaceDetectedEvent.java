package org.reactnative.camera.events;

import android.support.v4.util.Pools.SynchronizedPool;
import android.util.SparseArray;
import com.brentvatne.react.ReactVideoView;
import com.brentvatne.react.ReactVideoViewManager;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import java.util.Map;
import org.reactnative.camera.CameraViewManager.Events;
import org.reactnative.camera.utils.ImageDimensions;

public class OpenCVProcessorFaceDetectedEvent extends Event<OpenCVProcessorFaceDetectedEvent> {
    private static final SynchronizedPool<OpenCVProcessorFaceDetectedEvent> EVENTS_POOL = new SynchronizedPool(3);
    private SparseArray<Map<String, Float>> mFaces;
    private ImageDimensions mImageDimensions;
    private double mScaleX;
    private double mScaleY;

    private OpenCVProcessorFaceDetectedEvent() {
    }

    public static OpenCVProcessorFaceDetectedEvent obtain(int viewTag, SparseArray<Map<String, Float>> faces, ImageDimensions dimensions, double scaleX, double scaleY) {
        OpenCVProcessorFaceDetectedEvent event = (OpenCVProcessorFaceDetectedEvent) EVENTS_POOL.acquire();
        if (event == null) {
            event = new OpenCVProcessorFaceDetectedEvent();
        }
        event.init(viewTag, faces, dimensions, scaleX, scaleY);
        return event;
    }

    private void init(int viewTag, SparseArray<Map<String, Float>> faces, ImageDimensions dimensions, double scaleX, double scaleY) {
        super.init(viewTag);
        this.mFaces = faces;
        this.mImageDimensions = dimensions;
        this.mScaleX = scaleX;
        this.mScaleY = scaleY;
    }

    public short getCoalescingKey() {
        if (this.mFaces.size() > 32767) {
            return Short.MAX_VALUE;
        }
        return (short) this.mFaces.size();
    }

    public String getEventName() {
        return Events.EVENT_ON_FACES_DETECTED.toString();
    }

    public void dispatch(RCTEventEmitter rctEventEmitter) {
        rctEventEmitter.receiveEvent(getViewTag(), getEventName(), serializeEventData());
    }

    private WritableMap serializeEventData() {
        WritableArray facesList = Arguments.createArray();
        for (int i = 0; i < this.mFaces.size(); i++) {
            Map<String, Float> face = (Map) this.mFaces.valueAt(i);
            WritableMap serializedFace = Arguments.createMap();
            serializedFace.putDouble("x", (double) ((Float) face.get("x")).floatValue());
            serializedFace.putDouble("y", (double) ((Float) face.get("y")).floatValue());
            serializedFace.putDouble("width", (double) ((Float) face.get("width")).floatValue());
            serializedFace.putDouble("height", (double) ((Float) face.get("height")).floatValue());
            serializedFace.putDouble(ReactVideoView.EVENT_PROP_ORIENTATION, 0.0d);
            facesList.pushMap(serializedFace);
        }
        WritableMap event = Arguments.createMap();
        event.putString(ReactVideoViewManager.PROP_SRC_TYPE, "face");
        event.putArray("faces", facesList);
        event.putInt("target", getViewTag());
        return event;
    }
}