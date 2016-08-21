/**
 * Created by Fabrice Armisen (farmisen@gmail.com) on 1/3/16.
 */

package com.lwansbrough.RCTCamera;

import android.content.res.Configuration;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.TextureView;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.DecodeHintType;

class RCTCameraViewFinder extends TextureView implements TextureView.SurfaceTextureListener, Camera.PreviewCallback {
    private int _cameraType;
    private SurfaceTexture _surfaceTexture;
    private boolean _isStarting;
    private boolean _isStopping;
    private Camera _camera;
    private MultiFormatReader _multiFormatReader;
    private boolean _scanForBarcodes;
    private boolean _useViewFinder;
    private int _viewFinderWidth;
    private int _viewFinderHeight;

    public RCTCameraViewFinder(Context context, int type) {
        super(context);
        this.setSurfaceTextureListener(this);
        this._cameraType = type;
        _scanForBarcodes = false;
        _multiFormatReader = new MultiFormatReader();
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


    public void setBarCodeTypes(List<String> barCodeTypes) {
      if(barCodeTypes == null){
        _scanForBarcodes = false;
        _multiFormatReader.setHints(null);
      }
      else {
        _scanForBarcodes = true;
        HashMap<DecodeHintType, Object> myMap = new HashMap<DecodeHintType, Object>();
        myMap.put(DecodeHintType.POSSIBLE_FORMATS, barCodeTypes);
        _multiFormatReader.setHints(myMap);
      }
    }

    public void setUseViewFinder(boolean useViewFinder){
      _useViewFinder = useViewFinder;
    }

    public void setViewFinderSize(int w, int h){
      _viewFinderWidth = w;
      _viewFinderHeight = h;
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

    // Grab preview and try to read barcodes
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
      if(_camera != null && _surfaceTexture != null && camera != null){

        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = parameters.getPreviewSize();
        int width = size.width;
        int height = size.height;
        int orgWidth = width;
        int orgHeight = height;
        int x = 0;
        int y = 0;

        // If using viewfinder, crop search area to this for performance
        if(_useViewFinder && _viewFinderWidth > 0 && _viewFinderHeight > 0){
          x = (width/2) - (_viewFinderWidth/2);
          y = (height/2) - (_viewFinderHeight/2);
          width = _viewFinderWidth;
          height = _viewFinderHeight;
        }

        // rotate image for zxing parser
        if (RCTCamera.getInstance().getOrientation() ==  Configuration.ORIENTATION_PORTRAIT) {
            byte[] rotatedData = new byte[data.length];
            for (int ypos = y; y < height; y++) {
                for (int xpos = x; x < width; x++)
                    rotatedData[xpos * height + height - ypos - 1] = data[xpos + ypos * width];
            }

            int tmp = width;
            width = height;
            height = tmp;
            data = rotatedData;
        }

        Result rawResult = null;
        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, orgWidth, orgHeight, x, y, width, height, false);
        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                rawResult = _multiFormatReader.decodeWithState(bitmap);
            } catch (ReaderException re) {
                // continue
                rawResult = null;
            } catch (NullPointerException npe) {
                // This is terrible
                rawResult = null;
            } catch (ArrayIndexOutOfBoundsException aoe) {
                rawResult = null;
            } finally {
                _multiFormatReader.reset();
            }
        }

        // We found a barcode
        if (rawResult != null) {
          String foundType = rawResult.getBarcodeFormat().toString();
          String eventType = "unknown";
          Map<String,Object> types = (Map<String,Object>)RCTCameraModule.getInstance().getConstants().get("BarCodeType");
          Set keys = types.keySet();
          for (Iterator i = keys.iterator(); i.hasNext(); ) {
            String key = (String) i.next();
            String value = (String) types.get(key);
            if(value==foundType){
              eventType = key;
              break;
            }
           }
           WritableMap event = Arguments.createMap();
           event.putString("data", rawResult.getText());
           event.putString("type", eventType);
           ReactContext reactContext = (ReactContext)getContext();
           reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onBarCodeRead",event);
        }
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


                // only scan if we want to
                if(_scanForBarcodes){
                  // if support for barcode scanning scene, use this mode
                  List<String> sceneModes = parameters.getSupportedSceneModes();
                  if (sceneModes != null && sceneModes.contains(Camera.Parameters.SCENE_MODE_BARCODE)) {
                      parameters.setSceneMode(Camera.Parameters.SCENE_MODE_BARCODE);
                  }
                  _camera.setPreviewCallback(this);
                }
                _camera.setPreviewTexture(_surfaceTexture);
                _camera.setParameters(parameters);
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
                    _camera.setPreviewCallback(null);
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
