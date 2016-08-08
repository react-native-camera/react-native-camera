#import "RCTViewManager.h"
#import <AVFoundation/AVFoundation.h>

@class RCTCamera;

typedef NS_ENUM(NSInteger, RCTCameraAspect) {
  RCTCameraAspectFill = 0,
  RCTCameraAspectFit = 1,
  RCTCameraAspectStretch = 2
};

typedef NS_ENUM(NSInteger, RCTCameraCaptureSessionPreset) {
  RCTCameraCaptureSessionPresetLow = 0,
  RCTCameraCaptureSessionPresetMedium = 1,
  RCTCameraCaptureSessionPresetHigh = 2,
  RCTCameraCaptureSessionPresetPhoto = 3
};

typedef NS_ENUM(NSInteger, RCTCameraCaptureMode) {
  RCTCameraCaptureModeStill = 0,
  RCTCameraCaptureModeVideo = 1
};

typedef NS_ENUM(NSInteger, RCTCameraCaptureTarget) {
  RCTCameraCaptureTargetMemory = 0,
  RCTCameraCaptureTargetDisk = 1,
  RCTCameraCaptureTargetTemp = 2,
  RCTCameraCaptureTargetCameraRoll = 3
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

@property (nonatomic, strong) dispatch_queue_t sessionQueue;
@property (nonatomic, strong) AVCaptureSession *session;
@property (nonatomic, strong) AVCaptureDeviceInput *audioCaptureDeviceInput;
@property (nonatomic, strong) AVCaptureDeviceInput *videoCaptureDeviceInput;
@property (nonatomic, strong) AVCaptureStillImageOutput *stillImageOutput;
@property (nonatomic, strong) AVCaptureMovieFileOutput *movieFileOutput;
@property (nonatomic, strong) AVCaptureMetadataOutput *metadataOutput;
@property (nonatomic, strong) id runtimeErrorHandlingObserver;
@property (nonatomic, assign) NSInteger presetCamera;
@property (nonatomic, strong) AVCaptureVideoPreviewLayer *previewLayer;
@property (nonatomic, assign) NSInteger videoTarget;
@property (nonatomic, assign) NSInteger orientation;
@property (nonatomic, assign) BOOL mirrorImage;
@property (nonatomic, strong) NSArray* barCodeTypes;
@property (nonatomic, strong) RCTPromiseResolveBlock videoResolve;
@property (nonatomic, strong) RCTPromiseRejectBlock videoReject;
@property (nonatomic, strong) RCTCamera *camera;


- (void)changeOrientation:(NSInteger)orientation;
- (AVCaptureDevice *)deviceWithMediaType:(NSString *)mediaType preferringPosition:(AVCaptureDevicePosition)position;
- (void)capture:(NSDictionary*)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
- (void)getFOV:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
- (void)hasFlash:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
- (void)initializeCaptureSessionInput:(NSString*)type;
- (void)stopCapture;
- (void)startSession;
- (void)stopSession;
- (void)focusAtThePoint:(CGPoint) atPoint;
- (void)zoom:(CGFloat)velocity reactTag:(NSNumber *)reactTag;


@end
