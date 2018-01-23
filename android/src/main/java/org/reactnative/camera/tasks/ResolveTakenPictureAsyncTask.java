package org.reactnative.camera.tasks;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.media.ExifInterface;

import org.reactnative.MutableImage;
import org.reactnative.camera.RNCameraViewHelper;
import org.reactnative.camera.utils.RNFileUtils;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ResolveTakenPictureAsyncTask extends AsyncTask<Void, Void, WritableMap> {
  private static final String ERROR_TAG = "E_TAKING_PICTURE_FAILED";
  private Promise mPromise;
  private byte[] mImageData;
  private ReadableMap mOptions;
  private File mCacheDirectory;

  public ResolveTakenPictureAsyncTask(byte[] imageData, Promise promise, ReadableMap options) {
    mPromise = promise;
    mOptions = options;
    mImageData = imageData;
  }

    public ResolveTakenPictureAsyncTask(byte[] imageData, Promise promise, ReadableMap options, File cacheDirectory) {
        mPromise = promise;
        mOptions = options;
        mImageData = imageData;
        mCacheDirectory = cacheDirectory;
    }

  private int getQuality() {
    return (int) (mOptions.getDouble("quality") * 100);
  }

  @Override
  protected WritableMap doInBackground(Void... voids) {
    WritableMap response = Arguments.createMap();
    ByteArrayInputStream inputStream = new ByteArrayInputStream(mImageData);

    try {
      MutableImage mutableImage = new MutableImage(mImageData);
      mutableImage.mirrorImage();
      mutableImage.fixOrientation();
      String encoded = mutableImage.toBase64(getQuality());

      response.putString("base64", encoded);
      response.putInt("width", mutableImage.getImageWidth());
      response.putInt("height", mutableImage.getImageHeight());
      if (mOptions.hasKey("exif") && mOptions.getBoolean("exif")) {
        ExifInterface exifInterface = new ExifInterface(inputStream);
        WritableMap exifData = RNCameraViewHelper.getExifData(exifInterface);
        response.putMap("exif", exifData);
      }

      ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
      mutableImage.getBitmap().compress(Bitmap.CompressFormat.JPEG, getQuality(), imageStream);

      // Write compressed image to file in cache directory
      String filePath = writeStreamToFile(imageStream);
      File imageFile = new File(filePath);
      String fileUri = Uri.fromFile(imageFile).toString();
      response.putString("uri", fileUri);

      return response;
    } catch (Resources.NotFoundException e) {
      mPromise.reject(ERROR_TAG, "Documents directory of the app could not be found.", e);
      e.printStackTrace();
   } catch (MutableImage.ImageMutationFailedException e) {
      e.printStackTrace();
    } catch (IOException e) {
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
      mPromise.resolve(response);
    }
  }

}
