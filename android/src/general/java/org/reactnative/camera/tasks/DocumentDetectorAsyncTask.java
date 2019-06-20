package org.reactnative.camera.tasks;

import android.os.AsyncTask;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

import org.reactnative.documentdetector.Document;
import org.reactnative.documentdetector.RNDocumentDetector;

public class DocumentDetectorAsyncTask extends AsyncTask<Void, Void, Document> {

    private DocumentDetectorAsyncTaskDelegate mDelegate;
    private RNDocumentDetector mDocumentDetector;
    private byte[] mImageData;
    private int mWidth;
    private int mHeight;
    private double mScaleX;
    private double mScaleY;

    public DocumentDetectorAsyncTask(DocumentDetectorAsyncTaskDelegate mDelegate, RNDocumentDetector mDocumentDetector, byte[] data, int width, int height, float density, int viewWidth, int viewHeight) {
        this.mDelegate = mDelegate;
        this.mDocumentDetector = mDocumentDetector;
        this.mImageData = data;
        this.mWidth = width;
        this.mHeight = height;
        this.mScaleX = (double) viewWidth / (width * density);
        this.mScaleY = (double) viewHeight / (height * density);
    }

    @Override
    protected Document doInBackground(Void... voids) {
        if (isCancelled() || mDelegate == null || mDocumentDetector == null) {
            return null;
        }

        return mDocumentDetector.detect(mImageData, mWidth, mHeight, mScaleX, mScaleY);
    }

    @Override
    protected void onPostExecute(Document document) {
        super.onPostExecute(document);
        if (document != null) {
            mDelegate.onDocumentDetected(serializeEventData(document));
        }
        mDelegate.onDocumentDetectingTaskCompleted();
    }

    private WritableMap serializeEventData(Document document) {
        WritableMap serializedDocument = Arguments.createMap();

        serializedDocument.putDouble("x", document.getTopLeft().x);
        serializedDocument.putDouble("y", document.getTopLeft().y);
        serializedDocument.putDouble("width", document.getWidth());
        serializedDocument.putDouble("height", document.getHeight());

        return serializedDocument;
    }
}
