/* Copyright 2017 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package org.reactnative.camera;

import android.app.Activity;

import java.io.IOException;

/**
 * This classifier works with the Inception-v3 slim model.
 * It applies floating point inference rather than using a quantized model.
 */
public class PoseEstimatorTFLite extends PoseEstimator {

  private float[][][][] heatmap;

  /**
   * Initializes an {@code PoseEstimator}.
   *
   * @param activity
   */
  PoseEstimatorTFLite(Activity activity) throws IOException {
    super(activity);
    bodyPoints = new int[getBodyPointCount()][2];
    heatmap = new float[1][96][96][getBodyPointCount()];
  }

  @Override
  protected String getModelPath() {
    return "cpm-model-192.tflite";
  }

  @Override
  protected int getImageSizeX() {
    return 192;
  }

  @Override
  protected int getImageSizeY() {
    return 192;
  }

  @Override
  protected int getNumBytesPerChannel() {
    // a 32bit float value requires 4 bytes
    return 4;
  }

  @Override
  protected void addPixelValue(int pixelValue) {
    imgData.putFloat((pixelValue >> 16) & 0xFF);
    imgData.putFloat((pixelValue >> 8) & 0xFF);
    imgData.putFloat(pixelValue & 0xFF);
  }

  @Override
  protected void runInference()
  {
    tflite.run(imgData, heatmap);
    //bodyPoints = TODO: convert them..
  }

  @Override
  protected int getBodyPointCount() {
    return 14;
  }
}
