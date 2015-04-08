#import "RCTCameraManager.h"
#import "RCTCamera.h"
#import "RCTBridge.h"
#import "RCTUtils.h"
#import "UIView+React.h"
#import <AVFoundation/AVFoundation.h>

@implementation RCTCameraManager

- (UIView *)view
{
    [self setCurrentCamera:[[RCTCamera alloc] init]];
    return _currentCamera;
}

RCT_EXPORT_VIEW_PROPERTY(aspect, NSString);
RCT_EXPORT_VIEW_PROPERTY(type, NSInteger);
RCT_EXPORT_VIEW_PROPERTY(orientation, NSInteger);

- (NSDictionary *)constantsToExport
{
    return @{
      @"aspects": @{
        @"Stretch": AVLayerVideoGravityResize,
        @"Fit": AVLayerVideoGravityResizeAspect,
        @"Fill": AVLayerVideoGravityResizeAspectFill
      },
      @"cameras": @{
        @"Front": @(AVCaptureDevicePositionFront),
        @"Back": @(AVCaptureDevicePositionBack)
      },
      @"orientations": @{
        @"LandscapeLeft": @(AVCaptureVideoOrientationLandscapeLeft),
        @"LandscapeRight": @(AVCaptureVideoOrientationLandscapeRight),
        @"Portrait": @(AVCaptureVideoOrientationPortrait),
        @"PortraitUpsideDown": @(AVCaptureVideoOrientationPortraitUpsideDown)
      }
    };
}

- (void)checkDeviceAuthorizationStatus:(RCTResponseSenderBlock) callback {
    RCT_EXPORT();
    NSString *mediaType = AVMediaTypeVideo;

    [AVCaptureDevice requestAccessForMediaType:mediaType completionHandler:^(BOOL granted) {
        callback(@[[NSNull null], @(granted)]);
    }];
}


- (void)switchCamera:(NSInteger)camera
{
    RCT_EXPORT();
    [_currentCamera changeCamera:camera];
}

- (void)setOrientation:(NSInteger)orientation
{
    RCT_EXPORT();
    [_currentCamera changeOrientation:orientation];
}

- (void)takePicture:(RCTResponseSenderBlock) callback {
    RCT_EXPORT();
    [_currentCamera takePicture:callback];
}


@end
