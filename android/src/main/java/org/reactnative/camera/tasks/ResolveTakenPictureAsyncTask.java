package org.reactnative.camera.tasks;

import android.content.res.Resources;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.support.media.ExifInterface;

import org.reactnative.MutableImage;
import org.reactnative.camera.RNCameraViewHelper;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ResolveTakenPictureAsyncTask extends AsyncTask<Void, Void, WritableMap> {
  private static final String ERROR_TAG = "E_TAKING_PICTURE_FAILED";
  private Promise mPromise;
  private byte[] mImageData;
  private ReadableMap mOptions;

  public ResolveTakenPictureAsyncTask(byte[] imageData, Promise promise, ReadableMap options) {
    mPromise = promise;
    mOptions = options;
    mImageData = imageData;
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

      //TODO: create local cache directory, save image to file and insert into response "uri" key
      // with the path to the file
      //response.putString("uri", outputPath);

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

  @Override
  protected void onPostExecute(WritableMap response) {
    super.onPostExecute(response);

    // If the response is not null everything went well and we can resolve the promise.
    if (response != null) {
      mPromise.resolve(response);
    }
  }

}
