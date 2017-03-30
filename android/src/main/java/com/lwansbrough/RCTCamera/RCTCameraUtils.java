package com.lwansbrough.RCTCamera;

import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.view.MotionEvent;

public class RCTCameraUtils {
    private static final int FOCUS_AREA_MOTION_EVENT_EDGE_LENGTH = 100;
    private static final int FOCUS_AREA_WEIGHT = 1000;

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
    protected static Camera.Area computeFocusAreaFromMotionEvent(final MotionEvent event, final int surfaceTextureWidth, final int surfaceTextureHeight) {
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
}
