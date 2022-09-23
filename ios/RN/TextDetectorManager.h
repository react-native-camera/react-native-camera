#if __has_include(<MLKitTextRecognition/MLKitTextRecognition.h>)
  @import MLKitTextRecognition;
@import MLKitVision;
//add all imports
@import MLKitTextRecognitionCommon;
@import MLKitVision;

#endif
  @interface TextDetectorManager : NSObject
  typedef void(^postRecognitionBlock)(NSArray *textBlocks);

  - (instancetype)init;

  -(BOOL)isRealDetector;
  -(void)findTextBlocksInFrame:(UIImage *)image scaleX:(float)scaleX scaleY:(float)scaleY completed:(postRecognitionBlock)completed;

  @end
