
#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventDispatcher.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTLog.h>
#import "TFLTensorFlowLite.h"
@import TensorFlowLite;

// @interface FaceDetectorManagerMlkit ()
// @property(nonatomic, strong) FIRVisionFaceDetector *faceRecognizer;
// @property(nonatomic, strong) FIRVision *vision;
// @property(nonatomic, strong) FIRVisionFaceDetectorOptions *options;
// @property(nonatomic, assign) float scaleX;
// @property(nonatomic, assign) float scaleY;
// @end
@interface MyModel : NSObject
  // typedef void(^postRecognitionBlock)(NSArray *faces);
  @property(nonatomic, strong) TFLInterpreter *interpreter;
  - (instancetype)init;
  -(NSArray *)runModelWithFrame:(UIImage *)image scaleX:(float)scaleX scaleY:(float)scaleY ;

  // -(BOOL)isRealDetector;
  // -(void)setTracking:(id)json queue:(dispatch_queue_t)sessionQueue;
  // -(void)setLandmarksMode:(id)json queue:(dispatch_queue_t)sessionQueue;
  // -(void)setPerformanceMode:(id)json queue:(dispatch_queue_t)sessionQueue;
  // -(void)setClassificationMode:(id)json queue:(dispatch_queue_t)sessionQueue;
  // -(void)findFacesInFrame:(UIImage *)image scaleX:(float)scaleX scaleY:(float)scaleY completed:(postRecognitionBlock)completed;
  // +(NSDictionary *)constants;
@end
