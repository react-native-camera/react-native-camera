#if __has_include(<GoogleMobileVision/GoogleMobileVision.h>)
#import "TextDetectorManager.h"

@interface TextDetectorManager ()
@property(nonatomic, strong) GMVDetector *textDetector;
@property(nonatomic, assign) float scaleX;
@property(nonatomic, assign) float scaleY;
@end

@implementation TextDetectorManager

- (instancetype)init
{
  if (self = [super init]) {
  self.textDetector = [GMVDetector detectorOfType:GMVDetectorTypeText options:nil];
  }
  return self;
}

-(BOOL)isRealDetector
{
  return true;
}

- (NSArray *)findTextBlocksInFrame:(UIImage *)image scaleX:(float)scaleX scaleY:(float) scaleY
{
  self.scaleX = scaleX;
  self.scaleY = scaleY;
  NSArray<GMVTextBlockFeature *> *features =
        [self.textDetector featuresInImage:image options:nil];
   NSArray *textBlocks = [self processFeature:features];
  return textBlocks;
}

- (NSArray *)processFeature:(NSArray *)features
{
  NSMutableArray *textBlocks = [[NSMutableArray alloc] init];
    for (GMVTextBlockFeature *textBlock in features) {
        NSDictionary *textBlockDict = 
        @{@"type": @"block", @"value" : textBlock.value, @"bounds" : [self processBounds:textBlock.bounds], @"components" : [self processLine:textBlock.lines]};
        [textBlocks addObject:textBlockDict];
  }
  return textBlocks;
}

-(NSArray *)processLine:(NSArray *)lines
{
  NSMutableArray *lineBlocks = [[NSMutableArray alloc] init];
  for (GMVTextLineFeature *textLine in lines) {
        NSDictionary *textLineDict = 
        @{@"type": @"line", @"value" : textLine.value, @"bounds" : [self processBounds:textLine.bounds], @"components" : [self processElement:textLine.elements]};
        [lineBlocks addObject:textLineDict];
        }
    return lineBlocks;
}

-(NSArray *)processElement:(NSArray *)elements 
{
  NSMutableArray *elementBlocks = [[NSMutableArray alloc] init];
  for (GMVTextElementFeature *textElement in elements) {
        NSDictionary *textElementDict = 
        @{@"type": @"element", @"value" : textElement.value, @"bounds" : [self processBounds:textElement.bounds]};
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

#endif