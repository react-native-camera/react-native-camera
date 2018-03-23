package org.reactnative.camera.tasks;

import android.util.SparseArray;

import com.google.android.gms.vision.text.TextBlock;

public interface TextRecognizerAsyncTaskDelegate {
  void onTextRecognized(SparseArray<TextBlock> textBlocks, int sourceWidth, int sourceHeight, int sourceRotation);
  void onTextRecognizerTaskCompleted();
}
