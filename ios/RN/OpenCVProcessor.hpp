#ifdef __cplusplus
#import <opencv2/opencv.hpp>
#import <opencv2/objdetect.hpp>
#import <opencv2/videoio/cap_ios.h>
using namespace cv;
#endif

#import <AVFoundation/AVFoundation.h>


@class OpenCVProcessor;

@interface OpenCVProcessor : NSObject <AVCaptureVideoDataOutputSampleBufferDelegate>
{
#ifdef __cplusplus
    std::vector<cv::Rect> objects;
    cv::CascadeClassifier cascade;
#endif
}

- (id) init;
@end


