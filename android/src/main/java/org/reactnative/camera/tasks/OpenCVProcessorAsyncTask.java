package org.reactnative.camera.tasks;

import android.os.AsyncTask;
import android.util.SparseArray;
import java.util.Map;
import org.reactnative.opencv.OpenCVProcessor;

public class OpenCVProcessorAsyncTask extends AsyncTask<Void, Void, SparseArray<Map<String, Float>>> {
  private OpenCVProcessorAsyncTaskDelegate mDelegate;
  private int mHeight;
  private byte[] mImageData;
  private OpenCVProcessor mOpenCVProcessor;
  private int mRotation;
  private int mWidth;

  public OpenCVProcessorAsyncTask(OpenCVProcessorAsyncTaskDelegate delegate, OpenCVProcessor openCVProcessor, byte[] imageData, int width, int height, int rotation) {
    this.mImageData = imageData;
    this.mWidth = width;
    this.mHeight = height;
    this.mRotation = rotation;
    this.mDelegate = delegate;
    this.mOpenCVProcessor = openCVProcessor;
  }

  protected SparseArray<Map<String, Float>> doInBackground(Void... ignored) {
    if (isCancelled() || this.mDelegate == null || this.mOpenCVProcessor == null) {
      return null;
    }
    return this.mOpenCVProcessor.detect(this.mImageData, this.mWidth, this.mHeight, this.mRotation);
  }

  protected void onPostExecute(SparseArray<Map<String, Float>> faces) {
    super.onPostExecute(faces);
    if (faces == null) {
      this.mDelegate.onFaceDetectionError(this.mOpenCVProcessor);
      return;
    }
    this.mDelegate.onFacesDetected(faces, this.mWidth, this.mHeight, this.mRotation);
    this.mDelegate.onFaceDetectingTaskCompleted();
  }
}
