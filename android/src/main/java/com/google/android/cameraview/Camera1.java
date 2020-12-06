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

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaActionSound;
import android.os.Build;
import android.os.Handler;
import androidx.collection.SparseArrayCompat;
import android.util.Log;
import android.view.SurfaceHolder;

import com.facebook.react.bridge.ReadableMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.reactnative.camera.utils.ObjectUtils;


@SuppressWarnings("deprecation")
class Camera1 extends CameraViewImpl implements MediaRecorder.OnInfoListener,
                                                MediaRecorder.OnErrorListener, Camera.PreviewCallback {

    private static final int INVALID_CAMERA_ID = -1;

    private static final SparseArrayCompat<String> FLASH_MODES = new SparseArrayCompat<>();

    static {
        FLASH_MODES.put(Constants.FLASH_OFF, Camera.Parameters.FLASH_MODE_OFF);
        FLASH_MODES.put(Constants.FLASH_ON, Camera.Parameters.FLASH_MODE_ON);
        FLASH_MODES.put(Constants.FLASH_TORCH, Camera.Parameters.FLASH_MODE_TORCH);
        FLASH_MODES.put(Constants.FLASH_AUTO, Camera.Parameters.FLASH_MODE_AUTO);
        FLASH_MODES.put(Constants.FLASH_RED_EYE, Camera.Parameters.FLASH_MODE_RED_EYE);
    }

    private static final SparseArrayCompat<String> WB_MODES = new SparseArrayCompat<>();

    static {
      WB_MODES.put(Constants.WB_AUTO, Camera.Parameters.WHITE_BALANCE_AUTO);
      WB_MODES.put(Constants.WB_CLOUDY, Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT);
      WB_MODES.put(Constants.WB_SUNNY, Camera.Parameters.WHITE_BALANCE_DAYLIGHT);
      WB_MODES.put(Constants.WB_SHADOW, Camera.Parameters.WHITE_BALANCE_SHADE);
      WB_MODES.put(Constants.WB_FLUORESCENT, Camera.Parameters.WHITE_BALANCE_FLUORESCENT);
      WB_MODES.put(Constants.WB_INCANDESCENT, Camera.Parameters.WHITE_BALANCE_INCANDESCENT);
    }

    private static final int FOCUS_AREA_SIZE_DEFAULT = 300;
    private static final int FOCUS_METERING_AREA_WEIGHT_DEFAULT = 1000;
    private static final int DELAY_MILLIS_BEFORE_RESETTING_FOCUS = 3000;

    private Handler mHandler = new Handler();

    private int mCameraId;
    private String _mCameraId;

    private final AtomicBoolean isPictureCaptureInProgress = new AtomicBoolean(false);

    Camera mCamera;

    // do not instantiate this every time since it allocates unnecessary resources
    MediaActionSound sound = new MediaActionSound();

    private Camera.Parameters mCameraParameters;

    private final Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();

    private MediaRecorder mMediaRecorder;

    private String mVideoPath;

    private final AtomicBoolean mIsRecording = new AtomicBoolean(false);

    private final SizeMap mPreviewSizes = new SizeMap();

    private boolean mIsPreviewActive = false;
    private boolean mShowingPreview = true; // preview enabled by default

    private final SizeMap mPictureSizes = new SizeMap();

    private Size mPictureSize;

    private AspectRatio mAspectRatio;

    private boolean mAutoFocus;

    private int mFacing;

    private int mFlash;

    private float mExposure;

    private int mDisplayOrientation;

    private int mDeviceOrientation;

    private int mOrientation = Constants.ORIENTATION_AUTO;

    private float mZoom;

    private int mWhiteBalance;

    private boolean mIsScanning;

    private Boolean mPlaySoundOnCapture = false;

    private boolean mustUpdateSurface;
    private boolean surfaceWasDestroyed;

    private SurfaceTexture mPreviewTexture;

    Camera1(Callback callback, PreviewImpl preview, Handler bgHandler) {
        super(callback, preview, bgHandler);

        preview.setCallback(new PreviewImpl.Callback() {
            @Override
            public void onSurfaceChanged() {

                // if we got our surface destroyed
                // we must re-start the camera and surface
                // otherwise, just update our surface


                synchronized(Camera1.this){
                    if(!surfaceWasDestroyed){
                        updateSurface();
                    }
                    else{
                        mBgHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                start();
                            }
                        });
                    }
                }
            }

            @Override
            public void onSurfaceDestroyed() {

                // need to this early so we don't get buffer errors due to sufrace going away.
                // Then call stop in bg thread since it might be quite slow and will freeze
                // the UI or cause an ANR while it is happening.
                synchronized(Camera1.this){
                    if(mCamera != null){

                        // let the instance know our surface was destroyed
                        // and we might need to re-create it and restart the camera
                        surfaceWasDestroyed = true;

                        try{
                            mCamera.setPreviewCallback(null);
                            // note: this might give a debug message that can be ignored.
                            mCamera.setPreviewDisplay(null);
                        }
                        catch(Exception e){
                            Log.e("CAMERA_1::", "onSurfaceDestroyed preview cleanup failed", e);
                        }
                    }
                }
                mBgHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        stop();
                    }
                });
            }
        });
    }

    private void updateSurface(){
        if (mCamera != null) {

            // do not update surface if we are currently capturing
            // since it will break capture events/video due to the
            // pause preview calls
            // capture callbacks will handle it if needed afterwards.
            if(!isPictureCaptureInProgress.get() && !mIsRecording.get()){
                mBgHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        synchronized(Camera1.this){
                            // check for camera null again since it might have changed
                            if(mCamera != null){
                                mustUpdateSurface = false;
                                setUpPreview();
                                adjustCameraParameters();

                                // only start preview if we are showing it
                                if(mShowingPreview){
                                    startCameraPreview();
                                }
                            }
                        }
                    }
                });
            }
            else{
                mustUpdateSurface = true;
            }
        }
    }

    @Override
    boolean start() {

        synchronized(this){
            chooseCamera();
            if (!openCamera()) {
                mCallback.onMountError();
                // returning false will result in invoking this method again
                return true;
            }

            // if our preview layer is not ready
            // do not set it up. Surface handler will do it for us
            // once ready.
            // This prevents some redundant camera work
            if (mPreview.isReady()) {
                setUpPreview();
                if(mShowingPreview){
                    startCameraPreview();
                }
            }
            return true;
        }

    }

    @Override
    void stop() {

        // make sure no other threads are trying to do this at the same time
        // such as another call to stop from surface destroyed
        // or host destroyed. Should avoid crashes with concurrent calls
        synchronized(this){
            if (mMediaRecorder != null) {
                try{
                    mMediaRecorder.stop();
                }
                catch(RuntimeException e){
                    Log.e("CAMERA_1::", "mMediaRecorder.stop() failed", e);
                }

                try{
                    mMediaRecorder.reset();
                    mMediaRecorder.release();
                }
                catch(RuntimeException e){
                    Log.e("CAMERA_1::", "mMediaRecorder.release() failed", e);
                }

                mMediaRecorder = null;

                if (mIsRecording.get()) {
                    mCallback.onRecordingEnd();

                    int deviceOrientation = displayOrientationToOrientationEnum(mDeviceOrientation);
                    mCallback.onVideoRecorded(mVideoPath, mOrientation != Constants.ORIENTATION_AUTO ? mOrientation : deviceOrientation, deviceOrientation);
                }
            }

            if (mCamera != null) {
                mIsPreviewActive = false;
                try{
                    mCamera.stopPreview();
                    mCamera.setPreviewCallback(null);
                }
                catch(Exception e){
                    Log.e("CAMERA_1::", "stop preview cleanup failed", e);
                }
            }

            releaseCamera();
        }
    }

    // Suppresses Camera#setPreviewTexture
    @SuppressLint("NewApi")
    void setUpPreview() {
        try {
            surfaceWasDestroyed = false;

            if(mCamera != null){
                if (mPreviewTexture != null) {
                    mCamera.setPreviewTexture(mPreviewTexture);
                } else if (mPreview.getOutputClass() == SurfaceHolder.class) {
                    final boolean needsToStopPreview = mIsPreviewActive && Build.VERSION.SDK_INT < 14;
                    if (needsToStopPreview) {
                        mCamera.stopPreview();
                        mIsPreviewActive = false;
                    }
                    mCamera.setPreviewDisplay(mPreview.getSurfaceHolder());
                    if (needsToStopPreview) {
                        startCameraPreview();
                    }
                } else {
                    mCamera.setPreviewTexture((SurfaceTexture) mPreview.getSurfaceTexture());
                }
            }
        } catch (Exception e) {
            Log.e("CAMERA_1::", "setUpPreview failed", e);
        }
    }

    private void startCameraPreview() {
        // only start the preview if we didn't yet.
        if(!mIsPreviewActive && mCamera != null){
            try{
                mIsPreviewActive = true;
                mCamera.startPreview();
                if (mIsScanning) {
                    mCamera.setPreviewCallback(this);
                }
            }
            catch(Exception e){
                mIsPreviewActive = false;
                Log.e("CAMERA_1::", "startCameraPreview failed", e);
            }
        }
    }

    @Override
    public void resumePreview() {
        mBgHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized(this){
                    mShowingPreview = true;
                    startCameraPreview();
                }
            }
        });
    }

    @Override
    public void pausePreview() {
        synchronized(this){
            mIsPreviewActive = false;
            mShowingPreview = false;

            if(mCamera != null){
                mCamera.stopPreview();
            }
        }
    }

    @Override
    boolean isCameraOpened() {
        return mCamera != null;
    }

    @Override
    void setFacing(int facing) {
        if (mFacing == facing) {
            return;
        }
        mFacing = facing;

        mBgHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isCameraOpened()) {
                    stop();
                    start();
                }
            }
        });

    }

    @Override
    int getFacing() {
        return mFacing;
    }

    @Override
    void setCameraId(String id) {

        if(!ObjectUtils.equals(_mCameraId, id)){
            _mCameraId = id;

            // only update if our camera ID actually changes
            // from what we currently have.
            // Passing null will always yield true
            if(!ObjectUtils.equals(_mCameraId, String.valueOf(mCameraId))){
                // this will call chooseCamera
                mBgHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isCameraOpened()) {
                            stop();
                            start();
                        }
                    }
                });
            }
        }

    }

    @Override
    String getCameraId() {
        return _mCameraId;
    }

    @Override
    Set<AspectRatio> getSupportedAspectRatios() {
        SizeMap idealAspectRatios = mPreviewSizes;
        for (AspectRatio aspectRatio : idealAspectRatios.ratios()) {
            if (mPictureSizes.sizes(aspectRatio) == null) {
                idealAspectRatios.remove(aspectRatio);
            }
        }
        return idealAspectRatios.ratios();
    }


    @Override
    List<Properties> getCameraIds() {
        List<Properties> ids = new ArrayList<>();

        Camera.CameraInfo info = new Camera.CameraInfo();

        for (int i = 0, count = Camera.getNumberOfCameras(); i < count; i++) {
            Properties p = new Properties();
            Camera.getCameraInfo(i, info);
            p.put("id", String.valueOf(i));
            p.put("type", String.valueOf(info.facing));
            ids.add(p);
        }
        return ids;
    }

    @Override
    SortedSet<Size> getAvailablePictureSizes(AspectRatio ratio) {
        return mPictureSizes.sizes(ratio);
    }

    // Returns the best available size match for a given
    // width and height
    // returns the biggest available size
    private Size getBestSizeMatch(int desiredWidth, int desiredHeight, SortedSet<Size> sizes) {
        if(sizes == null || sizes.isEmpty()){
            return null;
        }

        Size result = sizes.last();

        // iterate from smallest to largest, and stay with the closest-biggest match
        if(desiredWidth != 0 && desiredHeight != 0){
            for (Size size : sizes) {
                if (desiredWidth <= size.getWidth() && desiredHeight <= size.getHeight()) {
                    result = size;
                    break;
                }
            }
        }

        return result;
    }


    @Override
    void setPictureSize(Size size) {

        // if no changes, don't do anything
        if(size == null && mPictureSize == null){
            return;
        }
        else if(size != null && size.equals(mPictureSize)){
            return;
        }

        mPictureSize = size;

        // if camera is opened, request parameters update
        if (isCameraOpened()) {
            mBgHandler.post(new Runnable() {
                @Override
                public void run() {
                    synchronized(Camera1.this){
                        if(mCamera != null){
                            adjustCameraParameters();
                        }
                    }
                }
            });
        }
    }

    @Override
    Size getPictureSize() {
        return mPictureSize;
    }

    @Override
    boolean setAspectRatio(final AspectRatio ratio) {
        if (mAspectRatio == null || !isCameraOpened()) {
            // Handle this later when camera is opened
            mAspectRatio = ratio;
            return true;
        } else if (!mAspectRatio.equals(ratio)) {
            final Set<Size> sizes = mPreviewSizes.sizes(ratio);
            if (sizes == null) {
                // do nothing, ratio remains unchanged. Consistent with Camera2 and initial mount behaviour
                Log.w("CAMERA_1::", "setAspectRatio received an unsupported value and will be ignored.");
            } else {
                mAspectRatio = ratio;
                mBgHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        synchronized(Camera1.this){
                            if(mCamera != null){
                                adjustCameraParameters();
                            }
                        }
                    }
                });
                return true;
            }
        }
        return false;
    }

    @Override
    AspectRatio getAspectRatio() {
        return mAspectRatio;
    }

    @Override
    void setAutoFocus(boolean autoFocus) {
        if (mAutoFocus == autoFocus) {
            return;
        }
        synchronized(this){
            if (setAutoFocusInternal(autoFocus)) {
                try{
                    if(mCamera != null){
                        mCamera.setParameters(mCameraParameters);
                    }
                }
                catch(RuntimeException e ) {
                    Log.e("CAMERA_1::", "setParameters failed", e);
                }
            }
        }
    }

    @Override
    boolean getAutoFocus() {
        if (!isCameraOpened()) {
            return mAutoFocus;
        }
        String focusMode = mCameraParameters.getFocusMode();
        return focusMode != null && focusMode.contains("continuous");
    }

    @Override
    void setFlash(int flash) {
        if (flash == mFlash) {
            return;
        }
        if (setFlashInternal(flash)) {
            try{
                if(mCamera != null){
                    mCamera.setParameters(mCameraParameters);
                }
            }
            catch(RuntimeException e ) {
                Log.e("CAMERA_1::", "setParameters failed", e);
            }
        }
    }

    @Override
    int getFlash() {
        return mFlash;
    }

    @Override
    float getExposureCompensation() {
        return mExposure;
    }

    @Override
    void setExposureCompensation(float exposure) {

        if (exposure == mExposure) {
            return;
        }
        if (setExposureInternal(exposure)) {
            try{
                if(mCamera != null){
                    mCamera.setParameters(mCameraParameters);
                }
            }
            catch(RuntimeException e ) {
                Log.e("CAMERA_1::", "setParameters failed", e);
            }
        }

    }

    @Override
    public void setFocusDepth(float value) {
        // not supported for Camera1
    }

    @Override
    float getFocusDepth() {
        return 0;
    }

    @Override
    void setZoom(float zoom) {
        if (zoom == mZoom) {
            return;
        }
        if (setZoomInternal(zoom)) {
            try{
                if(mCamera != null){
                    mCamera.setParameters(mCameraParameters);
                }
            }
            catch(RuntimeException e ) {
                Log.e("CAMERA_1::", "setParameters failed", e);
            }
        }
    }

    @Override
    float getZoom() {
        return mZoom;
    }


    @Override
    public void setWhiteBalance(int whiteBalance) {
        if (whiteBalance == mWhiteBalance) {
            return;
        }
        if (setWhiteBalanceInternal(whiteBalance)) {
            try{
                if(mCamera != null){
                    mCamera.setParameters(mCameraParameters);
                }
            }
            catch(RuntimeException e ) {
                Log.e("CAMERA_1::", "setParameters failed", e);
            }
        }
    }

    @Override
    public int getWhiteBalance() {
        return mWhiteBalance;
    }

    @Override
    void setScanning(boolean isScanning) {
        if (isScanning == mIsScanning) {
            return;
        }
        setScanningInternal(isScanning);
    }

    @Override
    boolean getScanning() {
        return mIsScanning;
    }

    @Override
    void takePicture(final ReadableMap options) {
        if (!isCameraOpened()) {
            throw new IllegalStateException(
                    "Camera is not ready. Call start() before takePicture().");
        }
        if (!mIsPreviewActive) {
            throw new IllegalStateException("Preview is paused - resume it before taking a picture.");
        }

        // UPDATE: Take picture right away instead of requesting/waiting for focus.
        // This will match closer what the native camera does,
        // and will capture whatever is on the preview without changing the camera focus.
        // This change will also help with autoFocusPointOfInterest not being usable to capture (Issue #2420)
        // and with takePicture never returning/resolving if the focus was reset (Issue #2421)
        takePictureInternal(options);
    }

    int orientationEnumToRotation(int orientation) {
        switch(orientation) {
            case Constants.ORIENTATION_UP:
                return 0;
            case Constants.ORIENTATION_DOWN:
                return 180;
            case Constants.ORIENTATION_LEFT:
                return 270;
            case Constants.ORIENTATION_RIGHT:
                return 90;
            default:
                return Constants.ORIENTATION_UP;
        }
    }

    int displayOrientationToOrientationEnum(int rotation) {
        switch (rotation) {
            case 0:
                return Constants.ORIENTATION_UP;
            case 90:
                return Constants.ORIENTATION_RIGHT;
            case 180:
                return Constants.ORIENTATION_DOWN;
            case 270:
                return Constants.ORIENTATION_LEFT;
            default:
                return 1;
        }
    }

    void takePictureInternal(final ReadableMap options) {
        // if not capturing already, atomically set it to true
        if (!mIsRecording.get() && isPictureCaptureInProgress.compareAndSet(false, true)) {

            try{
                if (options.hasKey("orientation") && options.getInt("orientation") != Constants.ORIENTATION_AUTO) {
                    mOrientation = options.getInt("orientation");
                    int rotation = orientationEnumToRotation(mOrientation);
                    mCameraParameters.setRotation(calcCameraRotation(rotation));
                    try{
                        mCamera.setParameters(mCameraParameters);
                    }
                    catch(RuntimeException e ) {
                        Log.e("CAMERA_1::", "setParameters rotation failed", e);
                    }
                }

                // set quality on capture since we might not process the image bitmap if not needed now.
                // This also achieves a much faster JPEG compression speed since it's done on the hardware
                if(options.hasKey("quality")){
                    mCameraParameters.setJpegQuality((int) (options.getDouble("quality") * 100));
                    try{
                        mCamera.setParameters(mCameraParameters);
                    }
                    catch(RuntimeException e ) {
                        Log.e("CAMERA_1::", "setParameters quality failed", e);
                    }
                }

                mCamera.takePicture(null, null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {

                        // this shouldn't be needed and messes up autoFocusPointOfInterest
                        // camera.cancelAutoFocus();

                        if(mPlaySoundOnCapture){
                            sound.play(MediaActionSound.SHUTTER_CLICK);
                        }

                        // our camera might have been released
                        // when this callback fires, so make sure we have
                        // exclusive access when restoring its preview
                        synchronized(Camera1.this){
                            if(mCamera != null){
                                if (options.hasKey("pauseAfterCapture") && !options.getBoolean("pauseAfterCapture")) {
                                    mCamera.startPreview();
                                    mIsPreviewActive = true;
                                    if (mIsScanning) {
                                        mCamera.setPreviewCallback(Camera1.this);
                                    }
                                } else {
                                    mCamera.stopPreview();
                                    mIsPreviewActive = false;
                                    mCamera.setPreviewCallback(null);
                                }
                            }
                        }

                        isPictureCaptureInProgress.set(false);

                        mOrientation = Constants.ORIENTATION_AUTO;
                        mCallback.onPictureTaken(data, displayOrientationToOrientationEnum(mDeviceOrientation));

                        if(mustUpdateSurface){
                            updateSurface();
                        }
                    }
                });
            }
            catch(Exception e){
                isPictureCaptureInProgress.set(false);
                throw e;
            }
        }
        else{
            throw new IllegalStateException("Camera capture failed. Camera is already capturing.");
        }
    }

    @Override
    boolean record(String path, int maxDuration, int maxFileSize, boolean recordAudio, CamcorderProfile profile, int orientation, int fps) {

        // make sure compareAndSet is last because we are setting it
        if (!isPictureCaptureInProgress.get() && mIsRecording.compareAndSet(false, true)) {
            if (orientation != Constants.ORIENTATION_AUTO) {
                mOrientation = orientation;
            }
            try {
                setUpMediaRecorder(path, maxDuration, maxFileSize, recordAudio, profile, fps);
                mMediaRecorder.prepare();
                mMediaRecorder.start();

                // after our media recorder is set and started, we must update
                // some camera parameters again because the recorder's exclusive access (after unlock is called)
                // might interfere with the camera parameters (e.g., flash and zoom)
                // This should also be safe to call since both recording and
                // camera parameters are getting set by the same thread and process.
                // https://stackoverflow.com/a/14855668/1777914
                try{
                    mCamera.setParameters(mCameraParameters);
                } catch (Exception e) {
                    Log.e("CAMERA_1::", "Record setParameters failed", e);
                }

                int deviceOrientation = displayOrientationToOrientationEnum(mDeviceOrientation);
                mCallback.onRecordingStart(path, mOrientation != Constants.ORIENTATION_AUTO ? mOrientation : deviceOrientation, deviceOrientation);


                return true;
            } catch (Exception e) {
                mIsRecording.set(false);
                Log.e("CAMERA_1::", "Record start failed", e);
                return false;
            }
        }
        return false;
    }

    @Override
    void stopRecording() {
        if (mIsRecording.compareAndSet(true, false)) {
            stopMediaRecorder();
            if (mCamera != null) {
                mCamera.lock();
            }
            if(mustUpdateSurface){
                updateSurface();
            }
        }
    }

    @Override
    void pauseRecording() {
        pauseMediaRecorder();
    }

    @Override
    void resumeRecording() {
        resumeMediaRecorder();
    }

    @Override
    int getCameraOrientation() {
        return mCameraInfo.orientation;
    }

    @Override
    void setDisplayOrientation(final int displayOrientation) {
        synchronized(this){
            if (mDisplayOrientation == displayOrientation) {
                return;
            }
            mDisplayOrientation = displayOrientation;
            if (isCameraOpened()) {
                boolean needsToStopPreview = mIsPreviewActive && Build.VERSION.SDK_INT < 14;
                if (needsToStopPreview) {
                    mCamera.stopPreview();
                    mIsPreviewActive = false;
                }

                try{
                    mCamera.setDisplayOrientation(calcDisplayOrientation(displayOrientation));
                }
                catch(RuntimeException e ) {
                    Log.e("CAMERA_1::", "setDisplayOrientation failed", e);
                }
                if (needsToStopPreview) {
                    startCameraPreview();
                }
            }
        }
    }

    @Override
    void setDeviceOrientation(final int deviceOrientation) {
        synchronized(this){
            if (mDeviceOrientation == deviceOrientation) {
                return;
            }
            mDeviceOrientation = deviceOrientation;
            if (isCameraOpened() && mOrientation == Constants.ORIENTATION_AUTO && !mIsRecording.get() && !isPictureCaptureInProgress.get()) {
                mCameraParameters.setRotation(calcCameraRotation(deviceOrientation));
                try{
                    mCamera.setParameters(mCameraParameters);
                }
                catch(RuntimeException e ) {
                    Log.e("CAMERA_1::", "setParameters failed", e);
                }
            }
        }
    }

    @Override
    public void setPreviewTexture(final SurfaceTexture surfaceTexture) {

        mBgHandler.post(new Runnable() {
            @Override
            public void run() {
                try{
                    if (mCamera == null) {
                        mPreviewTexture = surfaceTexture;
                        return;
                    }

                    mCamera.stopPreview();
                    mIsPreviewActive = false;

                    if (surfaceTexture == null) {
                        mCamera.setPreviewTexture((SurfaceTexture) mPreview.getSurfaceTexture());
                    } else {
                        mCamera.setPreviewTexture(surfaceTexture);
                    }

                    mPreviewTexture = surfaceTexture;
                    startCameraPreview();
                } catch (IOException e) {
                    Log.e("CAMERA_1::", "setPreviewTexture failed", e);
                }
            }
        });
    }

    @Override
    public Size getPreviewSize() {
        Camera.Size cameraSize = mCameraParameters.getPreviewSize();
        return new Size(cameraSize.width, cameraSize.height);
    }

    /**
     * This rewrites {@link #mCameraId} and {@link #mCameraInfo}.
     */
    private void chooseCamera() {
        if(_mCameraId == null){

            try{
                int count = Camera.getNumberOfCameras();
                if(count == 0){
                    //throw new RuntimeException("No camera available.");
                    mCameraId = INVALID_CAMERA_ID;
                    Log.w("CAMERA_1::", "getNumberOfCameras returned 0. No camera available.");
                    return;
                }

                for (int i = 0; i < count; i++) {
                    Camera.getCameraInfo(i, mCameraInfo);
                    if (mCameraInfo.facing == mFacing) {
                        mCameraId = i;
                        return;
                    }
                }
                // no camera found, set the one we have
                mCameraId = 0;
                Camera.getCameraInfo(mCameraId, mCameraInfo);
            }
            // getCameraInfo may fail if hardware is unavailable
            // and crash the whole app. Return INVALID_CAMERA_ID
            // which will in turn fire a mount error event
            catch(Exception e){
                Log.e("CAMERA_1::", "chooseCamera failed.", e);
                mCameraId = INVALID_CAMERA_ID;
            }
        }
        else{
            try{
                mCameraId = Integer.parseInt(_mCameraId);
                Camera.getCameraInfo(mCameraId, mCameraInfo);
            }
            catch(Exception e){
                mCameraId = INVALID_CAMERA_ID;
            }
        }
    }

    private boolean openCamera() {
        if (mCamera != null) {
            releaseCamera();
        }

        // in case we got an invalid camera ID
        // due to no cameras or invalid ID provided,
        // return false so we can raise a mount error
        if(mCameraId == INVALID_CAMERA_ID){
            return false;
        }

        try {
            mCamera = Camera.open(mCameraId);
            mCameraParameters = mCamera.getParameters();

            // Supported preview sizes
            mPreviewSizes.clear();
            for (Camera.Size size : mCameraParameters.getSupportedPreviewSizes()) {
                mPreviewSizes.add(new Size(size.width, size.height));
            }

            // Supported picture sizes;
            mPictureSizes.clear();
            for (Camera.Size size : mCameraParameters.getSupportedPictureSizes()) {
                mPictureSizes.add(new Size(size.width, size.height));
            }

            // to be consistent with Camera2, and to prevent crashes on some devices
            // do not allow preview sizes that are not also in the picture sizes set
            for (AspectRatio aspectRatio : mPreviewSizes.ratios()) {
                if (mPictureSizes.sizes(aspectRatio) == null) {
                    mPreviewSizes.remove(aspectRatio);
                }
            }

            // AspectRatio
            if (mAspectRatio == null) {
                mAspectRatio = Constants.DEFAULT_ASPECT_RATIO;
            }

            adjustCameraParameters();
            mCamera.setDisplayOrientation(calcDisplayOrientation(mDisplayOrientation));
            mCallback.onCameraOpened();
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private AspectRatio chooseAspectRatio() {
        AspectRatio r = null;
        for (AspectRatio ratio : mPreviewSizes.ratios()) {
            r = ratio;
            if (ratio.equals(Constants.DEFAULT_ASPECT_RATIO)) {
                return ratio;
            }
        }
        return r;
    }

    void adjustCameraParameters() {
        SortedSet<Size> sizes = mPreviewSizes.sizes(mAspectRatio);
        if (sizes == null) { // Not supported
            Log.w("CAMERA_1::", "adjustCameraParameters received an unsupported aspect ratio value and will be ignored.");
            mAspectRatio = chooseAspectRatio();
            sizes = mPreviewSizes.sizes(mAspectRatio);
        }

        // make sure both preview and picture size are always
        // valid for the currently chosen camera and aspect ratio
        Size size = chooseOptimalSize(sizes);
        Size pictureSize = null;

        // do not alter mPictureSize
        // since it may be valid for other camera/aspect ratio updates
        // just make sure we get the right and most suitable value
        if(mPictureSize != null){
            pictureSize = getBestSizeMatch(
                mPictureSize.getWidth(),
                mPictureSize.getHeight(),
                mPictureSizes.sizes(mAspectRatio)
            );
        }
        else{
            pictureSize = getBestSizeMatch(
                0,
                0,
                mPictureSizes.sizes(mAspectRatio)
            );
        }

        boolean needsToStopPreview = mIsPreviewActive;
        if (needsToStopPreview) {
            mCamera.stopPreview();
            mIsPreviewActive = false;
        }
        mCameraParameters.setPreviewSize(size.getWidth(), size.getHeight());
        mCameraParameters.setPictureSize(pictureSize.getWidth(), pictureSize.getHeight());
        if (mOrientation != Constants.ORIENTATION_AUTO) {
            mCameraParameters.setRotation(calcCameraRotation(orientationEnumToRotation(mOrientation)));
        } else {
            mCameraParameters.setRotation(calcCameraRotation(mDeviceOrientation));
        }

        setAutoFocusInternal(mAutoFocus);
        setFlashInternal(mFlash);
        setExposureInternal(mExposure);
        setAspectRatio(mAspectRatio);
        setZoomInternal(mZoom);
        setWhiteBalanceInternal(mWhiteBalance);
        setScanningInternal(mIsScanning);
        setPlaySoundInternal(mPlaySoundOnCapture);

        try{
            mCamera.setParameters(mCameraParameters);
        }
        catch(RuntimeException e ) {
            Log.e("CAMERA_1::", "setParameters failed", e);
        }
        if (needsToStopPreview) {
            startCameraPreview();
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private Size chooseOptimalSize(SortedSet<Size> sizes) {
        if (!mPreview.isReady()) { // Not yet laid out
            return sizes.first(); // Return the smallest size
        }
        int desiredWidth;
        int desiredHeight;
        final int surfaceWidth = mPreview.getWidth();
        final int surfaceHeight = mPreview.getHeight();
        if (isLandscape(mDisplayOrientation)) {
            desiredWidth = surfaceHeight;
            desiredHeight = surfaceWidth;
        } else {
            desiredWidth = surfaceWidth;
            desiredHeight = surfaceHeight;
        }
        Size result = null;
        for (Size size : sizes) { // Iterate from small to large
            if (desiredWidth <= size.getWidth() && desiredHeight <= size.getHeight()) {
                return size;

            }
            result = size;
        }
        return result;
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
            mCallback.onCameraClosed();

            // reset these flags
            isPictureCaptureInProgress.set(false);
            mIsRecording.set(false);
        }
    }

    // Most credit: https://github.com/CameraKit/camerakit-android/blob/master/camerakit-core/src/main/api16/com/wonderkiln/camerakit/Camera1.java
    void setFocusArea(final float x, final float y) {
        mBgHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized(Camera1.this){
                    if (mCamera != null) {

                        // do not create a new object, use existing.
                        Camera.Parameters parameters = mCameraParameters;

                        if (parameters == null) return;

                        String focusMode = parameters.getFocusMode();
                        Rect rect = calculateFocusArea(x, y);

                        List<Camera.Area> meteringAreas = new ArrayList<>();
                        meteringAreas.add(new Camera.Area(rect, FOCUS_METERING_AREA_WEIGHT_DEFAULT));

                        if (parameters.getMaxNumFocusAreas() != 0 && focusMode != null &&
                                (focusMode.equals(Camera.Parameters.FOCUS_MODE_AUTO) ||
                                        focusMode.equals(Camera.Parameters.FOCUS_MODE_MACRO) ||
                                        focusMode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) ||
                                        focusMode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
                                ) {
                            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                            parameters.setFocusAreas(meteringAreas);
                            if (parameters.getMaxNumMeteringAreas() > 0) {
                                parameters.setMeteringAreas(meteringAreas);
                            }
                            if (!parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                                return; //cannot autoFocus
                            }
                            try{
                                mCamera.setParameters(parameters);
                            }
                            catch(RuntimeException e ) {
                                Log.e("CAMERA_1::", "setParameters failed", e);
                            }

                            try{
                                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                                    @Override
                                    public void onAutoFocus(boolean success, Camera camera) {
                                        //resetFocus(success, camera);
                                    }
                                });
                            }
                            catch(RuntimeException e ) {
                                Log.e("CAMERA_1::", "autoFocus failed", e);
                            }
                        } else if (parameters.getMaxNumMeteringAreas() > 0) {
                            if (!parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                                return; //cannot autoFocus
                            }
                            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                            parameters.setFocusAreas(meteringAreas);
                            parameters.setMeteringAreas(meteringAreas);

                            try{
                                mCamera.setParameters(parameters);
                            }
                            catch(RuntimeException e ) {
                                Log.e("CAMERA_1::", "setParameters failed", e);
                            }

                            try{
                                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                                    @Override
                                    public void onAutoFocus(boolean success, Camera camera) {
                                        //resetFocus(success, camera);
                                    }
                                });
                            }
                            catch(RuntimeException e ) {
                                Log.e("CAMERA_1::", "autoFocus failed", e);
                            }
                        } else {
                            try{
                                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                                    @Override
                                    public void onAutoFocus(boolean success, Camera camera) {
                                        //mCamera.cancelAutoFocus();
                                    }
                                });
                            }
                            catch(RuntimeException e ) {
                                Log.e("CAMERA_1::", "autoFocus failed", e);
                            }
                        }
                    }
                }
            }
        });
    }

    private void resetFocus(final boolean success, final Camera camera) {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mCamera != null) {
                    mCamera.cancelAutoFocus();

                    // do not create a new object, use existing.
                    Camera.Parameters parameters = mCameraParameters;

                    if (parameters == null) return;

                    if (parameters.getFocusMode() != Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                        parameters.setFocusAreas(null);
                        parameters.setMeteringAreas(null);
                        try{
                          mCamera.setParameters(parameters);
                        }
                        catch(RuntimeException e ) {
                          Log.e("CAMERA_1::", "setParameters failed", e);
                        }
                    }

                    mCamera.cancelAutoFocus();
                }
            }
        }, DELAY_MILLIS_BEFORE_RESETTING_FOCUS);
    }

    private Rect calculateFocusArea(float x, float y) {
        int padding = FOCUS_AREA_SIZE_DEFAULT / 2;
        int centerX = (int) (x * 2000);
        int centerY = (int) (y * 2000);

        int left = centerX - padding;
        int top = centerY - padding;
        int right = centerX + padding;
        int bottom = centerY + padding;

        if (left < 0) left = 0;
        if (right > 2000) right = 2000;
        if (top < 0) top = 0;
        if (bottom > 2000) bottom = 2000;

        return new Rect(left - 1000, top - 1000, right - 1000, bottom - 1000);
    }

    /**
     * Calculate display orientation
     * https://developer.android.com/reference/android/hardware/Camera.html#setDisplayOrientation(int)
     *
     * This calculation is used for orienting the preview
     *
     * Note: This is not the same calculation as the camera rotation
     *
     * @param screenOrientationDegrees Screen orientation in degrees
     * @return Number of degrees required to rotate preview
     */
    private int calcDisplayOrientation(int screenOrientationDegrees) {
        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return (360 - (mCameraInfo.orientation + screenOrientationDegrees) % 360) % 360;
        } else {  // back-facing
            return (mCameraInfo.orientation - screenOrientationDegrees + 360) % 360;
        }
    }

    /**
     * Calculate camera rotation
     *
     * This calculation is applied to the output JPEG either via Exif Orientation tag
     * or by actually transforming the bitmap. (Determined by vendor camera API implementation)
     *
     * Note: This is not the same calculation as the display orientation
     *
     * @param screenOrientationDegrees Screen orientation in degrees
     * @return Number of degrees to rotate image in order for it to view correctly.
     */
    private int calcCameraRotation(int screenOrientationDegrees) {
       if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
           return (mCameraInfo.orientation + screenOrientationDegrees) % 360;
       }
       // back-facing
       final int landscapeFlip = isLandscape(screenOrientationDegrees) ? 180 : 0;
       return (mCameraInfo.orientation + screenOrientationDegrees + landscapeFlip) % 360;
    }

    /**
     * Test if the supplied orientation is in landscape.
     *
     * @param orientationDegrees Orientation in degrees (0,90,180,270)
     * @return True if in landscape, false if portrait
     */
    private boolean isLandscape(int orientationDegrees) {
        return (orientationDegrees == Constants.LANDSCAPE_90 ||
                orientationDegrees == Constants.LANDSCAPE_270);
    }

    /**
     * @return {@code true} if {@link #mCameraParameters} was modified.
     */
    private boolean setAutoFocusInternal(boolean autoFocus) {
        mAutoFocus = autoFocus;
        if (isCameraOpened()) {
            final List<String> modes = mCameraParameters.getSupportedFocusModes();
            if (autoFocus && modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else if (mIsScanning && modes.contains(Camera.Parameters.FOCUS_MODE_MACRO)) {
                mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
            } else if (modes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
                mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
            } else if (modes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
                mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
            } else {
                mCameraParameters.setFocusMode(modes.get(0));
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return {@code true} if {@link #mCameraParameters} was modified.
     */
    private boolean setFlashInternal(int flash) {
        if (isCameraOpened()) {
            List<String> modes = mCameraParameters.getSupportedFlashModes();
            String mode = FLASH_MODES.get(flash);
            if(modes == null) {
                return false;
            }
            if (modes.contains(mode)) {
                mCameraParameters.setFlashMode(mode);
                mFlash = flash;
                return true;
            }
            String currentMode = FLASH_MODES.get(mFlash);
            if (!modes.contains(currentMode)) {
                mCameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                return true;
            }
            return false;
        } else {
            mFlash = flash;
            return false;
        }
    }

    private boolean setExposureInternal(float exposure) {
        mExposure = exposure;
        if (isCameraOpened()){
            int minExposure = mCameraParameters.getMinExposureCompensation();
            int maxExposure = mCameraParameters.getMaxExposureCompensation();

            if (minExposure != maxExposure) {
                int scaledValue = 0;
                if (mExposure >= 0 && mExposure <= 1) {
                    scaledValue = (int) (mExposure * (maxExposure - minExposure)) + minExposure;
                }

                mCameraParameters.setExposureCompensation(scaledValue);
                return true;
            }
        }
        return false;
    }


    /**
     * @return {@code true} if {@link #mCameraParameters} was modified.
     */
    private boolean setZoomInternal(float zoom) {
        if (isCameraOpened() && mCameraParameters.isZoomSupported()) {
            int maxZoom = mCameraParameters.getMaxZoom();
            int scaledValue = (int) (zoom * maxZoom);
            mCameraParameters.setZoom(scaledValue);
            mZoom = zoom;
            return true;
        } else {
            mZoom = zoom;
            return false;
        }
    }

    /**
     * @return {@code true} if {@link #mCameraParameters} was modified.
     */
    private boolean setWhiteBalanceInternal(int whiteBalance) {
        mWhiteBalance = whiteBalance;
        if (isCameraOpened()) {
            final List<String> modes = mCameraParameters.getSupportedWhiteBalance();
            String mode = WB_MODES.get(whiteBalance);
            if (modes != null && modes.contains(mode)) {
                mCameraParameters.setWhiteBalance(mode);
                return true;
            }
            String currentMode = WB_MODES.get(mWhiteBalance);
            if (modes == null || !modes.contains(currentMode)) {
                mCameraParameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    private void setScanningInternal(boolean isScanning) {
        mIsScanning = isScanning;
        if (isCameraOpened()) {
            if (mIsScanning) {
                mCamera.setPreviewCallback(this);
            } else {
                mCamera.setPreviewCallback(null);
            }
        }
    }

    private void setPlaySoundInternal(boolean playSoundOnCapture){
        mPlaySoundOnCapture = playSoundOnCapture;
        if(mCamera != null){
            try{
                // Always disable shutter sound, and play our own.
                // This is because not all devices honor this value when set to true
                boolean res = mCamera.enableShutterSound(false);

                // if we fail to disable the shutter sound
                // set mPlaySoundOnCapture to false since it means
                // we cannot change it and the system will play it
                // playing the sound ourselves also makes it consistent with Camera2
                if(!res){
                    mPlaySoundOnCapture = false;
                }
            }
            catch(Exception ex){
                Log.e("CAMERA_1::", "setPlaySoundInternal failed", ex);
                mPlaySoundOnCapture = false;
            }
        }
    }

    @Override
    void setPlaySoundOnCapture(boolean playSoundOnCapture) {
        if (playSoundOnCapture == mPlaySoundOnCapture) {
            return;
        }
        setPlaySoundInternal(playSoundOnCapture);
    }

    @Override
    public boolean getPlaySoundOnCapture(){
        return mPlaySoundOnCapture;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Camera.Size previewSize = mCameraParameters.getPreviewSize();
        mCallback.onFramePreview(data, previewSize.width, previewSize.height, mDeviceOrientation);
    }

    private void setUpMediaRecorder(String path, int maxDuration, int maxFileSize, boolean recordAudio, CamcorderProfile profile, int fps) {

        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();

        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        if (recordAudio) {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        }

        mMediaRecorder.setOutputFile(path);
        mVideoPath = path;

        CamcorderProfile camProfile;
        if (CamcorderProfile.hasProfile(mCameraId, profile.quality)) {
            camProfile = CamcorderProfile.get(mCameraId, profile.quality);
        } else {
            camProfile = CamcorderProfile.get(mCameraId, CamcorderProfile.QUALITY_HIGH);
        }
        camProfile.videoBitRate = profile.videoBitRate;
        setCamcorderProfile(camProfile, recordAudio, fps);

        mMediaRecorder.setOrientationHint(calcCameraRotation(mOrientation != Constants.ORIENTATION_AUTO ? orientationEnumToRotation(mOrientation) : mDeviceOrientation));

        if (maxDuration != -1) {
            mMediaRecorder.setMaxDuration(maxDuration);
        }
        if (maxFileSize != -1) {
            mMediaRecorder.setMaxFileSize(maxFileSize);
        }

        mMediaRecorder.setOnInfoListener(this);
        mMediaRecorder.setOnErrorListener(this);

    }

    private void stopMediaRecorder() {

        synchronized(this){
            if (mMediaRecorder != null) {
                try {
                    mMediaRecorder.stop();
                } catch (RuntimeException ex) {
                    Log.e("CAMERA_1::", "stopMediaRecorder stop failed", ex);
                }

                try{
                    mMediaRecorder.reset();
                    mMediaRecorder.release();
                } catch (RuntimeException ex) {
                    Log.e("CAMERA_1::", "stopMediaRecorder reset failed", ex);
                }

                mMediaRecorder = null;
            }

            mCallback.onRecordingEnd();

            int deviceOrientation = displayOrientationToOrientationEnum(mDeviceOrientation);

            if (mVideoPath == null || !new File(mVideoPath).exists()) {
                mCallback.onVideoRecorded(null, mOrientation != Constants.ORIENTATION_AUTO ? mOrientation : deviceOrientation, deviceOrientation);
                return;
            }

            mCallback.onVideoRecorded(mVideoPath, mOrientation != Constants.ORIENTATION_AUTO ? mOrientation : deviceOrientation, deviceOrientation);
            mVideoPath = null;
        }
    }

    private void pauseMediaRecorder() {
        if (Build.VERSION.SDK_INT >= 24) {
            mMediaRecorder.pause();
        }
    }

    private void resumeMediaRecorder() {
        if (Build.VERSION.SDK_INT >= 24) {
            mMediaRecorder.resume();
        }
    }

    @Override
    public ArrayList<int[]> getSupportedPreviewFpsRange() {
      return (ArrayList<int[]>) mCameraParameters.getSupportedPreviewFpsRange();
    }

    private boolean isCompatibleWithDevice(int fps) {
        ArrayList<int[]> validValues;
        validValues = getSupportedPreviewFpsRange();
        int accurate_fps = fps * 1000;
        for(int[] row : validValues) {
            boolean is_included = accurate_fps >= row[0] && accurate_fps <= row[1];
            boolean greater_then_zero = accurate_fps > 0;
            boolean compatible_with_device = is_included && greater_then_zero;
            if (compatible_with_device) return true;
        }
        Log.w("CAMERA_1::", "fps (framePerSecond) received an unsupported value and will be ignored.");
        return false;
    }

    private void setCamcorderProfile(CamcorderProfile profile, boolean recordAudio, int fps) {
        int compatible_fps = isCompatibleWithDevice(fps) ? fps : profile.videoFrameRate;
        mMediaRecorder.setOutputFormat(profile.fileFormat);
        mMediaRecorder.setVideoFrameRate(compatible_fps);
        mMediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
        mMediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);
        mMediaRecorder.setVideoEncoder(profile.videoCodec);
        if (recordAudio) {
            mMediaRecorder.setAudioEncodingBitRate(profile.audioBitRate);
            mMediaRecorder.setAudioChannels(profile.audioChannels);
            mMediaRecorder.setAudioSamplingRate(profile.audioSampleRate);
            mMediaRecorder.setAudioEncoder(profile.audioCodec);
        }
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        if ( what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED ||
              what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
            stopRecording();
        }
    }


    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        stopRecording();
    }
}
