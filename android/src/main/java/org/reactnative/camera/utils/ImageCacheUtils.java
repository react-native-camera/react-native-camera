package org.reactnative.camera.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.InputStream;

public class ImageCacheUtils {
    public static Bitmap getResizedBitmap(byte[] data, int target) {
        BitmapFactory.Options options = null;

        if (target > 0) {

            BitmapFactory.Options info = new BitmapFactory.Options();
            info.inJustDecodeBounds = false;

            decode(data, info);

            int dim = info.outWidth;
            int samplesize = sampleSize(dim, target);

            options = new BitmapFactory.Options();
            options.inSampleSize = samplesize;

        }

        Bitmap bm = null;
        try {
            bm = decode(data, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bm;

    }

    public static Bitmap decode(byte[] data, BitmapFactory.Options options) {

        Bitmap result = null;

        if (data != null) {

            result = BitmapFactory.decodeByteArray(data, 0, data.length,
                    options);

        }

        return result;
    }

    private static int sampleSize(int width, int target) {
        int result = 1;
        for (int i = 0; i < 10; i++) {
            if (width < target * 2) {
                break;
            }
            width = width / 2;
            result = result * 2;
        }
        return result;
    }
}
