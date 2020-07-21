package org.reactnative.barcodedetector;

import android.content.Context;
import android.util.Log;

import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;


public class RNBarcodeDetector {

    public static int NORMAL_MODE = 0;
    public static int ALTERNATE_MODE = 1;
    public static int INVERTED_MODE = 2;
    public static int ALL_FORMATS = Barcode.FORMAT_ALL_FORMATS;

    private BarcodeScanner mBarcodeScanner = null;
    private BarcodeScannerOptions.Builder  mBuilder;

    private int mBarcodeType = Barcode.FORMAT_ALL_FORMATS;

    public RNBarcodeDetector(Context context) {
        mBuilder = new BarcodeScannerOptions.Builder().setBarcodeFormats(mBarcodeType);
    }

    public boolean isOperational() {
        // Legacy api from GMV
        return true;
    }

    public BarcodeScanner getDetector() {

        if (mBarcodeScanner == null) {
            createBarcodeScanner();
        }
        return mBarcodeScanner;
    }

    public void setBarcodeType(int barcodeType) {
        if (barcodeType != mBarcodeType) {
            release();
            mBuilder.setBarcodeFormats(barcodeType);
            mBarcodeType = barcodeType;
        }
    }


    public void release() {
        if (mBarcodeScanner != null) {
            try {
                mBarcodeScanner.close();
            } catch (Exception e) {
                Log.e("RNCamera", "Attempt to close BarcodeScanner failed");
            }
            mBarcodeScanner = null;
        }
    }

    private void createBarcodeScanner() {
        BarcodeScannerOptions options = mBuilder.build();
        mBarcodeScanner = BarcodeScanning.getClient(options);
    }
}
