package org.reactnative.camera.tasks;

import com.dynamsoft.dbr.TextResult;

public interface DynamsoftBarcodeDetectorAsyncTaskDelegate {
    void onDynamsoftBarCodeDetected(TextResult[] barcodes, int width, int height);
    void onDynamsoftBarCodeDetectingTaskCompleted();
}
