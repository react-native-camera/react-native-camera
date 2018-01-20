#import <React/RCTViewManager.h>
#import <React/RCTBridgeModule.h>
#import <AVFoundation/AVFoundation.h>

@class RCTCamera;

static const int RCTFlashModeTorch = 3;

typedef NS_ENUM(NSInteger, RCTCameraType) {
    RCTCameraTypeFront = AVCaptureDevicePositionFront,
    RCTCameraTypeBack = AVCaptureDevicePositionBack
};

typedef NS_ENUM(NSInteger, RCTCameraFlashMode) {
    RCTCameraFlashModeOff = AVCaptureFlashModeOff,
    RCTCameraFlashModeOn = AVCaptureFlashModeOn,
    RCTCameraFlashModeTorch = RCTFlashModeTorch,
    RCTCameraFlashModeAuto = AVCaptureFlashModeAuto
};

typedef NS_ENUM(NSInteger, RCTCameraAutoFocus) {
    RCTCameraAutoFocusOff = AVCaptureFocusModeLocked,
    RCTCameraAutoFocusOn = AVCaptureFocusModeContinuousAutoFocus,
};

typedef NS_ENUM(NSInteger, RCTCameraWhiteBalance) {
    RCTCameraWhiteBalanceAuto = 0,
    RCTCameraWhiteBalanceSunny = 1,
    RCTCameraWhiteBalanceCloudy = 2,
    RCTCameraWhiteBalanceFlash = 3,
    RCTCameraWhiteBalanceShadow = 4,
    RCTCameraWhiteBalanceIncandescent = 5,
    RCTCameraWhiteBalanceFluorescent = 6,
};

typedef NS_ENUM(NSInteger, RCTCameraExposureMode) {
    RCTCameraExposureLocked = AVCaptureExposureModeLocked,
    RCTCameraExposureAuto = AVCaptureExposureModeContinuousAutoExposure,
    RCTCameraExposureCustom = AVCaptureExposureModeCustom,
};

typedef NS_ENUM(NSInteger, RCTCameraVideoResolution) {
    RCTCameraVideo2160p = 0,
    RCTCameraVideo1080p = 1,
    RCTCameraVideo720p = 2,
    RCTCameraVideo4x3 = 3,
};

@interface RCTCameraManager : RCTViewManager <RCTBridgeModule>

+ (NSDictionary *)validBarCodeTypes;

@end
