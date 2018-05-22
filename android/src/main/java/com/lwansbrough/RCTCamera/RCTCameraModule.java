/**
 * Created by Fabrice Armisen (farmisen@gmail.com) on 1/4/16.
 * Android video recording support by Marc Johnson (me@marc.mn) 4/2016
 */

package com.lwansbrough.RCTCamera;

import android.content.ContentValues;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.media.*;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Surface;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class RCTCameraModule extends ReactContextBaseJavaModule
    implements MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener, LifecycleEventListener {
    private static final String TAG = "RCTCameraModule";

    public static final int RCT_CAMERA_ASPECT_FILL = 0;
    public static final int RCT_CAMERA_ASPECT_FIT = 1;
    public static final int RCT_CAMERA_ASPECT_STRETCH = 2;
    public static final int RCT_CAMERA_CAPTURE_MODE_STILL = 0;
    public static final int RCT_CAMERA_CAPTURE_MODE_VIDEO = 1;
    public static final int RCT_CAMERA_CAPTURE_TARGET_MEMORY = 0;
    public static final int RCT_CAMERA_CAPTURE_TARGET_DISK = 1;
    public static final int RCT_CAMERA_CAPTURE_TARGET_CAMERA_ROLL = 2;
    public static final int RCT_CAMERA_CAPTURE_TARGET_TEMP = 3;
    public static final int RCT_CAMERA_ORIENTATION_AUTO = Integer.MAX_VALUE;
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
    public static final int MEDIA_TYPE_VIDEO = 2;

    private static ReactApplicationContext _reactContext;
    private RCTSensorOrientationChecker _sensorOrientationChecker;

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
                        put("stretch", RCT_CAMERA_ASPECT_STRETCH);
                        put("fit", RCT_CAMERA_ASPECT_FIT);
                        put("fill", RCT_CAMERA_ASPECT_FILL);
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
                        put("front", RCT_CAMERA_TYPE_FRONT);
                        put("back", RCT_CAMERA_TYPE_BACK);
                    }
                });
            }

            private Map<String, Object> getCaptureQualityConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("low", RCT_CAMERA_CAPTURE_QUALITY_LOW);
                        put("medium", RCT_CAMERA_CAPTURE_QUALITY_MEDIUM);
                        put("high", RCT_CAMERA_CAPTURE_QUALITY_HIGH);
                        put("photo", RCT_CAMERA_CAPTURE_QUALITY_HIGH);
                        put("preview", RCT_CAMERA_CAPTURE_QUALITY_PREVIEW);
                        put("480p", RCT_CAMERA_CAPTURE_QUALITY_480P);
                        put("720p", RCT_CAMERA_CAPTURE_QUALITY_720P);
                        put("1080p", RCT_CAMERA_CAPTURE_QUALITY_1080P);
                    }
                });
            }

            private Map<String, Object> getCaptureModeConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("still", RCT_CAMERA_CAPTURE_MODE_STILL);
                        put("video", RCT_CAMERA_CAPTURE_MODE_VIDEO);
                    }
                });
            }

            private Map<String, Object> getCaptureTargetConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("memory", RCT_CAMERA_CAPTURE_TARGET_MEMORY);
                        put("disk", RCT_CAMERA_CAPTURE_TARGET_DISK);
                        put("cameraRoll", RCT_CAMERA_CAPTURE_TARGET_CAMERA_ROLL);
                        put("temp", RCT_CAMERA_CAPTURE_TARGET_TEMP);
                    }
                });
            }

            private Map<String, Object> getOrientationConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("auto", RCT_CAMERA_ORIENTATION_AUTO);
                        put("landscapeLeft", RCT_CAMERA_ORIENTATION_LANDSCAPE_LEFT);
                        put("landscapeRight", RCT_CAMERA_ORIENTATION_LANDSCAPE_RIGHT);
                        put("portrait", RCT_CAMERA_ORIENTATION_PORTRAIT);
                        put("portraitUpsideDown", RCT_CAMERA_ORIENTATION_PORTRAIT_UPSIDE_DOWN);
                    }
                });
            }

            private Map<String, Object> getFlashModeConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("off", RCT_CAMERA_FLASH_MODE_OFF);
                        put("on", RCT_CAMERA_FLASH_MODE_ON);
                        put("auto", RCT_CAMERA_FLASH_MODE_AUTO);
                    }
                });
            }

            private Map<String, Object> getTorchModeConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("off", RCT_CAMERA_TORCH_MODE_OFF);
                        put("on", RCT_CAMERA_TORCH_MODE_ON);
                        put("auto", RCT_CAMERA_TORCH_MODE_AUTO);
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
    private Throwable prepareMediaRecorder(ReadableMap options, int deviceOrientation) {
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
        // mMediaRecorder.setOrientationHint(RCTCamera.getInstance().getAdjustedDeviceOrientation());
        switch (deviceOrientation) {
            case 0: mMediaRecorder.setOrientationHint(90);
              break;
            case 1: mMediaRecorder.setOrientationHint(0);
              break;
            case 2: mMediaRecorder.setOrientationHint(270);
              break;
            case 3: mMediaRecorder.setOrientationHint(180);
              break;
          }

        // Set video output format and encoding using CamcorderProfile.
        cm.fileFormat = MediaRecorder.OutputFormat.MPEG_4;
        mMediaRecorder.setProfile(cm);

        // Set video output file.
        mVideoFile = null;
        switch (options.getInt("target")) {
            case RCT_CAMERA_CAPTURE_TARGET_MEMORY:
                mVideoFile = getTempMediaFile(MEDIA_TYPE_VIDEO); // temporarily
                break;
            case RCT_CAMERA_CAPTURE_TARGET_CAMERA_ROLL:
                mVideoFile = getOutputCameraRollFile(MEDIA_TYPE_VIDEO);
                break;
            case RCT_CAMERA_CAPTURE_TARGET_TEMP:
                mVideoFile = getTempMediaFile(MEDIA_TYPE_VIDEO);
                break;
            default:
            case RCT_CAMERA_CAPTURE_TARGET_DISK:
                mVideoFile = getOutputMediaFile(MEDIA_TYPE_VIDEO);
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

    private void record(final ReadableMap options, final Promise promise, final int deviceOrientation) {
        if (mRecordingPromise != null) {
            return;
        }

        mCamera = RCTCamera.getInstance().acquireCameraInstance(options.getInt("type"));
        if (mCamera == null) {
            promise.reject(new RuntimeException("No camera found."));
            return;
        }

        Throwable prepareError = prepareMediaRecorder(options, deviceOrientation);
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
            case RCT_CAMERA_CAPTURE_TARGET_MEMORY:
                byte[] encoded = convertFileToByteArray(mVideoFile);
                response.putString("data", new String(encoded, Base64.DEFAULT));
                mRecordingPromise.resolve(response);
                f.delete();
                break;
            case RCT_CAMERA_CAPTURE_TARGET_CAMERA_ROLL:
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
            case RCT_CAMERA_CAPTURE_TARGET_TEMP:
            case RCT_CAMERA_CAPTURE_TARGET_DISK:
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

    @ReactMethod
    public void capture(final ReadableMap options, final Promise promise) {
        if (RCTCamera.getInstance() == null) {
            promise.reject("Camera is not ready yet.");
            return;
        }

        int orientation = options.hasKey("orientation") ? options.getInt("orientation") : RCTCamera.getInstance().getOrientation();
        if (orientation == RCT_CAMERA_ORIENTATION_AUTO) {
            _sensorOrientationChecker.onResume();
            _sensorOrientationChecker.registerOrientationListener(new RCTSensorOrientationListener() {
                @Override
                public void orientationEvent() {
                    int deviceOrientation = _sensorOrientationChecker.getOrientation();
                    _sensorOrientationChecker.unregisterOrientationListener();
                    _sensorOrientationChecker.onPause();
                    captureWithOrientation(options, promise, deviceOrientation);
                }
            });
        } else {
            captureWithOrientation(options, promise, orientation);
        }
    }

    private void captureWithOrientation(final ReadableMap options, final Promise promise, int deviceOrientation) {
        final Camera camera = RCTCamera.getInstance().acquireCameraInstance(options.getInt("type"));
        if (null == camera) {
            promise.reject("No camera found.");
            return;
        }

        if (options.getInt("mode") == RCT_CAMERA_CAPTURE_MODE_VIDEO) {
            record(options, promise, deviceOrientation);
            return;
        }

        RCTCamera.getInstance().setCaptureQuality(options.getInt("type"), options.getString("quality"));

        if (options.hasKey("playSoundOnCapture") && options.getBoolean("playSoundOnCapture")) {
            MediaActionSound sound = new MediaActionSound();
            sound.play(MediaActionSound.SHUTTER_CLICK);
        }

        if (options.hasKey("quality")) {
            RCTCamera.getInstance().setCaptureQuality(options.getInt("type"), options.getString("quality"));
        }

        RCTCamera.getInstance().adjustCameraRotationToDeviceOrientation(options.getInt("type"), deviceOrientation);
        camera.setPreviewCallback(null);

        Camera.PictureCallback captureCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {
                camera.stopPreview();
                camera.startPreview();

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        processImage(new MutableImage(data), options, promise);
                    }
                });

                mSafeToCapture = true;
            }
        };

        Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
            @Override
            public void onShutter() {
                try {
                    camera.setPreviewCallback(null);
                    camera.setPreviewTexture(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        if(mSafeToCapture) {
          try {
            camera.takePicture(shutterCallback, null, captureCallback);
            mSafeToCapture = false;
          } catch(RuntimeException ex) {
              Log.e(TAG, "Couldn't capture photo.", ex);
          }
        }
    }

    /**
     * synchronized in order to prevent the user crashing the app by taking many photos and them all being processed
     * concurrently which would blow the memory (esp on smaller devices), and slow things down.
     */
    private synchronized void processImage(MutableImage mutableImage, ReadableMap options, Promise promise) {
        boolean shouldFixOrientation = options.hasKey("fixOrientation") && options.getBoolean("fixOrientation");
        if(shouldFixOrientation) {
            try {
                mutableImage.fixOrientation();
            } catch (MutableImage.ImageMutationFailedException e) {
                promise.reject("Error fixing orientation image", e);
            }
        }

        boolean needsReorient = false;
        double previewRatio, pictureRatio = (double) mutableImage.getWidth() / (double) mutableImage.getHeight();
        try {
            int type = options.getInt("type");
            previewRatio = (double) RCTCamera.getInstance().getPreviewVisibleWidth(type) / (double) RCTCamera.getInstance().getPreviewVisibleHeight(type);
            needsReorient = (previewRatio > 1) != (pictureRatio > 1);
        } catch (IllegalArgumentException e) {
            previewRatio = pictureRatio;
        }

        boolean shouldCropToPreview = options.hasKey("cropToPreview") && options.getBoolean("cropToPreview");
        if (shouldCropToPreview) {
            try {
                mutableImage.cropToPreview(needsReorient ? 1.0 / previewRatio : previewRatio);
            } catch (IllegalArgumentException e) {
                promise.reject("Error cropping image to preview", e);
            }
        }

        boolean shouldMirror = options.hasKey("mirrorImage") && options.getBoolean("mirrorImage");
        if (shouldMirror) {
            try {
                mutableImage.mirrorImage();
            } catch (MutableImage.ImageMutationFailedException e) {
                promise.reject("Error mirroring image", e);
            }
        }

        int jpegQualityPercent = 80;
        if(options.hasKey("jpegQuality")) {
            jpegQualityPercent = options.getInt("jpegQuality");
        }

        int imgWidth = (needsReorient) ? mutableImage.getHeight() : mutableImage.getWidth();
        int imgHeight = (needsReorient) ? mutableImage.getWidth() : mutableImage.getHeight();

        switch (options.getInt("target")) {
            case RCT_CAMERA_CAPTURE_TARGET_MEMORY:
                String encoded = mutableImage.toBase64(jpegQualityPercent);
                WritableMap response = new WritableNativeMap();
                response.putString("data", encoded);
                response.putInt("width", imgWidth);
                response.putInt("height", imgHeight);
                promise.resolve(response);
                break;
            case RCT_CAMERA_CAPTURE_TARGET_CAMERA_ROLL: {
                File cameraRollFile = getOutputCameraRollFile(MEDIA_TYPE_IMAGE);
                if (cameraRollFile == null) {
                    promise.reject("Error creating media file.");
                    return;
                }

                try {
                    mutableImage.writeDataToFile(cameraRollFile, options, jpegQualityPercent);
                } catch (IOException | NullPointerException e) {
                    promise.reject("failed to save image file", e);
                    return;
                }

                addToMediaStore(cameraRollFile.getAbsolutePath());

                resolveImage(cameraRollFile, imgWidth, imgHeight, promise, true);

                break;
            }
            case RCT_CAMERA_CAPTURE_TARGET_DISK: {
                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null) {
                    promise.reject("Error creating media file.");
                    return;
                }

                try {
                    mutableImage.writeDataToFile(pictureFile, options, jpegQualityPercent);
                } catch (IOException e) {
                    promise.reject("failed to save image file", e);
                    return;
                }

                resolveImage(pictureFile, imgWidth, imgHeight, promise, false);

                break;
            }
            case RCT_CAMERA_CAPTURE_TARGET_TEMP: {
                File tempFile = getTempMediaFile(MEDIA_TYPE_IMAGE);
                if (tempFile == null) {
                    promise.reject("Error creating media file.");
                    return;
                }

                try {
                    mutableImage.writeDataToFile(tempFile, options, jpegQualityPercent);
                } catch (IOException e) {
                    promise.reject("failed to save image file", e);
                    return;
                }

                resolveImage(tempFile, imgWidth, imgHeight, promise, false);

                break;
            }
        }
    }

    @ReactMethod
    public void stopCapture(final Promise promise) {
        if (mRecordingPromise != null) {
            releaseMediaRecorder(); // release the MediaRecorder object
            promise.resolve("Finished recording.");
        } else {
            promise.resolve("Not recording.");
        }
    }

    @ReactMethod
    public void hasFlash(ReadableMap options, final Promise promise) {
        Camera camera = RCTCamera.getInstance().acquireCameraInstance(options.getInt("type"));
        if (null == camera) {
            promise.reject("No camera found.");
            return;
        }
        List<String> flashModes = camera.getParameters().getSupportedFlashModes();
        promise.resolve(null != flashModes && !flashModes.isEmpty());
    }

    @ReactMethod
    public void setZoom(ReadableMap options, int zoom) {
        RCTCamera instance = RCTCamera.getInstance();
        if (instance == null) return;

        Camera camera = instance.acquireCameraInstance(options.getInt("type"));
        if (camera == null) return;

        Camera.Parameters parameters = camera.getParameters();
        int maxZoom = parameters.getMaxZoom();
        if (parameters.isZoomSupported()) {
            if (zoom >=0 && zoom < maxZoom) {
                parameters.setZoom(zoom);
                camera.setParameters(parameters);
            }
        }
    }

    private File getOutputMediaFile(int type) {
        // Get environment directory type id from requested media type.
        String environmentDirectoryType;
        if (type == MEDIA_TYPE_IMAGE) {
            environmentDirectoryType = Environment.DIRECTORY_PICTURES;
        } else if (type == MEDIA_TYPE_VIDEO) {
            environmentDirectoryType = Environment.DIRECTORY_MOVIES;
        } else {
            Log.e(TAG, "Unsupported media type:" + type);
            return null;
        }

        return getOutputFile(
                type,
                Environment.getExternalStoragePublicDirectory(environmentDirectoryType)
        );
    }

    private File getOutputCameraRollFile(int type) {
        return getOutputFile(
                type,
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        );
    }

    private File getOutputFile(int type, File storageDir) {
        // Create the storage directory if it does not exist
        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                Log.e(TAG, "failed to create directory:" + storageDir.getAbsolutePath());
                return null;
            }
        }

        // Create a media file name
        String fileName = String.format("%s", new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));

        if (type == MEDIA_TYPE_IMAGE) {
            fileName = String.format("IMG_%s.jpg", fileName);
        } else if (type == MEDIA_TYPE_VIDEO) {
            fileName = String.format("VID_%s.mp4", fileName);
        } else {
            Log.e(TAG, "Unsupported media type:" + type);
            return null;
        }

        return new File(String.format("%s%s%s", storageDir.getPath(), File.separator, fileName));
    }

    private File getTempMediaFile(int type) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File outputDir = _reactContext.getCacheDir();
            File outputFile;

            if (type == MEDIA_TYPE_IMAGE) {
                outputFile = File.createTempFile("IMG_" + timeStamp, ".jpg", outputDir);
            } else if (type == MEDIA_TYPE_VIDEO) {
                outputFile = File.createTempFile("VID_" + timeStamp, ".mp4", outputDir);
            } else {
                Log.e(TAG, "Unsupported media type:" + type);
                return null;
            }
            return outputFile;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    private void addToMediaStore(String path) {
        MediaScannerConnection.scanFile(_reactContext, new String[] { path }, null, null);
    }

    /**
     * LifecycleEventListener overrides
     */
    @Override
    public void onHostResume() {
        mSafeToCapture = true;
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

    private void resolveImage(final File imageFile, final int imgWidth, final int imgHeight, final Promise promise, boolean addToMediaStore) {
        final WritableMap response = new WritableNativeMap();
        response.putString("path", Uri.fromFile(imageFile).toString());
        response.putInt("width", imgWidth);
        response.putInt("height", imgHeight);

        if(addToMediaStore) {
            // borrowed from react-native CameraRollManager, it finds and returns the 'internal'
            // representation of the image uri that was just saved.
            // e.g. content://media/external/images/media/123
            MediaScannerConnection.scanFile(
                    _reactContext,
                    new String[]{imageFile.getAbsolutePath()},
                    null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            if (uri != null) {
                                response.putString("mediaUri", uri.toString());
                            }

                            promise.resolve(response);
                        }
                    });
        } else {
            promise.resolve(response);
        }
    }

}
