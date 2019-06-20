package org.reactnative.documentdetector;

import android.graphics.PointF;

import org.opencv.core.Point;

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

    public Document(Point[] points) {
        this.topLeft = new PointF((float)points[0].x, (float)points[0].y);
        this.width = (float) (points[1].x-points[0].x+points[2].x-points[3].x)/2;
        this.height = (float) (points[3].y-points[0].y+points[2].y-points[1].y)/2;
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
