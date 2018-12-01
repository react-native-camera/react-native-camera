#import "TextDetectorManagerStub.h"
#import <React/RCTLog.h>

@interface TextDetectorManager ()
@end

@implementation TextDetectorManager

- (instancetype)init
{
  self = [super init];
  return self;
}

-(BOOL)isRealDetector
{
  return false;
}

-(NSArray *)findTextBlocksInFrame:(UIImage *)image scaleX:(float)scaleX scaleY:(float) scaleY;
{
  RCTLogWarn(@"TextDetector not installed, stub used!");
  NSArray *features = @[@"Error, Text Detector not installed"];
  return features;
}

@end