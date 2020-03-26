package org.reactnative.camera.tasks;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import org.reactnative.camera.utils.ScanArea;
import android.util.Log;

public class BarCodeScannerAsyncTask extends android.os.AsyncTask<Void, Void, Result> {
  private byte[] mImageData;
  private int mWidth;
  private int mHeight;
  private BarCodeScannerAsyncTaskDelegate mDelegate;
  private final MultiFormatReader mMultiFormatReader;
  private ScanArea mScanArea;

  public BarCodeScannerAsyncTask(
      BarCodeScannerAsyncTaskDelegate delegate,
      MultiFormatReader multiFormatReader,
      byte[] imageData,
      ScanArea scanArea
  ) {
    mScanArea = scanArea;
    mWidth = mScanArea.getWidth();
    mHeight = mScanArea.getHeight();
    mImageData = rotateImage(imageData,mWidth, mHeight);
    mDelegate = delegate;
    mMultiFormatReader = multiFormatReader;
  }

  @Override
  protected Result doInBackground(Void... ignored) {
    if (isCancelled() || mDelegate == null) {
      return null;
    }

    Result result = null;
    boolean mLimitScanArea = mScanArea.getLimitScanArea();
    int scanWidth = mLimitScanArea ? mScanArea.getCropArea("width") : mWidth;
    int scanHeight = mLimitScanArea ? mScanArea.getCropArea("height") : mHeight;
    int left = mLimitScanArea ? mScanArea.getLeft() : 0;
    int top = mLimitScanArea ? mScanArea.getTop() : 0;
    PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
      mImageData,
      mHeight,
      mWidth, 
      mHeight - scanHeight - top,
      left,
      scanHeight,
      scanWidth,
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
      mDelegate.onBarCodeRead(result, mWidth, mHeight);
    }
    mDelegate.onBarCodeScanningTaskCompleted();
  }
}
