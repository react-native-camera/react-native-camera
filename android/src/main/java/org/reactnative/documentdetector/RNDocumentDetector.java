package org.reactnative.documentdetector;

import android.graphics.PointF;
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

    public Document detect(byte[] imageData, double width, double height, double scaleX, double scaleY){

        Mat yuv = new Mat(new Size(width, height * 1.5), CvType.CV_8UC1);
        yuv.put(0, 0, imageData);

        Mat mat = new Mat(new Size(width, height), CvType.CV_8UC4);
        Imgproc.cvtColor(yuv, mat, Imgproc.COLOR_YUV2RGBA_NV21, 4);

        yuv.release();

        Document document = processPreviewFrame(mat, scaleX, scaleY);

        mat.release();

        return document;
    }

    private Document processPreviewFrame(Mat previewFrame, double scaleX, double scaleY) {
        ArrayList<MatOfPoint> contours = findContours(previewFrame);

        Quadrilateral quadrilateral = getQuadrilateral(contours, previewFrame.size());

        if (quadrilateral != null) {
            Point[] rescaledPoints = scalePoints(quadrilateral.getPoints(), scaleX, scaleY);

            // ATTENTION: axis are swapped
            Log.i("processPreviewFrame", rescaledPoints[0].toString() + rescaledPoints[1].toString() + rescaledPoints[2].toString() + rescaledPoints[3].toString());

            return new Document(rescaledPoints);
        }

        return null;
    }

    private Point[] scalePoints(Point[] points, double scaleX, double scaleY) {
        for (int i = 0; i < 4; i++) {
            int x = Double.valueOf(points[i].x * scaleX).intValue();
            int y = Double.valueOf(points[i].y * scaleY).intValue();
            points[i] = new Point(x, y);
        }
        return points;
    }

    private ArrayList<MatOfPoint> findContours(Mat src) {
        Mat grayImage = null;
        Mat cannedImage = null;
        Mat resizedImage = null;

        double ratio = src.size().height / 500;
        int height = Double.valueOf(src.size().height / ratio).intValue();
        int width = Double.valueOf(src.size().width / ratio).intValue();
        Size size = new Size(width,height);

        resizedImage = new Mat(size, CvType.CV_8UC4);
        grayImage = new Mat(size, CvType.CV_8UC4);
        cannedImage = new Mat(size, CvType.CV_8UC1);

        Imgproc.resize(src,resizedImage,size);
        Imgproc.cvtColor(resizedImage, grayImage, Imgproc.COLOR_RGBA2GRAY, 4);
        Imgproc.GaussianBlur(grayImage, grayImage, new Size(5, 5), 0);
        Imgproc.Canny(grayImage, cannedImage, 80, 100, 3, false);

        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(cannedImage, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        hierarchy.release();

        Collections.sort(contours, new Comparator<MatOfPoint>() {

            @Override
            public int compare(MatOfPoint lhs, MatOfPoint rhs) {
                return Double.valueOf(Imgproc.contourArea(rhs)).compareTo(Imgproc.contourArea(lhs));
            }
        });

        resizedImage.release();
        grayImage.release();
        cannedImage.release();

        return contours;
    }

    private Quadrilateral getQuadrilateral(ArrayList<MatOfPoint> contours , Size srcSize) {

        double ratio = srcSize.height / 500;
        int height = Double.valueOf(srcSize.height / ratio).intValue();
        int width = Double.valueOf(srcSize.width / ratio).intValue();
        Size size = new Size(width, height);

        for (MatOfPoint c: contours) {
            MatOfPoint2f c2f = new MatOfPoint2f(c.toArray());
            double peri = Imgproc.arcLength(c2f, true);
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(c2f, approx, 0.02 * peri, true);

            Point[] points = approx.toArray();

            Point[] foundPoints = sortPoints(points);

            if (insideArea(foundPoints, size)) {
                return new Quadrilateral(c , foundPoints);
            }
        }

        return null;
    }

    private Point[] sortPoints(Point[] src) {

        ArrayList<Point> srcPoints = new ArrayList<>(Arrays.asList(src));

        Point[] result = { null , null , null , null };

        Comparator<Point> sumComparator = new Comparator<Point>() {
            @Override
            public int compare(Point lhs, Point rhs) {
                return Double.valueOf(lhs.y + lhs.x).compareTo(rhs.y + rhs.x);
            }
        };

        Comparator<Point> diffComparator = new Comparator<Point>() {

            @Override
            public int compare(Point lhs, Point rhs) {
                return Double.valueOf(lhs.y - lhs.x).compareTo(rhs.y - rhs.x);
            }
        };

        // top-left corner = minimal sum
        result[0] = Collections.min(srcPoints, sumComparator);

        // bottom-right corner = maximal sum
        result[2] = Collections.max(srcPoints, sumComparator);

        // top-right corner = minimal diference
        result[1] = Collections.min(srcPoints, diffComparator);

        // bottom-left corner = maximal diference
        result[3] = Collections.max(srcPoints, diffComparator);

        return result;
    }

    private boolean insideArea(Point[] rp, Size size) {

        int width = Double.valueOf(size.width).intValue();
        int height = Double.valueOf(size.height).intValue();
        int baseMeasure = height / 4;

        int bottomPos = height - baseMeasure;
        int topPos = baseMeasure;
        int leftPos = width / 2 - baseMeasure;
        int rightPos = width / 2 + baseMeasure;

        return (
                rp[0].x <= leftPos && rp[0].y <= topPos
                        && rp[1].x >= rightPos && rp[1].y <= topPos
                        && rp[2].x >= rightPos && rp[2].y >= bottomPos
                        && rp[3].x <= leftPos && rp[3].y >= bottomPos

        );
    }
}
