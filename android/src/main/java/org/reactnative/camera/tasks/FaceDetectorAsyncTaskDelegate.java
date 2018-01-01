package org.reactnative.camera.tasks;

import android.util.SparseArray;

import org.reactnative.facedetector.RNFaceDetector;
import com.google.android.gms.vision.face.Face;

public interface FaceDetectorAsyncTaskDelegate {
  void onFacesDetected(SparseArray<Face> face, int sourceWidth, int sourceHeight, int sourceRotation);
  void onFaceDetectionError(RNFaceDetector faceDetector);
  void onFaceDetectingTaskCompleted();
}
