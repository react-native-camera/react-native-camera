#import "RCTViewManager.h"
#import <AVFoundation/AVFoundation.h>

@class RCTCamera;

typedef NS_ENUM(NSInteger, RCTCameraAspect) {
    RCTCameraAspectFill = 0,
    RCTCameraAspectFit = 1,
    RCTCameraAspectStretch = 2
};

typedef NS_ENUM(NSInteger, RCTCameraCaptureMode) {
    RCTCameraCaptureModeStill = 0,
    RCTCameraCaptureModeVideo = 1
};

typedef NS_ENUM(NSInteger, RCTCameraCaptureTarget) {
    RCTCameraCaptureTargetMemory = 0,
    RCTCameraCaptureTargetDisk = 1
};

typedef NS_ENUM(NSInteger, RCTCameraOrientation) {
    RCTCameraOrientationAuto = 0,
    RCTCameraOrientationLandscapeLeft = AVCaptureVideoOrientationLandscapeLeft,
    RCTCameraOrientationLandscapeRight = AVCaptureVideoOrientationLandscapeRight,
    RCTCameraOrientationPortrait = AVCaptureVideoOrientationPortrait,
    RCTCameraOrientationPortraitUpsideDown = AVCaptureVideoOrientationPortraitUpsideDown
};

typedef NS_ENUM(NSInteger, RCTCameraType) {
    RCTCameraTypeFront = AVCaptureDevicePositionFront,
    RCTCameraTypeBack = AVCaptureDevicePositionBack
};

typedef NS_ENUM(NSInteger, RCTCameraFlashMode) {
    RCTCameraFlashModeOff = AVCaptureFlashModeOff,
    RCTCameraFlashModeOn = AVCaptureFlashModeOn,
    RCTCameraFlashModeAuto = AVCaptureFlashModeAuto
};

@interface RCTCameraManager : RCTViewManager<AVCaptureMetadataOutputObjectsDelegate>

@property (nonatomic) dispatch_queue_t sessionQueue;
@property (nonatomic) AVCaptureSession *session;
@property (nonatomic) AVCaptureDeviceInput *captureDeviceInput;
@property (nonatomic) AVCaptureStillImageOutput *stillImageOutput;
@property (nonatomic) AVCaptureMetadataOutput *metadataOutput;
@property (nonatomic) id runtimeErrorHandlingObserver;
@property (nonatomic) NSInteger presetCamera;
@property (nonatomic) AVCaptureVideoPreviewLayer *previewLayer;

- (void)changeAspect:(NSString *)aspect;
- (void)changeCamera:(NSInteger)camera;
- (void)changeOrientation:(NSInteger)orientation;
- (void)changeFlashMode:(NSInteger)flashMode;
- (AVCaptureDevice *)deviceWithMediaType:(NSString *)mediaType preferringPosition:(AVCaptureDevicePosition)position;
- (void)capture:(NSDictionary*)options callback:(RCTResponseSenderBlock)callback;

@end
