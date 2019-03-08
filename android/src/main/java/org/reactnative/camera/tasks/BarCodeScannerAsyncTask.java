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

    Result result = null;

    try {
      BinaryBitmap bitmap = generateBitmapFromImageData(
              mImageData,
              mWidth,
              mHeight,
              false
      );
      result = mMultiFormatReader.decodeWithState(bitmap);
    } catch (NotFoundException e) {
      BinaryBitmap bitmap = generateBitmapFromImageData(
              rotateImage(mImageData,mWidth, mHeight),
              mHeight,
              mWidth,
              false
      );
      try {
        result = mMultiFormatReader.decodeWithState(bitmap);
      } catch (NotFoundException e1) {
          BinaryBitmap invertedBitmap = generateBitmapFromImageData(
                  mImageData,
                  mWidth,
                  mHeight,
                  true
          );
        try {
          result = mMultiFormatReader.decodeWithState(invertedBitmap);
        } catch (NotFoundException e2) {
          BinaryBitmap invertedRotatedBitmap = generateBitmapFromImageData(
                  rotateImage(mImageData,mWidth, mHeight),
                  mHeight,
                  mWidth,
                  true
          );
          try {
            result = mMultiFormatReader.decodeWithState(invertedRotatedBitmap);
          } catch (NotFoundException e3) {
            //no barcode Found
          }
        }
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }

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

  private BinaryBitmap generateBitmapFromImageData(byte[] imageData, int width, int height, boolean inverse) {
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
    if (inverse) {
      return new BinaryBitmap(new HybridBinarizer(source.invert()));
    } else {
      return new BinaryBitmap(new HybridBinarizer(source));
    }
  }
}
