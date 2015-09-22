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
  RCTCameraCaptureTargetDisk = 1,
  RCTCameraCaptureTargetCameraRoll = 2
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

typedef NS_ENUM(NSInteger, RCTCameraTorchMode) {
  RCTCameraTorchModeOff = AVCaptureTorchModeOff,
  RCTCameraTorchModeOn = AVCaptureTorchModeOn,
  RCTCameraTorchModeAuto = AVCaptureTorchModeAuto
};

@interface RCTCameraManager : RCTViewManager<AVCaptureMetadataOutputObjectsDelegate, AVCaptureFileOutputRecordingDelegate>

@property (nonatomic) dispatch_queue_t sessionQueue;
@property (nonatomic) AVCaptureSession *session;
@property (nonatomic) AVCaptureDeviceInput *audioCaptureDeviceInput;
@property (nonatomic) AVCaptureDeviceInput *videoCaptureDeviceInput;
@property (nonatomic) AVCaptureStillImageOutput *stillImageOutput;
@property (nonatomic) AVCaptureMovieFileOutput *movieFileOutput;
@property (nonatomic) AVCaptureMetadataOutput *metadataOutput;
@property (nonatomic) id runtimeErrorHandlingObserver;
@property (nonatomic) NSInteger presetCamera;
@property (nonatomic) AVCaptureVideoPreviewLayer *previewLayer;
@property (nonatomic) NSInteger videoTarget;
@property (nonatomic, strong) RCTResponseSenderBlock videoCallback;

- (void)changeAspect:(NSString *)aspect;
- (void)changeCamera:(NSInteger)camera;
- (void)changeOrientation:(NSInteger)orientation;
- (void)changeFlashMode:(NSInteger)flashMode;
- (void)changeTorchMode:(NSInteger)torchMode;
- (AVCaptureDevice *)deviceWithMediaType:(NSString *)mediaType preferringPosition:(AVCaptureDevicePosition)position;
- (void)capture:(NSDictionary*)options callback:(RCTResponseSenderBlock)callback;
- (void)initializeCaptureSessionInput:(NSString*)type;
- (void)stopCapture;
- (void)startSession;
- (void)stopSession;

@end
