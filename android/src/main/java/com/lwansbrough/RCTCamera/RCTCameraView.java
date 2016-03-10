/**
 * Created by Fabrice Armisen (farmisen@gmail.com) on 1/3/16.
 */

package com.lwansbrough.RCTCamera;

import java.util.List;

import android.content.Context;
import android.graphics.*;
import android.hardware.SensorManager;
import android.view.OrientationEventListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.util.Log;
import android.support.annotation.Nullable;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.*;

import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.*;

public class RCTCameraView extends ViewGroup {
	private ThemedReactContext mContext;
	private BarcodeView mScanner;

	private void sendEvent(String eventName,
												 @Nullable WritableMap params) {
		mContext
				.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
				.emit(eventName, params);
	}

	private BarcodeCallback callback = new BarcodeCallback() {
		@Override
		public void barcodeResult(BarcodeResult result) {
			if (result.getText() != null) {
				Log.d("BARCODE", result.getText());
				WritableMap params = Arguments.createMap();
				params.putString("code", result.getText());
				sendEvent("CameraBarCodeRead", params);
			}
		}

		@Override
		public void possibleResultPoints(List<ResultPoint> resultPoints) {
		}
	};

  public RCTCameraView(ThemedReactContext context) {
    super(context);
		mContext = context;
		mScanner = new BarcodeView(context);
		addView(mScanner);
		mScanner.resume();
		mScanner.decodeContinuous(callback);
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		this.mScanner.layout(
			0, 0, right - left, bottom - top
		);
  }

  public void setAspect(int aspect) {
  }

  public void setCameraType(final int type) {
  }

  public void setTorchMode(int torchMode) {
      //this._torchMode = torchMode;
      //if (this._viewFinder != null) {
          //this._viewFinder.setTorchMode(torchMode);
      //}
  }

  public void setFlashMode(int flashMode) {
      //this._flashMode = flashMode;
      //if (this._viewFinder != null) {
          //this._viewFinder.setFlashMode(flashMode);
      //}
  }

  public void setOrientation(int orientation) {
      //RCTCamera.getInstance().setOrientation(orientation);
      //if (this._viewFinder != null) {
          //layoutViewFinder();
      //}
  }

  //private boolean setActualDeviceOrientation(Context context) {
      //int actualDeviceOrientation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getOrientation();
      //if (_actualDeviceOrientation != actualDeviceOrientation) {
          //_actualDeviceOrientation = actualDeviceOrientation;
          //RCTCamera.getInstance().setActualDeviceOrientation(_actualDeviceOrientation);
          //return true;
      //} else {
          //return false;
      //}
  //}

  //private void layoutViewFinder() {
      //layoutViewFinder(this.getLeft(), this.getTop(), this.getRight(), this.getBottom());
  //}

  //private void layoutViewFinder(int left, int top, int right, int bottom) {
      //if (null == _viewFinder) {
          //return;
      //}
      //float width = right - left;
      //float height = bottom - top;
      //int viewfinderWidth;
      //int viewfinderHeight;
      //double ratio;
      //switch (this._aspect) {
          //case RCTCameraModule.RCT_CAMERA_ASPECT_FIT:
              //ratio = this._viewFinder.getRatio();
              //if (ratio * height > width) {
                  //viewfinderHeight = (int) (width / ratio);
                  //viewfinderWidth = (int) width;
              //} else {
                  //viewfinderWidth = (int) (ratio * height);
                  //viewfinderHeight = (int) height;
              //}
              //break;
          //case RCTCameraModule.RCT_CAMERA_ASPECT_FILL:
              //ratio = this._viewFinder.getRatio();
              //if (ratio * height < width) {
                  //viewfinderHeight = (int) (width / ratio);
                  //viewfinderWidth = (int) width;
              //} else {
                  //viewfinderWidth = (int) (ratio * height);
                  //viewfinderHeight = (int) height;
              //}
              //break;
          //default:
              //viewfinderWidth = (int) width;
              //viewfinderHeight = (int) height;
      //}

      //int viewFinderPaddingX = (int) ((width - viewfinderWidth) / 2);
      //int viewFinderPaddingY = (int) ((height - viewfinderHeight) / 2);

      //this._viewFinder.layout(viewFinderPaddingX, viewFinderPaddingY, viewFinderPaddingX + viewfinderWidth, viewFinderPaddingY + viewfinderHeight);
      //this.postInvalidate(this.getLeft(), this.getTop(), this.getRight(), this.getBottom());
  //}
}
