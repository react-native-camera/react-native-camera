/**
 * Created by Fabrice Armisen (farmisen@gmail.com) on 1/3/16.
 */

package com.lwansbrough.RCTCamera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.MotionEvent;
import android.view.TextureView;

import java.util.List;

class RCTCameraViewFinder extends TextureView implements TextureView.SurfaceTextureListener {
    private int _cameraType;
    private SurfaceTexture _surfaceTexture;
    private boolean _isStarting;
    private boolean _isStopping;
    private Camera _camera;
    private float mFingerSpacing;

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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Get the pointer ID
        Camera.Parameters params = _camera.getParameters();
        int action = event.getAction();


        if (event.getPointerCount() > 1) {
            // handle multi-touch events
            if (action == MotionEvent.ACTION_POINTER_DOWN) {
                mFingerSpacing = getFingerSpacing(event);
            } else if (action == MotionEvent.ACTION_MOVE && params.isZoomSupported()) {
                _camera.cancelAutoFocus();
                handleZoom(event, params);
            }
        } else {
            // handle single touch events
            if (action == MotionEvent.ACTION_UP) {
                handleFocus(event, params);
            }
        }
        return true;
    }

    private void handleZoom(MotionEvent event, Camera.Parameters params) {
        int maxZoom = params.getMaxZoom();
        int zoom = params.getZoom();
        float newDist = getFingerSpacing(event);
        if (newDist > mFingerSpacing) {
            //zoom in
            if (zoom < maxZoom)
                zoom++;
        } else if (newDist < mFingerSpacing) {
            //zoom out
            if (zoom > 0)
                zoom--;
        }
        mFingerSpacing = newDist;
        params.setZoom(zoom);
        _camera.setParameters(params);
    }

    public void handleFocus(MotionEvent event, Camera.Parameters params) {
        int pointerId = event.getPointerId(0);
        int pointerIndex = event.findPointerIndex(pointerId);
        // Get the pointer's current position
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        List<String> supportedFocusModes = params.getSupportedFocusModes();
        if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            _camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean b, Camera camera) {
                    // currently set to auto-focus on single touch
                }
            });
        }
    }

    /** Determine the space between the first two fingers */
    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }
}
