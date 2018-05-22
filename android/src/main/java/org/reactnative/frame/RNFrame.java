package org.reactnative.frame;

import org.reactnative.camera.utils.ImageDimensions;
import com.google.android.gms.vision.Frame;

/**
 * Wrapper around Frame allowing us to track Frame dimensions.
 * Tracking dimensions is used in RNFaceDetector and RNBarcodeDetector to provide painless FaceDetector/BarcodeDetector recreation
 * when image dimensions change.
 */

public class RNFrame {
  private Frame mFrame;
  private ImageDimensions mDimensions;

  public RNFrame(Frame frame, ImageDimensions dimensions) {
    mFrame = frame;
    mDimensions = dimensions;
  }

  public Frame getFrame() {
    return mFrame;
  }

  public ImageDimensions getDimensions() {
    return mDimensions;
  }
}
