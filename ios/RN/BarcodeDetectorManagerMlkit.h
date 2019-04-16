
#import <UIKit/UIKit.h>
#if __has_include(<FirebaseMLVision/FirebaseMLVision.h>)
  #import <FirebaseMLVision/FirebaseMLVision.h>
#endif

@interface BarcodeDetectorManagerMlkit : NSObject
typedef void(^postRecognitionBlock)(NSArray *barcodes);

- (instancetype)init;

-(BOOL)isRealDetector;
-(void)setType:(id)json queue:(dispatch_queue_t)sessionQueue;
-(void)findBarcodesInFrame:(UIImage *)image scaleX:(float)scaleX scaleY:(float)scaleY completed:(postRecognitionBlock)completed;
+(NSDictionary *)constants;

@end 
