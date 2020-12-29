package org.reactnative.camera.tasks;

import org.reactnative.facedetector.RNFaceDetector;

import com.facebook.react.bridge.WritableArray;

import java.util.HashMap;

public interface FaceDetectorAsyncTaskDelegate {
    // =============<<<<<<<<<<<<<<<<< check here
    void onFacesDetected(WritableArray faces);
    void onFaceDetectionError(RNFaceDetector faceDetector);
    void onFaceDetectingTaskCompleted();
    void saveFaceDetected(HashMap<String,Float> face);
}
