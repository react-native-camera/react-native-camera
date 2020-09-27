package org.reactnative.camera.tasks;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import androidx.exifinterface.media.ExifInterface;
import android.util.Base64;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.reactnative.camera.RNCameraViewHelper;
import org.reactnative.camera.utils.RNFileUtils;
import org.reactnative.documentdetector.Document;
import org.reactnative.documentdetector.RNDocumentDetector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Bitmap.createBitmap;
import static java.lang.Math.hypot;
import static java.lang.Math.max;
import static org.opencv.android.Utils.matToBitmap;
import static org.opencv.core.CvType.CV_32FC2;
import static org.opencv.core.CvType.CV_8UC4;
import static org.opencv.imgproc.Imgproc.getPerspectiveTransform;
import static org.opencv.imgproc.Imgproc.warpPerspective;

public class ResolveTakenPictureAsyncTask extends AsyncTask<Void, Void, WritableMap> {
    private static final String ERROR_TAG = "E_TAKING_PICTURE_FAILED";
    private final RNDocumentDetector mDocumentDetector;
    private Promise mPromise;
    private Bitmap mBitmap;
    private byte[] mImageData;
    private ReadableMap mOptions;
    private File mCacheDirectory;
    private int mDeviceOrientation;
    private PictureSavedDelegate mPictureSavedDelegate;

    public ResolveTakenPictureAsyncTask(byte[] imageData, Promise promise, ReadableMap options, File cacheDirectory, int deviceOrientation, PictureSavedDelegate delegate, RNDocumentDetector documentDetector) {
        mPromise = promise;
        mOptions = options;
        mImageData = imageData;
        mCacheDirectory = cacheDirectory;
        mDeviceOrientation = deviceOrientation;
        mPictureSavedDelegate = delegate;
        mDocumentDetector = documentDetector;
    }

    private int getQuality() {
        return (int) (mOptions.getDouble("quality") * 100);
    }

    // loads bitmap only if necessary
    private void loadBitmap() throws IOException {
        if(mBitmap == null){
            mBitmap = BitmapFactory.decodeByteArray(mImageData, 0, mImageData.length);
        }
        if(mBitmap == null){
            throw new IOException("Failed to decode Image Bitmap");
        }
    }

    @Override
    protected WritableMap doInBackground(Void... voids) {
        WritableMap response = Arguments.createMap();
        ByteArrayInputStream inputStream = null;
        ExifInterface exifInterface = null;
        WritableMap exifData = null;
        ReadableMap exifExtraData = null;

        boolean orientationChanged = false;

        response.putInt("deviceOrientation", mDeviceOrientation);
        response.putInt("pictureOrientation", mOptions.hasKey("orientation") ? mOptions.getInt("orientation") : mDeviceOrientation);


        try{
            // this replaces the skipProcessing flag, we will process only if needed, and in
            // an orderly manner, so that skipProcessing is the default behaviour if no options are given
            // and this behaves more like the iOS version.
            // We will load all data lazily only when needed.

            // this should not incurr in any overhead if not read/used
            inputStream = new ByteArrayInputStream(mImageData);


            // If we use document detection feature the detector was passed to this constructor
            if (mDocumentDetector != null) {
                loadBitmap();
                Document document = mDocumentDetector.detectCaptured(mImageData, mBitmap.getWidth(), mBitmap.getHeight());
                if (document != null) {
                    mBitmap = cropBitmapWithPerspectiveCorrection(mBitmap, document);
                    // replace the byte array with the data from the cropped bitmap
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    mBitmap.compress(Bitmap.CompressFormat.JPEG, getQuality(), outputStream);
                    mImageData = outputStream.toByteArray();
                }
            }

            WritableMap fileExifData = null;
            // Rotate the bitmap to the proper orientation if requested
            if(mOptions.hasKey("fixOrientation") && mOptions.getBoolean("fixOrientation")){

                exifInterface = new ExifInterface(inputStream);

                // Get orientation of the image from mImageData via inputStream
                int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                if(orientation != ExifInterface.ORIENTATION_UNDEFINED){
                    loadBitmap();
                    mBitmap = rotateBitmap(mBitmap, getImageRotation(orientation));
                    orientationChanged = true;
                }
            }

            if (mOptions.hasKey("width")) {
                loadBitmap();
                mBitmap = resizeBitmap(mBitmap, mOptions.getInt("width"));
            }

            if (mOptions.hasKey("mirrorImage") && mOptions.getBoolean("mirrorImage")) {
                loadBitmap();
                mBitmap = flipHorizontally(mBitmap);
            }


            // EXIF code - we will adjust exif info later if we manipulated the bitmap
            boolean writeExifToResponse = mOptions.hasKey("exif") && mOptions.getBoolean("exif");

            // default to true if not provided so it is consistent with iOS and with what happens if no
            // processing is done and the image is saved as is.
            boolean writeExifToFile = true;

            if (mOptions.hasKey("writeExif")) {
                switch (mOptions.getType("writeExif")) {
                    case Boolean:
                        writeExifToFile = mOptions.getBoolean("writeExif");
                        break;
                    case Map:
                        exifExtraData = mOptions.getMap("writeExif");
                        writeExifToFile = true;
                        break;
                }
            }

            // Read Exif data if needed
            if (writeExifToResponse || writeExifToFile) {

                // if we manipulated the image, or need to add extra data, or need to add it to the response,
                // then we need to load the actual exif data.
                // Otherwise we can just use w/e exif data we have right now in our byte array
                if(mBitmap != null || exifExtraData != null || writeExifToResponse){
                    if(exifInterface == null){
                        exifInterface = new ExifInterface(inputStream);
                    }
                    exifData = RNCameraViewHelper.getExifData(exifInterface);

                    if(exifExtraData != null){
                        exifData.merge(exifExtraData);
                    }
                }

                // if we did anything to the bitmap, adjust exif
                if(mBitmap != null){
                    exifData.putInt("width", mBitmap.getWidth());
                    exifData.putInt("height", mBitmap.getHeight());

                    if(orientationChanged){
                        exifData.putInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    }
                }

                // Write Exif data to the response if requested
                if (writeExifToResponse) {
                    response.putMap("exif", exifData);
                }
            }



            // final processing
            // Based on whether or not we loaded the full bitmap into memory, final processing differs
            if(mBitmap == null){

                // set response dimensions. If we haven't read our bitmap, get it efficiently
                // without loading the actual bitmap into memory
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(mImageData, 0, mImageData.length, options);
                if(options != null){
                    response.putInt("width", options.outWidth);
                    response.putInt("height", options.outHeight);
                }


                // save to file if requested
                if (!mOptions.hasKey("doNotSave") || !mOptions.getBoolean("doNotSave")) {

                    // Prepare file output
                    File imageFile = new File(getImagePath());

                    imageFile.createNewFile();

                    FileOutputStream fOut = new FileOutputStream(imageFile);

                    // Save byte array (it is already a JPEG)
                    fOut.write(mImageData);
                    fOut.flush();
                    fOut.close();

                    // update exif data if needed.
                    // Since we didn't modify the image, we only update if we have extra exif info
                    if (writeExifToFile && exifExtraData != null) {
                        ExifInterface fileExifInterface = new ExifInterface(imageFile.getAbsolutePath());
                        RNCameraViewHelper.setExifData(fileExifInterface, exifExtraData);
                        fileExifInterface.saveAttributes();
                    }
                    else if (!writeExifToFile){
                        // if we were requested to NOT store exif, we actually need to
                        // clear the exif tags
                        ExifInterface fileExifInterface = new ExifInterface(imageFile.getAbsolutePath());
                        RNCameraViewHelper.clearExifData(fileExifInterface);
                        fileExifInterface.saveAttributes();
                    }
                    // else: exif is unmodified, no need to update anything

                    // Return file system URI
                    String fileUri = Uri.fromFile(imageFile).toString();
                    response.putString("uri", fileUri);
                }

                if (mOptions.hasKey("base64") && mOptions.getBoolean("base64")) {
                    response.putString("base64", Base64.encodeToString(mImageData, Base64.NO_WRAP));
                }

            }
            else{

                // get response dimensions right from the bitmap if we have it
                response.putInt("width", mBitmap.getWidth());
                response.putInt("height", mBitmap.getHeight());

                // Cache compressed image in imageStream
                ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
                mBitmap.compress(Bitmap.CompressFormat.JPEG, getQuality(), imageStream);


                // Write compressed image to file in cache directory unless otherwise specified
                if (!mOptions.hasKey("doNotSave") || !mOptions.getBoolean("doNotSave")) {
                    String filePath = writeStreamToFile(imageStream);

                    // since we lost any exif data on bitmap creation, we only need
                    // to add it if requested
                    if (writeExifToFile && exifData != null) {
                        ExifInterface fileExifInterface = new ExifInterface(filePath);
                        RNCameraViewHelper.setExifData(fileExifInterface, exifData);
                        fileExifInterface.saveAttributes();
                    }
                    File imageFile = new File(filePath);
                    String fileUri = Uri.fromFile(imageFile).toString();
                    response.putString("uri", fileUri);
                }

                // Write base64-encoded image to the response if requested
                if (mOptions.hasKey("base64") && mOptions.getBoolean("base64")) {
                    response.putString("base64", Base64.encodeToString(imageStream.toByteArray(), Base64.NO_WRAP));
                }

            }

            return response;

        }
        catch (Resources.NotFoundException e) {
            mPromise.reject(ERROR_TAG, "Documents directory of the app could not be found.", e);
            e.printStackTrace();
        }
        catch (IOException e) {
            mPromise.reject(ERROR_TAG, "An unknown I/O exception has occurred.", e);
            e.printStackTrace();
        }
        finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private Bitmap cropBitmapWithPerspectiveCorrection(Bitmap source, Document document) throws IllegalArgumentException {
        if (document == null) return source;

        Mat src = new Mat();
        Utils.bitmapToMat(source, src);
        Point tl = document.getTopLeft();
        Point tr = document.getTopRight();
        Point bl = document.getBottomLeft();
        Point br = document.getBottomRight();

        boolean ratioAlreadyApplied = tr.x * (src.size().width / 500) < src.size().width;
        double ratio = ratioAlreadyApplied ? src.size().width / 500 : 1;

        double widthA = hypot(br.x - bl.x, br.y - bl.y);
        double widthB = hypot(tr.x - tl.x, tr.y - tl.y);

        double dw = max(widthA, widthB) * ratio;
        int maxWidth = Double.valueOf(dw).intValue();

        double heightA = hypot(tr.x - br.x, tr.y - br.y);
        double heightB = hypot(tl.x - bl.x, tl.y - bl.y);

        double dh = max(heightA, heightB) * ratio;
        int maxHeight = Double.valueOf(dh).intValue();

        Mat doc = new Mat(maxHeight, maxWidth, CV_8UC4);

        Mat src_mat = new Mat(4, 1, CV_32FC2);
        Mat dst_mat = new Mat(4, 1, CV_32FC2);

        src_mat.put(0, 0, tl.x * ratio, tl.y * ratio, tr.x * ratio, tr.y * ratio, br.x * ratio, br.y * ratio, bl.x * ratio,
                bl.y * ratio);
        dst_mat.put(0, 0, 0.0, 0.0, dw, 0.0, dw, dh, 0.0, dh);

        Mat m = getPerspectiveTransform(src_mat, dst_mat);

        warpPerspective(src, doc, m, doc.size());

        Bitmap bitmap = createBitmap(doc.cols(), doc.rows(), ARGB_8888);
        matToBitmap(doc, bitmap);

        m.release();
        return bitmap;
    }

    private Bitmap rotateBitmap(Bitmap source, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private Bitmap resizeBitmap(Bitmap bm, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleRatio = (float) newWidth / (float) width;

        return Bitmap.createScaledBitmap(bm, newWidth, (int) (height * scaleRatio), true);
    }

    private Bitmap flipHorizontally(Bitmap source) {
        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);
        return createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    // Get rotation degrees from Exif orientation enum

    private int getImageRotation(int orientation) {
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

    private String getImagePath() throws IOException{
        if(mOptions.hasKey("path")){
            return mOptions.getString("path");
        }
        return RNFileUtils.getOutputFilePath(mCacheDirectory, ".jpg");
    }

    private String writeStreamToFile(ByteArrayOutputStream inputStream) throws IOException {
        String outputPath = null;
        IOException exception = null;
        FileOutputStream outputStream = null;

        try {
            outputPath = getImagePath();
            outputStream = new FileOutputStream(outputPath);
            inputStream.writeTo(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
            exception = e;
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (exception != null) {
            throw exception;
        }

        return outputPath;
    }

    @Override
    protected void onPostExecute(WritableMap response) {
        super.onPostExecute(response);

        // If the response is not null everything went well and we can resolve the promise.
        if (response != null) {
            if (mOptions.hasKey("fastMode") && mOptions.getBoolean("fastMode")) {
                WritableMap wrapper = Arguments.createMap();
                wrapper.putInt("id", mOptions.getInt("id"));
                wrapper.putMap("data", response);
                mPictureSavedDelegate.onPictureSaved(wrapper);
            } else {
                mPromise.resolve(response);
            }
        }
    }

}
