package org.reactnative.documentdetector.helpers;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

public class Quadrilateral {
    private MatOfPoint contour;
    private Point[] points;

    public Quadrilateral(MatOfPoint contour, Point[] points) {
        this.contour = contour;
        this.points = points;
    }

    public MatOfPoint getContour() {
        return contour;
    }

    public Point[] getPoints() {
        return points;
    }
}
