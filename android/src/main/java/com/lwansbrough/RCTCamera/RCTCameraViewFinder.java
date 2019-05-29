/**
 * Created by Fabrice Armisen (farmisen@gmail.com) on 1/3/16.
 */

package com.lwansbrough.RCTCamera;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.MotionEvent;
import android.view.TextureView;
import android.os.AsyncTask;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.ArrayList;
import java.util.List;
import java.util.EnumMap;
import java.util.EnumSet;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.HybridBinarizer;

class RCTCameraViewFinder extends TextureView implements TextureView.SurfaceTextureListener, Camera.PreviewCallback {
    private int _cameraType;
    private int _captureMode;
    private SurfaceTexture _surfaceTexture;
    private int _surfaceTextureWidth;
    private int _surfaceTextureHeight;
    private boolean _isStarting;
    private boolean _isStopping;
    private Camera _camera;
    private boolean _clearWindowBackground = false;
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
        _surfaceTextureWidth = width;
        _surfaceTextureHeight = height;
        startCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        _surfaceTextureWidth = width;
        _surfaceTextureHeight = height;
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        _surfaceTexture = null;
        _surfaceTextureWidth = 0;
        _surfaceTextureHeight = 0;
        stopCamera();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    public int getCameraType() {
        return _cameraType;
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

    public void setClearWindowBackground(boolean clearWindowBackground) {
        this._clearWindowBackground = clearWindowBackground;
    }

    public void setZoom(int zoom) {
        RCTCamera.getInstance().setZoom(_cameraType, zoom);
    }

    public void startPreview() {
        if (_surfaceTexture != null) {
            startCamera();
        }
    }

    public void stopPreview() {
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

                final boolean isCaptureModeStill = (_captureMode == RCTCameraModule.RCT_CAMERA_CAPTURE_MODE_STILL);
                final boolean isCaptureModeVideo = (_captureMode == RCTCameraModule.RCT_CAMERA_CAPTURE_MODE_VIDEO);
                if (!isCaptureModeStill && !isCaptureModeVideo) {
                    throw new RuntimeException("Unsupported capture mode:" + _captureMode);
                }

                // Set auto-focus. Try to set to continuous picture/video, and fall back to general
                // auto if available.
                List<String> focusModes = parameters.getSupportedFocusModes();
                if (isCaptureModeStill && focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                } else if (isCaptureModeVideo && focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                }

                // set picture size
                // defaults to max available size
                List<Camera.Size> supportedSizes;
                if (isCaptureModeStill) {
                    supportedSizes = parameters.getSupportedPictureSizes();
                } else if (isCaptureModeVideo) {
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

                try{
                  _camera.setParameters(parameters);
                }
                catch(RuntimeException e ) {
                  Log.e("RCTCameraViewFinder", "setParameters failed", e);
                }
                _camera.setPreviewTexture(_surfaceTexture);
                _camera.startPreview();
                // clear window background if needed
                if (_clearWindowBackground) {
                    Activity activity = getActivity();
                    if (activity != null)
                        activity.getWindow().setBackgroundDrawable(null);
                }
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

    private Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
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

        private Result getBarcode(int width, int height, boolean inverse) {
            try{
                PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(imageData, width, height, 0, 0, width, height, false);
                BinaryBitmap bitmap;
                if (inverse) {
                    bitmap = new BinaryBitmap(new HybridBinarizer(source.invert()));
                } else {
                    bitmap = new BinaryBitmap(new HybridBinarizer(source));
                }
                return _multiFormatReader.decodeWithState(bitmap);
            } catch (Throwable t) {
                // meh
            } finally {
                _multiFormatReader.reset();
            }
            return null;
        }

        private Result getBarcodeAnyOrientation() {
            Camera.Size size = camera.getParameters().getPreviewSize();

            int width = size.width;
            int height = size.height;
            Result result = getBarcode(width, height, false);
            if (result != null) {
                return result;
            }
            // inverse
            result = getBarcode(width, height, true);
            if (result != null) {
                return result;
            }
            // rotate
            rotateImage(width, height);
            width = size.height;
            height = size.width;
            result = getBarcode(width, height, false);
            if (result != null) {
                return result;
            }
            return getBarcode(width, height, true);

        }

        private void rotateImage(int width, int height) {
            byte[] rotated = new byte[imageData.length];
            for (int y = 0; y < width; y++) {
                for (int x = 0; x < height; x++) {
                    int sourceIx = x + y * height;
                    int destIx = x * width + width - y - 1;
                    if (sourceIx >= 0 && sourceIx < imageData.length && destIx >= 0 && destIx < imageData.length) {
                        rotated[destIx] = imageData[sourceIx];
                    }
                }
            }
            imageData = rotated;
        }

        @Override
        protected Void doInBackground(Void... ignored) {
            if (isCancelled()) {
                return null;
            }

            try {
                // rotate for zxing if orientation is portrait
                Result result = getBarcodeAnyOrientation();
                if (result == null){
                    throw new Exception();
                }

                ReactContext reactContext = RCTCameraModule.getReactContextSingleton();
                WritableMap event = Arguments.createMap();
                WritableArray resultPoints = Arguments.createArray();
                ResultPoint[] points = result.getResultPoints();

                if(points != null) {
                    for (ResultPoint point : points) {
                        WritableMap newPoint = Arguments.createMap();
                        newPoint.putString("x", String.valueOf(point.getX()));
                        newPoint.putString("y", String.valueOf(point.getY()));
                        resultPoints.pushMap(newPoint);
                    }
                }

                event.putArray("bounds", resultPoints);
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
        // Fast swiping and touching while component is being loaded can cause _camera to be null.
        if (_camera == null) {
            return false;
        }

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
        try{
          _camera.setParameters(params);
        }
        catch(RuntimeException e ) {
          Log.e("RCTCameraViewFinder", "setParameters failed", e);
        }
    }

    /**
     * Handles setting focus to the location of the event.
     *
     * Note that this will override the focus mode on the camera to FOCUS_MODE_AUTO if available,
     * even if this was previously something else (such as FOCUS_MODE_CONTINUOUS_*; see also
     * {@link #startCamera()}. However, this makes sense - after the user has initiated any
     * specific focus intent, we shouldn't be refocusing and overriding their request!
     */
    public void handleFocus(MotionEvent event, Camera.Parameters params) {
        List<String> supportedFocusModes = params.getSupportedFocusModes();
        if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            // Ensure focus areas are enabled. If max num focus areas is 0, then focus area is not
            // supported, so we cannot do anything here.
            if (params.getMaxNumFocusAreas() == 0) {
                return;
            }

            // Cancel any previous focus actions.
            _camera.cancelAutoFocus();

            // Compute focus area rect.
            Camera.Area focusAreaFromMotionEvent;
            try {
                focusAreaFromMotionEvent = RCTCameraUtils.computeFocusAreaFromMotionEvent(event, _surfaceTextureWidth, _surfaceTextureHeight);
            } catch (final RuntimeException e) {
                e.printStackTrace();
                return;
            }

            // Set focus mode to auto.
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            // Set focus area.
            final ArrayList<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
            focusAreas.add(focusAreaFromMotionEvent);
            params.setFocusAreas(focusAreas);

            // Also set metering area if enabled. If max num metering areas is 0, then metering area
            // is not supported. We can usually safely omit this anyway, though.
            if (params.getMaxNumMeteringAreas() > 0) {
                params.setMeteringAreas(focusAreas);
            }

            // Set parameters before starting auto-focus.
            try{
              _camera.setParameters(params);
            }
            catch(RuntimeException e ) {
              Log.e("RCTCameraViewFinder", "setParameters failed", e);
            }

            // Start auto-focus now that focus area has been set. If successful, then can cancel
            // it afterwards. Wrap in try-catch to avoid crashing on merely autoFocus fails.
            try {
                _camera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success) {
                            camera.cancelAutoFocus();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** Determine the space between the first two fingers */
    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }
}
