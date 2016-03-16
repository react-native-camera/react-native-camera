package com.lwansbrough.RCTCamera;

import android.hardware.Camera;
import android.os.AsyncTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.EnumMap;
import java.util.EnumSet;

/**
 * Created by lance.zhou on 2016/3/17.
 */
public class BarcodeReader {
    private final MultiFormatReader multiFormatReader;

    public BarcodeReader() {
        EnumMap<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        EnumSet<BarcodeFormat> decodeFormats = EnumSet.noneOf(BarcodeFormat.class);
        decodeFormats.add(BarcodeFormat.QR_CODE);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);
    }

    public void read(byte[] image, Camera camera, CallBack callBack) {
        new ReaderAsyncTask(camera, image, callBack).execute();
    }

    public void stop() {

    }

    private class ReaderAsyncTask extends AsyncTask<Void, Void, Void> {
        private final Camera camera;
        private final byte[] image;
        private final CallBack callBack;

        ReaderAsyncTask(Camera camera, byte[] image, CallBack callBack) {
            this.camera = camera;
            this.image = image;
            this.callBack = callBack;
        }

        @Override
        protected Void doInBackground(Void... images) {
            if (isCancelled())
                return null;

            Camera.Size size = camera.getParameters().getPreviewSize();
            try {
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(buildLuminanceSource(image, size.width, size.height)));
                callBack.onResult(camera, multiFormatReader.decodeWithState(bitmap));
            } catch (ReaderException re) {
                // continue
                callBack.onResult(camera, null);
            } finally {
                multiFormatReader.reset();
            }
            return null;
        }

        // Go ahead and assume it's YUV rather than die.
        private PlanarYUVLuminanceSource buildLuminanceSource(byte[] image, int width, int height) {
            return new PlanarYUVLuminanceSource(image, width, height, 0, 0, width, height, false);
        }
    }

    public interface CallBack {
        void onResult(Camera camera, Result result);
    }
}
