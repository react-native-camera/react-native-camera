package org.reactnative.camera.tasks;

import com.dynamsoft.dbr.BarcodeReader;
import com.dynamsoft.dbr.TextResult;
import com.dynamsoft.dbr.EnumImagePixelFormat;

public class DynamsoftBarcodeDetectorAsyncTask extends android.os.AsyncTask<Void, Void, TextResult[]> {
  private byte[] mImageData;
  private int mWidth;
  private int mHeight;
  private DynamsoftBarcodeDetectorAsyncTaskDelegate mDelegate;
  private final BarcodeReader mBarcodeReader;

  public DynamsoftBarcodeDetectorAsyncTask(
      DynamsoftBarcodeDetectorAsyncTaskDelegate delegate,
      BarcodeReader barcodeReader,
      byte[] imageData,
      int width,
      int height
  ) {
    mImageData = imageData;
    mWidth = width;
    mHeight = height;
    mDelegate = delegate;
    mBarcodeReader = barcodeReader;
  }

  @Override
  protected TextResult[] doInBackground(Void... ignored) {
    if (isCancelled() || mDelegate == null) {
      return null;
    }

    TextResult[] results = null;

    try {
        mBarcodeReader.decodeBuffer(mImageData, mWidth, mHeight, mWidth, EnumImagePixelFormat.IPF_NV21, "");
        results = mBarcodeReader.getAllTextResults();
    } catch (Exception e) {
        e.printStackTrace();
    }

    return results;
  }

  @Override
  protected void onPostExecute(TextResult[] results) {
    super.onPostExecute(results);
    if (results != null) {
      mDelegate.onDynamsoftBarCodeDetected(results, mWidth, mHeight);
    }
    mDelegate.onDynamsoftBarCodeDetectingTaskCompleted();
  }
}
