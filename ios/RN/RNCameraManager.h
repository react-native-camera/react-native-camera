#import <React/RCTViewManager.h>
#import <React/RCTBridgeModule.h>
#import <AVFoundation/AVFoundation.h>

@class RNCameraManager;

static const int RNFlashModeTorch = 3;

typedef NS_ENUM(NSInteger, RNCameraType) {
    RNCameraTypeFront = AVCaptureDevicePositionFront,
    RNCameraTypeBack = AVCaptureDevicePositionBack
};

typedef NS_ENUM(NSInteger, RNCameraFlashMode) {
    RNCameraFlashModeOff = AVCaptureFlashModeOff,
    RNCameraFlashModeOn = AVCaptureFlashModeOn,
    RNCameraFlashModeTorch = RNFlashModeTorch,
    RNCameraFlashModeAuto = AVCaptureFlashModeAuto
};

typedef NS_ENUM(NSInteger, RNCameraOrientation) {
    RNCameraOrientationAuto = 0,
    RNCameraOrientationLandscapeLeft = AVCaptureVideoOrientationLandscapeLeft,
    RNCameraOrientationLandscapeRight = AVCaptureVideoOrientationLandscapeRight,
    RNCameraOrientationPortrait = AVCaptureVideoOrientationPortrait,
    RNCameraOrientationPortraitUpsideDown = AVCaptureVideoOrientationPortraitUpsideDown
};

typedef NS_ENUM(NSInteger, RNCameraAutoFocus) {
    RNCameraAutoFocusOff = AVCaptureFocusModeLocked,
    RNCameraAutoFocusOn = AVCaptureFocusModeContinuousAutoFocus,
};

typedef NS_ENUM(NSInteger, RNCameraWhiteBalance) {
    RNCameraWhiteBalanceAuto = 0,
    RNCameraWhiteBalanceSunny = 1,
    RNCameraWhiteBalanceCloudy = 2,
    RNCameraWhiteBalanceFlash = 3,
    RNCameraWhiteBalanceShadow = 4,
    RNCameraWhiteBalanceIncandescent = 5,
    RNCameraWhiteBalanceFluorescent = 6,
};

typedef NS_ENUM(NSInteger, RNCameraExposureMode) {
    RNCameraExposureLocked = AVCaptureExposureModeLocked,
    RNCameraExposureAuto = AVCaptureExposureModeContinuousAutoExposure,
    RNCameraExposureCustom = AVCaptureExposureModeCustom,
};

typedef NS_ENUM(NSInteger, RNCameraVideoResolution) {
    RNCameraVideo2160p = 0,
    RNCameraVideo1080p = 1,
    RNCameraVideo720p = 2,
    RNCameraVideo4x3 = 3,
    RNCameraVideo288p = 4,
};

@interface RNCameraManager : RCTViewManager <RCTBridgeModule>

+ (NSDictionary *)validBarCodeTypes;
+ (NSDictionary *)validCodecTypes;
+ (NSDictionary *)validVideoStabilizationModes;

@end

