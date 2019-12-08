package org.reactnative.barcodedetector;

import android.content.Context;
import android.util.Log;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;


public class RNBarcodeDetector {

    public static int NORMAL_MODE = 0;
    public static int ALTERNATE_MODE = 1;
    public static int INVERTED_MODE = 2;
    public static int ALL_FORMATS = FirebaseVisionBarcode.FORMAT_ALL_FORMATS;

    private FirebaseVisionBarcodeDetector mBarcodeDetector = null;
    private FirebaseVisionBarcodeDetectorOptions.Builder  mBuilder;

    private int mBarcodeType = FirebaseVisionBarcode.FORMAT_ALL_FORMATS;

    public RNBarcodeDetector(Context context) {
        mBuilder = new FirebaseVisionBarcodeDetectorOptions.Builder().setBarcodeFormats(mBarcodeType);
    }

    public boolean isOperational() {
        // Legacy api from GMV
        return true;
    }

    public FirebaseVisionBarcodeDetector getDetector() {

        if (mBarcodeDetector == null) {
            createBarcodeDetector();
        }
        return mBarcodeDetector;
    }

    public void setBarcodeType(int barcodeType) {
        if (barcodeType != mBarcodeType) {
            release();
            mBuilder.setBarcodeFormats(barcodeType);
            mBarcodeType = barcodeType;
        }
    }


    public void release() {
        if (mBarcodeDetector != null) {
            try {
                mBarcodeDetector.close();
            } catch (Exception e) {
                Log.e("RNCamera", "Attempt to close BarcodeDetector failed");
            }
            mBarcodeDetector = null;
        }
    }

    private void createBarcodeDetector() {
        FirebaseVisionBarcodeDetectorOptions options = mBuilder.build();
        mBarcodeDetector = FirebaseVision.getInstance()
                .getVisionBarcodeDetector(options);

    }
}
