package org.reactnative.camera.tasks;

import com.facebook.react.bridge.WritableArray;
import org.reactnative.barcodedetector.RNBarcodeDetector;

public interface BarcodeDetectorAsyncTaskDelegate {

    void onBarcodesDetected(WritableArray barcodes);

    void onBarcodeDetectionError(RNBarcodeDetector barcodeDetector);

    void onBarcodeDetectingTaskCompleted();
}
