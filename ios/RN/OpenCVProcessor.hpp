#ifdef __cplusplus
#import <opencv2/opencv.hpp>
#import <opencv2/objdetect.hpp>
#import <opencv2/videoio/cap_ios.h>
using namespace cv;
#endif

#import <AVFoundation/AVFoundation.h>

@protocol OpenCVProcessorFaceDetectorDelegate
- (void)onFacesDetected:(NSArray<NSDictionary *> *)faces;
@end

@class OpenCVProcessor;

@interface OpenCVProcessor : NSObject <AVCaptureVideoDataOutputSampleBufferDelegate>
{
#ifdef __cplusplus
    std::vector<cv::Rect> objects;
    cv::CascadeClassifier cascade;
#endif
    id delegate;
}

- (id) init;
- (id) initWithDelegate:(id <OpenCVProcessorFaceDetectorDelegate>)delegateObj;
- (void)setExpectedFaceOrientation:(NSInteger)expectedFaceOrientation;
@end
