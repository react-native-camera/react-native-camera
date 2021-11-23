package org.reactnative.frame;

import org.reactnative.camera.utils.ImageDimensions;
import com.google.mlkit.vision.common.InputImage;

/**
 * Wrapper around Frame allowing us to track Frame dimensions.
 * Tracking dimensions is used in RNFaceDetector and RNBarcodeDetector to provide painless FaceDetector/BarcodeDetector recreation
 * when image dimensions change.
 */

public class RNFrame {
  private InputImage mFrame;
  private ImageDimensions mDimensions;

  public RNFrame(InputImage frame, ImageDimensions dimensions) {
    mFrame = frame;
    mDimensions = dimensions;
  }

  public InputImage getFrame() {
    return mFrame;
  }

  public ImageDimensions getDimensions() {
    return mDimensions;
  }
}
