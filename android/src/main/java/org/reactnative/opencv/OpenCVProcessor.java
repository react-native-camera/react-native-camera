package org.reactnative.opencv;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.view.Surface;
import android.view.WindowManager;

import com.facebook.react.common.ReactConstants;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.reactnative.camera.R;

import static org.opencv.core.CvType.CV_32F;
import static org.opencv.imgproc.Imgproc.morphologyEx;

public class OpenCVProcessor {
    private CascadeClassifier faceDetector;
    private int frame = 0;
    private Context reactContext;
    private int faceDetectionExpectedOrientation = -1;
    private int objectsToDetect = 0;
    private boolean saveDemoFrame = false;

    public OpenCVProcessor(Context context) {
        this.reactContext = context;
        try {
            InputStream is = this.reactContext.getResources().openRawResource(R.raw.lbpcascade_frontalface_improved);
            File mCascadeFile = new File(this.reactContext.getDir("cascade", 0), "lbpcascade_frontalface_improved.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);
            byte[] buffer = new byte[4096];
            while (true) {
                int bytesRead = is.read(buffer);
                if (bytesRead == -1) {
                    break;
                }
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();
            this.faceDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            if (this.faceDetector.empty()) {
                Log.e(ReactConstants.TAG, "Failed to load cascade classifier");
                this.faceDetector = null;
            } else {
                Log.i(ReactConstants.TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(ReactConstants.TAG, "Failed to load cascade. Exception thrown: " + e);
        }
        Log.d(ReactConstants.TAG, "---OpenCV Constructor---");
    }

    private void saveMatToDisk(Mat mat) {
        Imgcodecs.imwrite("/sdcard/nect/" + String.valueOf(System.currentTimeMillis()) + ".jpg", mat);
    }

    private int rotateImage(Mat image, int rotation){
        int imageRotation = 1;
        switch(rotation) {
            case 90:
                imageRotation = 2;
                break;
            case 180:
                imageRotation = 3;
                break;
            case 270:
                imageRotation = 0;
                break;
        }

        int expectedFaceOrientation = 3;

        if(faceDetectionExpectedOrientation != -1){
            expectedFaceOrientation = faceDetectionExpectedOrientation;
        } else {
            // rotate image according to device-orientation
            WindowManager wManager = (WindowManager) reactContext.getSystemService(reactContext.WINDOW_SERVICE);
            int deviceRotation = wManager.getDefaultDisplay().getRotation();

            switch (deviceRotation) {
                case Surface.ROTATION_0:
                    expectedFaceOrientation = 0;
                    break;
                case Surface.ROTATION_90:
                    expectedFaceOrientation = 1;
                    break;
                case Surface.ROTATION_180:
                    expectedFaceOrientation = 2;
                    break;
            }
        }

        int rotationToBeApplied = expectedFaceOrientation + imageRotation % 4;

        switch(rotationToBeApplied){
            case 2:
                Core.transpose(image, image);
                Core.flip(image, image,1);
                break;
            case 3:
                Core.flip(image, image,-1);
                break;
            case 0:
                Core.transpose(image, image);
                Core.flip(image, image,0);
                break;
        }

        return expectedFaceOrientation;
    }

    private float resizeImage(Mat image, float width){
        float scale = width / image.cols();

        Imgproc.resize(image, image, new Size(), scale, scale, 2);

        return scale;
    }

    public SparseArray<Map<String, Float>> detect(byte[] imageData, int width, int height, int rotation) {
        SparseArray<Map<String, Float>> objects = new SparseArray();
        if (this.frame % 15 == 0) {
            Mat mat = new Mat((height / 2) + height, width, CvType.CV_8UC1);
            mat.put(0, 0, imageData);

            Mat grayMat = new Mat();
            Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_YUV2GRAY_420);

            switch(objectsToDetect){
                case 0:
                    objects = detectFaces(grayMat, rotation);
                    break;
                case 1:
                    objects = detectTextBlocks(grayMat, rotation);
                    break;
            }

        }
        this.frame++;
        return objects;
    }

    private SparseArray<Map<String, Float>> detectFaces(Mat image, int rotation) {
        SparseArray<Map<String, Float>> faces = new SparseArray();
        int expectedFaceOrientation = rotateImage(image, rotation);

        float imageWidth = 480f;
        float scale = resizeImage(image, imageWidth);

        float imageHeight = image.rows();

        // Save Demo Frame
        if (saveDemoFrame && this.frame == 30) {
            Log.d(ReactConstants.TAG, "---SAVE IMAGE!!--- ");
            saveMatToDisk(image);
        }

        MatOfRect rec = new MatOfRect();
        this.faceDetector.detectMultiScale(image, rec, 1.3, 3, 0, new Size(50, 50), new Size());

        Rect[] detectedObjects = rec.toArray();
        if (detectedObjects.length > 0) {
            Log.d(ReactConstants.TAG, "---FOUND FACE!!--- ");

            for (int i = 0; i < detectedObjects.length; i++) {
                Map<String, Float> face = new HashMap();
                face.put("x", detectedObjects[i].x / imageWidth);
                face.put("y", detectedObjects[i].y / imageHeight);
                face.put("width", detectedObjects[i].width / imageWidth);
                face.put("height", detectedObjects[i].height / imageHeight);
                face.put("orientation", (float) expectedFaceOrientation);
                faces.append(i, face);
            }
        }

        return faces;
    }

    private SparseArray<Map<String, Float>> detectTextBlocks(Mat image, int rotation) {
        SparseArray<Map<String, Float>> objects = new SparseArray();

        int orientation = rotateImage(image, rotation);

        float algorithmWidth = 1080f;
        float imageWidth = 480f;
        float algorithmScale = imageWidth / algorithmWidth;
        float scale = resizeImage(image, imageWidth);

        float imageHeight = image.rows();

        float rectKernX = 17f * algorithmScale;
        float rectKernY = 6f * algorithmScale;
        float sqKernXY = 40f * algorithmScale;
        float minSize = 3000f * algorithmScale;
        float maxSize = 100000f * algorithmScale;

        Mat processedImage = image.clone();

        // initialize a rectangular and square structuring kernel
        //float factor = (float)min(image.rows, image.cols) / 600.;
        Mat rectKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(rectKernX, rectKernY));
        Mat rectKernel2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(sqKernXY, (int)(0.666666*sqKernXY)));

        // Smooth the image using a 3x3 Gaussian, then apply the blackhat morphological
        // operator to find dark regions on a light background
        Imgproc.GaussianBlur(processedImage, processedImage, new Size(3, 3), 0);
        morphologyEx(processedImage, processedImage, Imgproc.MORPH_BLACKHAT, rectKernel);


        // Compute the Scharr gradient of the blackhat image
        Mat imageGrad = new Mat();

        //Sobel(processedImage, imageGrad, CV_32F, 1, 0, CV_SCHARR);
        Imgproc.Sobel(processedImage, imageGrad, CV_32F, 1, 0);
//        Core.convertScaleAbs(imageGrad/8, processedImage);
        Core.convertScaleAbs(imageGrad, processedImage);

        // Apply a closing operation using the rectangular kernel to close gaps in between
        // letters, then apply Otsu's thresholding method
        morphologyEx(processedImage, processedImage, Imgproc.MORPH_CLOSE, rectKernel);
        Imgproc.threshold(processedImage, processedImage, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        // , 1, 1
        Imgproc.erode(processedImage, processedImage, new Mat(), new Point(-1, -1), 2);


        // Perform another closing operation, this time using the square kernel to close gaps
        // between lines of TextBlocks
        morphologyEx(processedImage, processedImage, Imgproc.MORPH_CLOSE, rectKernel2);


        // Find contours in the thresholded image and sort them by size
        float minContourArea = minSize;
        float maxContourArea = maxSize;

        // https://github.com/codertimo/Vision/issues/7
        // http://answers.opencv.org/question/6206/opencv4android-conversion-from-matofkeypoint-to-matofpoint2f/
        List<MatOfPoint> contours = new ArrayList<>();
        MatOfInt4 hierarchy = new MatOfInt4();
        Imgproc.findContours(processedImage, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Create a result vector
        List<RotatedRect> minRects = new ArrayList<>();
        for (int i = 0, I = contours.size(); i < I; ++i) {
            // Filter by provided area limits
            if (Imgproc.contourArea(contours.get(i)) > minContourArea && Imgproc.contourArea(contours.get(i)) < maxContourArea)
                minRects.add(Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i).toArray())));
        }

        if(saveDemoFrame){
            Mat debugDrawing = image.clone();
            for (int i = 0, I = minRects.size(); i < I; ++i) {
                Point[] rect_points = new Point[4];
                minRects.get(i).points( rect_points );
                for( int j = 0; j < 4; ++j )
                    Imgproc.line( debugDrawing, rect_points[j], rect_points[(j+1)%4], new Scalar(255,0,0), 1, 8, 0 );
            }

            saveMatToDisk(debugDrawing);
        }

        if (minRects.size() > 0) {

            for (int i = 0; i < minRects.size(); i++) {
                Point[] rect_points = new Point[4];
                minRects.get(i).points( rect_points );

                float xRel = (float) rect_points[1].x / imageWidth;
                float yRel = (float) rect_points[1].y / imageHeight;
                float widthRel = (float) Math.abs(rect_points[3].x - rect_points[1].x) / imageWidth;
                float heightRel = (float) Math.abs(rect_points[3].y - rect_points[1].y) / imageHeight;
                float sizeRel = Math.abs(widthRel * heightRel);
                float ratio =  (float) Math.abs(rect_points[3].x - rect_points[1].x) / (float) Math.abs(rect_points[3].y - rect_points[1].y);

                // if object large enough
                if(sizeRel >= 0.025 & ratio >= 5.5 & ratio <= 8.5){
                    Map<String, Float> object = new HashMap();
                    object.put("x", xRel);
                    object.put("y", yRel);
                    object.put("width", widthRel);
                    object.put("height", heightRel);
                    object.put("orientation", (float) orientation);
                    objects.append(i, object);
                }
            }
        }

        return objects;
    }

    public void setFaceDetectionExpectedOrientation(int expectedOrientation){
        faceDetectionExpectedOrientation = expectedOrientation;
    }

    public void updateObjectsToDetect(int objToDetect){
        objectsToDetect = objToDetect;
    }
}
