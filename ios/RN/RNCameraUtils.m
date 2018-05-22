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

@end

