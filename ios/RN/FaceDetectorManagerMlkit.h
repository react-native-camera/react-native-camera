
#import <UIKit/UIKit.h>
#if __has_include(<FirebaseMLVision/FirebaseMLVision.h>)
  #import <FirebaseMLVision/FirebaseMLVision.h>
  typedef NS_ENUM(NSInteger, RNFaceDetectionMode) {
      RNFaceDetectionFastMode = FIRVisionFaceDetectorPerformanceModeFast,
      RNFaceDetectionAccurateMode = FIRVisionFaceDetectorPerformanceModeAccurate
  };

  typedef NS_ENUM(NSInteger, RNFaceDetectionLandmarks) {
      RNFaceDetectAllLandmarks = FIRVisionFaceDetectorLandmarkModeAll,
      RNFaceDetectNoLandmarks = FIRVisionFaceDetectorLandmarkModeNone
  };

  typedef NS_ENUM(NSInteger, RNFaceDetectionClassifications) {
      RNFaceRunAllClassifications = FIRVisionFaceDetectorClassificationModeAll,
      RNFaceRunNoClassifications = FIRVisionFaceDetectorClassificationModeNone
  };
#endif

  @interface FaceDetectorManagerMlkit : NSObject
  typedef void(^postRecognitionBlock)(NSArray *faces);

  - (instancetype)init;

  -(BOOL)isRealDetector;
  -(void)setTracking:(id)json queue:(dispatch_queue_t)sessionQueue;
  -(void)setLandmarksMode:(id)json queue:(dispatch_queue_t)sessionQueue;
  -(void)setPerformanceMode:(id)json queue:(dispatch_queue_t)sessionQueue;
  -(void)setClassificationMode:(id)json queue:(dispatch_queue_t)sessionQueue;
  -(void)findFacesInFrame:(UIImage *)image scaleX:(float)scaleX scaleY:(float)scaleY completed:(postRecognitionBlock)completed;
  +(NSDictionary *)constants;

  @end 
