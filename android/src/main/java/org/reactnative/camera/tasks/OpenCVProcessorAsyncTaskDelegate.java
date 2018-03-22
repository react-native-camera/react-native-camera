package org.reactnative.camera.tasks;

import android.util.SparseArray;
import java.util.Map;
import org.reactnative.opencv.OpenCVProcessor;

public interface OpenCVProcessorAsyncTaskDelegate {
  void onFaceDetectingTaskCompleted();

  void onFaceDetectionError(OpenCVProcessor openCVProcessor);

  void onFacesDetected(SparseArray<Map<String, Float>> sparseArray, int sourceWidth, int sourceHeight, int sourceRotation);
}