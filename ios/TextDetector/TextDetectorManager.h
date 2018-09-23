#if __has_include(<GoogleMobileVision/GoogleMobileVision.h>)
#import <GoogleMobileVision/GoogleMobileVision.h>

@interface TextDetectorManager : NSObject

- (instancetype)init;

-(BOOL)isRealDetector;
-(NSArray *)findTextBlocksInFrame:(UIImage *)image scaleX:(float)scaleX scaleY:(float) scaleY;

@end
#endif