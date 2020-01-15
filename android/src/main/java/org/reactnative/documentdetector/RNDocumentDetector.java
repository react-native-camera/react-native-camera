package org.reactnative.documentdetector;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.reactnative.documentdetector.helpers.Quadrilateral;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RNDocumentDetector {
    static final double MINIMUM_SIZE_FACTOR = .15; // document at least 15% of image area

    private final Comparator<Point> sumComparator = new Comparator<Point>() {
        @Override
        public int compare(Point lhs, Point rhs) {
            return Double.compare(lhs.y + lhs.x, rhs.y + rhs.x);
        }
    };
    private final Comparator<Point> diffComparator = new Comparator<Point>() {

        @Override
        public int compare(Point lhs, Point rhs) {
            return Double.compare(lhs.y - lhs.x, rhs.y - rhs.x);
        }
    };
    private final Comparator<MatOfPoint> contourAreaComparator = new Comparator<MatOfPoint>() {

        @Override
        public int compare(MatOfPoint lhs, MatOfPoint rhs) {
            return Double.compare(Imgproc.contourArea(rhs), Imgproc.contourArea(lhs));
        }
    };

    public RNDocumentDetector() {}

    public Document detectPreview(byte[] imageData, double landscapeWidth, double landscapeHeight, double scaleX, double scaleY) {
        Mat mat = createMat(imageData, landscapeWidth, landscapeHeight, false);

        Quadrilateral quadrilateral = processFrame(mat);

        mat.release();
        if (quadrilateral == null) return null;

        // points need to be transformed for pixels to be used in RN
        Point[] transformedPoints = transformPoints(quadrilateral.getPoints(), scaleX, scaleY, landscapeHeight);
        return new Document(transformedPoints);
    }

    public Document detectCaptured(byte[] imageData, double landscapeWidth, double landscapeHeight) {
        Mat mat = createMat(imageData, landscapeWidth, landscapeHeight, true);

        Quadrilateral quadrilateral = processFrame(mat);

        mat.release();
        if (quadrilateral == null) return null;

        // to work on the captured image, points should not be transformed
        return new Document(quadrilateral.getPoints());
    }

    private Mat createMat(byte[] imageData, double landscapeWidth, double landscapeHeight, boolean compressed) {
        if (compressed) {
            return Imgcodecs.imdecode(new MatOfByte(imageData), Imgcodecs.IMREAD_REDUCED_COLOR_2 | Imgcodecs.IMREAD_IGNORE_ORIENTATION );
        }
        Mat yuv = new Mat(new Size(landscapeWidth, landscapeHeight * 1.5), CvType.CV_8UC1);
        yuv.put(0, 0, imageData);

        Mat mat = new Mat(new Size(landscapeWidth, landscapeHeight), CvType.CV_8UC4);
        Imgproc.cvtColor(yuv, mat, Imgproc.COLOR_YUV2RGBA_NV21, 4);

        yuv.release();

        return mat;
    }

    private Quadrilateral processFrame(Mat frame) {
        List<MatOfPoint> contours = findContours(frame);

        return getQuadrilateral(contours, frame.size());
    }

    /**
     * Rotates points as axis are swapped (image always in landscape)
     *
     * +----------+      +--------+
     * |          |      | 0  1   |
     * | 0  1     |  =>  | 3  2   |
     * | 3  2     |      |        |
     * +----------+      |        |
     *                   +--------+
     */
    private Point[] transformPoints(Point[] points, double scaleX, double scaleY, double landscapeHeight) {
        return new Point[]{
                new Point((landscapeHeight - points[3].y) * scaleY, points[3].x * scaleX),// top left
                new Point((landscapeHeight - points[0].y) * scaleY, points[0].x * scaleX),// top right
                new Point((landscapeHeight - points[1].y) * scaleY, points[1].x * scaleX),// bottom right
                new Point((landscapeHeight - points[2].y) * scaleY, points[2].x * scaleX)// bottom left
        };
    }

    private List<MatOfPoint> findContours(Mat src) {
        double ratio = src.size().height / 500;
        int height = Double.valueOf(src.size().height / ratio).intValue();
        int width = Double.valueOf(src.size().width / ratio).intValue();
        Size size = new Size(width, height);

        Mat resizedImage = new Mat(size, CvType.CV_8UC4);
        Mat grayImage = new Mat(size, CvType.CV_8UC4);
        Mat cannedImage = new Mat(size, CvType.CV_8UC1);
        Mat _unused = new Mat(size, CvType.CV_8UC1);

        Imgproc.resize(src, resizedImage, size);
        Imgproc.cvtColor(resizedImage, grayImage, Imgproc.COLOR_RGBA2GRAY, 4);
        Imgproc.GaussianBlur(grayImage, grayImage, new Size(5, 5), 0);
        double thresholdHigh = Imgproc.threshold(grayImage, _unused,0, 255, Imgproc.THRESH_BINARY+Imgproc.THRESH_OTSU);
        Imgproc.Canny(grayImage, cannedImage, .1 * thresholdHigh, thresholdHigh, 3, false);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(cannedImage, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_TC89_KCOS);

        hierarchy.release();
        resizedImage.release();
        grayImage.release();
        cannedImage.release();

        Collections.sort(contours, contourAreaComparator);

        if (contours.size() > 10) {
            return contours.subList(0, 10);
        }
        return contours;
    }

    private Quadrilateral getQuadrilateral(List<MatOfPoint> contours, Size srcSize) {

        double ratio = srcSize.height / 500;
        int height = Double.valueOf(srcSize.height / ratio).intValue();
        int width = Double.valueOf(srcSize.width / ratio).intValue();
        Size size = new Size(width, height);

        for (MatOfPoint contour : contours) {
            MatOfPoint2f approx = getApproximation(contour);

            // cant get a quadrilateral from less than 4 points
            if (approx.toArray().length < 4) continue;

            Point[] points = selectCornersFromPoints(approx.toArray());
            if (isQuadrilateralLargeEnough(points, size)) {
                scalePoints(points, ratio); // reset scale to src size
                return new Quadrilateral(contour, points);
            }
        }

        return null;
    }

    /**
     * Find contour perimeter and approximate the shape by Ramer–Douglas–Peucker
     *
     * @param contour A contour
     * @return approx A shape approximation
     */
    private MatOfPoint2f getApproximation(MatOfPoint contour) {
        MatOfPoint2f c2f = new MatOfPoint2f(contour.toArray());
        double perimeter = Imgproc.arcLength(c2f, true);

        MatOfPoint2f approx = new MatOfPoint2f();
        Imgproc.approxPolyDP(c2f, approx, 0.1 * perimeter, true);
        return approx;
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

    private boolean isQuadrilateralLargeEnough(Point[] rp, Size size) {
        double widthMax = size.width; // 666
        double heightMax = size.height; // 500

        Point tl = rp[0];
        Point tr = rp[1];
        Point br = rp[2];
        Point bl = rp[3];

        double width1 = Math.hypot(tr.x-tl.x, tr.y-tl.y);
        double width2 = Math.hypot(br.x-bl.x, br.y-bl.y);
        double width = Math.max(width1, width2);

        double height1 = Math.hypot(tr.x-br.x, tr.y-br.y);
        double height2 = Math.hypot(tl.x-bl.x, tl.y-bl.y);
        double height = Math.max(height1, height2);

        return width*height > MINIMUM_SIZE_FACTOR * (widthMax*heightMax);
    }
}
