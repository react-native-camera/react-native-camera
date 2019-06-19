package org.reactnative.documentdetector;

import android.graphics.PointF;

public class Document {
    private PointF topLeft;
    private PointF topRight;
    private PointF bottomLeft;
    private PointF bottomRight;
    private float width;
    private float height;

    public Document(PointF topLeft, PointF topRight, PointF bottomLeft, PointF bottomRight) {
        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomLeft = bottomLeft;
        this.bottomRight = bottomRight;
    }

    public Document(PointF topLeft, float width, float height) {
        this.topLeft = topLeft;
        this.width = width;
        this.height = height;
    }

    public PointF getTopLeft() {
        return topLeft;
    }

    public PointF getTopRight() {
        return topRight;
    }

    public PointF getBottomLeft() {
        return bottomLeft;
    }

    public PointF getBottomRight() {
        return bottomRight;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
