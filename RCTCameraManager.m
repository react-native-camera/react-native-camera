#import "RCTCameraManager.h"
#import "RCTCamera.h"
#import "RCTBridge.h"
#import "RCTUtils.h"
#import <AVFoundation/AVFoundation.h>

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
        [_session setSessionPreset:AVCaptureSessionPresetHigh];

        NSError *error = nil;

        AVCaptureDevice *captureDevice = [self deviceWithMediaType:AVMediaTypeVideo preferringPosition:AVCaptureDevicePositionBack];

        _captureDeviceInput = [AVCaptureDeviceInput deviceInputWithDevice:captureDevice error:&error];


        if (error)
        {
            NSLog(@"%@", error);
        }

        if ([_session canAddInput:_captureDeviceInput])
        {
            [_session addInput:_captureDeviceInput];

            [[(AVCaptureVideoPreviewLayer *)_currentCamera.viewfinder.layer connection] setVideoOrientation:AVCaptureVideoOrientationPortrait];
            [(AVCaptureVideoPreviewLayer *)_currentCamera.viewfinder.layer setVideoGravity:AVLayerVideoGravityResizeAspectFill];
        }

        AVCaptureStillImageOutput *stillImageOutput = [[AVCaptureStillImageOutput alloc] init];
        if ([_session canAddOutput:stillImageOutput])
        {
            [_stillImageOutput setOutputSettings:@{AVVideoCodecKey : AVVideoCodecJPEG}];
            [_session addOutput:stillImageOutput];
            [self setStillImageOutput:stillImageOutput];
        }

        [_session startRunning];
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

- (void)setAspect:(NSString *)aspect
{
    [(AVCaptureVideoPreviewLayer *)_currentCamera.viewfinder.layer setVideoGravity:aspect];
}


- (void)setCamera:(NSInteger)camera
{
    //    AVCaptureDevice *currentVideoDevice = [_captureDeviceInput device];
    AVCaptureDevicePosition position = (AVCaptureDevicePosition)camera;
    AVCaptureDevice *videoDevice = [self deviceWithMediaType:AVMediaTypeVideo preferringPosition:(AVCaptureDevicePosition)position];

    NSError *error = nil;
    AVCaptureDeviceInput *videoDeviceInput = [AVCaptureDeviceInput deviceInputWithDevice:videoDevice error:&error];

    if (error)
    {
        NSLog(@"%@", error);
    }

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
    [[(AVCaptureVideoPreviewLayer *)_currentCamera.viewfinder.layer connection] setVideoOrientation:orientation];
}

- (void)takePicture:(RCTResponseSenderBlock) callback {
    RCT_EXPORT();
    // Update the orientation on the still image output video connection before capturing.
    [[[self stillImageOutput] connectionWithMediaType:AVMediaTypeVideo] setVideoOrientation:[[(AVCaptureVideoPreviewLayer *)_currentCamera.viewfinder.layer connection] videoOrientation]];

    // Flash set to Auto for Still Capture
    //    [AVCamViewController setFlashMode:AVCaptureFlashModeAuto forDevice:[[self videoDeviceInput] device]];

    // Capture a still image.
    [[self stillImageOutput] captureStillImageAsynchronouslyFromConnection:[[self stillImageOutput] connectionWithMediaType:AVMediaTypeVideo] completionHandler:^(CMSampleBufferRef imageDataSampleBuffer, NSError *error) {

        if (imageDataSampleBuffer)
        {
            NSData *imageData = [AVCaptureStillImageOutput jpegStillImageNSDataRepresentation:imageDataSampleBuffer];
            NSString *imageBase64 = [imageData base64EncodedStringWithOptions:0];
            callback(@[[NSNull null], imageBase64]);
        }
        else {
            callback(@[RCTMakeError(error.description, nil, nil)]);
        }
    }];
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

@end
