package org.reactnative.camera.events;

import android.support.v4.util.Pools;
import android.support.v4.util.Pools.SynchronizedPool;
import android.util.SparseArray;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.google.android.cameraview.CameraView;
import com.google.android.gms.vision.face.Face;

import java.util.Map;

import org.reactnative.camera.CameraViewManager;
import org.reactnative.camera.CameraViewManager.Events;
import org.reactnative.camera.utils.ImageDimensions;
import org.reactnative.facedetector.FaceDetectorUtils;

public class OpenCVProcessorFacesDetectedEvent extends Event<OpenCVProcessorFacesDetectedEvent> {
    private static final Pools.SynchronizedPool<OpenCVProcessorFacesDetectedEvent> EVENTS_POOL =
            new Pools.SynchronizedPool<>(3);

    private SparseArray<Map<String, Float>> mFaces;
    private double mScaleX;
    private double mScaleY;
    private ImageDimensions mImageDimensions;

    private OpenCVProcessorFacesDetectedEvent() {}

    public static OpenCVProcessorFacesDetectedEvent obtain(
            int viewTag,
            SparseArray<Map<String, Float>> faces,
            ImageDimensions dimensions,
            double scaleX,
            double scaleY
    ) {
        OpenCVProcessorFacesDetectedEvent event = EVENTS_POOL.acquire();
        if (event == null) {
            event = new OpenCVProcessorFacesDetectedEvent();
        }
        event.init(viewTag, faces, dimensions, scaleX, scaleY);
        return event;
    }

    private void init(
            int viewTag,
            SparseArray<Map<String, Float>> faces,
            ImageDimensions dimensions,
            double scaleX,
            double scaleY
    ) {
        super.init(viewTag);
        mFaces = faces;
        mImageDimensions = dimensions;
        mScaleX = scaleX;
        mScaleY = scaleY;
    }

    /**
     * note(@sjchmiela)
     * Should the events about detected faces coalesce, the best strategy will be
     * to ensure that events with different faces count are always being transmitted.
     */
    @Override
    public short getCoalescingKey() {
        if (mFaces.size() > Short.MAX_VALUE) {
            return Short.MAX_VALUE;
        }

        return (short) mFaces.size();
    }

    @Override
    public String getEventName() {
        return CameraViewManager.Events.EVENT_ON_FACES_DETECTED.toString();
    }

    @Override
    public void dispatch(RCTEventEmitter rctEventEmitter) {
        rctEventEmitter.receiveEvent(getViewTag(), getEventName(), serializeEventData());
    }

    private WritableMap serializeEventData() {
        WritableArray facesList = Arguments.createArray();
        for (int i = 0; i < this.mFaces.size(); i++) {
            Map<String, Float> face = (Map) this.mFaces.valueAt(i);
            WritableMap serializedFace = Arguments.createMap();
            serializedFace.putDouble("x", face.get("x"));
            serializedFace.putDouble("y", face.get("y"));
            serializedFace.putDouble("width", face.get("width"));
            serializedFace.putDouble("height", face.get("height"));
            serializedFace.putDouble("orientation", face.get("orientation"));
            facesList.pushMap(serializedFace);
        }
        WritableMap event = Arguments.createMap();
        event.putString("type", "face");
        event.putArray("faces", facesList);
        event.putInt("target", getViewTag());
        return event;
    }
}
