/**
 * Created by Fabrice Armisen (farmisen@gmail.com) on 1/4/16.
 * Android video recording support by Marc Johnson (me@marc.mn) 4/2016
 */

package com.lwansbrough.RCTCamera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaActionSound;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import com.facebook.react.bridge.*;
import android.content.ContentValues;

import javax.annotation.Nullable;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class RCTCameraModule extends ReactContextBaseJavaModule implements MediaRecorder.OnInfoListener {
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
    public static final int RCT_CAMERA_ORIENTATION_AUTO = 0;
    public static final int RCT_CAMERA_ORIENTATION_LANDSCAPE_LEFT = 1;
    public static final int RCT_CAMERA_ORIENTATION_LANDSCAPE_RIGHT = 2;
    public static final int RCT_CAMERA_ORIENTATION_PORTRAIT = 3;
    public static final int RCT_CAMERA_ORIENTATION_PORTRAIT_UPSIDE_DOWN = 4;
    public static final int RCT_CAMERA_TYPE_FRONT = 1;
    public static final int RCT_CAMERA_TYPE_BACK = 2;
    public static final int RCT_CAMERA_FLASH_MODE_OFF = 0;
    public static final int RCT_CAMERA_FLASH_MODE_ON = 1;
    public static final int RCT_CAMERA_FLASH_MODE_AUTO = 2;
    public static final int RCT_CAMERA_TORCH_MODE_OFF = 0;
    public static final int RCT_CAMERA_TORCH_MODE_ON = 1;
    public static final int RCT_CAMERA_TORCH_MODE_AUTO = 2;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private final ReactApplicationContext _reactContext;

    private MediaRecorder mediaRecorder = new MediaRecorder(); 
    private long MRStartTime;
    private File videoFile;
    private Camera mCamera = null;
    private Promise recordingPromise = null;
    private ReadableMap recordingOptions;

    public RCTCameraModule(ReactApplicationContext reactContext) {
        super(reactContext);
        _reactContext = reactContext;
    }

    public void onInfo(MediaRecorder mr, int what, int extra) {
        if ( what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED ||
            what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
            if (recordingPromise != null) {
                releaseMediaRecorder(); // release the MediaRecorder object and resolve promise
            }
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
                        put("low", "low");
                        put("medium", "medium");
                        put("high", "high");
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

    private boolean prepareMediaRecorder(ReadableMap options) {

        CamcorderProfile cm = RCTCamera.getInstance().setCaptureVideoQuality(options.getInt("type"), options.getString("quality"));
        
        mCamera.unlock();  // make available for mediarecorder

        mediaRecorder.setCamera(mCamera);

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
    
        int mediaRecorderHintOrientation = (90 + ((720 - RCTCamera.getInstance().getActualDeviceOrientation() * 90))) % 360;

        // adjust for differences in how devices display front facing http://www.theverge.com/2015/11/9/9696774/google-nexus-5x-upside-down-camera
        if (RCT_CAMERA_TYPE_FRONT == options.getInt("type")) {
            if ((RCT_CAMERA_ORIENTATION_PORTRAIT == RCTCamera.getInstance().getOrientation()) && (RCTCamera.getInstance().getAdjustedDeviceOrientation() == 90)) {
                mediaRecorderHintOrientation += 180;
            } 
        }
        mediaRecorder.setOrientationHint(mediaRecorderHintOrientation);

        if (null == cm) {
            return false; 
        }

        cm.fileFormat = MediaRecorder.OutputFormat.MPEG_4;
        cm.videoCodec = MediaRecorder.VideoEncoder.MPEG_4_SP;

        mediaRecorder.setProfile(cm);        

        videoFile = null;
        switch (options.getInt("target")) {
            case RCT_CAMERA_CAPTURE_TARGET_MEMORY:
                videoFile = getTempMediaFile(MEDIA_TYPE_VIDEO); // save to temp file temporarily 
                break;
            case RCT_CAMERA_CAPTURE_TARGET_CAMERA_ROLL:
                videoFile = getTempMediaFile(MEDIA_TYPE_VIDEO); // save to temp file temporarily 
                break;
            case RCT_CAMERA_CAPTURE_TARGET_TEMP:
                videoFile = getTempMediaFile(MEDIA_TYPE_VIDEO);
                break;
            default:
            case RCT_CAMERA_CAPTURE_TARGET_DISK:
                videoFile = getOutputMediaFile(MEDIA_TYPE_VIDEO);
                break;
        }
        if (videoFile == null) {
            return false;
        }

        mediaRecorder.setOutputFile(videoFile.getPath());

        // Attach callback for maxDuration and maxFileSize
        mediaRecorder.setOnInfoListener(this);
 
        if (options.hasKey("maxDuration")) {
            int maxDuration = options.getInt("maxDuration");
            mediaRecorder.setMaxDuration(maxDuration * 1000);
        }

        if (options.hasKey("maxFileSize")) {
            int maxFileSize = options.getInt("maxFileSize");
            mediaRecorder.setMaxFileSize(maxFileSize);
        }

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            releaseMediaRecorder();
            return false;
        }
        return true;

    }

    @ReactMethod
    private void record(final ReadableMap options, final Promise promise) {
        if (recordingPromise == null) {
            mCamera = RCTCamera.getInstance().acquireCameraInstance(options.getInt("type"));
            if (null == mCamera) {
                promise.reject("No camera found.");
                return;
            }
            if (!prepareMediaRecorder(options)) {
                promise.reject("Fail in prepareMediaRecorder()!");
                return;
            }
            try {    
                mediaRecorder.start();
                MRStartTime =  System.currentTimeMillis();
                recordingOptions = options;
                recordingPromise = promise;  // only got here if mediaRecorder started
            } catch (final Exception ex) {
                promise.reject("Exception in thread");
                return;
            }
        }
    }

    private void releaseMediaRecorder() {

        if (recordingPromise != null) {

            // Must record at least a second or MediaRecorder throws exceptions on some platforms
            long duration = System.currentTimeMillis() - MRStartTime;
            if (duration < 1500) {
                try {
                    Thread.sleep(1500-duration);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }              
            }
            try {
                mediaRecorder.stop(); // stop the recording
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }      
        mediaRecorder.reset(); // clear recorder configuration
        
        if (mCamera != null) {
            mCamera.lock(); // relock camera for later use since we unlocked it
        }

        // Make sure readable 
        File f = new File(videoFile.getPath());
        if (f.exists()) {
            f.setReadable(true, false); // so mediaplayer can play it 
            f.setWritable(true, false); // so can clean it up

            if (recordingPromise != null) {
                switch (recordingOptions.getInt("target")) {
                    case RCT_CAMERA_CAPTURE_TARGET_MEMORY:
                        byte[] encoded = convertFileToByteArray(videoFile);
                        recordingPromise.resolve(new String(encoded, Base64.DEFAULT));
                        f.delete(); // delete since transferred to memory
                        break;
                    case RCT_CAMERA_CAPTURE_TARGET_CAMERA_ROLL:
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Video.Media.DATA, videoFile.getAbsolutePath());
                        values.put(MediaStore.Video.Media.TITLE, recordingOptions.hasKey("title") ? recordingOptions.getString("title") : "video");
                        if (recordingOptions.hasKey("description")) values.put(MediaStore.Video.Media.DESCRIPTION, recordingOptions.hasKey("description"));
                        if (recordingOptions.hasKey("latitude")) values.put(MediaStore.Video.Media.LATITUDE, recordingOptions.getString("latitude"));
                        if (recordingOptions.hasKey("longitude")) values.put(MediaStore.Video.Media.LONGITUDE, recordingOptions.getString("longitude"));
                        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
                        values.put(MediaStore.Video.Media.CONTENT_TYPE, "video/mp4");
                        recordingPromise.resolve(_reactContext.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values));
                        f.delete(); // delete since transferred to camera roll
                        break;
                    case RCT_CAMERA_CAPTURE_TARGET_TEMP:
                    case RCT_CAMERA_CAPTURE_TARGET_DISK:
                    default:
                        recordingPromise.resolve(Uri.fromFile(videoFile).toString());
                        break;
                }
            }
        } else {
            recordingPromise.reject("Nothing recorded");
        }
        recordingPromise = null;
    }

    public static byte[] convertFileToByteArray(File f)
    {
        byte[] byteArray = null;
        try
        {
            InputStream inputStream = new FileInputStream(f);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024*8];
            int bytesRead =0;

            while ((bytesRead = inputStream.read(b)) != -1) bos.write(b, 0, bytesRead);
    
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

        if (options.getBoolean("playSoundOnCapture")) {
            MediaActionSound sound = new MediaActionSound();
            sound.play(MediaActionSound.SHUTTER_CLICK);
        }

        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                camera.stopPreview();
                camera.startPreview();
                switch (options.getInt("target")) {
                    case RCT_CAMERA_CAPTURE_TARGET_MEMORY:
                        String encoded = Base64.encodeToString(data, Base64.DEFAULT);
                        promise.resolve(encoded);
                        break;
                    case RCT_CAMERA_CAPTURE_TARGET_CAMERA_ROLL:
                        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, bitmapOptions);
                        String url = MediaStore.Images.Media.insertImage(
                                _reactContext.getContentResolver(),
                                bitmap, options.getString("title"),
                                options.getString("description"));
                        promise.resolve(url);
                        break;
                    case RCT_CAMERA_CAPTURE_TARGET_DISK:
                        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                        if (pictureFile == null) {
                            promise.reject("Error creating media file.");
                            return;
                        }
                        try {
                            FileOutputStream fos = new FileOutputStream(pictureFile);
                            fos.write(data);
                            fos.close();
                        } catch (FileNotFoundException e) {
                            promise.reject("File not found: " + e.getMessage());
                        } catch (IOException e) {
                            promise.reject("Error accessing file: " + e.getMessage());
                        }
                        promise.resolve(Uri.fromFile(pictureFile).toString());
                        break;
                    case RCT_CAMERA_CAPTURE_TARGET_TEMP:
                        File tempFile = getTempMediaFile(MEDIA_TYPE_IMAGE);

                        if (tempFile == null) {
                            promise.reject("Error creating media file.");
                            return;
                        }

                        try {
                            FileOutputStream fos = new FileOutputStream(tempFile);
                            fos.write(data);
                            fos.close();
                        } catch (FileNotFoundException e) {
                            promise.reject("File not found: " + e.getMessage());
                        } catch (IOException e) {
                            promise.reject("Error accessing file: " + e.getMessage());
                        }
                        promise.resolve(Uri.fromFile(tempFile).toString());
                        break;
                }
            }
        });
    }

    @ReactMethod
    public void stopCapture(final Promise promise) {
        if (recordingPromise != null) {
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

    private File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "RCTCameraModule");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e(TAG, "failed to create directory:" + mediaStorageDir.getAbsolutePath());
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            Log.e(TAG, "Unsupported media type:" + type);
            return null;
        }
        return mediaFile;
    }


    private File getTempMediaFile(int type) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File outputDir;
            File outputFile;

            if (type == MEDIA_TYPE_IMAGE) {
                outputDir = _reactContext.getCacheDir(); // for backwards compatibility
                outputFile = File.createTempFile("IMG_" + timeStamp, ".jpg", outputDir);
            } else if (type == MEDIA_TYPE_VIDEO) {
                outputDir = _reactContext.getFilesDir(); // for compatibility with react-native-fs
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
}
