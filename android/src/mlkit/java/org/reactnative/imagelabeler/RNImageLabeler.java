package org.reactnative.imagelabeler;

import android.content.Context;
import android.util.Log;

import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;


public class RNImageLabeler {

  private ImageLabeler mImageLabeler = null;
  private ImageLabelerOptions.Builder mBuilder;

  public RNImageLabeler(Context context) {
    mBuilder = new ImageLabelerOptions.Builder();
  }

  public boolean isOperational() {
    // Legacy api from GMV
    return true;
  }

  public ImageLabeler getDetector() {

    if (mImageLabeler == null) {
      createImageLabeler();
    }
    return mImageLabeler;
  }

  public void release() {
    if (mImageLabeler != null) {
      try {
        mImageLabeler.close();
      } catch (Exception e) {
        Log.e("RNCamera", "Attempt to close ImageLabeler failed");
      }
      mImageLabeler = null;
    }
  }

  private void createImageLabeler() {
    ImageLabelerOptions options = mBuilder.build();
    mImageLabeler = ImageLabeling.getClient(options);
  }
}
