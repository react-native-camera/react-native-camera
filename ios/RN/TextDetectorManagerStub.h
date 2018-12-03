@interface TextDetectorManager : NSObject

- (instancetype)init;

-(BOOL)isRealDetector;
-(NSArray *)findTextBlocksInFrame:(UIImage *)image scaleX:(float)scaleX scaleY:(float) scaleY;

@end