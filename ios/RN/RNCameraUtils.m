//
//  RNCameraUtils.m
//  RCTCamera
//
//  Created by Joao Guilherme Daros Fidelis on 19/01/18.
//

#import "RNCameraUtils.h"

@implementation RNCameraUtils

# pragma mark - Camera utilities

+ (AVCaptureDevice *)deviceWithMediaType:(AVMediaType)mediaType preferringPosition:(AVCaptureDevicePosition)position
{
    NSArray *devices = [AVCaptureDevice devicesWithMediaType:mediaType];
    AVCaptureDevice *captureDevice = [devices firstObject];
    
    for (AVCaptureDevice *device in devices) {
        if ([device position] == position) {
            captureDevice = device;
            break;
        }
    }
    
    return captureDevice;
}

+ (AVCaptureDevice *)deviceWithCameraId:(NSString *)cameraId
{
    AVCaptureDevice *device = [AVCaptureDevice deviceWithUniqueID:cameraId];
    return device;
}

# pragma mark - Enum conversion

+ (AVCaptureVideoOrientation)videoOrientationForInterfaceOrientation:(UIInterfaceOrientation)orientation
{
    switch (orientation) {
        case UIInterfaceOrientationPortrait:
            return AVCaptureVideoOrientationPortrait;
        case UIInterfaceOrientationPortraitUpsideDown:
            return AVCaptureVideoOrientationPortraitUpsideDown;
        case UIInterfaceOrientationLandscapeRight:
            return AVCaptureVideoOrientationLandscapeRight;
        case UIInterfaceOrientationLandscapeLeft:
            return AVCaptureVideoOrientationLandscapeLeft;
        default:
            return 0;
    }
}

+ (AVCaptureVideoOrientation)videoOrientationForDeviceOrientation:(UIDeviceOrientation)orientation
{
    switch (orientation) {
        case UIDeviceOrientationPortrait:
            return AVCaptureVideoOrientationPortrait;
        case UIDeviceOrientationPortraitUpsideDown:
            return AVCaptureVideoOrientationPortraitUpsideDown;
        case UIDeviceOrientationLandscapeLeft:
            return AVCaptureVideoOrientationLandscapeRight;
        case UIDeviceOrientationLandscapeRight:
            return AVCaptureVideoOrientationLandscapeLeft;
        default:
            return AVCaptureVideoOrientationPortrait;
    }
}

+ (float)temperatureForWhiteBalance:(RNCameraWhiteBalance)whiteBalance
{
    switch (whiteBalance) {
        case RNCameraWhiteBalanceSunny: default:
            return 5200;
        case RNCameraWhiteBalanceCloudy:
            return 6000;
        case RNCameraWhiteBalanceShadow:
            return 7000;
        case RNCameraWhiteBalanceIncandescent:
            return 3000;
        case RNCameraWhiteBalanceFluorescent:
            return 4200;
    }
}

+ (NSString *)captureSessionPresetForVideoResolution:(RNCameraVideoResolution)resolution
{
    switch (resolution) {
        case RNCameraVideo2160p:
            return AVCaptureSessionPreset3840x2160;
        case RNCameraVideo1080p:
            return AVCaptureSessionPreset1920x1080;
        case RNCameraVideo720p:
            return AVCaptureSessionPreset1280x720;
        case RNCameraVideo4x3:
            return AVCaptureSessionPreset640x480;
        case RNCameraVideo288p:
            return AVCaptureSessionPreset352x288;
        default:
            return AVCaptureSessionPresetHigh;
    }
}

+ (UIImage *)convertBufferToUIImage:(CMSampleBufferRef)sampleBuffer previewSize:(CGSize)previewSize position:(NSInteger)position
{
    CVImageBufferRef imageBuffer = CMSampleBufferGetImageBuffer(sampleBuffer);
    CIImage *ciImage = [CIImage imageWithCVPixelBuffer:imageBuffer];
    // set correct orientation
    __block UIInterfaceOrientation orientation;
    dispatch_sync(dispatch_get_main_queue(), ^{
        orientation = [[UIApplication sharedApplication] statusBarOrientation];
    });
    UIInterfaceOrientation curOrientation = orientation;
    NSInteger orientationToApply = 1;
    BOOL isBackCamera = position == 1;
    if (curOrientation == UIInterfaceOrientationLandscapeLeft){
        orientationToApply = isBackCamera ? kCGImagePropertyOrientationDown : kCGImagePropertyOrientationUpMirrored;
    } else if (curOrientation == UIInterfaceOrientationLandscapeRight){
        orientationToApply = isBackCamera ? kCGImagePropertyOrientationUp  : kCGImagePropertyOrientationDownMirrored;
    } else if (curOrientation == UIInterfaceOrientationPortrait){
        orientationToApply = isBackCamera ? kCGImagePropertyOrientationRight : kCGImagePropertyOrientationLeftMirrored;
    } else if (curOrientation == UIInterfaceOrientationPortraitUpsideDown){
        orientationToApply = isBackCamera ? kCGImagePropertyOrientationLeft : kCGImagePropertyOrientationRightMirrored;
    }
    ciImage = [ciImage imageByApplyingOrientation:orientationToApply];

    // scale down CIImage
    float bufferWidth = CVPixelBufferGetWidth(imageBuffer);
    float bufferHeight = CVPixelBufferGetHeight(imageBuffer);
    float scale = scale = bufferHeight>bufferWidth ? 720 / bufferWidth : 720 / bufferHeight;;
    if (position == 1) {
        scale = bufferHeight>bufferWidth ? 400 / bufferWidth : 400 / bufferHeight;
    }
    CIFilter* scaleFilter = [CIFilter filterWithName:@"CILanczosScaleTransform"];
    [scaleFilter setValue:ciImage forKey:kCIInputImageKey];
    [scaleFilter setValue:@(scale) forKey:kCIInputScaleKey];
    [scaleFilter setValue:@(1) forKey:kCIInputAspectRatioKey];
    ciImage = scaleFilter.outputImage;

    // convert to UIImage and crop to preview aspect ratio
    NSDictionary *contextOptions = @{kCIContextUseSoftwareRenderer : @(false)};
    CIContext *temporaryContext = [CIContext contextWithOptions:contextOptions];
    CGImageRef videoImage;
    CGRect boundingRect;
    if (curOrientation == UIInterfaceOrientationLandscapeLeft || curOrientation == UIInterfaceOrientationLandscapeRight) {
        boundingRect = CGRectMake(0, 0, bufferWidth*scale, bufferHeight*scale);
    } else {
        boundingRect = CGRectMake(0, 0, bufferHeight*scale, bufferWidth*scale);
    }
    videoImage = [temporaryContext createCGImage:ciImage fromRect:boundingRect];
    CGRect croppedSize = AVMakeRectWithAspectRatioInsideRect(previewSize, boundingRect);
    CGImageRef croppedCGImage = CGImageCreateWithImageInRect(videoImage, croppedSize);
    UIImage *image = [[UIImage alloc] initWithCGImage:croppedCGImage];
    CGImageRelease(videoImage);
    CGImageRelease(croppedCGImage);
    return image;
}

@end

