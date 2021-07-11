package org.reactnative.barcodedetector;

import android.content.Context;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;

import org.reactnative.camera.utils.ImageDimensions;
import org.reactnative.frame.RNFrame;

import java.util.List;

public class RNBarcodeDetector {

    public static int NORMAL_MODE = 0;
    public static int ALTERNATE_MODE = 1;
    public static int INVERTED_MODE = 2;
    public static int ALL_FORMATS = Barcode.FORMAT_ALL_FORMATS;

    private BarcodeScanner mBarcodeDetector = null;
    private ImageDimensions mPreviousDimensions;

    private int mBarcodeType = Barcode.FORMAT_ALL_FORMATS;
    private BarcodeScannerOptions.Builder mBuilder;

    public RNBarcodeDetector(Context context) {
        mBuilder = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(mBarcodeType);
    }

    // Public API

    public boolean isOperational() {
        if (mBarcodeDetector == null) {
            createBarcodeDetector();
        }

        return true;
    }

    public List<Barcode> detect(RNFrame frame) {
        // If the frame has different dimensions, create another barcode detector.
        // Otherwise we will most likely get nasty "inconsistent image dimensions" error from detector
        // and no barcode will be detected.
        if (!frame.getDimensions().equals(mPreviousDimensions)) {
            releaseBarcodeDetector();
        }

        if (mBarcodeDetector == null) {
            createBarcodeDetector();
            mPreviousDimensions = frame.getDimensions();
        }

        return mBarcodeDetector.process(frame.getFrame()).getResult();
    }

    public void setBarcodeType(int barcodeType) {
        if (barcodeType != mBarcodeType) {
            release();
            mBuilder.setBarcodeFormats(barcodeType);
            mBarcodeType = barcodeType;
        }
    }


    public void release() {
        releaseBarcodeDetector();
        mPreviousDimensions = null;
    }

    // Lifecycle methods

    private void releaseBarcodeDetector() {
        if (mBarcodeDetector != null) {
            mBarcodeDetector.close();
            mBarcodeDetector = null;
        }
    }

    private void createBarcodeDetector() {
        mBarcodeDetector = BarcodeScanning.getClient(mBuilder.build());
    }
}
