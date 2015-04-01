#import "RCTCameraManager.h"
#import "RCTCamera.h"
#import "RCTBridge.h"
#import <AVFoundation/AVFoundation.h>

@implementation RCTCameraManager

@synthesize bridge = _bridge;

- (UIView *)view
{
    return [[RCTCamera alloc] init];
}

RCT_EXPORT_VIEW_PROPERTY(orientation, NSInteger);

- (void)checkDeviceAuthorizationStatus:(RCTResponseSenderBlock) callback
{
    RCT_EXPORT();
    NSString *mediaType = AVMediaTypeVideo;
    
    [AVCaptureDevice requestAccessForMediaType:mediaType completionHandler:^(BOOL granted) {
        callback(@[[NSNull null], @(granted)]);
    }];
}

@end
