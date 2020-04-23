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
        this.mScaleX = (double) viewHeight / (width * density);
        this.mScaleY = (double) viewWidth / (height * density);
    }

    @Override
    protected Document doInBackground(Void... voids) {
        if (isCancelled() || mDelegate == null || mDocumentDetector == null) {
            return null;
        }

        return mDocumentDetector.detectPreview(mImageData, mWidth, mHeight, mScaleX, mScaleY);
    }

    @Override
    protected void onPostExecute(Document document) {
        super.onPostExecute(document);
        mDelegate.onDocumentDetected(document != null ? serializeEventData(document) : null);
        mDelegate.onDocumentDetectingTaskCompleted();
    }

    private WritableMap serializeEventData(Document document) {
        WritableMap serializedDocument = Arguments.createMap();

        WritableMap tl = Arguments.createMap();
        tl.putDouble("x", document.getTopLeft().x);
        tl.putDouble("y", document.getTopLeft().y);
        serializedDocument.putMap("tl", tl);

        WritableMap tr = Arguments.createMap();
        tr.putDouble("x", document.getTopRight().x);
        tr.putDouble("y", document.getTopRight().y);
        serializedDocument.putMap("tr", tr);

        WritableMap br = Arguments.createMap();
        br.putDouble("x", document.getBottomRight().x);
        br.putDouble("y", document.getBottomRight().y);
        serializedDocument.putMap("br", br);

        WritableMap bl = Arguments.createMap();
        bl.putDouble("x", document.getBottomLeft().x);
        bl.putDouble("y", document.getBottomLeft().y);
        serializedDocument.putMap("bl", bl);

        return serializedDocument;
    }
}
