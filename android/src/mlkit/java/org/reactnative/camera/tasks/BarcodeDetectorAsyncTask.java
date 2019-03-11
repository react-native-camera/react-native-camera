package org.reactnative.camera.tasks;

//import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;

import org.reactnative.barcodedetector.BarcodeFormatUtils;
import org.reactnative.barcodedetector.RNBarcodeDetector;
import org.reactnative.camera.utils.ImageDimensions;

import java.util.List;

public class BarcodeDetectorAsyncTask extends android.os.AsyncTask<Void, Void, Void> {

  private byte[] mImageData;
  private int mWidth;
  private int mHeight;
  private int mRotation;
  private RNBarcodeDetector mBarcodeDetector;
  private BarcodeDetectorAsyncTaskDelegate mDelegate;
  private double mScaleX;
  private double mScaleY;
  private ImageDimensions mImageDimensions;
  private int mPaddingLeft;
  private int mPaddingTop;
  private String TAG = "RNCamera";

  public BarcodeDetectorAsyncTask(
      BarcodeDetectorAsyncTaskDelegate delegate,
      RNBarcodeDetector barcodeDetector,
      byte[] imageData,
      int width,
      int height,
      int rotation,
      float density,
      int facing,
      int viewWidth,
      int viewHeight,
      int viewPaddingLeft,
      int viewPaddingTop
  ) {
    mImageData = imageData;
    mWidth = width;
    mHeight = height;
    mRotation = rotation;
    mDelegate = delegate;
    mBarcodeDetector = barcodeDetector;
    mImageDimensions = new ImageDimensions(width, height, rotation, facing);
    mScaleX = (double) (viewWidth) / (mImageDimensions.getWidth() * density);
    mScaleY = (double) (viewHeight) / (mImageDimensions.getHeight() * density);
    mPaddingLeft = viewPaddingLeft;
    mPaddingTop = viewPaddingTop;
  }

  @Override
  protected Void doInBackground(Void... ignored) {
    if (isCancelled() || mDelegate == null || mBarcodeDetector == null) {
      return null;
    }

    final FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
            .setWidth(mWidth)
            .setHeight(mHeight)
            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_YV12)
            .setRotation(getFirebaseRotation())
            .build();
    FirebaseVisionImage image = FirebaseVisionImage.fromByteArray(mImageData, metadata);

    FirebaseVisionBarcodeDetector barcode = mBarcodeDetector.getDetector();
    barcode.detectInImage(image)
            .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
              @Override
              public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                WritableArray serializedBarcodes = serializeEventData(barcodes);
                mDelegate.onBarcodesDetected(serializedBarcodes);
                mDelegate.onBarcodeDetectingTaskCompleted();
              }
            })
            .addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(Exception e) {
                Log.e(TAG, "Text recognition task failed" + e);
                mDelegate.onBarcodeDetectingTaskCompleted();
              }
            });
    return null;
  }

  private int getFirebaseRotation(){
    int result;
    switch (mRotation) {
      case 0:
        result = FirebaseVisionImageMetadata.ROTATION_0;
        break;
      case 90:
        result = FirebaseVisionImageMetadata.ROTATION_90;
        break;
      case 180:
        result = FirebaseVisionImageMetadata.ROTATION_180;
        break;
      case -90:
        result = FirebaseVisionImageMetadata.ROTATION_270;
        break;
      default:
        result = FirebaseVisionImageMetadata.ROTATION_0;
        Log.e(TAG, "Bad rotation value: " + mRotation);
    }
    return result;
  }


  private WritableArray serializeEventData(List<FirebaseVisionBarcode> barcodes) {
    WritableArray barcodesList = Arguments.createArray();

    for (FirebaseVisionBarcode barcode: barcodes) {
      // TODO implement position and data from all barcode types
      Rect bounds = barcode.getBoundingBox();
//      Point[] corners = barcode.getCornerPoints();

      String rawValue = barcode.getRawValue();

      int valueType = barcode.getValueType();

      WritableMap serializedBarcode = Arguments.createMap();

      switch (valueType) {
        case FirebaseVisionBarcode.TYPE_WIFI:
          String ssid = barcode.getWifi().getSsid();
          String password = barcode.getWifi().getPassword();
          int type = barcode.getWifi().getEncryptionType();
          String typeString = "UNKNOWN";
          switch (type) {
            case FirebaseVisionBarcode.WiFi.TYPE_OPEN:
              typeString = "Open";
              break;
            case FirebaseVisionBarcode.WiFi.TYPE_WEP:
              typeString = "WEP";
              break;
            case FirebaseVisionBarcode.WiFi.TYPE_WPA:
              typeString = "WPA";
              break;
          }
          serializedBarcode.putString("encryptionType", typeString);
          serializedBarcode.putString("password", password);
          serializedBarcode.putString("ssid", ssid);
          break;
        case FirebaseVisionBarcode.TYPE_URL:
          String title = barcode.getUrl().getTitle();
          String url = barcode.getUrl().getUrl();
          serializedBarcode.putString("url", url);
          serializedBarcode.putString("title", title);
          break;
      }

      serializedBarcode.putString("data", barcode.getDisplayValue());
      serializedBarcode.putString("dataRaw", rawValue);
      serializedBarcode.putString("type", BarcodeFormatUtils.get(valueType));
      serializedBarcode.putMap("bounds", processBounds(bounds));
      barcodesList.pushMap(serializedBarcode);
    }

    return barcodesList;
  }

  private WritableMap processBounds(Rect frame) {
    WritableMap origin = Arguments.createMap();
    int x = frame.left;
    int y = frame.top;

    if (frame.left < mWidth / 2) {
      x = x + mPaddingLeft / 2;
    } else if (frame.left > mWidth /2) {
      x = x - mPaddingLeft / 2;
    }

    if (frame.top < mHeight / 2) {
      y = y + mPaddingTop / 2;
    } else if (frame.top > mHeight / 2) {
      y = y - mPaddingTop / 2;
    }

    origin.putDouble("x", x * mScaleX);
    origin.putDouble("y", y * mScaleY);

    WritableMap size = Arguments.createMap();
    size.putDouble("width", frame.width() * mScaleX);
    size.putDouble("height", frame.height() * mScaleY);

    WritableMap bounds = Arguments.createMap();
    bounds.putMap("origin", origin);
    bounds.putMap("size", size);
    return bounds;
  }

}
