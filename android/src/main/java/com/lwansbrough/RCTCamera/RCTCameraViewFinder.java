/**
 * Created by Fabrice Armisen (farmisen@gmail.com) on 1/3/16.
 */

package com.lwansbrough.RCTCamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.view.MotionEvent;
import android.view.TextureView;
import android.os.AsyncTask;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.EnumMap;
import java.util.EnumSet;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
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
    private float mFingerSpacing;
    private List<String> _barCodeTypes;

    // concurrency lock for barcode scanner to avoid flooding the runtime
    public static volatile boolean barcodeScannerTaskLock = false;

    // reader instance for the barcode scanner
    private final MultiFormatReader _multiFormatReader = new MultiFormatReader();

    public RCTCameraViewFinder(Context context, int type) {
        super(context);
        this.setSurfaceTextureListener(this);
        this._cameraType = type;
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

    public void setBarCodeTypes(List<String> barCodeTypes) {
        _barCodeTypes = barCodeTypes;
        this.initBarcodeReader(_barCodeTypes);
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
        if ("AZTEC".equals(c)) {
            return BarcodeFormat.AZTEC;
        } else if ("CODABAR".equals(c)) {
            return BarcodeFormat.CODABAR;
        } else if ("CODE_128".equals(c)) {
            return BarcodeFormat.CODE_128;
        } else if ("CODE_93".equals(c)) {
            return BarcodeFormat.CODE_93;
        } else if ("CODE_39".equals(c)) {
            return BarcodeFormat.CODE_39;
        } else if ("DATA_MATRIX".equals(c)) {
            return BarcodeFormat.DATA_MATRIX;
        } else if ("EAN_13".equals(c)) {
            return BarcodeFormat.EAN_13;
        } else if ("EAN_8".equals(c)) {
            return BarcodeFormat.EAN_8;
        } else if ("ITF".equals(c)) {
            return BarcodeFormat.ITF;
        } else if ("MAXICODE".equals(c)) {
            return BarcodeFormat.MAXICODE;
        } else if ("PDF_417".equals(c)) {
            return BarcodeFormat.PDF_417;
        } else if ("QR_CODE".equals(c)) {
            return BarcodeFormat.QR_CODE;
        } else if ("RSS_14".equals(c)) {
            return BarcodeFormat.RSS_14;
        } else if ("RSS_EXPANDED".equals(c)) {
            return BarcodeFormat.RSS_EXPANDED;
        } else if ("UPC_A".equals(c)) {
            return BarcodeFormat.UPC_A;
        } else if ("UPC_E".equals(c)) {
            return BarcodeFormat.UPC_E;
        } else if ("UPC_EAN_EXTENSION".equals(c)) {
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

            RCTCamera settings = RCTCamera.getInstance();
            Camera.Parameters params = camera.getParameters();
            // lets convert preview to bytearray that we can use
            YuvImage imageConvert;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int quality = 50; //set quality
            try {

                imageConvert = new YuvImage(this.imageData, params.getPreviewFormat(), params.getPreviewSize().width, params.getPreviewSize().height, null);
                imageConvert.compressToJpeg(new Rect(0, 0, params.getPreviewSize().width, params.getPreviewSize().height), quality, baos);//this line decreases the image quality
                this.imageData = baos.toByteArray();
                baos = null;
            }
            catch(Exception e){
                android.util.Log.e("RCTCamera","Convert preview",e);
                return null;
            }

            // find rotation that will be used
            int deviceRotation = settings.getActualDeviceOrientation();
            int rotationIndex = 0;
            switch(deviceRotation){
                case 0: // portrait
                    rotationIndex = 6;
                    break;
                case 1: // landscape left
                    rotationIndex = 1;
                    break;
                case 2: // portrait upside down

                    break;
                case 3: // landscape right
                    rotationIndex = 3;
                    break;
            }

            // rotate
            MutableImage mutableImage = new MutableImage(this.imageData);
            try{
                mutableImage.rotate(rotationIndex);
            } catch (MutableImage.ImageMutationFailedException e) {
                android.util.Log.e("RCTCamera","Rotate temp image",e);
                return null;
            }

            Bitmap bitmap = mutableImage.getCurrentRepresentation();
            int width; int height;

            // if using viewfinder, crop search area
            if ( settings.barcodeFinderVisible() && settings.barcodeFinderHeight() != 0 && settings.barcodeFinderWidth() != 0) {

                // Get actual size based on %
                width = (int) (bitmap.getWidth() * settings.barcodeFinderWidth() );
                height = (int) (bitmap.getHeight() * settings.barcodeFinderHeight() );
                // find center - new position
                int x = (bitmap.getWidth() / 2) - (width / 2);
                int y = (bitmap.getHeight() / 2) - (height / 2);
                // our small image
                bitmap = Bitmap.createBitmap(bitmap, x, y, width, height);
            }
            else {
                width = bitmap.getWidth();
                height = bitmap.getHeight();
            }

            int[] intArray = new int[width*height];
            bitmap.getPixels(intArray, 0, width, 0, 0, width, height);



            try {

                LuminanceSource source = new RGBLuminanceSource(width, height,intArray);
                BinaryBitmap bbitmap = new BinaryBitmap(new HybridBinarizer(source));
                Result result = _multiFormatReader.decodeWithState(bbitmap);

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
                android.util.Log.v("RCTCamera", "Barcode scan not found anything");
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
            _camera.setParameters(params);

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
