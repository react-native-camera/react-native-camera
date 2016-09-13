package com.lwansbrough.RCTCamera;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import android.support.annotation.Nullable;
import com.facebook.react.uimanager.*;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.bridge.ReadableArray;

public class RCTCameraViewManager extends ViewGroupManager<RCTCameraView> {
    private static final String REACT_CLASS = "RCTCamera";

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public RCTCameraView createViewInstance(ThemedReactContext context) {
        return new RCTCameraView(context);
    }

    @ReactProp(name = "aspect")
    public void setAspect(RCTCameraView view, int aspect) {
        view.setAspect(aspect);
    }

    @ReactProp(name = "captureMode")
    public void setCaptureMode(RCTCameraView view, int captureMode) {
        // TODO - implement video mode
    }

    @ReactProp(name = "captureTarget")
    public void setCaptureTarget(RCTCameraView view, int captureTarget) {
        // No reason to handle this props value here since it's passed again to the RCTCameraModule capture method
    }

    @ReactProp(name = "type")
    public void setType(RCTCameraView view, int type) {
        view.setCameraType(type);
    }

    @ReactProp(name = "defaultOnFocusComponent")
    public void setDefaultOnFocusComponent(RCTCameraView view, boolean useDefault) {
        view.setDefaultOnFocusComponent(useDefault);
    }

    @ReactProp(name = "showViewFinder")
    public void setUseViewFinder(RCTCameraView view, boolean useViewFinder) {
        view.setUseViewFinder(useViewFinder);
    }

    @ReactProp(name = "viewFinderSize")
    public void setViewFinderSize(RCTCameraView view, ReadableArray viewFinderSize) {
      if(viewFinderSize==null || viewFinderSize.size()!=2){
        view.setUseViewFinder(false);
      }else{
        view.setViewFinderSize(viewFinderSize.getDouble(0),viewFinderSize.getDouble(1));
      }
    }

    @ReactProp(name = "barCodeTypes")
    public void setBarCodeTypes(RCTCameraView view, ReadableArray barCodeTypes) {
      if(barCodeTypes==null || barCodeTypes.size()==0){
        view.setBarCodeTypes(null);
      }
      else {
        List types = new ArrayList();
        for (int i = 0; i < barCodeTypes.size(); i++) {
          String type = barCodeTypes.getString(i);
          switch(type){
            case "aztec":
              types.add(RCTCameraModule.CODE_TYPE_AZTEC);
              break;
            case "codabar":
              types.add(RCTCameraModule.CODE_TYPE_CODABAR);
              break;
            case "code128":
              types.add(RCTCameraModule.CODE_TYPE_CODE_128);
              break;
            case "code93":
              types.add(RCTCameraModule.CODE_TYPE_CODE_93);
              break;
            case "code39":
              types.add(RCTCameraModule.CODE_TYPE_CODE_39);
              break;
            case "datamatrix":
              types.add(RCTCameraModule.CODE_TYPE_DATA_MATRIX);
              break;
            case "ean13":
              types.add(RCTCameraModule.CODE_TYPE_EAN_13);
              break;
            case "ean8":
              types.add(RCTCameraModule.CODE_TYPE_EAN_8);
              break;
            case "itf":
              types.add(RCTCameraModule.CODE_TYPE_ITF);
              break;
            case "maxicode":
              types.add(RCTCameraModule.CODE_TYPE_MAXICODE);
              break;
            case "pdf417":
              types.add(RCTCameraModule.CODE_TYPE_PDF_417);
              break;
            case "qr":
              types.add(RCTCameraModule.CODE_TYPE_QR_CODE);
              break;
            case "rss14":
              types.add(RCTCameraModule.CODE_TYPE_RSS_14);
              break;
            case "rss":
              types.add(RCTCameraModule.CODE_TYPE_RSS_EXPANDED);
              break;
            case "upca":
              types.add(RCTCameraModule.CODE_TYPE_UPC_A);
              break;
            case "upce":
              types.add(RCTCameraModule.CODE_TYPE_UPC_E);
              break;
            case "upc":
              types.add(RCTCameraModule.CODE_UPC_EAN_EXTENSION);
              break;
          }
        }
        android.util.Log.v("react-native-camera", "Barcode scanner for types: "+ types.toString());
        view.setBarCodeTypes(types);
      }
    }


    @ReactProp(name = "captureQuality")
    public void setCaptureQuality(RCTCameraView view, String captureQuality) {
        view.setCaptureQuality(captureQuality);
    }

    @ReactProp(name = "torchMode")
    public void setTorchMode(RCTCameraView view, int torchMode) {
        view.setTorchMode(torchMode);
    }

    @ReactProp(name = "flashMode")
    public void setFlashMode(RCTCameraView view, int flashMode) {
        view.setFlashMode(flashMode);
    }

    @ReactProp(name = "orientation")
    public void setOrientation(RCTCameraView view, int orientation) {
        view.setOrientation(orientation);
    }

    @ReactProp(name = "captureAudio")
    public void setCaptureAudio(RCTCameraView view, boolean captureAudio) {
        // TODO - implement video mode
    }
}
