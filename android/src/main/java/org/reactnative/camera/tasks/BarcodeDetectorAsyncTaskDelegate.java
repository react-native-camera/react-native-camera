package org.reactnative.camera.tasks;

import com.facebook.react.bridge.WritableArray;
import org.reactnative.barcodedetector.RNBarcodeDetector;

public interface BarcodeDetectorAsyncTaskDelegate {

    void onBarcodesDetected(WritableArray barcodes, int width, int height, byte[] imageData);

    void onBarcodeDetectionError(RNBarcodeDetector barcodeDetector);

    void onBarcodeDetectingTaskCompleted();
}
