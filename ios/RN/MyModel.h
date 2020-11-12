
#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventDispatcher.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTLog.h>
#import "TFLTensorFlowLite.h"
@import TensorFlowLite;

@interface MyModel : NSObject
  
  @property(nonatomic, strong) TFLInterpreter *interpreter;
  - (instancetype)init;
  -(NSArray *)runModelWithFrame:(UIImage *)image scaleX:(float)scaleX scaleY:(float)scaleY ;

  - (NSArray *)runModelWithFrame:(UIImage *)uiImage scaleX:(float)scaleX scaleY:(float)scaleY faces: (NSDictionary *)eventFace ;
  // typedef void(^postRecognitionBlock)(NSArray *faces);
  // -(void)findFacesInFrame:(UIImage *)image scaleX:(float)scaleX scaleY:(float)scaleY completed:(postRecognitionBlock)completed;
  - (NSData *)ImagePreprocess:(UIImage *)faceImage  scaleX:(float)scaleX scaleY:(float)scaleY faces: (NSDictionary *)eventFace    ;
  - (NSData *)preprocessImage:(UIImage *)uiImage    ;
  -(BOOL)isRealVerifier;
  - (void)findFacesInFrame:(UIImage *)uiImage
              scaleX:(float)scaleX
              scaleY:(float)scaleY
              faces:(NSDictionary *)eventFace
              completed:(void (^)(float result))completed;
  // -(void)setTracking:(id)json queue:(dispatch_queue_t)sessionQueue;
  // -(void)setLandmarksMode:(id)json queue:(dispatch_queue_t)sessionQueue;
  // -(void)setPerformanceMode:(id)json queue:(dispatch_queue_t)sessionQueue;
  // -(void)setClassificationMode:(id)json queue:(dispatch_queue_t)sessionQueue;
  // -(void)findFacesInFrame:(UIImage *)image scaleX:(float)scaleX scaleY:(float)scaleY completed:(postRecognitionBlock)completed;
  // +(NSDictionary *)constants;
@end
