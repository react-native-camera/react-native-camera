package org.reactnative.camera.tasks;

import android.util.SparseArray;
import com.google.android.gms.vision.barcode.Barcode;
import org.reactnative.frame.RNFrame;
import org.reactnative.frame.RNFrameFactory;
import org.reactnative.barcodedetector.RNBarcodeDetector;

public class BarcodeDetectorAsyncTask extends android.os.AsyncTask<Void, Void, SparseArray<Barcode>> {

  private byte[] mImageData;
  private int mWidth;
  private int mHeight;
  private int mRotation;
  private RNBarcodeDetector mBarcodeDetector;
  private BarcodeDetectorAsyncTaskDelegate mDelegate;

  public BarcodeDetectorAsyncTask(
      BarcodeDetectorAsyncTaskDelegate delegate,
      RNBarcodeDetector barcodeDetector,
      byte[] imageData,
      int width,
      int height,
      int rotation
  ) {
    mImageData = imageData;
    mWidth = width;
    mHeight = height;
    mRotation = rotation;
    mDelegate = delegate;
    mBarcodeDetector = barcodeDetector;
  }

  @Override
  protected SparseArray<Barcode> doInBackground(Void... ignored) {
    if (isCancelled() || mDelegate == null || mBarcodeDetector == null || !mBarcodeDetector.isOperational()) {
      return null;
    }

    RNFrame frame = RNFrameFactory.buildFrame(mImageData, mWidth, mHeight, mRotation);
    return mBarcodeDetector.detect(frame);
  }

  @Override
  protected void onPostExecute(SparseArray<Barcode> barcodes) {
    super.onPostExecute(barcodes);

    if (barcodes == null) {
      mDelegate.onBarcodeDetectionError(mBarcodeDetector);
    } else {
      if (barcodes.size() > 0) {
        mDelegate.onBarcodesDetected(barcodes, mWidth, mHeight, mRotation);
      }
      mDelegate.onBarcodeDetectingTaskCompleted();
    }
  }
}
