#import <Foundation/Foundation.h>
#import "RNDocumentScanner.h"



@interface RNDocumentScanner ()
@end

@implementation RNDocumentScanner {
    CIRectangleFeature *_lastRectBorder;
}

- (BOOL)isRealDetector
{
    return true;
}

- (UIImage *)getDocument:(UIImage *)image
{
    CIImage *cimage = [[CIImage alloc] initWithCGImage:image.CGImage];
    CIRectangleFeature *rectangleFeature = [self biggestRectangleInRectangles:[[self highAccuracyRectangleDetector] featuresInImage:cimage]];
    
    if (rectangleFeature) {
        cimage = [self correctPerspectiveForImage:cimage withFeatures:rectangleFeature];
        
        UIGraphicsBeginImageContext(CGSizeMake(cimage.extent.size.height, cimage.extent.size.width));
        [[UIImage imageWithCIImage:cimage scale:1.0 orientation:UIImageOrientationRight] drawInRect:CGRectMake(0, 0, cimage.extent.size.height, cimage.extent.size.width)];
        image = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
    }
    return image;
}


- (void)findDocumentInFrame:(UIImage *)image
                     scaleX:(float)scaleX
                     scaleY:(float)scaleY
                  completed:(postDetectionBlock)completed
{
    CIImage *cimage = [[CIImage alloc] initWithCGImage:image.CGImage];
    
    _lastRectBorder = [self biggestRectangleInRectangles:[[self highAccuracyRectangleDetector] featuresInImage:cimage]];

    if (0 == (_lastRectBorder.topRight.x - _lastRectBorder.topLeft.x)) {
        completed(nil);
    } else {
        completed(@{
                    @"tl": @{@"x": [NSNumber numberWithFloat:_lastRectBorder.topLeft.x * scaleX],
                             @"y": [NSNumber numberWithFloat:(image.size.height - _lastRectBorder.topLeft.y) * scaleY]},
                    @"tr": @{@"x": [NSNumber numberWithFloat:_lastRectBorder.topRight.x * scaleX],
                             @"y": [NSNumber numberWithFloat:(image.size.height - _lastRectBorder.topRight.y) * scaleY]},
                    @"bl": @{@"x": [NSNumber numberWithFloat:_lastRectBorder.bottomLeft.x * scaleX],
                             @"y": [NSNumber numberWithFloat:(image.size.height - _lastRectBorder.bottomLeft.y) * scaleY]},
                    @"br": @{@"x": [NSNumber numberWithFloat:_lastRectBorder.bottomRight.x * scaleX],
                             @"y": [NSNumber numberWithFloat:(image.size.height - _lastRectBorder.bottomRight.y) * scaleY]},
                    @"x": [NSNumber numberWithFloat:_lastRectBorder.topLeft.x * scaleX],
                    @"y": [NSNumber numberWithFloat:(image.size.height - _lastRectBorder.topLeft.y) * scaleY],
                    @"width": [NSNumber numberWithFloat:(_lastRectBorder.topRight.x - _lastRectBorder.topLeft.x) * scaleX],
                    @"height": [NSNumber numberWithFloat:(_lastRectBorder.topLeft.y - _lastRectBorder.bottomLeft.y) * scaleY]});
    }

}

- (CIRectangleFeature *)biggestRectangleInRectangles:(NSArray *)rectangles
{
    if (![rectangles count]) return nil;
    
    float halfPerimiterValue = 0;
    
    CIRectangleFeature *biggestRectangle = [rectangles firstObject];
    
    for (CIRectangleFeature *rect in rectangles)
    {
        CGPoint p1 = rect.topLeft;
        CGPoint p2 = rect.topRight;
        CGFloat width = hypotf(p1.x - p2.x, p1.y - p2.y);
        
        CGPoint p3 = rect.topLeft;
        CGPoint p4 = rect.bottomLeft;
        CGFloat height = hypotf(p3.x - p4.x, p3.y - p4.y);
        
        CGFloat currentHalfPerimiterValue = height + width;
        
        if (halfPerimiterValue < currentHalfPerimiterValue)
        {
            halfPerimiterValue = currentHalfPerimiterValue;
            biggestRectangle = rect;
        }
    }
    return biggestRectangle;
}

- (CIDetector *)highAccuracyRectangleDetector
{
    static CIDetector *detector = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^
                  {
                      detector = [CIDetector detectorOfType:CIDetectorTypeRectangle context:nil options:@{CIDetectorAccuracy : CIDetectorAccuracyHigh, CIDetectorReturnSubFeatures: @(YES) }];
                  });
    return detector;
}

- (CIImage *)correctPerspectiveForImage:(CIImage *)image withFeatures:(CIRectangleFeature *)rectangleFeature
{
    NSMutableDictionary *rectangleCoordinates = [NSMutableDictionary new];
    CGPoint newLeft = CGPointMake(rectangleFeature.topLeft.x, rectangleFeature.topLeft.y);
    CGPoint newRight = CGPointMake(rectangleFeature.topRight.x, rectangleFeature.topRight.y);
    CGPoint newBottomLeft = CGPointMake(rectangleFeature.bottomLeft.x, rectangleFeature.bottomLeft.y);
    CGPoint newBottomRight = CGPointMake(rectangleFeature.bottomRight.x, rectangleFeature.bottomRight.y);
    
    
    rectangleCoordinates[@"inputTopLeft"] = [CIVector vectorWithCGPoint:newLeft];
    rectangleCoordinates[@"inputTopRight"] = [CIVector vectorWithCGPoint:newRight];
    rectangleCoordinates[@"inputBottomLeft"] = [CIVector vectorWithCGPoint:newBottomLeft];
    rectangleCoordinates[@"inputBottomRight"] = [CIVector vectorWithCGPoint:newBottomRight];
    return [image imageByApplyingFilter:@"CIPerspectiveCorrection" withInputParameters:rectangleCoordinates];
}

@end
