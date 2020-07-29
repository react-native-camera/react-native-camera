#import "LabelDetectorManagerMlkit.h"
#import <React/RCTConvert.h>
#if __has_include(<MLKitImageLabeling/MLKitImageLabeling.h>)

@interface LabelDetectorManagerMlkit ()
@property(nonatomic, strong) MLKImageLabeler *imageLabeler;
@property(nonatomic, strong) MLKImageLabelerOptions *options;
@property(nonatomic, assign) float scaleX;
@property(nonatomic, assign) float scaleY;
@end

@implementation LabelDetectorManagerMlkit

- (instancetype)init 
{
  if (self = [super init]) {
    self.options = [[MLKImageLabelerOptions alloc] init];

    self.imageLabeler = [MLKImageLabeler imageLabelerWithOptions:_options];
  }
  return self;
}

- (BOOL)isRealDetector 
{
  return true;
}

- (void)findLabelsInFrame:(UIImage *)uiImage
                  scaleX:(float)scaleX
                  scaleY:(float)scaleY
               completed:(void (^)(NSArray *result))completed 
{
    self.scaleX = scaleX;
    self.scaleY = scaleY;
    MLKVisionImage *image = [[MLKVisionImage alloc] initWithImage:uiImage];
    NSMutableArray *emptyResult = [[NSMutableArray alloc] init];
    [_imageLabeler
     processImage:image
     completion:^(NSArray<MLKImageLabel *> *labels, NSError *error) {
         if (error != nil || labels == nil) {
             completed(emptyResult);
         } else {
             completed([self processLabels:labels]);
         }
     }];
}

- (NSArray *)processLabels:(NSArray *)labels 
{
    NSMutableArray *result = [[NSMutableArray alloc] init];
    for (MLKImageLabel *label in labels) {
        NSMutableDictionary *resultDict =
        [[NSMutableDictionary alloc] initWithCapacity:2];
        [resultDict setObject:label.text forKey:@"text"];
        [resultDict setObject:@(label.confidence) forKey:@"confidence"];
        [result addObject:resultDict];
    }
    return result;
}

@end
#else

@interface LabelDetectorManagerMlkit ()
@end

@implementation LabelDetectorManagerMlkit

- (instancetype)init {
 self = [super init];
 return self;
}

- (BOOL)isRealDetector {
 return false;
}

- (NSArray *)findLabelsInFrame:(UIImage *)image
                    scaleX:(float)scaleX
                    scaleY:(float)scaleY
                    completed:(void (^)(NSArray *result))completed;
{
 NSLog(@"LabelDetector not installed, stub used!");
 NSArray *features = @[ @"Error, Label Detector not installed" ];
 return features;
}

@end
#endif
