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
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableMap;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.reactnative.camera.RNCameraViewHelper;
import org.reactnative.camera.utils.RNFileUtils;
import org.reactnative.documentdetector.Document;
import org.reactnative.documentdetector.RNDocumentDetector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ResolveTakenPictureAsyncTask extends AsyncTask<Void, Void, WritableMap> {
    private static final String ERROR_TAG = "E_TAKING_PICTURE_FAILED";
    private final RNDocumentDetector mDocumentDetector;
    private Promise mPromise;
    private byte[] mImageData;
    private ReadableMap mOptions;
    private File mCacheDirectory;
    private Bitmap mBitmap;
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

    @Override
    protected WritableMap doInBackground(Void... voids) {
        WritableMap response = Arguments.createMap();
        ByteArrayInputStream inputStream = null;

        response.putInt("deviceOrientation", mDeviceOrientation);
        response.putInt("pictureOrientation", mOptions.hasKey("orientation") ? mOptions.getInt("orientation") : mDeviceOrientation);

        if (mOptions.hasKey("skipProcessing")) {
            try {
                // Prepare file output
                File imageFile = new File(RNFileUtils.getOutputFilePath(mCacheDirectory, ".jpg"));
                imageFile.createNewFile();
                FileOutputStream fOut = new FileOutputStream(imageFile);

                // Save byte array (it is already a JPEG)
                fOut.write(mImageData);

                // get image size
                if (mBitmap == null) {
                    mBitmap = BitmapFactory.decodeByteArray(mImageData, 0, mImageData.length);
                }
                if(mBitmap == null){
                    throw new IOException("Failed to decode Image bitmap.");
                }

                response.putInt("width", mBitmap.getWidth());
                response.putInt("height", mBitmap.getHeight());

                // Return file system URI
                String fileUri = Uri.fromFile(imageFile).toString();
                response.putString("uri", fileUri);

            } catch (Resources.NotFoundException e) {
                response = null; // do not resolve
                mPromise.reject(ERROR_TAG, "Documents directory of the app could not be found.", e);
                e.printStackTrace();
            } catch (IOException e) {
                response = null; // do not resolve
                mPromise.reject(ERROR_TAG, "An unknown I/O exception has occurred.", e);
                e.printStackTrace();
            }

            return response;
        }

        // we need the stream only for photos from a device
        if (mBitmap == null) {
            mBitmap = BitmapFactory.decodeByteArray(mImageData, 0, mImageData.length);
            inputStream = new ByteArrayInputStream(mImageData);
        }

        // If we use document detection feature the detector was passed to this constructor
        if (mDocumentDetector != null) {
            Document document = mDocumentDetector.detectCaptured(mImageData, mBitmap.getWidth(), mBitmap.getHeight());
            if (document != null) {
                try {
                    mBitmap = cropBitmapWithPerspectiveCorrection(mBitmap, document);
                    // replace the byte array with the data from the cropped bitmap
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    mBitmap.compress(Bitmap.CompressFormat.JPEG, getQuality(), outputStream);
                    mImageData = outputStream.toByteArray();
                } catch (IllegalArgumentException e) {
                    //invalid document parameters
                }
            }
        }

        try {
            WritableMap fileExifData = null;

            if (inputStream != null) {
                ExifInterface exifInterface = new ExifInterface(inputStream);
                // Get orientation of the image from mImageData via inputStream
                int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);

                // Rotate the bitmap to the proper orientation if needed
                boolean fixOrientation = mOptions.hasKey("fixOrientation")
                        && mOptions.getBoolean("fixOrientation")
                        && orientation != ExifInterface.ORIENTATION_UNDEFINED;
                if (fixOrientation) {
                    mBitmap = rotateBitmap(mBitmap, getImageRotation(orientation));
                }

                if (mOptions.hasKey("width")) {
                    mBitmap = resizeBitmap(mBitmap, mOptions.getInt("width"));
                }

                if (mOptions.hasKey("mirrorImage") && mOptions.getBoolean("mirrorImage")) {
                    mBitmap = flipHorizontally(mBitmap);
                }

                WritableMap exifData = null;
                ReadableMap exifExtraData = null;
                boolean writeExifToResponse = mOptions.hasKey("exif") && mOptions.getBoolean("exif");
                boolean writeExifToFile = false;
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
                    exifData = RNCameraViewHelper.getExifData(exifInterface);
                }

                // Write Exif data to output file if requested
                if (writeExifToFile) {
                    fileExifData = Arguments.createMap();
                    fileExifData.merge(exifData);
                    fileExifData.putInt("width", mBitmap.getWidth());
                    fileExifData.putInt("height", mBitmap.getHeight());
                    if (fixOrientation) {
                        fileExifData.putInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    }
                    if (exifExtraData != null) {
                        fileExifData.merge(exifExtraData);
                    }
                }

                // Write Exif data to the response if requested
                if (writeExifToResponse) {
                    response.putMap("exif", exifData);
                }
            }

            // Upon rotating, write the image's dimensions to the response
            response.putInt("width", mBitmap.getWidth());
            response.putInt("height", mBitmap.getHeight());

            // Cache compressed image in imageStream
            ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
            mBitmap.compress(Bitmap.CompressFormat.JPEG, getQuality(), imageStream);

            // Write compressed image to file in cache directory unless otherwise specified
            if (!mOptions.hasKey("doNotSave") || !mOptions.getBoolean("doNotSave")) {
                String filePath = writeStreamToFile(imageStream);
                if (fileExifData != null) {
                    ExifInterface fileExifInterface = new ExifInterface(filePath);
                    RNCameraViewHelper.setExifData(fileExifInterface, fileExifData);
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

            // Cleanup
            imageStream.close();
            if (inputStream != null) {
                inputStream.close();
                inputStream = null;
            }

            return response;
        } catch (Resources.NotFoundException e) {
            mPromise.reject(ERROR_TAG, "Documents directory of the app could not be found.", e);
            e.printStackTrace();
        } catch (IOException e) {
            mPromise.reject(ERROR_TAG, "An unknown I/O exception has occurred.", e);
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // An exception had to occur, promise has already been rejected. Do not try to resolve it again.
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

        double widthA = Math.sqrt(Math.pow(br.x - bl.x, 2) + Math.pow(br.y - bl.y, 2));
        double widthB = Math.sqrt(Math.pow(tr.x - tl.x, 2) + Math.pow(tr.y - tl.y, 2));

        double dw = Math.max(widthA, widthB) * ratio;
        int maxWidth = Double.valueOf(dw).intValue();

        double heightA = Math.sqrt(Math.pow(tr.x - br.x, 2) + Math.pow(tr.y - br.y, 2));
        double heightB = Math.sqrt(Math.pow(tl.x - bl.x, 2) + Math.pow(tl.y - bl.y, 2));

        double dh = Math.max(heightA, heightB) * ratio;
        int maxHeight = Double.valueOf(dh).intValue();

        Mat doc = new Mat(maxHeight, maxWidth, CvType.CV_8UC4);

        Mat src_mat = new Mat(4, 1, CvType.CV_32FC2);
        Mat dst_mat = new Mat(4, 1, CvType.CV_32FC2);

        src_mat.put(0, 0, tl.x * ratio, tl.y * ratio, tr.x * ratio, tr.y * ratio, br.x * ratio, br.y * ratio, bl.x * ratio,
                bl.y * ratio);
        dst_mat.put(0, 0, 0.0, 0.0, dw, 0.0, dw, dh, 0.0, dh);

        Mat m = Imgproc.getPerspectiveTransform(src_mat, dst_mat);

        Imgproc.warpPerspective(src, doc, m, doc.size());

        Bitmap bitmap = Bitmap.createBitmap(doc.cols(), doc.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(doc, bitmap);

        m.release();
        return bitmap;
    }

    private Bitmap rotateBitmap(Bitmap source, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
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
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
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

    private String writeStreamToFile(ByteArrayOutputStream inputStream) throws IOException {
        String outputPath = null;
        IOException exception = null;
        FileOutputStream outputStream = null;

        try {
            outputPath = RNFileUtils.getOutputFilePath(mCacheDirectory, ".jpg");
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
