//
//  RNFaceDetectorUtils.m
//  RCTCamera
//
//  Created by Joao Guilherme Daros Fidelis on 21/01/18.
//

#if __has_include(<GoogleMobileVision/GoogleMobileVision.h>)
#import "RNCameraUtils.h"
#import "RNFaceDetectorUtils.h"
#import "RNFaceDetectorPointTransformCalculator.h"

NSString *const RNGMVDataOutputWidthKey = @"Width";
NSString *const RNGMVDataOutputHeightKey = @"Height";

@implementation RNFaceDetectorUtils

+ (NSDictionary *)constantsToExport
{
    return @{
             @"Mode" : @{
                     @"fast" : @(RNFaceDetectionFastMode),
                     @"accurate" : @(RNFaceDetectionAccurateMode)
                     },
             @"Landmarks" : @{
                     @"all" : @(RNFaceDetectAllLandmarks),
                     @"none" : @(RNFaceDetectNoLandmarks)
                     },
             @"Classifications" : @{
                     @"all" : @(RNFaceRunAllClassifications),
                     @"none" : @(RNFaceRunNoClassifications)
                     }
             };
}

# pragma mark - GMVDataOutput transformations

+ (CGAffineTransform)transformFromDeviceVideoOrientation:(AVCaptureVideoOrientation)deviceVideoOrientation toInterfaceVideoOrientation:(AVCaptureVideoOrientation)interfaceVideoOrientation videoWidth:(NSNumber *)width videoHeight:(NSNumber *)height
{
    RNFaceDetectorPointTransformCalculator *calculator = [[RNFaceDetectorPointTransformCalculator alloc] initToTransformFromOrientation:deviceVideoOrientation toOrientation:interfaceVideoOrientation forVideoWidth:[width floatValue] andVideoHeight:[height floatValue]];
    return [calculator transform];
}

// Normally we would use `dataOutput.xScale`, `.yScale` and `.offset`.
// Unfortunately, it turns out that using these attributes results in different results
// on iPhone {6, 7} and iPhone 5S. On newer iPhones the transform works properly,
// whereas on iPhone 5S the scale is too big (~0.7, while it should be ~0.4) and the offset
// moves the face points away. This workaround (using screen + orientation + video resolution
// to calculate proper scale) has been proven to work all three devices.
+ (CGAffineTransform)transformFromDeviceOutput:(GMVDataOutput *)dataOutput withInterfaceOrientation:(AVCaptureVideoOrientation)interfaceVideoOrientation
{
    UIScreen *mainScreen = [UIScreen mainScreen];
    BOOL interfaceIsLandscape = interfaceVideoOrientation == AVCaptureVideoOrientationLandscapeLeft || interfaceVideoOrientation == AVCaptureVideoOrientationLandscapeRight;
    CGFloat interfaceWidth = interfaceIsLandscape ? mainScreen.bounds.size.height : mainScreen.bounds.size.width;
    CGFloat interfaceHeight = interfaceIsLandscape ? mainScreen.bounds.size.width : mainScreen.bounds.size.height;
    CGFloat xScale = interfaceWidth / [(NSNumber *)dataOutput.videoSettings[RNGMVDataOutputHeightKey] floatValue];
    CGFloat yScale = interfaceHeight / [(NSNumber *)dataOutput.videoSettings[RNGMVDataOutputWidthKey] floatValue];
    CGAffineTransform dataOutputTransform = CGAffineTransformIdentity;
    dataOutputTransform = CGAffineTransformScale(dataOutputTransform, xScale, yScale);
    return dataOutputTransform;
}

+ (CGAffineTransform)transformFromDeviceOutput:(GMVDataOutput *)dataOutput toInterfaceVideoOrientation:(AVCaptureVideoOrientation)interfaceVideoOrientation
{
    UIDeviceOrientation currentDeviceOrientation = [[UIDevice currentDevice] orientation];
    AVCaptureVideoOrientation deviceVideoOrientation = [RNCameraUtils videoOrientationForDeviceOrientation:currentDeviceOrientation];
    
    NSNumber *videoWidth = dataOutput.videoSettings[RNGMVDataOutputWidthKey];
    NSNumber *videoHeight = dataOutput.videoSettings[RNGMVDataOutputHeightKey];
    
    CGAffineTransform interfaceTransform = [self transformFromDeviceVideoOrientation:deviceVideoOrientation toInterfaceVideoOrientation:interfaceVideoOrientation videoWidth:videoWidth videoHeight:videoHeight];
    
    CGAffineTransform dataOutputTransform = [self transformFromDeviceOutput:dataOutput withInterfaceOrientation:interfaceVideoOrientation];
    
    return CGAffineTransformConcat(interfaceTransform, dataOutputTransform);
}

@end
#endif
