
#import <UIKit/UIKit.h>
#if __has_include(<MLKitImageLabeling/MLKitImageLabeling.h>)
  #import <MLKitVision/MLKitVision.h>
  #import <MLKitImageLabeling/MLKitImageLabeling.h>
  #import <MLKitImageLabelingCommon/MLKitImageLabelingCommon.h>
#endif

@interface LabelDetectorManagerMlkit : NSObject
typedef void(^postRecognitionBlock)(NSArray *labels);

- (instancetype)init;

-(BOOL)isRealDetector;
-(void)findLabelsInFrame:(UIImage *)image scaleX:(float)scaleX scaleY:(float)scaleY completed:(postRecognitionBlock)completed;

@end 
