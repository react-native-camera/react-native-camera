package org.reactnative.opencv;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import com.facebook.react.common.ReactConstants;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.reactnative.camera.R;

public class OpenCVProcessor {
    private CascadeClassifier faceDetector;
    private int frame = 0;
    private Context reactContext;

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

    public SparseArray<Map<String, Float>> detect(byte[] imageData, int width, int height) {
        SparseArray<Map<String, Float>> faces = new SparseArray();
        if (this.frame % 15 == 0) {
            Mat mat = new Mat((height / 2) + height, width, CvType.CV_8UC1);
            mat.put(0, 0, imageData);

            Mat grayMat = new Mat();
            Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_YUV2GRAY_420);

            Core.transpose(grayMat, grayMat);
            Core.flip(grayMat, grayMat, -1);

            float imageWidth = 480f;
            float scale = imageWidth / grayMat.cols();
            float imageHeight = grayMat.rows() * scale;

            Imgproc.resize(grayMat, grayMat, new Size(), scale, scale, 2);

            if (this.frame == 30) {
                Log.d(ReactConstants.TAG, "---SAVE IMAGE!!--- ");
//                saveMatToDisk(grayMat);
            }

            MatOfRect rec = new MatOfRect();
            this.faceDetector.detectMultiScale(grayMat, rec, 1.2, 3, 0, new Size(10, 10), new Size());

            Rect[] detectedObjects = rec.toArray();
            if (detectedObjects.length > 0) {
                Log.d(ReactConstants.TAG, "---FOUND FACE!!--- ");

                for (int i = 0; i < detectedObjects.length; i++) {
                    Map<String, Float> face = new HashMap();
                    face.put("x", detectedObjects[i].x / imageWidth);
                    face.put("y", detectedObjects[i].y / imageHeight);
                    face.put("width", detectedObjects[i].width / imageWidth);
                    face.put("height", detectedObjects[i].height / imageHeight);
                    faces.append(i, face);
                }
            }
        }
        this.frame++;
        return faces;
    }
}