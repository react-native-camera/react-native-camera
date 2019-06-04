package org.reactnative.facedetector.tasks;

import android.content.Context;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.reactnative.facedetector.RNFaceDetector;
import org.reactnative.facedetector.FaceDetectorUtils;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FileFaceDetectionAsyncTask extends AsyncTask<Void, Void, Void> {
  private static final String ERROR_TAG = "E_FACE_DETECTION_FAILED";

  private static final String MODE_OPTION_KEY = "mode";
  private static final String DETECT_LANDMARKS_OPTION_KEY = "detectLandmarks";
  private static final String RUN_CLASSIFICATIONS_OPTION_KEY = "runClassifications";

  private String mUri;
  private String mPath;
  private Promise mPromise;
  private int mWidth = 0;
  private int mHeight = 0;
  private Context mContext;
  private ReadableMap mOptions;
  private int mOrientation = ExifInterface.ORIENTATION_UNDEFINED;
  private RNFaceDetector mRNFaceDetector;

  public FileFaceDetectionAsyncTask(Context context, ReadableMap options, Promise promise) {
    mUri = options.getString("uri");
    mPromise = promise;
    mOptions = options;
    mContext = context;
  }

  @Override
  protected void onPreExecute() {
    if (mUri == null) {
      mPromise.reject(ERROR_TAG, "You have to provide an URI of an image.");
      cancel(true);
      return;
    }

    Uri uri = Uri.parse(mUri);
    mPath = uri.getPath();

    if (mPath == null) {
      mPromise.reject(ERROR_TAG, "Invalid URI provided: `" + mUri + "`.");
      cancel(true);
      return;
    }

    // We have to check if the requested image is in a directory safely accessible by our app.
    boolean fileIsInSafeDirectories =
          mPath.startsWith(mContext.getCacheDir().getPath()) || mPath.startsWith(mContext.getFilesDir().getPath());

    if (!fileIsInSafeDirectories) {
      mPromise.reject(ERROR_TAG, "The image has to be in the local app's directories.");
      cancel(true);
      return;
    }

    if(!new File(mPath).exists()) {
      mPromise.reject(ERROR_TAG, "The file does not exist. Given path: `" + mPath + "`.");
      cancel(true);
    }
  }

  @Override
  protected Void doInBackground(Void... voids) {
    if (isCancelled()) {
      return null;
    }

    mRNFaceDetector = detectorForOptions(mOptions, mContext);

    try {
      ExifInterface exif = new ExifInterface(mPath);
      mOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
    } catch (IOException e) {
      Log.e(ERROR_TAG, "Reading orientation from file `" + mPath + "` failed.", e);
    }

    try {
      FirebaseVisionImage image = FirebaseVisionImage.fromFilePath(mContext, Uri.parse(mUri));
      FirebaseVisionFaceDetector detector = mRNFaceDetector.getDetector();
      detector.detectInImage(image)
              .addOnSuccessListener(
                      new OnSuccessListener<List<FirebaseVisionFace>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionFace> faces) {
                          serializeEventData(faces);
                        }
                      })
              .addOnFailureListener(
                      new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                          Log.e(ERROR_TAG, "Text recognition task failed", e);
                          mPromise.reject(ERROR_TAG, "Text recognition task failed", e);
                        }
                      });
    } catch (IOException e) {
      e.printStackTrace();
      Log.e(ERROR_TAG, "Creating Firebase Image from uri" + mUri + "failed", e);
      mPromise.reject(ERROR_TAG, "Creating Firebase Image from uri" + mUri + "failed", e);
    }
    return null;
  }

  private void serializeEventData(List<FirebaseVisionFace> faces) {
    WritableMap result = Arguments.createMap();
    WritableArray facesArray = Arguments.createArray();

    for(FirebaseVisionFace face : faces) {
      WritableMap encodedFace = FaceDetectorUtils.serializeFace(face);
      encodedFace.putDouble("yawAngle", (-encodedFace.getDouble("yawAngle") + 360) % 360);
      encodedFace.putDouble("rollAngle", (-encodedFace.getDouble("rollAngle") + 360) % 360);
      facesArray.pushMap(encodedFace);
    }

    result.putArray("faces", facesArray);

    WritableMap image = Arguments.createMap();
    image.putInt("width", mWidth);
    image.putInt("height", mHeight);
    image.putInt("orientation", mOrientation);
    image.putString("uri", mUri);
    result.putMap("image", image);

    mRNFaceDetector.release();
    mPromise.resolve(result);
  }

  private static RNFaceDetector detectorForOptions(ReadableMap options, Context context) {
    RNFaceDetector detector = new RNFaceDetector(context);
    detector.setTracking(false);

    if(options.hasKey(MODE_OPTION_KEY)) {
      detector.setMode(options.getInt(MODE_OPTION_KEY));
    }

    if(options.hasKey(RUN_CLASSIFICATIONS_OPTION_KEY)) {
      detector.setClassificationType(options.getInt(RUN_CLASSIFICATIONS_OPTION_KEY));
    }

    if(options.hasKey(DETECT_LANDMARKS_OPTION_KEY)) {
      detector.setLandmarkType(options.getInt(DETECT_LANDMARKS_OPTION_KEY));
    }

    return detector;
  }
}
