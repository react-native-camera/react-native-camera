package com.lwansbrough.RCTCamera;

import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RCTCameraUtils {
    private static final String TAG = "RCTCameraUtils";

    private static final int FOCUS_AREA_MOTION_EVENT_EDGE_LENGTH = 100;
    private static final int FOCUS_AREA_WEIGHT = 1000;

    public static final int RCT_CAMERA_ASPECT_FILL = 0;
    public static final int RCT_CAMERA_ASPECT_FIT = 1;
    public static final int RCT_CAMERA_ASPECT_STRETCH = 2;
    public static final int RCT_CAMERA_CAPTURE_MODE_STILL = 0;
    public static final int RCT_CAMERA_CAPTURE_TARGET_MEMORY = 0;
    public static final int RCT_CAMERA_CAPTURE_TARGET_DISK = 1;
    public static final int RCT_CAMERA_CAPTURE_TARGET_CAMERA_ROLL = 2;
    public static final int RCT_CAMERA_CAPTURE_TARGET_TEMP = 3;
    public static final int RCT_CAMERA_ORIENTATION_PORTRAIT = Surface.ROTATION_0;
    public static final int RCT_CAMERA_ORIENTATION_PORTRAIT_UPSIDE_DOWN = Surface.ROTATION_180;
    public static final int RCT_CAMERA_ORIENTATION_LANDSCAPE_LEFT = Surface.ROTATION_90;
    public static final int RCT_CAMERA_ORIENTATION_LANDSCAPE_RIGHT = Surface.ROTATION_270;
    public static final int RCT_CAMERA_TYPE_FRONT = 1;
    public static final int RCT_CAMERA_TYPE_BACK = 2;
    public static final int RCT_CAMERA_FLASH_MODE_OFF = 0;
    public static final int RCT_CAMERA_FLASH_MODE_ON = 1;
    public static final int RCT_CAMERA_FLASH_MODE_AUTO = 2;
    public static final int RCT_CAMERA_TORCH_MODE_OFF = 0;
    public static final int RCT_CAMERA_TORCH_MODE_ON = 1;
    public static final int RCT_CAMERA_TORCH_MODE_AUTO = 2;
    public static final String RCT_CAMERA_CAPTURE_QUALITY_PREVIEW = "preview";
    public static final String RCT_CAMERA_CAPTURE_QUALITY_HIGH = "high";
    public static final String RCT_CAMERA_CAPTURE_QUALITY_MEDIUM = "medium";
    public static final String RCT_CAMERA_CAPTURE_QUALITY_LOW = "low";
    public static final String RCT_CAMERA_CAPTURE_QUALITY_1080P = "1080p";
    public static final String RCT_CAMERA_CAPTURE_QUALITY_720P = "720p";
    public static final String RCT_CAMERA_CAPTURE_QUALITY_480P = "480p";
    public static final int MEDIA_TYPE_IMAGE = 1;

    /**
     * Computes a Camera.Area corresponding to the new focus area to focus the camera on. This is
     * done by deriving a square around the center of a MotionEvent pointer (with side length equal
     * to FOCUS_AREA_MOTION_EVENT_EDGE_LENGTH), then transforming this rectangle's/square's
     * coordinates into the (-1000, 1000) coordinate system used for camera focus areas.
     *
     * Also note that we operate on RectF instances for the most part, to avoid any integer
     * division rounding errors going forward. We only round at the very end for playing into
     * the final focus areas list.
     *
     * @throws RuntimeException if unable to compute valid intersection between MotionEvent region
     * and SurfaceTexture region.
     */
    //TODO: Pedro = Changed from Protected to Public!!!
    public static Camera.Area computeFocusAreaFromMotionEvent(final MotionEvent event, final int surfaceTextureWidth, final int surfaceTextureHeight) {
        // Get position of first touch pointer.
        final int pointerId = event.getPointerId(0);
        final int pointerIndex = event.findPointerIndex(pointerId);
        final float centerX = event.getX(pointerIndex);
        final float centerY = event.getY(pointerIndex);

        // Build event rect. Note that coordinates increase right and down, such that left <= right
        // and top <= bottom.
        final RectF eventRect = new RectF(
                centerX - FOCUS_AREA_MOTION_EVENT_EDGE_LENGTH, // left
                centerY - FOCUS_AREA_MOTION_EVENT_EDGE_LENGTH, // top
                centerX + FOCUS_AREA_MOTION_EVENT_EDGE_LENGTH, // right
                centerY + FOCUS_AREA_MOTION_EVENT_EDGE_LENGTH // bottom
        );

        // Intersect this rect with the rect corresponding to the full area of the parent surface
        // texture, making sure we are not placing any amount of the eventRect outside the parent
        // surface's area.
        final RectF surfaceTextureRect = new RectF(
                (float) 0, // left
                (float) 0, // top
                (float) surfaceTextureWidth, // right
                (float) surfaceTextureHeight // bottom
        );
        final boolean intersectSuccess = eventRect.intersect(surfaceTextureRect);
        if (!intersectSuccess) {
            throw new RuntimeException(
                    "MotionEvent rect does not intersect with SurfaceTexture rect; unable to " +
                            "compute focus area"
            );
        }

        // Transform into (-1000, 1000) focus area coordinate system. See
        // https://developer.android.com/reference/android/hardware/Camera.Area.html.
        // Note that if this is ever changed to a Rect instead of RectF, be cautious of integer
        // division rounding!
        final RectF focusAreaRect = new RectF(
                (eventRect.left / surfaceTextureWidth) * 2000 - 1000, // left
                (eventRect.top / surfaceTextureHeight) * 2000 - 1000, // top
                (eventRect.right / surfaceTextureWidth) * 2000 - 1000, // right
                (eventRect.bottom / surfaceTextureHeight) * 2000 - 1000 // bottom
        );
        Rect focusAreaRectRounded = new Rect();
        focusAreaRect.round(focusAreaRectRounded);
        return new Camera.Area(focusAreaRectRounded, FOCUS_AREA_WEIGHT);
    }

    /** Converts a {@link ReadableMap} into an Json {@link ObjectNode} */
    static ObjectNode toJsonObject(ReadableMap readableMap) {
        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectNode result = nodeFactory.objectNode();
        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            ReadableType type = readableMap.getType(key);
            switch (type) {
                case Null:
                    result.putNull(key);
                    break;
                case Boolean:
                    result.put(key, readableMap.getBoolean(key));
                    break;
                case Number:
                    result.put(key, readableMap.getDouble(key));
                    break;
                case String:
                    result.put(key, readableMap.getString(key));
                    break;
                case Map:
                    result.set(key, toJsonObject(readableMap.getMap(key)));
                    break;
                case Array:
                    result.set(key, toJsonArray(readableMap.getArray(key)));
                    break;
                default:
                    Log.e(TAG, "Could not convert object with key: " + key + ".");
            }
        }
        return result;
    }

    /** Converts a {@link ReadableArray} into an Json {@link ArrayNode} */
    static  ArrayNode toJsonArray(ReadableArray readableArray) {
        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ArrayNode result = nodeFactory.arrayNode();
        for (int i = 0; i < readableArray.size(); i++) {
            ReadableType indexType = readableArray.getType(i);
            switch (indexType) {
                case Null:
                    result.addNull();
                    break;
                case Boolean:
                    result.add(readableArray.getBoolean(i));
                    break;
                case Number:
                    result.add(readableArray.getDouble(i));
                    break;
                case String:
                    result.add(readableArray.getString(i));
                    break;
                case Map:
                    result.add(toJsonObject(readableArray.getMap(i)));
                    break;
                case Array:
                    result.add(toJsonArray(readableArray.getArray(i)));
                    break;
                default:
                    Log.e(TAG, "Could not convert object at index " + i + ".");
            }
        }
        return result;
    }
}
