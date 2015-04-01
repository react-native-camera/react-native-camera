#import "RCTCamera.h"
#import "RCTLog.h"
#import "ViewfinderView.h"
#import <AVFoundation/AVFoundation.h>

@implementation RCTCamera

- (void)setSrc:(NSString *)source
{
    
}

- (id)init
{
    if ((self = [super init])) {

        _viewfinder = [[ViewfinderView alloc] init];
        AVCaptureSession *session = [[AVCaptureSession alloc] init];

        [[self viewfinder] setSession:session];
        [self addSubview:_viewfinder];
        
        NSError *error = nil;
        
        NSArray *devices = [AVCaptureDevice devicesWithMediaType:AVMediaTypeVideo];
        AVCaptureDevice *captureDevice = [devices firstObject];
        
        AVCaptureDevicePosition position = AVCaptureDevicePositionBack;
        AVCaptureVideoOrientation interfaceOrientation = (AVCaptureVideoOrientation)UIInterfaceOrientationMaskLandscape;
        
        for (AVCaptureDevice *device in devices)
        {
            if ([device position] == position)
            {
                captureDevice = device;
                break;
            }
        }
        
        AVCaptureDeviceInput *captureDeviceInput = [AVCaptureDeviceInput deviceInputWithDevice:captureDevice error:&error];
        
        if (error)
        {
            NSLog(@"%@", error);
        }
        
        if ([session canAddInput:captureDeviceInput])
        {
            [session addInput:captureDeviceInput];
            
//            [[(AVCaptureVideoPreviewLayer *)[[self viewfinder] layer] connection] setVideoOrientation:(AVCaptureVideoOrientation)interfaceOrientation];
            [(AVCaptureVideoPreviewLayer *)[[self viewfinder] layer] setVideoGravity:AVLayerVideoGravityResizeAspectFill];
        }
        
        [session startRunning];

    }
    return self;
}

- (NSArray *)reactSubviews
{
    NSArray *subviews = @[_viewfinder];
    return subviews;
}

- (void)layoutSubviews
{
    [super layoutSubviews];
    _viewfinder.frame = self.bounds;
}

- (void)insertReactSubview:(UIView *)view atIndex:(NSInteger)atIndex
{
    RCTLogError(@"Camera does not support subviews");
    return;
}

- (void)removeReactSubview:(UIView *)subview
{
    RCTLogError(@"Camera does not support subviews");
    return;
}

@end
