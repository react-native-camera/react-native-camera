/**
 * Created by Fabrice Armisen (farmisen@gmail.com) on 1/4/16.
 * Android video recording support by Marc Johnson (me@marc.mn) 4/2016
 */

package com.lwansbrough.RCTCamera;

import android.content.ContentValues;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaActionSound;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import com.facebook.react.bridge.*;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.lwansbrough.JavaCamera.CameraModule;

public class RCTCameraModule extends ReactContextBaseJavaModule
    implements MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener, LifecycleEventListener {
    private static final String TAG = "RCTCameraModule";

    private CameraModule cameraModule;

    private static ReactApplicationContext _reactContext;
    private RCTSensorOrientationChecker _sensorOrientationChecker;
    private MediaActionSound sound = new MediaActionSound();

    private MediaRecorder mMediaRecorder;
    private long MRStartTime;
    private File mVideoFile;
    private Camera mCamera = null;
    private Promise mRecordingPromise = null;
    private ReadableMap mRecordingOptions;
    private Boolean mSafeToCapture = true;


    public RCTCameraModule(ReactApplicationContext reactContext) {
        super(reactContext);
        _reactContext = reactContext;

        _sensorOrientationChecker = new RCTSensorOrientationChecker(_reactContext);
        _reactContext.addLifecycleEventListener(this);
        sound.load(MediaActionSound.SHUTTER_CLICK);

        cameraModule = new CameraModule(_reactContext);
    }

    public static ReactApplicationContext getReactContextSingleton() {
      return _reactContext;
    }

    /**
     * Callback invoked on new MediaRecorder info.
     *
     * See https://developer.android.com/reference/android/media/MediaRecorder.OnInfoListener.html
     * for more information.
     *
     * @param mr MediaRecorder instance for which this callback is being invoked.
     * @param what Type of info we have received.
     * @param extra Extra code, specific to the info type.
     */
    public void onInfo(MediaRecorder mr, int what, int extra) {
        if ( what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED ||
                what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
            if (mRecordingPromise != null) {
                releaseMediaRecorder(); // release the MediaRecorder object and resolve promise
            }
        }
    }

    /**
     * Callback invoked when a MediaRecorder instance encounters an error while recording.
     *
     * See https://developer.android.com/reference/android/media/MediaRecorder.OnErrorListener.html
     * for more information.
     *
     * @param mr MediaRecorder instance for which this callback is being invoked.
     * @param what Type of error that has occurred.
     * @param extra Extra code, specific to the error type.
     */
    public void onError(MediaRecorder mr, int what, int extra) {
        // On any error, release the MediaRecorder object and resolve promise. In particular, this
        // prevents leaving the camera in an unrecoverable state if we crash in the middle of
        // recording.
        if (mRecordingPromise != null) {
            releaseMediaRecorder();
        }
    }

    @Override
    public String getName() {
        return "RCTCameraModule";
    }

    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        return Collections.unmodifiableMap(new HashMap<String, Object>() {
            {
                put("Aspect", getAspectConstants());
                put("BarCodeType", getBarCodeConstants());
                put("Type", getTypeConstants());
                put("CaptureQuality", getCaptureQualityConstants());
                put("CaptureMode", getCaptureModeConstants());
                put("CaptureTarget", getCaptureTargetConstants());
                put("Orientation", getOrientationConstants());
                put("FlashMode", getFlashModeConstants());
                put("TorchMode", getTorchModeConstants());
            }

            private Map<String, Object> getAspectConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("stretch", RCTCameraUtils.RCT_CAMERA_ASPECT_STRETCH);
                        put("fit", RCTCameraUtils.RCT_CAMERA_ASPECT_FIT);
                        put("fill", RCTCameraUtils.RCT_CAMERA_ASPECT_FILL);
                    }
                });
            }

            private Map<String, Object> getBarCodeConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        // @TODO add barcode types
                    }
                });
            }

            private Map<String, Object> getTypeConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("front", RCTCameraUtils.RCT_CAMERA_TYPE_FRONT);
                        put("back", RCTCameraUtils.RCT_CAMERA_TYPE_BACK);
                    }
                });
            }

            private Map<String, Object> getCaptureQualityConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("low", RCTCameraUtils.RCT_CAMERA_CAPTURE_QUALITY_LOW);
                        put("medium", RCTCameraUtils.RCT_CAMERA_CAPTURE_QUALITY_MEDIUM);
                        put("high", RCTCameraUtils.RCT_CAMERA_CAPTURE_QUALITY_HIGH);
                        put("photo", RCTCameraUtils.RCT_CAMERA_CAPTURE_QUALITY_HIGH);
                        put("preview", RCTCameraUtils.RCT_CAMERA_CAPTURE_QUALITY_PREVIEW);
                        put("480p", RCTCameraUtils.RCT_CAMERA_CAPTURE_QUALITY_480P);
                        put("720p", RCTCameraUtils.RCT_CAMERA_CAPTURE_QUALITY_720P);
                        put("1080p", RCTCameraUtils.RCT_CAMERA_CAPTURE_QUALITY_1080P);
                    }
                });
            }

            private Map<String, Object> getCaptureModeConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("still", RCTCameraUtils.RCT_CAMERA_CAPTURE_MODE_STILL);
                        put("video", RCTCameraUtils.RCT_CAMERA_CAPTURE_MODE_VIDEO);
                    }
                });
            }

            private Map<String, Object> getCaptureTargetConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("memory", RCTCameraUtils.RCT_CAMERA_CAPTURE_TARGET_MEMORY);
                        put("disk", RCTCameraUtils.RCT_CAMERA_CAPTURE_TARGET_DISK);
                        put("cameraRoll", RCTCameraUtils.RCT_CAMERA_CAPTURE_TARGET_CAMERA_ROLL);
                        put("temp", RCTCameraUtils.RCT_CAMERA_CAPTURE_TARGET_TEMP);
                    }
                });
            }

            private Map<String, Object> getOrientationConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("auto", RCTCameraUtils.RCT_CAMERA_ORIENTATION_AUTO);
                        put("landscapeLeft", RCTCameraUtils.RCT_CAMERA_ORIENTATION_LANDSCAPE_LEFT);
                        put("landscapeRight", RCTCameraUtils.RCT_CAMERA_ORIENTATION_LANDSCAPE_RIGHT);
                        put("portrait", RCTCameraUtils.RCT_CAMERA_ORIENTATION_PORTRAIT);
                        put("portraitUpsideDown", RCTCameraUtils.RCT_CAMERA_ORIENTATION_PORTRAIT_UPSIDE_DOWN);
                    }
                });
            }

            private Map<String, Object> getFlashModeConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("off", RCTCameraUtils.RCT_CAMERA_FLASH_MODE_OFF);
                        put("on", RCTCameraUtils.RCT_CAMERA_FLASH_MODE_ON);
                        put("auto", RCTCameraUtils.RCT_CAMERA_FLASH_MODE_AUTO);
                    }
                });
            }

            private Map<String, Object> getTorchModeConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("off", RCTCameraUtils.RCT_CAMERA_TORCH_MODE_OFF);
                        put("on", RCTCameraUtils.RCT_CAMERA_TORCH_MODE_ON);
                        put("auto", RCTCameraUtils.RCT_CAMERA_TORCH_MODE_AUTO);
                    }
                });
            }
        });
    }



    /**
     * Prepare media recorder for video capture.
     *
     * See "Capturing Videos" at https://developer.android.com/guide/topics/media/camera.html for
     * a guideline of steps and more information in general.
     *
     * @param options Options.
     * @return Throwable; null if no errors.
     */
    private Throwable prepareMediaRecorder(ReadableMap options) {
        // Prepare CamcorderProfile instance, setting essential options.
        CamcorderProfile cm = RCTCamera.getInstance().setCaptureVideoQuality(options.getInt("type"), options.getString("quality"));
        if (cm == null) {
            return new RuntimeException("CamcorderProfile not found in prepareMediaRecorder.");
        }

        // Unlock camera to make available for MediaRecorder. Note that this statement must be
        // executed before calling setCamera when configuring the MediaRecorder instance.
        mCamera.unlock();

        // Create new MediaRecorder instance.
        mMediaRecorder = new MediaRecorder();

        // Attach callback to handle maxDuration (@see onInfo method in this file).
        mMediaRecorder.setOnInfoListener(this);
        // Attach error listener (@see onError method in this file).
        mMediaRecorder.setOnErrorListener(this);

        // Set camera.
        mMediaRecorder.setCamera(mCamera);

        // Set AV sources.
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Adjust for orientation.
        mMediaRecorder.setOrientationHint(RCTCamera.getInstance().getAdjustedDeviceOrientation());

        // Set video output format and encoding using CamcorderProfile.
        cm.fileFormat = MediaRecorder.OutputFormat.MPEG_4;
        mMediaRecorder.setProfile(cm);

        // Set video output file.
        mVideoFile = null;
        switch (options.getInt("target")) {
            case RCTCameraUtils.RCT_CAMERA_CAPTURE_TARGET_MEMORY:
                mVideoFile = getTempMediaFile(RCTCameraUtils.MEDIA_TYPE_VIDEO); // temporarily
                break;
            case RCTCameraUtils.RCT_CAMERA_CAPTURE_TARGET_CAMERA_ROLL:
                mVideoFile = getOutputCameraRollFile(RCTCameraUtils.MEDIA_TYPE_VIDEO);
                break;
            case RCTCameraUtils.RCT_CAMERA_CAPTURE_TARGET_TEMP:
                mVideoFile = getTempMediaFile(RCTCameraUtils.MEDIA_TYPE_VIDEO);
                break;
            default:
            case RCTCameraUtils.RCT_CAMERA_CAPTURE_TARGET_DISK:
                mVideoFile = getOutputMediaFile(RCTCameraUtils.MEDIA_TYPE_VIDEO);
                break;
        }
        if (mVideoFile == null) {
            return new RuntimeException("Error while preparing output file in prepareMediaRecorder.");
        }
        mMediaRecorder.setOutputFile(mVideoFile.getPath());

        if (options.hasKey("totalSeconds")) {
            int totalSeconds = options.getInt("totalSeconds");
            mMediaRecorder.setMaxDuration(totalSeconds * 1000);
        }

        if (options.hasKey("maxFileSize")) {
            int maxFileSize = options.getInt("maxFileSize");
            mMediaRecorder.setMaxFileSize(maxFileSize);
        }

        // Prepare the MediaRecorder instance with the provided configuration settings.
        try {
            mMediaRecorder.prepare();
        } catch (Exception ex) {
            Log.e(TAG, "Media recorder prepare error.", ex);
            releaseMediaRecorder();
            return ex;
        }

        return null;
    }

    private void record(final ReadableMap options, final Promise promise) {
        if (mRecordingPromise != null) {
            return;
        }

        mCamera = RCTCamera.getInstance().acquireCameraInstance(options.getInt("type"));
        if (mCamera == null) {
            promise.reject(new RuntimeException("No camera found."));
            return;
        }

        Throwable prepareError = prepareMediaRecorder(options);
        if (prepareError != null) {
            promise.reject(prepareError);
            return;
        }

        try {
            mMediaRecorder.start();
            MRStartTime =  System.currentTimeMillis();
            mRecordingOptions = options;
            mRecordingPromise = promise;  // only got here if mediaRecorder started
        } catch (Exception ex) {
            Log.e(TAG, "Media recorder start error.", ex);
            promise.reject(ex);
        }
    }



    /**
     * Release media recorder following video capture (or failure to start recording session).
     *
     * See "Capturing Videos" at https://developer.android.com/guide/topics/media/camera.html for
     * a guideline of steps and more information in general.
     */
    private void releaseMediaRecorder() {
        // Must record at least a second or MediaRecorder throws exceptions on some platforms
        long duration = System.currentTimeMillis() - MRStartTime;
        if (duration < 1500) {
            try {
                Thread.sleep(1500 - duration);
            } catch(InterruptedException ex) {
                Log.e(TAG, "releaseMediaRecorder thread sleep error.", ex);
            }
        }

        // Release actual MediaRecorder instance.
        if (mMediaRecorder != null) {
            // Stop recording video.
            try {
                mMediaRecorder.stop(); // stop the recording
            } catch (RuntimeException ex) {
                Log.e(TAG, "Media recorder stop error.", ex);
            }

            // Optionally, remove the configuration settings from the recorder.
            mMediaRecorder.reset();

            // Release the MediaRecorder.
            mMediaRecorder.release();

            // Reset variable.
            mMediaRecorder = null;
        }

        // Lock the camera so that future MediaRecorder sessions can use it by calling
        // Camera.lock(). Note this is not required on Android 4.0+ unless the
        // MediaRecorder.prepare() call fails.
        if (mCamera != null) {
            mCamera.lock();
        }

        if (mRecordingPromise == null) {
            return;
        }

        File f = new File(mVideoFile.getPath());
        if (!f.exists()) {
            mRecordingPromise.reject(new RuntimeException("There is nothing recorded."));
            mRecordingPromise = null;
            return;
        }

        f.setReadable(true, false); // so mediaplayer can play it
        f.setWritable(true, false); // so can clean it up

        WritableMap response = new WritableNativeMap();
        switch (mRecordingOptions.getInt("target")) {
            case RCTCameraUtils.RCT_CAMERA_CAPTURE_TARGET_MEMORY:
                byte[] encoded = convertFileToByteArray(mVideoFile);
                response.putString("data", new String(encoded, Base64.DEFAULT));
                mRecordingPromise.resolve(response);
                f.delete();
                break;
            case RCTCameraUtils.RCT_CAMERA_CAPTURE_TARGET_CAMERA_ROLL:
                ContentValues values = new ContentValues();
                values.put(MediaStore.Video.Media.DATA, mVideoFile.getPath());
                values.put(MediaStore.Video.Media.TITLE, mRecordingOptions.hasKey("title") ? mRecordingOptions.getString("title") : "video");

                if (mRecordingOptions.hasKey("description")) {
                    values.put(MediaStore.Video.Media.DESCRIPTION, mRecordingOptions.hasKey("description"));
                }

                if (mRecordingOptions.hasKey("latitude")) {
                    values.put(MediaStore.Video.Media.LATITUDE, mRecordingOptions.getString("latitude"));
                }

                if (mRecordingOptions.hasKey("longitude")) {
                    values.put(MediaStore.Video.Media.LONGITUDE, mRecordingOptions.getString("longitude"));
                }

                values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
                _reactContext.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                addToMediaStore(mVideoFile.getAbsolutePath());
                response.putString("path", Uri.fromFile(mVideoFile).toString());
                mRecordingPromise.resolve(response);
                break;
            case RCTCameraUtils.RCT_CAMERA_CAPTURE_TARGET_TEMP:
            case RCTCameraUtils.RCT_CAMERA_CAPTURE_TARGET_DISK:
                response.putString("path", Uri.fromFile(mVideoFile).toString());
                mRecordingPromise.resolve(response);
        }

        mRecordingPromise = null;
    }

    public static byte[] convertFileToByteArray(File f)
    {
        byte[] byteArray = null;
        try
        {
            InputStream inputStream = new FileInputStream(f);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024*8];
            int bytesRead;

            while ((bytesRead = inputStream.read(b)) != -1) {
                bos.write(b, 0, bytesRead);
            }

            byteArray = bos.toByteArray();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return byteArray;
    }



    //region CAPTURE
    @ReactMethod
    public void capture(final ReadableMap options, final Promise promise)throws Exception {

        int orientation = options.getInt("orientation");

        orientation = RCTCameraUtils.RCT_CAMERA_ORIENTATION_LANDSCAPE_LEFT;

        int type = options.getInt("type");

        String quality = options.getString("quality");
        Boolean playSoundOnCapture = options.getBoolean("playSoundOnCapture");
        int mode = options.getInt("mode");
        Boolean fixOrientation = options.getBoolean("fixOrientation");

        fixOrientation = false;

        int jpegQuality = options.getInt("jpegQuality");
        int target = options.getInt("target");
        double latitude = options.getDouble("latitude");
        double longitude = options.getDouble("longitude");

       cameraModule.__capture(sound, promise);
    }

//    private void captureWithOrientation(final ReadableMap options, final Promise promise, int deviceOrientation) {
//         Camera camera = RCTCamera.getInstance().acquireCameraInstance(options.getInt("type"));
//        if (null == camera) {
//            promise.reject("No camera found.");
//            return;
//        }
//
//        if (options.getInt("mode") == RCTCameraUtils.RCT_CAMERA_CAPTURE_MODE_VIDEO) {
//            record(options, promise);
//            return;
//        }
//
//        RCTCamera.getInstance().setCaptureQuality(options.getInt("type"), options.getString("quality"));
//
//        if (options.hasKey("playSoundOnCapture") && options.getBoolean("playSoundOnCapture")) {
//            sound.play(MediaActionSound.SHUTTER_CLICK);
//        }
//
//        if (options.hasKey("quality")) {
//            RCTCamera.getInstance().setCaptureQuality(options.getInt("type"), options.getString("quality"));
//        }
//
//        RCTCamera.getInstance().adjustCameraRotationToDeviceOrientation(options.getInt("type"), deviceOrientation);
//        camera.setPreviewCallback(null);
//
//        Camera.PictureCallback captureCallback = new Camera.PictureCallback() {
//
//            @Override
//            public void onPictureTaken(final byte[] data, Camera camera) {
//                camera.stopPreview();
//                camera.startPreview();
//
//                AsyncTask.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        processImage(new MutableImage(data), options, promise);
//                    }
//                });
//
//                mSafeToCapture = true;
//            }
//        };
//
//        if(mSafeToCapture) {
//            try {
//                camera.takePicture(null, null, captureCallback);
//                mSafeToCapture = false;
//            } catch(RuntimeException ex) {
//                Log.e(TAG, "Couldn't capture photo.", ex);
//            }
//        }
//    }

//    /**
//     * synchronized in order to prevent the user crashing the app by taking many photos and them all being processed
//     * concurrently which would blow the memory (esp on smaller devices), and slow things down.
//     */
//    private synchronized void processImage(MutableImage mutableImage, ReadableMap options, Promise promise) {
//        Boolean fixOrientation = options.getBoolean("fixOrientation");
//        int jpegQuality = options.getInt("jpegQuality");
//        int target = options.getInt("target");
//
//         __processImage(mutableImage, fixOrientation, jpegQuality, target);
//    }
    //endregion



    //region STOP CAPTURE
    @ReactMethod
    public void stopCapture(final Promise promise) {
        cameraModule.__stopCapture(promise);
    }
    //endregion




    //region HAS FLASH
    @ReactMethod
    public void hasFlash(ReadableMap options, final Promise promise) {
        cameraModule.__hasFlash(options.getInt("type"), promise);
    }
    //endregion



    //region MEDIAFILE TREATMENTS
    private File getOutputMediaFile(int type) {
        return cameraModule.__getOutputMediaFile(type);
    }

    private File getOutputCameraRollFile(int type) {
        return cameraModule.__getOutputCameraRollFile(type);
    }

    private File getOutputFile(int type, File storageDir) {
        return cameraModule.__getOutputFile(type, storageDir);
    }

    private File getTempMediaFile(int type) {
        return cameraModule.__getTempMediaFile(type);
    }

    private void addToMediaStore(String path) {
        cameraModule.__addToMediaStore(path);
    }
    //endregion


    //region RESOLVE IMAGE
    private void resolveImage(final File imageFile, final Promise promise, boolean addToMediaStore) {
        cameraModule.__resolveImage(imageFile, promise, addToMediaStore);
    }
    //endregion


    /**
     * LifecycleEventListener overrides
     */
    @Override
    public void onHostResume() {
        // ... do nothing
    }

    @Override
    public void onHostPause() {
        // On pause, we stop any pending recording session
        if (mRecordingPromise != null) {
            releaseMediaRecorder();
        }
    }

    @Override
    public void onHostDestroy() {
        // ... do nothing
    }




}
