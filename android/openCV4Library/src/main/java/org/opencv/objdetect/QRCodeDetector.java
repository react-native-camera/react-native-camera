//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.objdetect;

import java.lang.String;
import org.opencv.core.Mat;

// C++: class QRCodeDetector
//javadoc: QRCodeDetector

public class QRCodeDetector {

    protected final long nativeObj;
    protected QRCodeDetector(long addr) { nativeObj = addr; }

    public long getNativeObjAddr() { return nativeObj; }

    // internal usage only
    public static QRCodeDetector __fromPtr__(long addr) { return new QRCodeDetector(addr); }

    //
    // C++:   cv::QRCodeDetector::QRCodeDetector()
    //

    //javadoc: QRCodeDetector::QRCodeDetector()
    public   QRCodeDetector()
    {
        
        nativeObj = QRCodeDetector_0();
        
        return;
    }


    //
    // C++:  bool cv::QRCodeDetector::detect(Mat img, Mat& points)
    //

    //javadoc: QRCodeDetector::detect(img, points)
    public  boolean detect(Mat img, Mat points)
    {
        
        boolean retVal = detect_0(nativeObj, img.nativeObj, points.nativeObj);
        
        return retVal;
    }


    //
    // C++:  string cv::QRCodeDetector::decode(Mat img, Mat points, Mat& straight_qrcode = Mat())
    //

    //javadoc: QRCodeDetector::decode(img, points, straight_qrcode)
    public  String decode(Mat img, Mat points, Mat straight_qrcode)
    {
        
        String retVal = decode_0(nativeObj, img.nativeObj, points.nativeObj, straight_qrcode.nativeObj);
        
        return retVal;
    }

    //javadoc: QRCodeDetector::decode(img, points)
    public  String decode(Mat img, Mat points)
    {
        
        String retVal = decode_1(nativeObj, img.nativeObj, points.nativeObj);
        
        return retVal;
    }


    //
    // C++:  string cv::QRCodeDetector::detectAndDecode(Mat img, Mat& points = Mat(), Mat& straight_qrcode = Mat())
    //

    //javadoc: QRCodeDetector::detectAndDecode(img, points, straight_qrcode)
    public  String detectAndDecode(Mat img, Mat points, Mat straight_qrcode)
    {
        
        String retVal = detectAndDecode_0(nativeObj, img.nativeObj, points.nativeObj, straight_qrcode.nativeObj);
        
        return retVal;
    }

    //javadoc: QRCodeDetector::detectAndDecode(img, points)
    public  String detectAndDecode(Mat img, Mat points)
    {
        
        String retVal = detectAndDecode_1(nativeObj, img.nativeObj, points.nativeObj);
        
        return retVal;
    }

    //javadoc: QRCodeDetector::detectAndDecode(img)
    public  String detectAndDecode(Mat img)
    {
        
        String retVal = detectAndDecode_2(nativeObj, img.nativeObj);
        
        return retVal;
    }


    //
    // C++:  void cv::QRCodeDetector::setEpsX(double epsX)
    //

    //javadoc: QRCodeDetector::setEpsX(epsX)
    public  void setEpsX(double epsX)
    {
        
        setEpsX_0(nativeObj, epsX);
        
        return;
    }


    //
    // C++:  void cv::QRCodeDetector::setEpsY(double epsY)
    //

    //javadoc: QRCodeDetector::setEpsY(epsY)
    public  void setEpsY(double epsY)
    {
        
        setEpsY_0(nativeObj, epsY);
        
        return;
    }


    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }



    // C++:   cv::QRCodeDetector::QRCodeDetector()
    private static native long QRCodeDetector_0();

    // C++:  bool cv::QRCodeDetector::detect(Mat img, Mat& points)
    private static native boolean detect_0(long nativeObj, long img_nativeObj, long points_nativeObj);

    // C++:  string cv::QRCodeDetector::decode(Mat img, Mat points, Mat& straight_qrcode = Mat())
    private static native String decode_0(long nativeObj, long img_nativeObj, long points_nativeObj, long straight_qrcode_nativeObj);
    private static native String decode_1(long nativeObj, long img_nativeObj, long points_nativeObj);

    // C++:  string cv::QRCodeDetector::detectAndDecode(Mat img, Mat& points = Mat(), Mat& straight_qrcode = Mat())
    private static native String detectAndDecode_0(long nativeObj, long img_nativeObj, long points_nativeObj, long straight_qrcode_nativeObj);
    private static native String detectAndDecode_1(long nativeObj, long img_nativeObj, long points_nativeObj);
    private static native String detectAndDecode_2(long nativeObj, long img_nativeObj);

    // C++:  void cv::QRCodeDetector::setEpsX(double epsX)
    private static native void setEpsX_0(long nativeObj, double epsX);

    // C++:  void cv::QRCodeDetector::setEpsY(double epsY)
    private static native void setEpsY_0(long nativeObj, double epsY);

    // native support for java finalize()
    private static native void delete(long nativeObj);

}
