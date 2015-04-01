#import "RCTCamera.h"
#import "RCTLog.h"
#import "ViewfinderView.h"
#import <AVFoundation/AVFoundation.h>

@implementation RCTCamera

- (void)setAspect:(NSString *)aspect
{
    [(AVCaptureVideoPreviewLayer *)[[self viewfinder] layer] setVideoGravity:aspect];
}

- (void)setCamera:(NSInteger)camera
{
//    AVCaptureDevice *currentVideoDevice = [_captureDeviceInput device];
    AVCaptureDevice *videoDevice = [self deviceWithMediaType:AVMediaTypeVideo preferringPosition:(AVCaptureDevicePosition)camera];
    AVCaptureDeviceInput *videoDeviceInput = [AVCaptureDeviceInput deviceInputWithDevice:videoDevice error:nil];

    [[self session] beginConfiguration];

    [[self session] removeInput:[self captureDeviceInput]];
    if ([[self session] canAddInput:videoDeviceInput])
    {
//        [[NSNotificationCenter defaultCenter] removeObserver:self name:AVCaptureDeviceSubjectAreaDidChangeNotification object:currentVideoDevice];
//
//        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(subjectAreaDidChange:) name:AVCaptureDeviceSubjectAreaDidChangeNotification object:videoDevice];
//
        [[self session] addInput:videoDeviceInput];
        [self setCaptureDeviceInput:videoDeviceInput];
    }
    else
    {
        [[self session] addInput:_captureDeviceInput];
    }

    [[self session] commitConfiguration];
}

- (void)setOrientation:(NSInteger)orientation
{
    [[(AVCaptureVideoPreviewLayer *)[[self viewfinder] layer] connection] setVideoOrientation:orientation];
}

- (id)init
{
    if ((self = [super init])) {

        _viewfinder = [[ViewfinderView alloc] init];
        AVCaptureSession *session = [[AVCaptureSession alloc] init];

        [[self viewfinder] setSession:session];
        [self addSubview:_viewfinder];

        NSError *error = nil;

        AVCaptureDevice *captureDevice = [self deviceWithMediaType:AVMediaTypeVideo preferringPosition:AVCaptureDevicePositionBack];

        _captureDeviceInput = [AVCaptureDeviceInput deviceInputWithDevice:captureDevice error:&error];

        if (error)
        {
            NSLog(@"%@", error);
        }

        if ([session canAddInput:_captureDeviceInput])
        {
            [session addInput:_captureDeviceInput];

            [[(AVCaptureVideoPreviewLayer *)[[self viewfinder] layer] connection] setVideoOrientation:AVCaptureVideoOrientationPortrait];
            [(AVCaptureVideoPreviewLayer *)[[self viewfinder] layer] setVideoGravity:AVLayerVideoGravityResizeAspectFill];
        }

        [session startRunning];

    }
    return self;
}

- (AVCaptureDevice *)deviceWithMediaType:(NSString *)mediaType preferringPosition:(AVCaptureDevicePosition)position
{
    NSArray *devices = [AVCaptureDevice devicesWithMediaType:mediaType];
    AVCaptureDevice *captureDevice = [devices firstObject];

    for (AVCaptureDevice *device in devices)
    {
        if ([device position] == position)
        {
            captureDevice = device;
            break;
        }
    }

    return captureDevice;
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
