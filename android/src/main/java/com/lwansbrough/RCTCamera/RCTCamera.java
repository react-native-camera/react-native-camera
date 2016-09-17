/**
 * Created by Fabrice Armisen (farmisen@gmail.com) on 1/4/16.
 */

package com.lwansbrough.RCTCamera;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RCTCamera {
    private static RCTCamera ourInstance;
    private final HashMap<Integer, CameraInfoWrapper> _cameraInfos;
    private final HashMap<Integer, Integer> _cameraTypeToIndex;
    private final Map<Number, Camera> _cameras;
    private boolean _barcodeScannerEnabled = false;
    private List<String> _barCodeTypes = null;
    private int _orientation = -1;
    private int _actualDeviceOrientation = 0;
    private int _adjustedDeviceOrientation = 0;

    public static RCTCamera getInstance() {
        return ourInstance;
    }
    public static void createInstance(int deviceOrientation) {
        ourInstance = new RCTCamera(deviceOrientation);
    }


    public Camera acquireCameraInstance(int type) {
        if (null == _cameras.get(type) && null != _cameraTypeToIndex.get(type)) {
            try {
                Camera camera = Camera.open(_cameraTypeToIndex.get(type));
                _cameras.put(type, camera);
                adjustPreviewLayout(type);
            } catch (Exception e) {
                Log.e("RCTCamera", "acquireCameraInstance failed", e);
            }
        }
        return _cameras.get(type);
    }

    public void releaseCameraInstance(int type) {
        if (null != _cameras.get(type)) {
            _cameras.get(type).release();
            _cameras.remove(type);
        }
    }

    public int getPreviewWidth(int type) {
        CameraInfoWrapper cameraInfo = _cameraInfos.get(type);
        if (null == cameraInfo) {
            return 0;
        }
        return cameraInfo.previewWidth;
    }

    public int getPreviewHeight(int type) {
        CameraInfoWrapper cameraInfo = _cameraInfos.get(type);
        if (null == cameraInfo) {
            return 0;
        }
        return cameraInfo.previewHeight;
    }

    public Camera.Size getBestSize(List<Camera.Size> supportedSizes, int maxWidth, int maxHeight) {
        Camera.Size bestSize = null;
        for (Camera.Size size : supportedSizes) {
            if (size.width > maxWidth || size.height > maxHeight) {
                continue;
            }

            if (bestSize == null) {
                bestSize = size;
                continue;
            }

            int resultArea = bestSize.width * bestSize.height;
            int newArea = size.width * size.height;

            if (newArea > resultArea) {
                bestSize = size;
            }
        }

        return bestSize;
    }

    private Camera.Size getSmallestSize(List<Camera.Size> supportedSizes) {
        Camera.Size smallestSize = null;
        for (Camera.Size size : supportedSizes) {
            if (smallestSize == null) {
                smallestSize = size;
                continue;
            }

            int resultArea = smallestSize.width * smallestSize.height;
            int newArea = size.width * size.height;

            if (newArea < resultArea) {
                smallestSize = size;
            }
        }

        return smallestSize;
    }

    private List<Camera.Size> getSupportedVideoSizes(Camera camera) {
        Camera.Parameters params = camera.getParameters();
        // defer to preview instead of params.getSupportedVideoSizes() http://bit.ly/1rxOsq0
        // but prefer SupportedVideoSizes!
        List<Camera.Size> sizes = params.getSupportedVideoSizes();
        if (sizes != null) {
            return sizes;
        }

        // Video sizes may be null, which indicates that all the supported
        // preview sizes are supported for video recording.
        return params.getSupportedPreviewSizes();
    }

    public int getOrientation() {
        return _orientation;
    }

    public void setOrientation(int orientation) {
        if (_orientation == orientation) {
            return;
        }
        _orientation = orientation;
        adjustPreviewLayout(RCTCameraModule.RCT_CAMERA_TYPE_FRONT);
        adjustPreviewLayout(RCTCameraModule.RCT_CAMERA_TYPE_BACK);
    }

    public boolean isBarcodeScannerEnabled() {
      return _barcodeScannerEnabled;
    }

    public void setBarcodeScannerEnabled(boolean barcodeScannerEnabled) {
        _barcodeScannerEnabled = barcodeScannerEnabled;
    }

    public List<String> getBarCodeTypes() {
        return _barCodeTypes;
    }

    public void setBarCodeTypes(List<String> barCodeTypes) {
        _barCodeTypes = barCodeTypes;
    }

    public int getActualDeviceOrientation() {
        return _actualDeviceOrientation;
    }

    public void setAdjustedDeviceOrientation(int orientation) {
        _adjustedDeviceOrientation = orientation;
    }

    public int getAdjustedDeviceOrientation() {
        return _adjustedDeviceOrientation;
    }

    public void setActualDeviceOrientation(int actualDeviceOrientation) {
        _actualDeviceOrientation = actualDeviceOrientation;
        adjustPreviewLayout(RCTCameraModule.RCT_CAMERA_TYPE_FRONT);
        adjustPreviewLayout(RCTCameraModule.RCT_CAMERA_TYPE_BACK);
    }

    public void setCaptureQuality(int cameraType, String captureQuality) {
        Camera camera = _cameras.get(cameraType);
        if (camera == null) {
            return;
        }

        Camera.Parameters parameters = camera.getParameters();
        Camera.Size pictureSize = null;
        switch (captureQuality) {
            case RCTCameraModule.RCT_CAMERA_CAPTURE_QUALITY_LOW:
                pictureSize = getSmallestSize(parameters.getSupportedPictureSizes());
                break;
            case RCTCameraModule.RCT_CAMERA_CAPTURE_QUALITY_MEDIUM:
                List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
                pictureSize = sizes.get(sizes.size() / 2);
                break;
            case RCTCameraModule.RCT_CAMERA_CAPTURE_QUALITY_HIGH:
                pictureSize = getBestSize(parameters.getSupportedPictureSizes(), Integer.MAX_VALUE, Integer.MAX_VALUE);
        }

        if (pictureSize != null) {
            parameters.setPictureSize(pictureSize.width, pictureSize.height);
            camera.setParameters(parameters);
        }
    }

    public CamcorderProfile setCaptureVideoQuality(int cameraType, String captureQuality) {
        Camera camera = _cameras.get(cameraType);
        if (camera == null) {
            return null;
        }

        Camera.Size videoSize = null;
        CamcorderProfile cm = null;
        switch (captureQuality) {
            case RCTCameraModule.RCT_CAMERA_CAPTURE_QUALITY_LOW:
                videoSize = getSmallestSize(getSupportedVideoSizes(camera));
                cm = CamcorderProfile.get(_cameraTypeToIndex.get(cameraType), CamcorderProfile.QUALITY_480P);
                break;
            case RCTCameraModule.RCT_CAMERA_CAPTURE_QUALITY_MEDIUM:
                List<Camera.Size> sizes = getSupportedVideoSizes(camera);
                videoSize = sizes.get(sizes.size() / 2);
                cm = CamcorderProfile.get(_cameraTypeToIndex.get(cameraType), CamcorderProfile.QUALITY_720P);
                break;
            case RCTCameraModule.RCT_CAMERA_CAPTURE_QUALITY_HIGH:
                videoSize = getBestSize(getSupportedVideoSizes(camera), Integer.MAX_VALUE, Integer.MAX_VALUE);
                cm = CamcorderProfile.get(_cameraTypeToIndex.get(cameraType), CamcorderProfile.QUALITY_HIGH);
        }

        if (cm == null){
            return null;
        }

        if (videoSize != null) {
            cm.videoFrameHeight = videoSize.height;
            cm.videoFrameWidth = videoSize.width;
        }

        return cm;
    }

    public void setTorchMode(int cameraType, int torchMode) {
        Camera camera = _cameras.get(cameraType);
        if (null == camera) {
            return;
        }

        Camera.Parameters parameters = camera.getParameters();
        String value = parameters.getFlashMode();
        switch (torchMode) {
            case RCTCameraModule.RCT_CAMERA_TORCH_MODE_ON:
                value = Camera.Parameters.FLASH_MODE_TORCH;
                break;
            case RCTCameraModule.RCT_CAMERA_TORCH_MODE_OFF:
                value = Camera.Parameters.FLASH_MODE_OFF;
                break;
        }

        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes != null && flashModes.contains(value)) {
            parameters.setFlashMode(value);
            camera.setParameters(parameters);
        }
    }

    public void setFlashMode(int cameraType, int flashMode) {
        Camera camera = _cameras.get(cameraType);
        if (null == camera) {
            return;
        }

        Camera.Parameters parameters = camera.getParameters();
        String value = parameters.getFlashMode();
        switch (flashMode) {
            case RCTCameraModule.RCT_CAMERA_FLASH_MODE_AUTO:
                value = Camera.Parameters.FLASH_MODE_AUTO;
                break;
            case RCTCameraModule.RCT_CAMERA_FLASH_MODE_ON:
                value = Camera.Parameters.FLASH_MODE_ON;
                break;
            case RCTCameraModule.RCT_CAMERA_FLASH_MODE_OFF:
                value = Camera.Parameters.FLASH_MODE_OFF;
                break;
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes != null && flashModes.contains(value)) {
            parameters.setFlashMode(value);
            camera.setParameters(parameters);
        }
    }

    public void adjustCameraRotationToDeviceOrientation(int type, int deviceOrientation) {
        Camera camera = _cameras.get(type);
        if (null == camera) {
            return;
        }

        CameraInfoWrapper cameraInfo = _cameraInfos.get(type);
        int rotation;
        int orientation = cameraInfo.info.orientation;
        if (cameraInfo.info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (orientation + deviceOrientation * 90) % 360;
        } else {
            rotation = (orientation - deviceOrientation * 90 + 360) % 360;
        }
        cameraInfo.rotation = rotation;
        Camera.Parameters parameters = camera.getParameters();
        parameters.setRotation(cameraInfo.rotation);

        try {
            camera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void adjustPreviewLayout(int type) {
        Camera camera = _cameras.get(type);
        if (null == camera) {
            return;
        }

        CameraInfoWrapper cameraInfo = _cameraInfos.get(type);
        int displayRotation;
        int rotation;
        int orientation = cameraInfo.info.orientation;
        if (cameraInfo.info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (orientation + _actualDeviceOrientation * 90) % 360;
            displayRotation = (720 - orientation - _actualDeviceOrientation * 90) % 360;
        } else {
            rotation = (orientation - _actualDeviceOrientation * 90 + 360) % 360;
            displayRotation = rotation;
        }
        cameraInfo.rotation = rotation;
        // TODO: take in account the _orientation prop

        setAdjustedDeviceOrientation(rotation);
        camera.setDisplayOrientation(displayRotation);

        Camera.Parameters parameters = camera.getParameters();
        parameters.setRotation(cameraInfo.rotation);

        // set preview size
        // defaults to highest resolution available
        Camera.Size optimalPreviewSize = getBestSize(parameters.getSupportedPreviewSizes(), Integer.MAX_VALUE, Integer.MAX_VALUE);
        int width = optimalPreviewSize.width;
        int height = optimalPreviewSize.height;

        parameters.setPreviewSize(width, height);
        try {
            camera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (cameraInfo.rotation == 0 || cameraInfo.rotation == 180) {
            cameraInfo.previewWidth = width;
            cameraInfo.previewHeight = height;
        } else {
            cameraInfo.previewWidth = height;
            cameraInfo.previewHeight = width;
        }
    }

    private RCTCamera(int deviceOrientation) {
        _cameras = new HashMap<>();
        _cameraInfos = new HashMap<>();
        _cameraTypeToIndex = new HashMap<>();

        _actualDeviceOrientation = deviceOrientation;

        // map camera types to camera indexes and collect cameras properties
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT && _cameraInfos.get(RCTCameraModule.RCT_CAMERA_TYPE_FRONT) == null) {
                _cameraInfos.put(RCTCameraModule.RCT_CAMERA_TYPE_FRONT, new CameraInfoWrapper(info));
                _cameraTypeToIndex.put(RCTCameraModule.RCT_CAMERA_TYPE_FRONT, i);
                acquireCameraInstance(RCTCameraModule.RCT_CAMERA_TYPE_FRONT);
                releaseCameraInstance(RCTCameraModule.RCT_CAMERA_TYPE_FRONT);
            } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK && _cameraInfos.get(RCTCameraModule.RCT_CAMERA_TYPE_BACK) == null) {
                _cameraInfos.put(RCTCameraModule.RCT_CAMERA_TYPE_BACK, new CameraInfoWrapper(info));
                _cameraTypeToIndex.put(RCTCameraModule.RCT_CAMERA_TYPE_BACK, i);
                acquireCameraInstance(RCTCameraModule.RCT_CAMERA_TYPE_BACK);
                releaseCameraInstance(RCTCameraModule.RCT_CAMERA_TYPE_BACK);
            }
        }
    }

    private class CameraInfoWrapper {
        public final Camera.CameraInfo info;
        public int rotation = 0;
        public int previewWidth = -1;
        public int previewHeight = -1;

        public CameraInfoWrapper(Camera.CameraInfo info) {
            this.info = info;
        }
    }
}
