package org.reactnative.camera.tasks;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import android.util.Log;
import android.graphics.Rect;

public class BarCodeScannerAsyncTask extends android.os.AsyncTask<Void, Void, Result> {
  private byte[] mImageData;
  private int mDataWidth;
  private int mDataHeight;
  private BarCodeScannerAsyncTaskDelegate mDelegate;
  private final MultiFormatReader mMultiFormatReader;
  private Rect mRect;

  public BarCodeScannerAsyncTask(
      BarCodeScannerAsyncTaskDelegate delegate,
      MultiFormatReader multiFormatReader,
      byte[] imageData,
      int dataWidth,
      int dataHeight,
      Rect rect
  ) {
    mDataWidth = dataWidth;
    mDataHeight = dataHeight;
    mImageData = imageData;
    mDelegate = delegate;
    mMultiFormatReader = multiFormatReader;
    mRect = rect.isEmpty() ? new Rect(0, 0, mDataWidth, mDataHeight) : rect;
  }

  @Override
  protected Result doInBackground(Void... ignored) {
    if (isCancelled() || mDelegate == null) {
      return null;
    }
    Result result = null;
    boolean landscapeMode = mDataWidth > mDataHeight;
    byte[] data = landscapeMode ? mImageData : rotateImage(mImageData, mDataWidth, mDataHeight);
    PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
      data,
      mDataWidth,
      mDataHeight,
      mRect.left,
      mRect.top,
      mRect.width(),
      mRect.height(),
      false
    );
    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
    try {
      result = mMultiFormatReader.decodeWithState(bitmap);
    } catch (NotFoundException e) {
      Log.w("CAMERA_1::", "BarCodeScannerAsyncTask doInBackground throws: ", e);
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
      mDelegate.onBarCodeRead(result, mDataWidth, mDataHeight);
    }
    mDelegate.onBarCodeScanningTaskCompleted();
  }
}
