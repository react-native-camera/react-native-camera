package org.reactnative.documentdetector;

import org.opencv.core.Point;

public class Document {
    private Point topLeft;
    private Point topRight;
    private Point bottomLeft;
    private Point bottomRight;

    public Document(Point[] points) {
        this.topLeft = new Point(points[0].x, points[0].y);
        this.topRight = new Point(points[1].x, points[1].y);
        this.bottomRight = new Point(points[2].x, points[2].y);
        this.bottomLeft = new Point(points[3].x, points[3].y);
    }

    public Point getTopLeft() {
        return topLeft;
    }

    public Point getTopRight() {
        return topRight;
    }

    public Point getBottomLeft() {
        return bottomLeft;
    }

    public Point getBottomRight() {
        return bottomRight;
    }
}
