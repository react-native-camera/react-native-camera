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

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.os.Build;
import android.os.HandlerThread;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.os.ParcelableCompat;
import androidx.core.os.ParcelableCompatCreatorCallbacks;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.graphics.SurfaceTexture;

import com.facebook.react.bridge.ReadableMap;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;

public class CameraView extends FrameLayout {

    /** The camera device faces the opposite direction as the device's screen. */
    public static final int FACING_BACK = Constants.FACING_BACK;

    /** The camera device faces the same direction as the device's screen. */
    public static final int FACING_FRONT = Constants.FACING_FRONT;

    /** Direction the camera faces relative to device screen. */
    @IntDef({FACING_BACK, FACING_FRONT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Facing {
    }

    /** Flash will not be fired. */
    public static final int FLASH_OFF = Constants.FLASH_OFF;

    /** Flash will always be fired during snapshot. */
    public static final int FLASH_ON = Constants.FLASH_ON;

    /** Constant emission of light during preview, auto-focus and snapshot. */
    public static final int FLASH_TORCH = Constants.FLASH_TORCH;

    /** Flash will be fired automatically when required. */
    public static final int FLASH_AUTO = Constants.FLASH_AUTO;

    /** Flash will be fired in red-eye reduction mode. */
    public static final int FLASH_RED_EYE = Constants.FLASH_RED_EYE;

    /** The mode for for the camera device's flash control */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FLASH_OFF, FLASH_ON, FLASH_TORCH, FLASH_AUTO, FLASH_RED_EYE})
    public @interface Flash {
    }

    CameraViewImpl mImpl;

    private final CallbackBridge mCallbacks;

    private boolean mAdjustViewBounds;

    private Context mContext;

    private final DisplayOrientationDetector mDisplayOrientationDetector;

    protected HandlerThread mBgThread;
    protected Handler mBgHandler;


    public CameraView(Context context, boolean fallbackToOldApi) {
        this(context, null, fallbackToOldApi);
    }

    public CameraView(Context context, AttributeSet attrs, boolean fallbackToOldApi) {
        this(context, attrs, 0, fallbackToOldApi);
    }

    @SuppressWarnings("WrongConstant")
    public CameraView(Context context, AttributeSet attrs, int defStyleAttr, boolean fallbackToOldApi) {
        super(context, attrs, defStyleAttr);

        // bg hanadler for non UI heavy work
        mBgThread = new HandlerThread("RNCamera-Handler-Thread");
        mBgThread.start();
        mBgHandler = new Handler(mBgThread.getLooper());


        if (isInEditMode()){
            mCallbacks = null;
            mDisplayOrientationDetector = null;
            return;
        }
        mAdjustViewBounds = true;
        mContext = context;

        // Internal setup
        final PreviewImpl preview = createPreviewImpl(context);
        mCallbacks = new CallbackBridge();
        if (fallbackToOldApi || Build.VERSION.SDK_INT < 21 || Camera2.isLegacy(context)) {
            mImpl = new Camera1(mCallbacks, preview, mBgHandler);
        } else if (Build.VERSION.SDK_INT < 23) {
            mImpl = new Camera2(mCallbacks, preview, context, mBgHandler);
        } else {
            mImpl = new Camera2Api23(mCallbacks, preview, context, mBgHandler);
        }

        // Display orientation detector
        mDisplayOrientationDetector = new DisplayOrientationDetector(context) {
            @Override
            public void onDisplayOrientationChanged(int displayOrientation, int deviceOrientation) {
                mImpl.setDisplayOrientation(displayOrientation);
                mImpl.setDeviceOrientation(deviceOrientation);
            }
        };
    }

    public void cleanup(){
        if(mBgThread != null){
            if(Build.VERSION.SDK_INT < 18){
                mBgThread.quit();
            }
            else{
                mBgThread.quitSafely();
            }

            mBgThread = null;
        }
    }

    @NonNull
    private PreviewImpl createPreviewImpl(Context context) {
        PreviewImpl preview;
        if (Build.VERSION.SDK_INT < 14) {
            preview = new SurfaceViewPreview(context, this);
        } else {
            preview = new TextureViewPreview(context, this);
        }
        return preview;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            mDisplayOrientationDetector.enable(ViewCompat.getDisplay(this));
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (!isInEditMode()) {
            mDisplayOrientationDetector.disable();
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isInEditMode()){
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        // Handle android:adjustViewBounds
        if (mAdjustViewBounds) {
            if (!isCameraOpened()) {
                mCallbacks.reserveRequestLayoutOnOpen();
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                return;
            }
            final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
                final AspectRatio ratio = getAspectRatio();
                assert ratio != null;
                int height = (int) (MeasureSpec.getSize(widthMeasureSpec) * ratio.toFloat());
                if (heightMode == MeasureSpec.AT_MOST) {
                    height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
                }
                super.onMeasure(widthMeasureSpec,
                        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            } else if (widthMode != MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
                final AspectRatio ratio = getAspectRatio();
                assert ratio != null;
                int width = (int) (MeasureSpec.getSize(heightMeasureSpec) * ratio.toFloat());
                if (widthMode == MeasureSpec.AT_MOST) {
                    width = Math.min(width, MeasureSpec.getSize(widthMeasureSpec));
                }
                super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                        heightMeasureSpec);
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        // Measure the TextureView
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        AspectRatio ratio = getAspectRatio();
        if (mDisplayOrientationDetector.getLastKnownDisplayOrientation() % 180 == 0) {
            ratio = ratio.inverse();
        }
        assert ratio != null;
        if (height < width * ratio.getY() / ratio.getX()) {
            mImpl.getView().measure(
                    MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(width * ratio.getY() / ratio.getX(),
                            MeasureSpec.EXACTLY));
        } else {
            mImpl.getView().measure(
                    MeasureSpec.makeMeasureSpec(height * ratio.getX() / ratio.getY(),
                            MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(super.onSaveInstanceState());
        state.facing = getFacing();
        state.cameraId = getCameraId();
        state.ratio = getAspectRatio();
        state.autoFocus = getAutoFocus();
        state.flash = getFlash();
        state.exposure = getExposureCompensation();
        state.focusDepth = getFocusDepth();
        state.zoom = getZoom();
        state.whiteBalance = getWhiteBalance();
        state.playSoundOnCapture = getPlaySoundOnCapture();
        state.playSoundOnRecord = getPlaySoundOnRecord();
        state.scanning = getScanning();
        state.pictureSize = getPictureSize();
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setFacing(ss.facing);
        setCameraId(ss.cameraId);
        setAspectRatio(ss.ratio);
        setAutoFocus(ss.autoFocus);
        setFlash(ss.flash);
        setExposureCompensation(ss.exposure);
        setFocusDepth(ss.focusDepth);
        setZoom(ss.zoom);
        setWhiteBalance(ss.whiteBalance);
        setPlaySoundOnCapture(ss.playSoundOnCapture);
        setPlaySoundOnRecord(ss.playSoundOnRecord);
        setScanning(ss.scanning);
        setPictureSize(ss.pictureSize);
    }

    public void setUsingCamera2Api(boolean useCamera2) {
        if (Build.VERSION.SDK_INT < 21) {
            return;
        }

        boolean wasOpened = isCameraOpened();
        Parcelable state = onSaveInstanceState();

        if (useCamera2 && !Camera2.isLegacy(mContext)) {
            if (wasOpened) {
                stop();
            }
            if (Build.VERSION.SDK_INT < 23) {
                mImpl = new Camera2(mCallbacks, mImpl.mPreview, mContext, mBgHandler);
            } else {
                mImpl = new Camera2Api23(mCallbacks, mImpl.mPreview, mContext, mBgHandler);
            }

            onRestoreInstanceState(state);
        } else {
            if (mImpl instanceof Camera1) {
                return;
            }

            if (wasOpened) {
                stop();
            }
            mImpl = new Camera1(mCallbacks, mImpl.mPreview, mBgHandler);
        }
        if(wasOpened){
            start();
        }
    }

    /**
     * Open a camera device and start showing camera preview. This is typically called from
     * {@link Activity#onResume()}.
     */
    public void start() {
        mImpl.start();

        // this fallback is no longer needed and was too buggy/slow
        // if (!mImpl.start()) {
        //     if (mImpl.getView() != null) {
        //         this.removeView(mImpl.getView());
        //     }
        //     //store the state and restore this state after fall back to Camera1
        //     Parcelable state = onSaveInstanceState();
        //     // Camera2 uses legacy hardware layer; fall back to Camera1
        //     mImpl = new Camera1(mCallbacks, createPreviewImpl(getContext()), mBgHandler);
        //     onRestoreInstanceState(state);
        //     mImpl.start();
        // }
    }

    /**
     * Stop camera preview and close the device. This is typically called from
     * {@link Activity#onPause()}.
     */
    public void stop() {
        mImpl.stop();
    }

    /**
     * @return {@code true} if the camera is opened.
     */
    public boolean isCameraOpened() {
        return mImpl.isCameraOpened();
    }

    /**
     * Add a new callback.
     *
     * @param callback The {@link Callback} to add.
     * @see #removeCallback(Callback)
     */
    public void addCallback(@NonNull Callback callback) {
        mCallbacks.add(callback);
    }

    /**
     * Remove a callback.
     *
     * @param callback The {@link Callback} to remove.
     * @see #addCallback(Callback)
     */
    public void removeCallback(@NonNull Callback callback) {
        mCallbacks.remove(callback);
    }

    /**
     * @param adjustViewBounds {@code true} if you want the CameraView to adjust its bounds to
     *                         preserve the aspect ratio of camera.
     * @see #getAdjustViewBounds()
     */
    public void setAdjustViewBounds(boolean adjustViewBounds) {
        if (mAdjustViewBounds != adjustViewBounds) {
            mAdjustViewBounds = adjustViewBounds;
            requestLayout();
        }
    }

    /**
     * @return True when this CameraView is adjusting its bounds to preserve the aspect ratio of
     * camera.
     * @see #setAdjustViewBounds(boolean)
     */
    public boolean getAdjustViewBounds() {
        return mAdjustViewBounds;
    }

    public View getView() {
      if (mImpl != null) {
        return mImpl.getView();
      }
      return null;
    }

    /**
     * Chooses camera by the direction it faces.
     *
     * @param facing The camera facing. Must be either {@link #FACING_BACK} or
     *               {@link #FACING_FRONT}.
     */
    public void setFacing(@Facing int facing) {
        mImpl.setFacing(facing);
    }

    /**
     * Gets the direction that the current camera faces.
     *
     * @return The camera facing.
     */
    @Facing
    public int getFacing() {
        //noinspection WrongConstant
        return mImpl.getFacing();
    }

     /**
     * Chooses camera by its camera iD
     *
     * @param id The camera ID
     */
    public void setCameraId(String id) {
      mImpl.setCameraId(id);
    }

    /**
     * Gets the currently set camera ID
     *
     * @return The camera facing.
     */
    public String getCameraId() {
      return mImpl.getCameraId();
    }

    /**
     * Gets all the aspect ratios supported by the current camera.
     */
    public Set<AspectRatio> getSupportedAspectRatios() {
        return mImpl.getSupportedAspectRatios();
    }

    /**
     * Gets all the camera IDs supported by the phone as a String
     */
    public List<Properties> getCameraIds() {
        return mImpl.getCameraIds();
    }

    /**
     * Sets the aspect ratio of camera.
     *
     * @param ratio The {@link AspectRatio} to be set.
     */
    public void setAspectRatio(@NonNull AspectRatio ratio) {
        if (mImpl.setAspectRatio(ratio)) {
            requestLayout();
        }
    }

    /**
     * Gets the current aspect ratio of camera.
     *
     * @return The current {@link AspectRatio}. Can be {@code null} if no camera is opened yet.
     */
    @Nullable
    public AspectRatio getAspectRatio() {
        return mImpl.getAspectRatio();
    }

    /**
     * Gets all the picture sizes for particular ratio supported by the current camera.
     *
     * @param ratio {@link AspectRatio} for which the available image sizes will be returned.
     */
    public SortedSet<Size> getAvailablePictureSizes(@NonNull AspectRatio ratio) {
        return mImpl.getAvailablePictureSizes(ratio);
    }

    /**
     * Sets the size of taken pictures.
     *
     * @param size The {@link Size} to be set.
     */
    public void setPictureSize(@NonNull Size size) {
        mImpl.setPictureSize(size);
    }

    /**
     * Gets the size of pictures that will be taken.
     */
    public Size getPictureSize() {
        return mImpl.getPictureSize();
    }

    /**
     * Enables or disables the continuous auto-focus mode. When the current camera doesn't support
     * auto-focus, calling this method will be ignored.
     *
     * @param autoFocus {@code true} to enable continuous auto-focus mode. {@code false} to
     *                  disable it.
     */
    public void setAutoFocus(boolean autoFocus) {
        mImpl.setAutoFocus(autoFocus);
    }

    /**
     * Returns whether the continuous auto-focus mode is enabled.
     *
     * @return {@code true} if the continuous auto-focus mode is enabled. {@code false} if it is
     * disabled, or if it is not supported by the current camera.
     */
    public boolean getAutoFocus() {
        return mImpl.getAutoFocus();
    }

    /**
     * Sets the flash mode.
     *
     * @param flash The desired flash mode.
     */
    public void setFlash(@Flash int flash) {
        mImpl.setFlash(flash);
    }

    public ArrayList<int[]> getSupportedPreviewFpsRange() {
      return mImpl.getSupportedPreviewFpsRange();
    }

    /**
     * Gets the current flash mode.
     *
     * @return The current flash mode.
     */
    @Flash
    public int getFlash() {
        //noinspection WrongConstant
        return mImpl.getFlash();
    }

    public void setExposureCompensation(float exposure) {
        mImpl.setExposureCompensation(exposure);
    }

    public float getExposureCompensation() {
        return mImpl.getExposureCompensation();
    }


    /**
     * Gets the camera orientation relative to the devices native orientation.
     *
     * @return The orientation of the camera.
     */
    public int getCameraOrientation() {
        return mImpl.getCameraOrientation();
    }

    /**
     * Sets the auto focus point.
     *
     * @param x sets the x coordinate for camera auto focus
     * @param y sets the y coordinate for camera auto focus
     */
    public void setAutoFocusPointOfInterest(float x, float y) {
        mImpl.setFocusArea(x, y);
    }

    public void setFocusDepth(float value) {
        mImpl.setFocusDepth(value);
    }

    public float getFocusDepth() { return mImpl.getFocusDepth(); }

    public void setZoom(float zoom) {
      mImpl.setZoom(zoom);
    }

    public float getZoom() {
      return mImpl.getZoom();
    }

    public void setWhiteBalance(int whiteBalance) {
      mImpl.setWhiteBalance(whiteBalance);
    }

    public int getWhiteBalance() {
      return mImpl.getWhiteBalance();
    }

    public void setPlaySoundOnCapture(boolean playSoundOnCapture) {
      mImpl.setPlaySoundOnCapture(playSoundOnCapture);
    }

    public boolean getPlaySoundOnCapture() {
      return mImpl.getPlaySoundOnCapture();
    }

    public void setPlaySoundOnRecord(boolean playSoundOnRecord) {
        mImpl.setPlaySoundOnRecord(playSoundOnRecord);
    }

    public boolean getPlaySoundOnRecord() {
        return mImpl.getPlaySoundOnRecord();
    }

    public void setScanning(boolean isScanning) { mImpl.setScanning(isScanning);}

    public boolean getScanning() { return mImpl.getScanning(); }

    /**
     * Take a picture. The result will be returned to
     * {@link Callback#onPictureTaken(CameraView, byte[], int)}.
     */
    public void takePicture(ReadableMap options) {
        mImpl.takePicture(options);
    }

    /**
     * Record a video and save it to file. The result will be returned to
     * {@link Callback#onVideoRecorded(CameraView, String, int, int)}.
     * @param path Path to file that video will be saved to.
     * @param maxDuration Maximum duration of the recording, in seconds.
     * @param maxFileSize Maximum recording file size, in bytes.
     * @param profile Quality profile of the recording.
     *
     * fires {@link Callback#onRecordingStart(CameraView, String, int, int)} and {@link Callback#onRecordingEnd(CameraView)}.
     */
    public boolean record(String path, int maxDuration, int maxFileSize,
                          boolean recordAudio, CamcorderProfile profile, int orientation, int fps) {
        return mImpl.record(path, maxDuration, maxFileSize, recordAudio, profile, orientation, fps);
    }

    public void stopRecording() {
        mImpl.stopRecording();
    }

    public void pauseRecording() {
        mImpl.pauseRecording();
    }

    public void resumeRecording() {
        mImpl.resumeRecording();
    }

    public void resumePreview() {
        mImpl.resumePreview();
    }

    public void pausePreview() {
        mImpl.pausePreview();
    }

    public void setPreviewTexture(SurfaceTexture surfaceTexture) {
        mImpl.setPreviewTexture(surfaceTexture);
    }

    public Size getPreviewSize() {
        return mImpl.getPreviewSize();
    }

    private class CallbackBridge implements CameraViewImpl.Callback {

        private final ArrayList<Callback> mCallbacks = new ArrayList<>();

        private boolean mRequestLayoutOnOpen;

        CallbackBridge() {
        }

        public void add(Callback callback) {
            mCallbacks.add(callback);
        }

        public void remove(Callback callback) {
            mCallbacks.remove(callback);
        }

        @Override
        public void onCameraOpened() {
            if (mRequestLayoutOnOpen) {
                mRequestLayoutOnOpen = false;
                requestLayout();
            }
            for (Callback callback : mCallbacks) {
                callback.onCameraOpened(CameraView.this);
            }
        }

        @Override
        public void onCameraClosed() {
            for (Callback callback : mCallbacks) {
                callback.onCameraClosed(CameraView.this);
            }
        }

        @Override
        public void onPictureTaken(byte[] data, int deviceOrientation, int softwareRotation) {
            for (Callback callback : mCallbacks) {
                callback.onPictureTaken(CameraView.this, data, deviceOrientation, softwareRotation);
            }
        }

        @Override
        public void onRecordingStart(String path, int videoOrientation, int deviceOrientation) {
            for (Callback callback : mCallbacks) {
                callback.onRecordingStart(CameraView.this, path, videoOrientation, deviceOrientation);
            }
        }

        @Override
        public void onRecordingEnd() {
            for (Callback callback : mCallbacks) {
                callback.onRecordingEnd(CameraView.this);
            }
        }

        @Override
        public void onVideoRecorded(String path, int videoOrientation, int deviceOrientation) {
            for (Callback callback : mCallbacks) {
                callback.onVideoRecorded(CameraView.this, path, videoOrientation, deviceOrientation);
            }
        }

        @Override
        public void onFramePreview(byte[] data, int width, int height, int orientation) {
            for (Callback callback : mCallbacks) {
                callback.onFramePreview(CameraView.this, data, width, height, orientation);
            }
        }

        @Override
        public void onMountError() {
            for (Callback callback : mCallbacks) {
                callback.onMountError(CameraView.this);
            }
        }

        public void reserveRequestLayoutOnOpen() {
            mRequestLayoutOnOpen = true;
        }
    }

    protected static class SavedState extends BaseSavedState {

        @Facing
        int facing;

        String cameraId;

        AspectRatio ratio;

        boolean autoFocus;

        @Flash
        int flash;

        float exposure;

        float focusDepth;

        float zoom;

        int whiteBalance;

        boolean playSoundOnCapture;

        boolean playSoundOnRecord;

        boolean scanning;

        Size pictureSize;

        @SuppressWarnings("WrongConstant")
        public SavedState(Parcel source, ClassLoader loader) {
            super(source);
            facing = source.readInt();
            cameraId = source.readString();
            ratio = source.readParcelable(loader);
            autoFocus = source.readByte() != 0;
            flash = source.readInt();
            exposure = source.readFloat();
            focusDepth = source.readFloat();
            zoom = source.readFloat();
            whiteBalance = source.readInt();
            playSoundOnCapture = source.readByte() != 0;
            playSoundOnRecord = source.readByte() != 0;
            scanning = source.readByte() != 0;
            pictureSize = source.readParcelable(loader);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(facing);
            out.writeString(cameraId);
            out.writeParcelable(ratio, 0);
            out.writeByte((byte) (autoFocus ? 1 : 0));
            out.writeInt(flash);
            out.writeFloat(exposure);
            out.writeFloat(focusDepth);
            out.writeFloat(zoom);
            out.writeInt(whiteBalance);
            out.writeByte((byte) (playSoundOnCapture ? 1 : 0));
            out.writeByte((byte) (playSoundOnRecord ? 1 : 0));
            out.writeByte((byte) (scanning ? 1 : 0));
            out.writeParcelable(pictureSize, flags);
        }

        public static final Creator<SavedState> CREATOR
                = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new SavedState(in, loader);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }

        });

    }

    /**
     * Callback for monitoring events about {@link CameraView}.
     */
    @SuppressWarnings("UnusedParameters")
    public abstract static class Callback {

        /**
         * Called when camera is opened.
         *
         * @param cameraView The associated {@link CameraView}.
         */
        public void onCameraOpened(CameraView cameraView) {}

        /**
         * Called when camera is closed.
         *
         * @param cameraView The associated {@link CameraView}.
         */
        public void onCameraClosed(CameraView cameraView) {}

        /**
         * Called when a picture is taken.
         *
         * @param cameraView The associated {@link CameraView}.
         * @param data       JPEG data.
         */
        public void onPictureTaken(CameraView cameraView, byte[] data, int deviceOrientation, int softwareRotation) {}

        /**
         * Called when a video recording starts
         *
         * @param cameraView The associated {@link CameraView}.
         * @param path       Path to recoredd video file.
         */
        public void onRecordingStart(CameraView cameraView, String path, int videoOrientation, int deviceOrientation) {}

        /**
         * Called when a video recording ends, but before video is saved/processed.
         *
         * @param cameraView The associated {@link CameraView}.
         * @param path       Path to recoredd video file.
         */
        public void onRecordingEnd(CameraView cameraView){}

        /**
         * Called when a video is recorded.
         *
         * @param cameraView The associated {@link CameraView}.
         * @param path       Path to recoredd video file.
         */
        public void onVideoRecorded(CameraView cameraView, String path, int videoOrientation, int deviceOrientation) {}

        public void onFramePreview(CameraView cameraView, byte[] data, int width, int height, int orientation) {}

        public void onMountError(CameraView cameraView) {}
    }

}
