/**
 * Created by Fabrice Armisen (farmisen@gmail.com) on 1/4/16.
 * Android video recording support by Marc Johnson (me@marc.mn) 4/2016
 */

package com.lwansbrough.RCTCamera;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.*;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Surface;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
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

    private byte[] saveImage(InputStream is, Bitmap image) {
        byte[] result = null;

        try {
            result = compress(image, 85);
        } catch (OutOfMemoryError e) {
            try {
                result = compress(image, 70);
            } catch (OutOfMemoryError e2) {
                e.printStackTrace();
            }
        }

        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private byte[] mirrorImage(byte[] data) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        Bitmap photo = BitmapFactory.decodeStream(inputStream);

        Matrix m = new Matrix();
        m.preScale(-1, 1);
        Bitmap mirroredImage = Bitmap.createBitmap(photo, 0, 0, photo.getWidth(), photo.getHeight(), m, false);

        return saveImage(inputStream, mirroredImage);
    }


    private byte[] rotate(byte[] data, int exifOrientation) {
        final Matrix bitmapMatrix = new Matrix();
        switch(exifOrientation)
        {
            case 1:
                break;
            case 2:
                bitmapMatrix.postScale(-1, 1);
                break;
            case 3:
                bitmapMatrix.postRotate(180);
                break;
            case 4:
                bitmapMatrix.postRotate(180);
                bitmapMatrix.postScale(-1, 1);
                break;
            case 5:
                bitmapMatrix.postRotate(90);
                bitmapMatrix.postScale(-1, 1);
                break;
            case 6:
                bitmapMatrix.postRotate(90);
                break;
            case 7:
                bitmapMatrix.postRotate(270);
                bitmapMatrix.postScale(-1, 1);
                break;
            case 8:
                bitmapMatrix.postRotate(270);
                break;
            default:
                break;
        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        Bitmap decodedBitmap = BitmapFactory.decodeStream(inputStream);
        final Bitmap transformedBitmap = Bitmap.createBitmap(
                decodedBitmap, 0, 0, decodedBitmap.getWidth(), decodedBitmap.getHeight(), bitmapMatrix, false
        );

        return saveImage(inputStream, transformedBitmap);
    }

    private byte[] fixOrientation(byte[] data) {
        final Metadata metadata;
        try {
            metadata = ImageMetadataReader.readMetadata(
                    new BufferedInputStream(new ByteArrayInputStream(data)), data.length
            );

            final ExifIFD0Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (exifIFD0Directory == null) {
                return data;
            } else if (exifIFD0Directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                final int exifOrientation = exifIFD0Directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
                return rotate(data, exifOrientation);
            }
            return data;
        } catch (IOException | ImageProcessingException | MetadataException e) {
            e.printStackTrace();
            return data;
        }
    }

    private void rewriteOrientation(String path) {
        try {
            ExifInterface exif = new ExifInterface(path);
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_NORMAL));
            exif.saveAttributes();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private byte[] compress(Bitmap bitmap, int quality) throws OutOfMemoryError {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);

        try {
            return outputStream.toByteArray();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @ReactMethod
    public void capture(final ReadableMap options, final Promise promise) {
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
        Camera camera = RCTCamera.getInstance().acquireCameraInstance(options.getInt("type"));
        if (null == camera) {
            promise.reject("No camera found.");
            return;
        }

        if (options.getInt("mode") == RCT_CAMERA_CAPTURE_MODE_VIDEO) {
            record(options, promise);
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

        final Boolean shouldMirror = options.hasKey("mirrorImage") && options.getBoolean("mirrorImage");

        RCTCamera.getInstance().adjustCameraRotationToDeviceOrientation(options.getInt("type"), deviceOrientation);
        camera.setPreviewCallback(null);

        Camera.PictureCallback captureCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                if (shouldMirror) {
                    data = mirrorImage(data);
                    if (data == null) {
                        promise.reject("Error mirroring image");
                    }
                }

                data = fixOrientation(data);

                camera.stopPreview();
                camera.startPreview();
                WritableMap response = new WritableNativeMap();
                switch (options.getInt("target")) {
                    case RCT_CAMERA_CAPTURE_TARGET_MEMORY:
                        String encoded = Base64.encodeToString(data, Base64.DEFAULT);
                        response.putString("data", encoded);
                        promise.resolve(response);
                        break;
                    case RCT_CAMERA_CAPTURE_TARGET_CAMERA_ROLL: {
                        File cameraRollFile = getOutputCameraRollFile(MEDIA_TYPE_IMAGE);
                        if (cameraRollFile == null) {
                            promise.reject("Error creating media file.");
                            return;
                        }

                        Throwable error = writeDataToFile(data, cameraRollFile);
                        if (error != null) {
                            promise.reject(error);
                            return;
                        }

                        rewriteOrientation(cameraRollFile.getAbsolutePath());
                        addToMediaStore(cameraRollFile.getAbsolutePath());
                        response.putString("path", Uri.fromFile(cameraRollFile).toString());
                        promise.resolve(response);
                        break;
                    }
                    case RCT_CAMERA_CAPTURE_TARGET_DISK: {
                        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                        if (pictureFile == null) {
                            promise.reject("Error creating media file.");
                            return;
                        }

                        Throwable error = writeDataToFile(data, pictureFile);
                        if (error != null) {
                            promise.reject(error);
                            return;
                        }

                        rewriteOrientation(pictureFile.getAbsolutePath());
                        response.putString("path", Uri.fromFile(pictureFile).toString());
                        promise.resolve(response);
                        break;
                    }
                    case RCT_CAMERA_CAPTURE_TARGET_TEMP: {
                        File tempFile = getTempMediaFile(MEDIA_TYPE_IMAGE);
                        if (tempFile == null) {
                            promise.reject("Error creating media file.");
                            return;
                        }

                        Throwable error = writeDataToFile(data, tempFile);
                        if (error != null) {
                            promise.reject(error);
                        }

                        rewriteOrientation(tempFile.getAbsolutePath());
                        response.putString("path", Uri.fromFile(tempFile).toString());
                        promise.resolve(response);
                        break;
                    }
                }

                mSafeToCapture = true;
            }
        };

        if(mSafeToCapture) {
          try {
            camera.takePicture(null, null, captureCallback);
            mSafeToCapture = false;
          } catch(RuntimeException ex) {
              Log.e(TAG, "Couldn't capture photo.", ex);
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

    private Throwable writeDataToFile(byte[] data, File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();
        } catch (FileNotFoundException e) {
            return e;
        } catch (IOException e) {
            return e;
        }

        return null;
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
