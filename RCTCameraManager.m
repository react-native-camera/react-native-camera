#import "RCTCameraManager.h"
#import "RCTCamera.h"
#import "RCTBridge.h"
#import "RCTUtils.h"
#import <AVFoundation/AVFoundation.h>

static void * CapturingStillImageContext = &CapturingStillImageContext;
static void * RecordingContext = &RecordingContext;
static void * SessionRunningAndDeviceAuthorizedContext = &SessionRunningAndDeviceAuthorizedContext;

@implementation RCTCameraManager

+ (id)sharedManager {
    static RCTCameraManager *sharedCameraManager = nil;
    @synchronized(self) {
        if (sharedCameraManager == nil)
            sharedCameraManager = [[self alloc] init];
    }
    return sharedCameraManager;
}

@synthesize bridge = _bridge;

- (UIView *)view
{
    [self setCurrentCamera:[[RCTCamera alloc] init]];
    return _currentCamera;
}

RCT_EXPORT_VIEW_PROPERTY(aspect, NSString);
RCT_EXPORT_VIEW_PROPERTY(camera, NSInteger);
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

- (id)init {
    if ((self = [super init])) {
        [self setSession:[[AVCaptureSession alloc] init]];
        [[self session] setSessionPreset:AVCaptureSessionPresetHigh];

        dispatch_queue_t sessionQueue = dispatch_queue_create("cameraManagerQueue", DISPATCH_QUEUE_SERIAL);
        [self setSessionQueue:sessionQueue];

        dispatch_async(sessionQueue, ^{
            NSError *error = nil;

            AVCaptureDevice *captureDevice = [self deviceWithMediaType:AVMediaTypeVideo preferringPosition:AVCaptureDevicePositionBack];
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

            [[self session] startRunning];
        });
    }
    return self;
}

- (void)checkDeviceAuthorizationStatus:(RCTResponseSenderBlock) callback {
    RCT_EXPORT();
    NSString *mediaType = AVMediaTypeVideo;

    [AVCaptureDevice requestAccessForMediaType:mediaType completionHandler:^(BOOL granted) {
        callback(@[[NSNull null], @(granted)]);
    }];
}


- (void)setCamera:(NSInteger)camera
{
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

- (void)setOrientation:(NSInteger)orientation
{
    [[(AVCaptureVideoPreviewLayer *)[[[self currentCamera] viewfinder] layer] connection] setVideoOrientation:orientation];
}

- (void)takePicture:(RCTResponseSenderBlock) callback {
    RCT_EXPORT();
    dispatch_async([self sessionQueue], ^{
        // Update the orientation on the still image output video connection before capturing.
        [[[self stillImageOutput] connectionWithMediaType:AVMediaTypeVideo] setVideoOrientation:[[(AVCaptureVideoPreviewLayer *)[[[self currentCamera] viewfinder] layer] connection] videoOrientation]];

        // Flash set to Auto for Still Capture
        [self setFlashMode:AVCaptureFlashModeAuto forDevice:[[self captureDeviceInput] device]];

        // Capture a still image.
        [[self stillImageOutput] captureStillImageAsynchronouslyFromConnection:[[self stillImageOutput] connectionWithMediaType:AVMediaTypeVideo] completionHandler:^(CMSampleBufferRef imageDataSampleBuffer, NSError *error) {

            if (imageDataSampleBuffer)
            {
                NSData *imageData = [AVCaptureStillImageOutput jpegStillImageNSDataRepresentation:imageDataSampleBuffer];
                UIImage *image = [[UIImage alloc] initWithData:imageData];
                NSString *imageBase64 = [UIImageJPEGRepresentation(image, 1.0) base64EncodedStringWithOptions:0];
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
