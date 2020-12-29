package org.reactnative.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.util.Log;

import com.google.android.gms.vision.Frame;

import org.reactnative.camera.utils.RNFileUtils;
import org.reactnative.frame.RNFrame;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;



public class ImageUtils {
    //    private static int[][] convertTo2DWithoutUsingGetRGB(Buffer image) {
//
//        final byte[] pixels = ((Buffer) image.getRaster().getDataBuffer()).getData();
//        final int width = image.getWidth();
//        final int height = image.getHeight();
//        final boolean hasAlphaChannel = image.getAlphaRaster() != null;
//
//        int[][] result = new int[height][width];
//        if (hasAlphaChannel) {
//            final int pixelLength = 4;
//            for (int pixel = 0, row = 0, col = 0; pixel + 3 < pixels.length; pixel += pixelLength) {
//                int argb = 0;
//                argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
//                argb += ((int) pixels[pixel + 1] & 0xff); // blue
//                argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
//                argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
//                result[row][col] = argb;
//                col++;
//                if (col == width) {
//                    col = 0;
//                    row++;
//                }
//            }
//        } else {
//            final int pixelLength = 3;
//            for (int pixel = 0, row = 0, col = 0; pixel + 2 < pixels.length; pixel += pixelLength) {
//                int argb = 0;
//                argb += -16777216; // 255 alpha
//                argb += ((int) pixels[pixel] & 0xff); // blue
//                argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
//                argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
//                result[row][col] = argb;
//                col++;
//                if (col == width) {
//                    col = 0;
//                    row++;
//                }
//            }
//        }
//
//        return result;
//    }
    public static ByteBuffer getInputFromColorImage(String imagePath){
//        an image classification model with an input shape of [1 224 224 1] floating-point values
        File imgFile = new  File(imagePath);
        if(!imgFile.exists()){
            return null;
        }

        //read image
        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        if(myBitmap == null){
            Log.i("Debug","image file failed to decoded"+imgFile.getAbsolutePath());
            return null;
        }
        Bitmap bitmap = Bitmap.createScaledBitmap(myBitmap, 112, 112, true);
        ByteBuffer input = ByteBuffer.allocateDirect(112*112*4).order(ByteOrder.nativeOrder());
        for (int y = 0; y < 112; y++) {
            for (int x = 0; x < 112; x++) {
                int px = bitmap.getPixel(x, y);
//                int px = myBitmap.getPixel(x, y);

                // Get channel values from the pixel value.
                int r = Color.red(px);
                int g = Color.green(px);
                int b = Color.blue(px);

                // normalized to the range [0.0, 1.0] instead.
                float rf = (r ) / 255.0f;
                float gf = (g ) / 255.0f;
                float bf = (b ) / 255.0f;

//                input.putFloat(rf);
//                input.putFloat(gf);
//                input.putFloat(bf);
                input.putFloat((rf+gf+bf)/3);
            }

        }
        return input;
    }
    public static ByteBuffer getInputFromStream(RNFrame imageFrame){
//        an image classification model with an input shape of [1 224 224 1] floating-point values
        Frame mframe = imageFrame.getFrame();
        if(mframe == null){
            return null;
        }
        //read image
        Bitmap myBitmap = mframe.getBitmap();
        if(myBitmap == null){
            Log.i("Debug","failed to get bitmap from frame");
            return null;
        }
        Bitmap bitmap = Bitmap.createScaledBitmap(myBitmap, 112, 112, true);
        ByteBuffer input = ByteBuffer.allocateDirect(112*112*4).order(ByteOrder.nativeOrder());
        for (int y = 0; y < 112; y++) {
            for (int x = 0; x < 112; x++) {
                int px = bitmap.getPixel(x, y);
//                int px = myBitmap.getPixel(x, y);

                // Get channel values from the pixel value.
                int r = Color.red(px);
                int g = Color.green(px);
                int b = Color.blue(px);

                // normalized to the range [0.0, 1.0] instead.
                float rf = (r ) / 255.0f;
                float gf = (g ) / 255.0f;
                float bf = (b ) / 255.0f;

//                input.putFloat(rf);
//                input.putFloat(gf);
//                input.putFloat(bf);
                input.putFloat((rf+gf+bf)/3);
            }

        }
        return input;
    }

    public static ByteBuffer getInputFromBitmap(Bitmap imageBitmap){

        if(imageBitmap == null){
            Log.i("Debug","imageBitmap is null");
            return null;
        }
        Bitmap bitmap = Bitmap.createScaledBitmap(imageBitmap, 112, 112, true);
        ByteBuffer input = ByteBuffer.allocateDirect(112*112*4).order(ByteOrder.nativeOrder());
        for (int y = 0; y < 112; y++) {
            for (int x = 0; x < 112; x++) {
                int px = bitmap.getPixel(x, y);
//                int px = myBitmap.getPixel(x, y);

                // Get channel values from the pixel value.
                int r = Color.red(px);
                int g = Color.green(px);
                int b = Color.blue(px);

                // normalized to the range [0.0, 1.0] instead.
                float rf = (r ) / 255.0f;
                float gf = (g ) / 255.0f;
                float bf = (b ) / 255.0f;

//                input.putFloat(rf);
//                input.putFloat(gf);
//                input.putFloat(bf);
                input.putFloat((rf+gf+bf)/3);
            }

        }
        return input;
    }
    //    public static ByteBuffer getInputFromImage(String imagePath){
////        an image classification model with an input shape of [1 224 224 3] floating-point values
//        File imgFile = new  File(imagePath);
//        if(!imgFile.exists()){
//            return null;
//        }
//        //read image
//        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
//        Bitmap bitmap = Bitmap.createScaledBitmap(myBitmap, 112, 112, true);
//        ByteBuffer input = ByteBuffer.allocateDirect(112*112*3/4).order(ByteOrder.nativeOrder());
//        for (int y = 0; y < 112; y++) {
//            for (int x = 0; x < 112; x++) {
//                int px = bitmap.getPixel(x, y);
//
//                // Get channel values from the pixel value.
//                int r = Color.red(px);
//                int g = Color.green(px);
//                int b = Color.blue(px);
//
//                // Normalize channel values to [-1.0, 1.0]. This requirement depends
//                // on the model. For example, some models might require values to be
//                // normalized to the range [0.0, 1.0] instead.
//                float rf = (r - 127) / 255.0f;
//                float gf = (g - 127) / 255.0f;
//                float bf = (b - 127) / 255.0f;
//
//                input.putFloat(rf);
//                input.putFloat(gf);
//                input.putFloat(bf);
//            }
//
//        }
//        return input;
//    }
    public static Bitmap toGrayScale(Bitmap image){
        int width, height;
        height = image.getHeight();
        width = image.getWidth();
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(image, 0, 0, paint);
        return bmpGrayscale;
    }
    public static Bitmap rescaleImage(Bitmap image,int width,int height){
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
    public static void rescaleSavedImage(String imagePath,int width){
        Bitmap imageBitmap = loadImageBitmap(imagePath);
        imageBitmap = resizeBitmap(imageBitmap,width);
        saveBitmapToFile(imageBitmap,imagePath);

    }

    public static Bitmap loadImageBitmap(String imagePath){
        File imgFile = new  File(imagePath);
        if(!imgFile.exists()){
            return null;
        }

        //read image
        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        if(myBitmap == null){
            Log.i("Debug","image file failed to decoded"+imgFile.getAbsolutePath());
            return null;
        }
        Log.i("Debug","ImageUtils imagebitmap w.h = "+myBitmap.getWidth() + " x "+myBitmap.getHeight());
        return myBitmap;
    }

    public static Bitmap cutFace(Bitmap image,int faceX,int faceY, int faceWidth, int faceHeight){
        return Bitmap.createBitmap(image,faceX,faceY,faceWidth,faceHeight);
    }

    public static Bitmap cutImage(Bitmap image, int x, int y, int faceWidth, int faceHeight){

        int width = image.getWidth();
        int height = image.getHeight();
        int newWidth = 200;
        int newHeight = 200;

// calculate the scale - in this case = 0.4f
        float scaleWidth = ((float) faceWidth) / width;
        float scaleHeight = ((float) faceHeight) / height;

// createa matrix for the manipulation
        Matrix matrix = new Matrix();

// resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);

// recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(image, x, y,
                width, height, matrix, true);

// make a Drawable from Bitmap to allow to set the BitMap
// to the ImageView, ImageButton or what ever
//        BitmapDrawable bmd = new BitmapDrawable(resizedBitmap);
//
//        ImageView imageView = new ImageView(this);

// set the Drawable on the ImageView
//        imageView.setImageDrawable(bmd);
        return resizedBitmap;
    }

    public static Bitmap rotateBitmap(Bitmap source, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }
    private static int getImageDegreeOfRotation(int orientation) {
        int rotationDegrees = 0;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotationDegrees = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotationDegrees = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotationDegrees = 270;
                break;
        }
        return rotationDegrees;
    }

    public static Bitmap resizeBitmap(Bitmap bm, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleRatio = (float) newWidth / (float) width;

        return Bitmap.createScaledBitmap(bm, newWidth, (int) (height * scaleRatio), true);
    }

    public static Bitmap flipHorizontally(Bitmap source) {
        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    // Get rotation degrees from Exif orientation enum

    public static int getImageRotation(int orientation) {
        int rotationDegrees = 0;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotationDegrees = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotationDegrees = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotationDegrees = 270;
                break;
        }
        return rotationDegrees;
    }
    public static void saveBitmapToFile(Bitmap imageBitmap, String path){
        File file = new File(path);
        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(file,false);
            boolean a = imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            imageBitmap.recycle();
            Log.i("Debug","FileFaceDetectionAsyncTask saveFaceImage success"+a);
        } catch (Exception e) {
            Log.i("Debug",e.getMessage());
        }
    }

}
