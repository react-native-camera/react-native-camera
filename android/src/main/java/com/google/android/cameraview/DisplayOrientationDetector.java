/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.cameraview;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;


/**
 * Monitors the value returned from {@link Display#getRotation()}.
 */
abstract class DisplayOrientationDetector {

    private final OrientationEventListener mOrientationEventListener;

    /** Mapping from Surface.Rotation_n to degrees. */
    static final SparseIntArray DISPLAY_ORIENTATIONS = new SparseIntArray();

    static {
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_0, 0);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_90, 90);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_180, 180);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_270, 270);
    }

    Display mDisplay;

    private int mLastKnownDisplayOrientation = 0;

    private int mLastKnownDeviceOrientation = 0;

    public DisplayOrientationDetector(Context context) {
        mOrientationEventListener = new OrientationEventListener(context) {

            /** This is either Surface.Rotation_0, _90, _180, _270, or -1 (invalid). */
            private int mLastKnownRotation = -1;

            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN ||
                        mDisplay == null) {
                    return;
                }
                boolean hasChanged = false;

                /** set device orientation */
                final int deviceOrientation;
                if (orientation > 315 || orientation < 45) {
                    deviceOrientation = 0;
                } else if (orientation > 45 && orientation < 135) {
                    deviceOrientation = 90;
                } else if (orientation > 135 && orientation < 225) {
                    deviceOrientation = 180;
                } else if (orientation > 225 && orientation < 315) {
                    deviceOrientation = 270;
                } else {
                    deviceOrientation = 0;
                }

                 if (mLastKnownDeviceOrientation != deviceOrientation) {
                    mLastKnownDeviceOrientation = deviceOrientation;
                    hasChanged = true;
                }

                 /** set screen orientation */
                final int rotation = mDisplay.getRotation();
                if (mLastKnownRotation != rotation) {
                    mLastKnownRotation = rotation;
                    hasChanged = true;
                }

                if (hasChanged) {
                    dispatchOnDisplayOrientationChanged(DISPLAY_ORIENTATIONS.get(rotation));
                }
            }
        };
    }

    public void enable(Display display) {
        mDisplay = display;
        mOrientationEventListener.enable();
        // Immediately dispatch the first callback
        dispatchOnDisplayOrientationChanged(DISPLAY_ORIENTATIONS.get(display.getRotation()));
    }

    public void disable() {
        mOrientationEventListener.disable();
        mDisplay = null;
    }

    public int getLastKnownDisplayOrientation() {
        return mLastKnownDisplayOrientation;
    }

    void dispatchOnDisplayOrientationChanged(int displayOrientation) {
        mLastKnownDisplayOrientation = displayOrientation;
        onDisplayOrientationChanged(displayOrientation, mLastKnownDeviceOrientation);
    }

    /**
     * Called when display orientation is changed.
     *
     * @param displayOrientation One of 0, 90, 180, and 270.
     * @param deviceOrientation One of 0, 90, 180, and 270.
     */
    public abstract void onDisplayOrientationChanged(int displayOrientation, int deviceOrientation);

}
