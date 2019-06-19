package org.reactnative.documentdetector;

import android.graphics.PointF;

public class RNDocumentDetector {
    public RNDocumentDetector() {}

    public Document detect(){
        // TODO ... do the magic!
        return new Document(new PointF(100, 100), 200, 400);
    }
}
