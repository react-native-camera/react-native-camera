#import "TextDetectorManager.h"
#if __has_include(<FirebaseMLVision/FirebaseMLVision.h>)

@interface TextDetectorManager ()
@property(nonatomic, strong) FIRVisionTextRecognizer *textRecognizer;
@property(nonatomic, assign) float scaleX;
@property(nonatomic, assign) float scaleY;
@end

@implementation TextDetectorManager

- (instancetype)init
{
  if (self = [super init]) {
    NSLog(@"init text detector");
    FIRVision *vision = [FIRVision vision];
    self.textRecognizer = [vision onDeviceTextRecognizer];
  }
  return self;
}

-(BOOL)isRealDetector
{
  return true;
}

- (void)findTextBlocksInFrame:(UIImage *)uiImage scaleX:(float)scaleX scaleY:(float) scaleY completed: (void (^)(NSArray * result)) completed
{
    self.scaleX = scaleX;
    self.scaleY = scaleY;
    FIRVisionImage *image = [[FIRVisionImage alloc] initWithImage:uiImage];
    NSMutableArray *textBlocks = [[NSMutableArray alloc] init];
    [_textRecognizer processImage:image
                       completion:^(FIRVisionText *_Nullable result,
                                    NSError *_Nullable error) {
                           if (error != nil || result == nil) {
                               completed(textBlocks);
                           } else {
                           completed([self processBlocks:result.blocks]);
                           }
                       }];
}

- (NSArray *)processBlocks:(NSArray *)features
{
  NSMutableArray *textBlocks = [[NSMutableArray alloc] init];
    for (FIRVisionTextBlock *textBlock in features) {
        NSDictionary *textBlockDict = 
        @{@"type": @"block", @"value" : textBlock.text, @"bounds" : [self processBounds:textBlock.frame], @"components" : [self processLine:textBlock.lines]};
        [textBlocks addObject:textBlockDict];
  }
  return textBlocks;
}

-(NSArray *)processLine:(NSArray *)lines
{
  NSMutableArray *lineBlocks = [[NSMutableArray alloc] init];
  for (FIRVisionTextLine *textLine in lines) {
        NSDictionary *textLineDict = 
        @{@"type": @"line", @"value" : textLine.text, @"bounds" : [self processBounds:textLine.frame], @"components" : [self processElement:textLine.elements]};
        [lineBlocks addObject:textLineDict];
        }
    return lineBlocks;
}

-(NSArray *)processElement:(NSArray *)elements 
{
  NSMutableArray *elementBlocks = [[NSMutableArray alloc] init];
  for (FIRVisionTextElement *textElement in elements) {
        NSDictionary *textElementDict = 
        @{@"type": @"element", @"value" : textElement.text, @"bounds" : [self processBounds:textElement.frame]};
        [elementBlocks addObject:textElementDict];
        }
    return elementBlocks;
}

-(NSDictionary *)processBounds:(CGRect)bounds 
{
  float width = bounds.size.width * _scaleX;
  float height = bounds.size.height * _scaleY;
  float originX = bounds.origin.x * _scaleX;
  float originY = bounds.origin.y * _scaleY;
  NSDictionary *boundsDict =
  @{
    @"size" : 
              @{
                @"width" : @(width), 
                @"height" : @(height)
                }, 
    @"origin" : 
              @{
                @"x" : @(originX),
                @"y" : @(originY)
                }
    };
  return boundsDict;
}

@end
#else
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
#endif
