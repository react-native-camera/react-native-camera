package org.reactnative.camera.tasks;

import com.facebook.react.bridge.WritableMap;

public interface DocumentDetectorAsyncTaskDelegate {
    void onDocumentDetected(WritableMap document);
    void onDocumentDetectingTaskCompleted();
}
