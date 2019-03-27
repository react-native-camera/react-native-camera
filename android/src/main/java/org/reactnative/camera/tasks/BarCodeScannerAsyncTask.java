package org.reactnative.camera.tasks;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

public class BarCodeScannerAsyncTask extends android.os.AsyncTask<Void, Void, Result> {
  private byte[] mImageData;
  private int mWidth;
  private int mHeight;
  private BarCodeScannerAsyncTaskDelegate mDelegate;
  private final MultiFormatReader mMultiFormatReader;

  //  note(sjchmiela): From my short research it's ok to ignore rotation of the image.
  public BarCodeScannerAsyncTask(
          BarCodeScannerAsyncTaskDelegate delegate,
          MultiFormatReader multiFormatReader,
          byte[] imageData,
          int width,
          int height
  ) {
    mImageData = imageData;
    mWidth = width;
    mHeight = height;
    mDelegate = delegate;
    mMultiFormatReader = multiFormatReader;
  }

  @Override
  protected Result doInBackground(Void... ignored) {
    if (isCancelled() || mDelegate == null) {
      return null;
    }
    Result result = generateBarcodeFromImageData(
            mImageData,
            mWidth,
            mHeight,
            false
    );
    if (result != null) {
      return result;
    }
    // rotate
    result = generateBarcodeFromImageData(
            rotateImage(mImageData,mWidth, mHeight),
            mHeight,
            mWidth,
            false
    );
    if (result != null) {
      return result;
    }
    // inverse
    result = generateBarcodeFromImageData(
            mImageData,
            mWidth,
            mHeight,
            true
    );
    if (result != null) {
      return result;
    }
    // rotate and inverse
    result = generateBarcodeFromImageData(
            rotateImage(mImageData,mWidth, mHeight),
            mHeight,
            mWidth,
            true
    );
    return result;
  }
  private byte[] rotateImage(byte[]imageData,int width, int height) {
    byte[] rotated = new byte[imageData.length];
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        rotated[x * height + height - y - 1] = imageData[x + y * width];
      }
    }
    return rotated;
  }
  @Override
  protected void onPostExecute(Result result) {
    super.onPostExecute(result);
    if (result != null) {
      mDelegate.onBarCodeRead(result, mWidth, mHeight);
    }
    mDelegate.onBarCodeScanningTaskCompleted();
  }

  private Result generateBarcodeFromImageData(byte[] imageData, int width, int height, boolean inverse) {
    try {
      PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
              imageData, // byte[] yuvData
              width, // int dataWidth
              height, // int dataHeight
              0, // int left
              0, // int top
              width, // int width
              height, // int height
              false // boolean reverseHorizontal
      );
      BinaryBitmap bitmap;
      if (inverse) {
        bitmap =  new BinaryBitmap(new HybridBinarizer(source.invert()));
      } else {
        bitmap =  new BinaryBitmap(new HybridBinarizer(source));
      }
      return mMultiFormatReader.decodeWithState(bitmap);
    } catch (Exception e) {
      e.printStackTrace();
    }
    mMultiFormatReader.reset();
    return null;
  }
}
