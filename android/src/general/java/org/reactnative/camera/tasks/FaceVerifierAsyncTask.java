package org.reactnative.camera.tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.util.Log;

import org.reactnative.camera.ImageUtils;
import org.reactnative.camera.utils.ImageDimensions;
import org.reactnative.frame.RNFrame;
import org.reactnative.frame.RNFrameFactory;
import org.tensorflow.lite.Interpreter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

public class FaceVerifierAsyncTask extends android.os.AsyncTask<Void, Void, Float> {
    private ByteBuffer userImageBuffer;
    //    private ByteBuffer user0ImageBuffer;
    private byte[] mImageData;
    private int mWidth;
    private int mHeight;
    private int mRotation;
    private Interpreter mFaceVerifier;
    private FaceVerifierAsyncTaskDelegate mDelegate;
    private ImageDimensions mImageDimensions;
    private double mScaleX;
    private double mScaleY;
    private int mPaddingLeft;
    private int mPaddingTop;

    public FaceVerifierAsyncTask(
            FaceVerifierAsyncTaskDelegate delegate,
            Interpreter faceVerifier,
            String userImagePath,
//            String user0ImagePath,
            byte[] imageData,
            int width,
            int height,
            int rotation,
            float density,
            int facing,
            int viewWidth,
            int viewHeight,
            int viewPaddingLeft,
            int viewPaddingTop
    ) {
//        Log.i("Debug",String.format("RNCameraView nothing"));
        Log.i("Debug","FaceVerifiertask init");
        userImageBuffer= ImageUtils.getInputFromColorImage(userImagePath);
//        user0ImageBuffer= ImageUtils.getInputFromColorImage(user0ImagePath);

        mImageData = imageData;
        mWidth = width;
        mHeight = height;
        mRotation = rotation;
        mDelegate = delegate;
        mFaceVerifier = faceVerifier;
        mImageDimensions = new ImageDimensions(width, height, rotation, facing);
        mScaleX = (double) (viewWidth) / (mImageDimensions.getWidth() * density);
        mScaleY = (double) (viewHeight) / (mImageDimensions.getHeight() * density);
        mPaddingLeft = viewPaddingLeft;
        mPaddingTop = viewPaddingTop;
    }
    /**
     * Downloading file in background thread
     * */
    @Override
    protected Float doInBackground(Void... voids) {
        if (isCancelled() || mDelegate == null || mFaceVerifier == null ) {
            return null;
        }
        Log.i("Debug","mImageData length="+mImageData.length);
        int bitperpixel = ImageFormat.getBitsPerPixel(ImageFormat.NV21);
        // =============<<<<<<<<<<<<<<<<< check here
        int bufferSize =  java.lang.Float.SIZE / java.lang.Byte.SIZE;
        Bitmap b = BitmapFactory.decodeByteArray(mImageData, 0, mImageData.length);
        ByteBuffer inputFromFrameBitmap= ImageUtils.getInputFromBitmap(b);;
        if(inputFromFrameBitmap == null) {
            Log.i("Debug", "FaceVerifierAsyncTask inputFromFrameBitmap from bitmap is " + inputFromFrameBitmap);
//            input0 = user0ImageBuffer;
            return (float) 400;
        }
        Object[] inputs = {inputFromFrameBitmap,userImageBuffer};
        Map<Integer, Object> outputs = new HashMap();
        ByteBuffer out = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
        outputs.put(0, out);

        mFaceVerifier.runForMultipleInputsOutputs(inputs,outputs);
        ByteBuffer result2 = (ByteBuffer) outputs.get(0);
        Log.i("Debug",String.format("verifytask  result = %.4f",result2.getFloat(0)));
//        float result = 0;

        return result2.getFloat(0);
    }

    /**
     * Before starting background thread Show Progress Bar Dialog
     * */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
//        Log.i("Debug", "DownloadFileFromURL start ");
//            showDialog(progress_bar_type);
    }




    /**
     * Updating progress bar
     * */
    protected void onProgressUpdate(String... progress) {
        Log.i("Debug", "DownloadFileFromURL progress: "+progress[0]);
        // setting progress percentage
//            pDialog.setProgress(Integer.parseInt(progress[0]));
    }

    /**
     * After completing background task Dismiss the progress dialog
     * **/
    @Override
    protected void onPostExecute(Float result) {
        super.onPostExecute(result);

        if (result == null) {
            mDelegate.onFaceVerificationError();
        } else {
            Log.i("Debug","faceverifierasynctask onpostexecute result = "+String.valueOf(result));
//            if (result > 0) {
            //      todo: add face verification task here

            mDelegate.onFaceVerified(result);
//            }
            mDelegate.onFaceVerificationTaskCompleted();
        }

    }
}