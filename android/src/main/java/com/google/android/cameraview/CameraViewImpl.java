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

import android.media.CamcorderProfile;
import android.view.View;
import android.graphics.SurfaceTexture;
import android.os.Handler;

import com.facebook.react.bridge.ReadableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;


abstract class CameraViewImpl {

    protected final Callback mCallback;
    protected final PreviewImpl mPreview;

    // Background handler that the implementation an use to run heavy tasks in background
    // in a thread/looper provided by the view.
    // Most calls should not require this since the view will already schedule it
    // on the bg thread. However, the implementation might need to do some heavy work
    // by itself.
    protected final Handler mBgHandler;

    CameraViewImpl(Callback callback, PreviewImpl preview, Handler bgHandler) {
        mCallback = callback;
        mPreview = preview;
        mBgHandler = bgHandler;
    }

    View getView() {
        return mPreview.getView();
    }

    /**
     * @return {@code true} if the implementation was able to start the camera session.
     */
    abstract boolean start();

    abstract void stop();

    abstract boolean isCameraOpened();

    abstract void setFacing(int facing);
  
    abstract int getFacing();

    abstract void setCameraId(String id);

    abstract String getCameraId();

    abstract Set<AspectRatio> getSupportedAspectRatios();

    abstract List<Properties> getCameraIds();

    abstract SortedSet<Size> getAvailablePictureSizes(AspectRatio ratio);

    abstract void setPictureSize(Size size);

    abstract Size getPictureSize();

    /**
     * @return {@code true} if the aspect ratio was changed.
     */
    abstract boolean setAspectRatio(AspectRatio ratio);

    abstract AspectRatio getAspectRatio();

    abstract void setAutoFocus(boolean autoFocus);

    abstract boolean getAutoFocus();

    abstract void setFlash(int flash);

    abstract int getFlash();

    abstract void setExposureCompensation(float exposure);

    abstract float getExposureCompensation();

    abstract void takePicture(ReadableMap options);

    abstract boolean record(String path, int maxDuration, int maxFileSize,
                            boolean recordAudio, CamcorderProfile profile, int orientation, int fps);

    abstract void stopRecording();

    abstract void pauseRecording();

    abstract void resumeRecording();

    abstract int getCameraOrientation();

    abstract void setDisplayOrientation(int displayOrientation);

    abstract void setDeviceOrientation(int deviceOrientation);

    abstract void setFocusArea(float x, float y);

    abstract void setFocusDepth(float value);

    abstract float getFocusDepth();

    abstract void setZoom(float zoom);

    abstract float getZoom();

    abstract public ArrayList<int[]> getSupportedPreviewFpsRange();

    abstract void setWhiteBalance(int whiteBalance);

    abstract int getWhiteBalance();

    abstract void setPlaySoundOnCapture(boolean playSoundOnCapture);

    abstract boolean getPlaySoundOnCapture();

    abstract void setScanning(boolean isScanning);

    abstract boolean getScanning();

    abstract public void resumePreview();

    abstract public void pausePreview();

    abstract public void setPreviewTexture(SurfaceTexture surfaceTexture);

    abstract public Size getPreviewSize();

    interface Callback {

        void onCameraOpened();

        void onCameraClosed();

        void onPictureTaken(byte[] data, int deviceOrientation);

        void onVideoRecorded(String path, int videoOrientation, int deviceOrientation);

        void onRecordingStart(String path, int videoOrientation, int deviceOrientation);

        void onRecordingEnd();

        void onFramePreview(byte[] data, int width, int height, int orientation);

        void onMountError();
    }

}
