package org.reactnative.camera.tasks;

import android.util.SparseArray;

import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import org.reactnative.frame.RNFrame;
import org.reactnative.frame.RNFrameFactory;


public class TextRecognizerAsyncTask extends android.os.AsyncTask<Void, Void, SparseArray<TextBlock>> {

  private TextRecognizerAsyncTaskDelegate mDelegate;
  private TextRecognizer mTextRecognizer;
  private byte[] mImageData;
  private int mWidth;
  private int mHeight;
  private int mRotation;

  public TextRecognizerAsyncTask(
      TextRecognizerAsyncTaskDelegate delegate,
      TextRecognizer textRecognizer,
      byte[] imageData,
      int width,
      int height,
      int rotation
  ) {
    mDelegate = delegate;
    mTextRecognizer = textRecognizer;
    mImageData = imageData;
    mWidth = width;
    mHeight = height;
    mRotation = rotation;
  }

  @Override
  protected SparseArray<TextBlock> doInBackground(Void... ignored) {
    if (isCancelled() || mDelegate == null || mTextRecognizer == null || !mTextRecognizer.isOperational()) {
      return null;
    }

    RNFrame frame = RNFrameFactory.buildFrame(mImageData, mWidth, mHeight, mRotation);
    return mTextRecognizer.detect(frame.getFrame());
  }

  @Override
  protected void onPostExecute(SparseArray<TextBlock> textBlocks) {
    super.onPostExecute(textBlocks);

    if (textBlocks != null) {
      mDelegate.onTextRecognized(textBlocks, mWidth, mHeight, mRotation);
    }
    mDelegate.onTextRecognizerTaskCompleted();
  }
}
