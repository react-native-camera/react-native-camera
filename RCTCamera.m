#import "RCTBridge.h"
#import "RCTCamera.h"
#import "RCTCameraManager.h"
#import "RCTLog.h"
#import "RCTUtils.h"
#import "ViewfinderView.h"
#import "UIImage+Resize.h"
#import <AVFoundation/AVFoundation.h>

@implementation RCTCamera

- (void)setAspect:(NSString *)aspect
{
    [(AVCaptureVideoPreviewLayer *)[[self viewfinder] layer] setVideoGravity:aspect];
}

- (void)setType:(NSInteger)camera
{
    if ([[self session] isRunning]) {
        [self changeCamera:camera];
    }
    else {
        [self setPresetCamera:camera];
    }
}

- (void)setOrientation:(NSInteger)orientation
{
    [self changeOrientation:orientation];
}

- (id)init
{
    if ((self = [super init])) {
        [self setViewfinder:[[ViewfinderView alloc] init]];

        [self setSession:[[AVCaptureSession alloc] init]];
        [[self session] setSessionPreset:AVCaptureSessionPresetHigh];

        [[self viewfinder] setSession:[self session]];
        [self addSubview:[self viewfinder]];

        [[self session] startRunning];

        dispatch_queue_t sessionQueue = dispatch_queue_create("cameraManagerQueue", DISPATCH_QUEUE_SERIAL);
        [self setSessionQueue:sessionQueue];

        dispatch_async(sessionQueue, ^{
            NSError *error = nil;

            NSInteger presetCamera = [self presetCamera];

            if ([self presetCamera] == AVCaptureDevicePositionUnspecified) {
                presetCamera = AVCaptureDevicePositionBack;
            }

            AVCaptureDevice *captureDevice = [self deviceWithMediaType:AVMediaTypeVideo preferringPosition:presetCamera];
            AVCaptureDeviceInput *captureDeviceInput = [AVCaptureDeviceInput deviceInputWithDevice:captureDevice error:&error];

            if (error)
            {
                NSLog(@"%@", error);
            }

            if ([[self session] canAddInput:captureDeviceInput])
            {
                [[self session] addInput:captureDeviceInput];
                [self setCaptureDeviceInput:captureDeviceInput];
            }

            AVCaptureStillImageOutput *stillImageOutput = [[AVCaptureStillImageOutput alloc] init];
            if ([[self session] canAddOutput:stillImageOutput])
            {
                [stillImageOutput setOutputSettings:@{AVVideoCodecKey : AVVideoCodecJPEG}];
                [[self session] addOutput:stillImageOutput];
                [self setStillImageOutput:stillImageOutput];
            }

            __weak RCTCamera *weakSelf = self;
            [self setRuntimeErrorHandlingObserver:[[NSNotificationCenter defaultCenter] addObserverForName:AVCaptureSessionRuntimeErrorNotification object:[self session] queue:nil usingBlock:^(NSNotification *note) {
                RCTCamera *strongSelf = weakSelf;
                dispatch_async([strongSelf sessionQueue], ^{
                    // Manually restarting the session since it must have been stopped due to an error.
                    [[strongSelf session] startRunning];
                });
            }]];
        });
    }
    return self;
}

- (NSArray *)reactSubviews
{
    NSArray *subviews = @[[self viewfinder]];
    return subviews;
}

- (void)layoutSubviews
{
    [super layoutSubviews];
    [[self viewfinder] setFrame:[self bounds]];
}

- (void)insertReactSubview:(UIView *)view atIndex:(NSInteger)atIndex
{
    [self insertSubview:view atIndex:atIndex + 1];
    return;
}

- (void)removeReactSubview:(UIView *)subview
{
    [subview removeFromSuperview];
    return;
}

- (void)changeCamera:(NSInteger)camera {
    dispatch_async([self sessionQueue], ^{
        AVCaptureDevice *currentCaptureDevice = [[self captureDeviceInput] device];
        AVCaptureDevicePosition position = (AVCaptureDevicePosition)camera;
        AVCaptureDevice *captureDevice = [self deviceWithMediaType:AVMediaTypeVideo preferringPosition:(AVCaptureDevicePosition)position];

        NSError *error = nil;
        AVCaptureDeviceInput *captureDeviceInput = [AVCaptureDeviceInput deviceInputWithDevice:captureDevice error:&error];

        if (error)
        {
            NSLog(@"%@", error);
        }

        [[self session] beginConfiguration];

        [[self session] removeInput:[self captureDeviceInput]];

        if ([[self session] canAddInput:captureDeviceInput])
        {
            [[NSNotificationCenter defaultCenter] removeObserver:self name:AVCaptureDeviceSubjectAreaDidChangeNotification object:currentCaptureDevice];

            [self setFlashMode:AVCaptureFlashModeAuto forDevice:captureDevice];

            [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(subjectAreaDidChange:) name:AVCaptureDeviceSubjectAreaDidChangeNotification object:captureDevice];
            [[self session] addInput:captureDeviceInput];
            [self setCaptureDeviceInput:captureDeviceInput];
        }
        else
        {
            [[self session] addInput:[self captureDeviceInput]];
        }


        [[self session] commitConfiguration];
    });
}

- (void)changeOrientation:(NSInteger)orientation {
    [[(AVCaptureVideoPreviewLayer *)[[self viewfinder] layer] connection] setVideoOrientation:orientation];
}

- (void)takePicture:(RCTResponseSenderBlock)callback {
    dispatch_async([self sessionQueue], ^{

        // Update the orientation on the still image output video connection before capturing.
        [[[self stillImageOutput] connectionWithMediaType:AVMediaTypeVideo] setVideoOrientation:[[(AVCaptureVideoPreviewLayer *)[[self viewfinder] layer] connection] videoOrientation]];

        // Flash set to Auto for Still Capture
        [self setFlashMode:AVCaptureFlashModeAuto forDevice:[[self captureDeviceInput] device]];

        // Capture a still image.
        [[self stillImageOutput] captureStillImageAsynchronouslyFromConnection:[[self stillImageOutput] connectionWithMediaType:AVMediaTypeVideo] completionHandler:^(CMSampleBufferRef imageDataSampleBuffer, NSError *error) {

            if (imageDataSampleBuffer)
            {
                NSData *imageData = [AVCaptureStillImageOutput jpegStillImageNSDataRepresentation:imageDataSampleBuffer];
                UIImage *image = [UIImage imageWithData:imageData];
                UIImage *rotatedImage = [image resizedImage:CGSizeMake(image.size.width, image.size.height) interpolationQuality:kCGInterpolationDefault];
                NSString *imageBase64 = [UIImageJPEGRepresentation(rotatedImage, 1.0) base64EncodedStringWithOptions:0];
                callback(@[[NSNull null], imageBase64]);
            }
            else {
                callback(@[RCTMakeError([error description], nil, nil)]);
            }
        }];
    });
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


- (void)setFlashMode:(AVCaptureFlashMode)flashMode forDevice:(AVCaptureDevice *)device
{
    if ([device hasFlash] && [device isFlashModeSupported:flashMode])
    {
        NSError *error = nil;
        if ([device lockForConfiguration:&error])
        {
            [device setFlashMode:flashMode];
            [device unlockForConfiguration];
        }
        else
        {
            NSLog(@"%@", error);
        }
    }
}

- (void)subjectAreaDidChange:(NSNotification *)notification
{
    CGPoint devicePoint = CGPointMake(.5, .5);
    [self focusWithMode:AVCaptureFocusModeContinuousAutoFocus exposeWithMode:AVCaptureExposureModeContinuousAutoExposure atDevicePoint:devicePoint monitorSubjectAreaChange:NO];
}

- (void)focusWithMode:(AVCaptureFocusMode)focusMode exposeWithMode:(AVCaptureExposureMode)exposureMode atDevicePoint:(CGPoint)point monitorSubjectAreaChange:(BOOL)monitorSubjectAreaChange
{
    dispatch_async([self sessionQueue], ^{
        AVCaptureDevice *device = [[self captureDeviceInput] device];
        NSError *error = nil;
        if ([device lockForConfiguration:&error])
        {
            if ([device isFocusPointOfInterestSupported] && [device isFocusModeSupported:focusMode])
            {
                [device setFocusMode:focusMode];
                [device setFocusPointOfInterest:point];
            }
            if ([device isExposurePointOfInterestSupported] && [device isExposureModeSupported:exposureMode])
            {
                [device setExposureMode:exposureMode];
                [device setExposurePointOfInterest:point];
            }
            [device setSubjectAreaChangeMonitoringEnabled:monitorSubjectAreaChange];
            [device unlockForConfiguration];
        }
        else
        {
            NSLog(@"%@", error);
        }
    });
}

@end
