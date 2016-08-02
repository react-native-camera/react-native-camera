/**
 * Created by Fabrice Armisen (farmisen@gmail.com) on 1/4/16.
 */

package com.lwansbrough.RCTCamera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Surface;
import com.facebook.react.bridge.*;

import javax.annotation.Nullable;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class RCTCameraModule extends ReactContextBaseJavaModule {
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
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private final ReactApplicationContext _reactContext;
    private RCTSensorOrientationChecker _sensorOrientationChecker;

    public RCTCameraModule(ReactApplicationContext reactContext) {
        super(reactContext);
        _reactContext = reactContext;
        _sensorOrientationChecker = new RCTSensorOrientationChecker(_reactContext);
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
                        put("low", "low");
                        put("medium", "medium");
                        put("high", "high");
                        put("photo","high");
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

    public void captureWithOrientation(final ReadableMap options, final Promise promise, int deviceOrientation) {
        Camera camera = RCTCamera.getInstance().acquireCameraInstance(options.getInt("type"));
        if (null == camera) {
            promise.reject("No camera found.");
            return;
        }

        if (options.hasKey("playSoundOnCapture") && options.getBoolean("playSoundOnCapture")) {
            MediaActionSound sound = new MediaActionSound();
            sound.play(MediaActionSound.SHUTTER_CLICK);
        }

        if (options.hasKey("quality")) {
            RCTCamera.getInstance().setCaptureQuality(options.getInt("type"), options.getString("quality"));
        }

        RCTCamera.getInstance().adjustCameraRotationToDeviceOrientation(options.getInt("type"), deviceOrientation);
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                camera.stopPreview();
                camera.startPreview();
                WritableMap response = new WritableNativeMap();
                switch (options.getInt("target")) {
                    case RCT_CAMERA_CAPTURE_TARGET_MEMORY:
                        String encoded = Base64.encodeToString(data, Base64.DEFAULT);
                        response.putString("data", encoded);
                        promise.resolve(response);
                        break;
                    case RCT_CAMERA_CAPTURE_TARGET_CAMERA_ROLL:
                        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, bitmapOptions);
                        String url = MediaStore.Images.Media.insertImage(
                                _reactContext.getContentResolver(),
                                bitmap, options.getString("title"),
                                options.getString("description"));
                        response.putString("path", url);
                        promise.resolve(response);
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
                        response.putString("path", Uri.fromFile(pictureFile).toString());
                        promise.resolve(response);
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
                        response.putString("path", Uri.fromFile(tempFile).toString());
                        promise.resolve(response);
                        break;
                }
            }
        });
    }

    @ReactMethod
    public void stopCapture(final ReadableMap options, final Promise promise) {
        // TODO: implement video capture
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
}
