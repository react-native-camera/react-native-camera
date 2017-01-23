/**
 * Created by Fabrice Armisen (farmisen@gmail.com) on 1/3/16.
 */

package com.lwansbrough.RCTCamera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.MotionEvent;
import android.view.TextureView;
import android.os.AsyncTask;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.List;
import java.util.EnumMap;
import java.util.EnumSet;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

class RCTCameraViewFinder extends TextureView implements TextureView.SurfaceTextureListener, Camera.PreviewCallback {
    private int _cameraType;
    private int _captureMode;
    private SurfaceTexture _surfaceTexture;
    private boolean _isStarting;
    private boolean _isStopping;
    private Camera _camera;
    private float mFingerSpacing;

    // concurrency lock for barcode scanner to avoid flooding the runtime
    public static volatile boolean barcodeScannerTaskLock = false;

    // reader instance for the barcode scanner
    private final MultiFormatReader _multiFormatReader = new MultiFormatReader();

    public RCTCameraViewFinder(Context context, int type) {
        super(context);
        this.setSurfaceTextureListener(this);
        this._cameraType = type;
        this.initBarcodeReader(RCTCamera.getInstance().getBarCodeTypes());
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

    public void setCaptureMode(final int captureMode) {
        RCTCamera.getInstance().setCaptureMode(_cameraType, captureMode);
        this._captureMode = captureMode;
    }

    public void setCaptureQuality(String captureQuality) {
        RCTCamera.getInstance().setCaptureQuality(_cameraType, captureQuality);
    }

    public void setTorchMode(int torchMode) {
        RCTCamera.getInstance().setTorchMode(_cameraType, torchMode);
    }

    public void setFlashMode(int flashMode) {
        RCTCamera.getInstance().setFlashMode(_cameraType, flashMode);
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
                List<Camera.Size> supportedSizes;
                if (_captureMode == RCTCameraModule.RCT_CAMERA_CAPTURE_MODE_STILL) {
                    supportedSizes = parameters.getSupportedPictureSizes();
                } else if (_captureMode == RCTCameraModule.RCT_CAMERA_CAPTURE_MODE_VIDEO) {
                    supportedSizes = RCTCamera.getInstance().getSupportedVideoSizes(_camera);
                } else {
                    throw new RuntimeException("Unsupported capture mode:" + _captureMode);
                }
                Camera.Size optimalPictureSize = RCTCamera.getInstance().getBestSize(
                        supportedSizes,
                        Integer.MAX_VALUE,
                        Integer.MAX_VALUE
                );
                parameters.setPictureSize(optimalPictureSize.width, optimalPictureSize.height);

                _camera.setParameters(parameters);
                _camera.setPreviewTexture(_surfaceTexture);
                _camera.startPreview();
                // send previews to `onPreviewFrame`
                _camera.setPreviewCallback(this);
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
                    // stop sending previews to `onPreviewFrame`
                    _camera.setPreviewCallback(null);
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

    /**
     * Parse barcodes as BarcodeFormat constants.
     *
     * Supports all iOS codes except [code39mod43, itf14]
     *
     * Additionally supports [codabar, maxicode, rss14, rssexpanded, upca, upceanextension]
     */
    private BarcodeFormat parseBarCodeString(String c) {
        if ("aztec".equals(c)) {
            return BarcodeFormat.AZTEC;
        } else if ("ean13".equals(c)) {
            return BarcodeFormat.EAN_13;
        } else if ("ean8".equals(c)) {
            return BarcodeFormat.EAN_8;
        } else if ("qr".equals(c)) {
            return BarcodeFormat.QR_CODE;
        } else if ("pdf417".equals(c)) {
            return BarcodeFormat.PDF_417;
        } else if ("upce".equals(c)) {
            return BarcodeFormat.UPC_E;
        } else if ("datamatrix".equals(c)) {
            return BarcodeFormat.DATA_MATRIX;
        } else if ("code39".equals(c)) {
            return BarcodeFormat.CODE_39;
        } else if ("code93".equals(c)) {
            return BarcodeFormat.CODE_93;
        } else if ("interleaved2of5".equals(c)) {
            return BarcodeFormat.ITF;
        } else if ("codabar".equals(c)) {
            return BarcodeFormat.CODABAR;
        } else if ("code128".equals(c)) {
            return BarcodeFormat.CODE_128;
        } else if ("maxicode".equals(c)) {
            return BarcodeFormat.MAXICODE;
        } else if ("rss14".equals(c)) {
            return BarcodeFormat.RSS_14;
        } else if ("rssexpanded".equals(c)) {
            return BarcodeFormat.RSS_EXPANDED;
        } else if ("upca".equals(c)) {
            return BarcodeFormat.UPC_A;
        } else if ("upceanextension".equals(c)) {
            return BarcodeFormat.UPC_EAN_EXTENSION;
        } else {
            android.util.Log.v("RCTCamera", "Unsupported code.. [" + c + "]");
            return null;
        }
    }

    /**
     * Initialize the barcode decoder.
     */
    private void initBarcodeReader(List<String> barCodeTypes) {
        EnumMap<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        EnumSet<BarcodeFormat> decodeFormats = EnumSet.noneOf(BarcodeFormat.class);

        if (barCodeTypes != null) {
            for (String code : barCodeTypes) {
                BarcodeFormat format = parseBarCodeString(code);
                if (format != null) {
                    decodeFormats.add(format);
                }
            }
        }

        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        _multiFormatReader.setHints(hints);
    }

    /**
     * Spawn a barcode reader task if
     *  - the barcode scanner is enabled (has a onBarCodeRead function)
     *  - one isn't already running
     *
     * See {Camera.PreviewCallback}
     */
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (RCTCamera.getInstance().isBarcodeScannerEnabled() && !RCTCameraViewFinder.barcodeScannerTaskLock) {
            RCTCameraViewFinder.barcodeScannerTaskLock = true;
            new ReaderAsyncTask(camera, data).execute();
        }
    }

    private class ReaderAsyncTask extends AsyncTask<Void, Void, Void> {
        private byte[] imageData;
        private final Camera camera;

        ReaderAsyncTask(Camera camera, byte[] imageData) {
            this.camera = camera;
            this.imageData = imageData;
        }

        @Override
        protected Void doInBackground(Void... ignored) {
            if (isCancelled()) {
                return null;
            }

            Camera.Size size = camera.getParameters().getPreviewSize();

            int width = size.width;
            int height = size.height;

            // rotate for zxing if orientation is portrait
            if (RCTCamera.getInstance().getActualDeviceOrientation() == 0) {
              byte[] rotated = new byte[imageData.length];
              for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                  rotated[x * height + height - y - 1] = imageData[x + y * width];
                }
              }
              width = size.height;
              height = size.width;
              imageData = rotated;
            }

            try {
                PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(imageData, width, height, 0, 0, width, height, false);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                Result result = _multiFormatReader.decodeWithState(bitmap);

                ReactContext reactContext = RCTCameraModule.getReactContextSingleton();
                WritableMap event = Arguments.createMap();
                event.putString("data", result.getText());
                event.putString("type", result.getBarcodeFormat().toString());
                reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("CameraBarCodeReadAndroid", event);

            } catch (Throwable t) {
                // meh
            } finally {
                _multiFormatReader.reset();
                RCTCameraViewFinder.barcodeScannerTaskLock = false;
                return null;
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
