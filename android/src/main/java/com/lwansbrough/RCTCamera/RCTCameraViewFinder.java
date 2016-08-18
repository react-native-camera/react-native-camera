/**
 * Created by Fabrice Armisen (farmisen@gmail.com) on 1/3/16.
 */

package com.lwansbrough.RCTCamera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.Log;
import android.view.TextureView;
import android.util.DisplayMetrics;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.List;

class RCTCameraViewFinder extends TextureView implements TextureView.SurfaceTextureListener {
    private int _cameraType;
    private SurfaceTexture _surfaceTexture;
    private boolean _isStarting;
    private boolean _isStopping;
    private Camera _camera;

    public RCTCameraViewFinder(Context context, int type) {
        super(context);
        this.setSurfaceTextureListener(this);
        this._cameraType = type;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        _surfaceTexture = surface;
        startCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        _surfaceTexture = null;
        stopCamera();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    private Camera.FaceDetectionListener faceDetectionListener = new Camera.FaceDetectionListener() {
        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            if (faces.length > 0) {
                Matrix matrix = new Matrix();
                boolean frontCamera = (getCameraType() == RCTCameraModule.RCT_CAMERA_TYPE_FRONT);

                int height = getHeight();
                int width = getWidth();

                matrix.setScale(frontCamera ? -1 : 1, 1);
                matrix.postRotate(RCTCamera.getInstance().getOrientation());
                matrix.postScale(width / 2000f, height / 2000f);
                matrix.postTranslate(width / 2f, height / 2f);

                double pixelDensity = getPixelDensity();

                for (Camera.Face face : faces) {
                    RectF faceRect = new RectF(face.rect);
                    matrix.mapRect(faceRect);

                    WritableMap faceEvent;
                    faceEvent = Arguments.createMap();
                    faceEvent.putInt("faceID", face.id);
                    faceEvent.putBoolean("isFrontCamera", frontCamera);

                    faceEvent.putDouble("x", faceRect.left / pixelDensity);
                    faceEvent.putDouble("y", faceRect.top / pixelDensity);
                    faceEvent.putDouble("h", faceRect.height() / pixelDensity);
                    faceEvent.putDouble("w", faceRect.width() / pixelDensity);

                    ((ReactContext) getContext()).getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("CameraFaceDetected", faceEvent);
                }
            }
        }

        private int getCameraType() {
            return _cameraType;
        }
    };

    public double getPixelDensity() {
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        return dm.density;
    }

    public double getRatio() {
        int width = RCTCamera.getInstance().getPreviewWidth(this._cameraType);
        int height = RCTCamera.getInstance().getPreviewHeight(this._cameraType);
        return ((float) width) / ((float) height);
    }

    public void setCameraType(final int type) {
        if (this._cameraType == type) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                stopPreview();
                _cameraType = type;
                startPreview();
            }
        }).start();
    }

    public void setCaptureQuality(String captureQuality) {
        RCTCamera.getInstance().setCaptureQuality(_cameraType, captureQuality);
    }

    public void setTorchMode(int torchMode) {
        RCTCamera.getInstance().setTorchMode(_cameraType, torchMode);
    }

    public void setFlashMode(int flashMode) {
        RCTCamera.getInstance().setTorchMode(_cameraType, flashMode);
    }

    private void startPreview() {
        if (_surfaceTexture != null) {
            startCamera();
        }
    }

    private void stopPreview() {
        if (_camera != null) {
            stopCamera();
        }
    }

    synchronized private void startCamera() {
        if (!_isStarting) {
            _isStarting = true;
            try {
                _camera = RCTCamera.getInstance().acquireCameraInstance(_cameraType);
                Camera.Parameters parameters = _camera.getParameters();

                // set autofocus
                List<String> focusModes = parameters.getSupportedFocusModes();
                if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                }
                // set picture size
                // defaults to max available size
                Camera.Size optimalPictureSize = RCTCamera.getInstance().getBestPictureSize(_cameraType, Integer.MAX_VALUE, Integer.MAX_VALUE);
                parameters.setPictureSize(optimalPictureSize.width, optimalPictureSize.height);

                _camera.setParameters(parameters);
                _camera.setPreviewTexture(_surfaceTexture);
                _camera.startPreview();

                if(parameters.getMaxNumDetectedFaces() > 0) {
                    _camera.setFaceDetectionListener(faceDetectionListener);
                    _camera.startFaceDetection();
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                stopCamera();
            } finally {
                _isStarting = false;
            }
        }
    }

    synchronized private void stopCamera() {
        if (!_isStopping) {
            _isStopping = true;
            try {
                if (_camera != null) {
                    _camera.stopPreview();
                    RCTCamera.getInstance().releaseCameraInstance(_cameraType);
                    _camera = null;
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                _isStopping = false;
            }
        }
    }
}