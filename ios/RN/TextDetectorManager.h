#if __has_include(<FirebaseMLVision/FirebaseMLVision.h>)
  #import <FirebaseMLVision/FirebaseMLVision.h>

  @interface TextDetectorManager : NSObject
  typedef void(^postRecognitionBlock)(NSArray *textBlocks);

  - (instancetype)init;

  -(BOOL)isRealDetector;
  -(void)findTextBlocksInFrame:(UIImage *)image scaleX:(float)scaleX scaleY:(float) scaleY completed:(postRecognitionBlock)completed;

  @end
#else
  @interface TextDetectorManager : NSObject
  typedef void(^postRecognitionBlock)(NSArray *textBlocks);

  - (instancetype)init;

  -(BOOL)isRealDetector;
  -(NSArray *)findTextBlocksInFrame:(UIImage *)image scaleX:(float)scaleX scaleY:(float) scaleY completed:(postRecognitionBlock)completed;

  @end 
#endif
