package org.reactnative.documentdetector;

import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.reactnative.documentdetector.helpers.Quadrilateral;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class RNDocumentDetector {
    public RNDocumentDetector() {}

    public Document detectPreview(byte[] imageData, double landscapeWidth, double landscapeHeight, double scaleX, double scaleY) {
        Mat mat = createMat(imageData, landscapeWidth, landscapeHeight);

        Quadrilateral quadrilateral = processFrame(mat);

        mat.release();
        if (quadrilateral != null) {
            Point[] transformedPoints = transformPoints(quadrilateral.getPoints(), scaleX, scaleY, landscapeHeight);
            return new Document(transformedPoints);
        }
        return null;
    }

    public Document detectCaptured(byte[] imageData, double landscapeWidth, double landscapeHeight) {
        Mat mat = createMat(imageData, landscapeWidth, landscapeHeight);

        Quadrilateral quadrilateral = processFrame(mat);

        mat.release();
        if (quadrilateral == null) return null;

        // to work on the captured image, points should not be transformed
        return new Document(quadrilateral.getPoints());
    }

    private Mat createMat(byte[] imageData, double landscapeWidth, double landscapeHeight) {
        Mat yuv = new Mat(new Size(landscapeWidth, landscapeHeight * 1.5), CvType.CV_8UC1);
        yuv.put(0, 0, imageData);

        Mat mat = new Mat(new Size(landscapeWidth, landscapeHeight), CvType.CV_8UC4);
        Imgproc.cvtColor(yuv, mat, Imgproc.COLOR_YUV2RGBA_NV21, 4);

        yuv.release();

        return mat;
    }

    private Quadrilateral processFrame(Mat frame) {
        ArrayList<MatOfPoint> contours = findContours(frame);

        return getQuadrilateral(contours, frame.size());
    }

    /**
     * Rotates points as axis are swapped (image always in landscape)
     *
     * +----------+      +--------+
     * |          |      | 0  1   |
     * | 1  2     |  =>  | 3  2   |
     * | 0  3     |      |        |
     * +----------+      |        |
     *                   +--------+
     */
    private Point[] transformPoints(Point[] points, double scaleX, double scaleY, double landscapeHeight) {
        return new Point[]{
                new Point((landscapeHeight - points[3].y) * scaleX, points[3].x * scaleY),// top left
                new Point((landscapeHeight - points[0].y) * scaleX, points[0].x * scaleY),// top right
                new Point((landscapeHeight - points[1].y) * scaleX, points[1].x * scaleY),// bottom right
                new Point((landscapeHeight - points[2].y) * scaleX, points[2].x * scaleY)// bottom left
        };
    }

    private ArrayList<MatOfPoint> findContours(Mat src) {
        Mat grayImage = null;
        Mat cannedImage = null;
        Mat resizedImage = null;

        double ratio = src.size().height / 500;
        int height = Double.valueOf(src.size().height / ratio).intValue();
        int width = Double.valueOf(src.size().width / ratio).intValue();
        Size size = new Size(width, height);

        resizedImage = new Mat(size, CvType.CV_8UC4);
        grayImage = new Mat(size, CvType.CV_8UC4);
        cannedImage = new Mat(size, CvType.CV_8UC1);

        Imgproc.resize(src, resizedImage, size);
        Imgproc.cvtColor(resizedImage, grayImage, Imgproc.COLOR_RGBA2GRAY, 4);
        Imgproc.GaussianBlur(grayImage, grayImage, new Size(5, 5), 0);
        Imgproc.Canny(grayImage, cannedImage, 80, 100, 3, false);

        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(cannedImage, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        hierarchy.release();

        Collections.sort(contours, new Comparator<MatOfPoint>() {

            @Override
            public int compare(MatOfPoint lhs, MatOfPoint rhs) {
                return Double.compare(Imgproc.contourArea(rhs), Imgproc.contourArea(lhs));
            }
        });

        resizedImage.release();
        grayImage.release();
        cannedImage.release();

        return contours;
    }

    private Quadrilateral getQuadrilateral(ArrayList<MatOfPoint> contours, Size srcSize) {

        double ratio = srcSize.height / 500;
        int height = Double.valueOf(srcSize.height / ratio).intValue();
        int width = Double.valueOf(srcSize.width / ratio).intValue();
        Size size = new Size(width, height);

        for (MatOfPoint matOfPoint : contours) {
            // find contour perimeter
            MatOfPoint2f c2f = new MatOfPoint2f(matOfPoint.toArray());
            double epsilon = 0.02 * Imgproc.arcLength(c2f, true);
            // approximate the shape by Ramer–Douglas–Peucker
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(c2f, approx, epsilon, true);
            if (approx.toArray().length < 4) continue; // cant get a quadrilateral from three points

            Point[] points = selectCornersFromPoints(approx.toArray());
            if (insideArea(points, size)) {
                scalePoints(points, ratio); // reset scaling
                return new Quadrilateral(matOfPoint, points);
            }
        }

        return null;
    }

    private void scalePoints(Point[] points, double ratio) {
        for (Point p : points) {
            p.x = ratio * p.x;
            p.y = ratio * p.y;
        }
    }

    private Point[] selectCornersFromPoints(Point[] src) {

        ArrayList<Point> srcPoints = new ArrayList<>(Arrays.asList(src));

        Point[] result = { null, null, null, null };

        Comparator<Point> sumComparator = new Comparator<Point>() {
            @Override
            public int compare(Point lhs, Point rhs) {
                return Double.compare(lhs.y + lhs.x, rhs.y + rhs.x);
            }
        };

        Comparator<Point> diffComparator = new Comparator<Point>() {

            @Override
            public int compare(Point lhs, Point rhs) {
                return Double.compare(lhs.y - lhs.x, rhs.y - rhs.x);
            }
        };

        // top-left corner = minimal sum
        result[0] = Collections.min(srcPoints, sumComparator);

        // bottom-right corner = maximal sum
        result[2] = Collections.max(srcPoints, sumComparator);

        // top-right corner = minimal difference
        result[1] = Collections.min(srcPoints, diffComparator);

        // bottom-left corner = maximal difference
        result[3] = Collections.max(srcPoints, diffComparator);

        return result;
    }

    private boolean insideArea(Point[] rp, Size size) {
        int width = Double.valueOf(size.width).intValue(); // 666
        int height = Double.valueOf(size.height).intValue(); // 500
        int baseMeasure = height / 4; // 125

        int bottomPos = height - baseMeasure; // 375
        int topPos = baseMeasure; // 125
        int leftPos = width / 2 - baseMeasure; // 208
        int rightPos = width / 2 + baseMeasure; // 458

        boolean isInside = (
                rp[0].x <= leftPos && rp[0].y <= topPos // topleft.x <= 208 && topleft.y <= 125
                        && rp[1].x >= rightPos && rp[1].y <= topPos // topright.x >= 458 && topright.y <= 125
                        && rp[2].x >= rightPos && rp[2].y >= bottomPos // bottomright.x >= 458 && bottomright.y >= 375
                        && rp[3].x <= leftPos && rp[3].y >= bottomPos // bottomleft.x <= 208 && bottomleft.y >= 375
        );
        if (!isInside) {
            Log.i("inside calc", String.format("not inside means at (%d, %d)",(int)rp[0].x, (int)rp[0].y));
        }
        return isInside;
    }
}
