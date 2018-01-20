#import "RCTCamera.h"
#import "RCTCameraManager.h"
#import "RCTFileSystem.h"
#import "RCTImageUtils.h"
#import <React/RCTBridge.h>
#import <React/RCTUIManager.h>
#import <React/RCTEventDispatcher.h>
#import <React/RCTLog.h>
#import <React/RCTUtils.h>
#import <React/UIView+React.h>

//#if __has_include("EXFaceDetectorManager.h")
//#import "EXFaceDetectorManager.h"
//#else
//#import "EXFaceDetectorManagerStub.h"
//#endif

@implementation RCTCameraManager

RCT_EXPORT_MODULE(RCTCameraManager);
RCT_EXPORT_VIEW_PROPERTY(onCameraReady, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onMountError, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onBarCodeRead, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onFacesDetected, RCTDirectEventBlock);

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

- (UIView *)view
{
    return [[RCTCamera alloc] initWithBridge:self.bridge];
}

- (NSDictionary *)constantsToExport
{
    return @{
             @"Type" :
                 @{@"front" : @(RCTCameraTypeFront), @"back" : @(RCTCameraTypeBack)},
             @"FlashMode" : @{
                     @"off" : @(RCTCameraFlashModeOff),
                     @"on" : @(RCTCameraFlashModeOn),
                     @"auto" : @(RCTCameraFlashModeAuto),
                     @"torch" : @(RCTCameraFlashModeTorch)
                     },
             @"AutoFocus" :
                 @{@"on" : @(RCTCameraAutoFocusOn), @"off" : @(RCTCameraAutoFocusOff)},
             @"WhiteBalance" : @{
                     @"auto" : @(RCTCameraWhiteBalanceAuto),
                     @"sunny" : @(RCTCameraWhiteBalanceSunny),
                     @"cloudy" : @(RCTCameraWhiteBalanceCloudy),
                     @"shadow" : @(RCTCameraWhiteBalanceShadow),
                     @"incandescent" : @(RCTCameraWhiteBalanceIncandescent),
                     @"fluorescent" : @(RCTCameraWhiteBalanceFluorescent)
                     },
             @"VideoQuality": @{
                     @"2160p": @(RCTCameraVideo2160p),
                     @"1080p": @(RCTCameraVideo1080p),
                     @"720p": @(RCTCameraVideo720p),
                     @"480p": @(RCTCameraVideo4x3),
                     @"4:3": @(RCTCameraVideo4x3),
                     },
             @"BarCodeType" : [[self class] validBarCodeTypes]
//             @"FaceDetection" : [[self  class] faceDetectorConstants]
             };
}

- (NSArray<NSString *> *)supportedEvents
{
    return @[@"onCameraReady", @"onMountError", @"onBarCodeRead", @"onFacesDetected"];
}

+ (NSDictionary *)validBarCodeTypes
{
    return @{
             @"upc_e" : AVMetadataObjectTypeUPCECode,
             @"code39" : AVMetadataObjectTypeCode39Code,
             @"code39mod43" : AVMetadataObjectTypeCode39Mod43Code,
             @"ean13" : AVMetadataObjectTypeEAN13Code,
             @"ean8" : AVMetadataObjectTypeEAN8Code,
             @"code93" : AVMetadataObjectTypeCode93Code,
             @"code138" : AVMetadataObjectTypeCode128Code,
             @"pdf417" : AVMetadataObjectTypePDF417Code,
             @"qr" : AVMetadataObjectTypeQRCode,
             @"aztec" : AVMetadataObjectTypeAztecCode,
             @"interleaved2of5" : AVMetadataObjectTypeInterleaved2of5Code,
             @"itf14" : AVMetadataObjectTypeITF14Code,
             @"datamatrix" : AVMetadataObjectTypeDataMatrixCode
             };
}

+ (NSDictionary *)faceDetectorConstants
{
#if __has_include("EXFaceDetectorManager.h")
    return [EXFaceDetectorManager constants];
#elif __has_include("EXFaceDetectorManagerStub.h")
    return [EXFaceDetectorManagerStub constants];
#endif
}

RCT_CUSTOM_VIEW_PROPERTY(type, NSInteger, RCTCamera)
{
    if (view.presetCamera != [RCTConvert NSInteger:json]) {
        [view setPresetCamera:[RCTConvert NSInteger:json]];
        [view updateType];
    }
}

RCT_CUSTOM_VIEW_PROPERTY(flashMode, NSInteger, RCTCamera)
{
    [view setFlashMode:[RCTConvert NSInteger:json]];
    [view updateFlashMode];
}

RCT_CUSTOM_VIEW_PROPERTY(autoFocus, NSInteger, RCTCamera)
{
    [view setAutoFocus:[RCTConvert NSInteger:json]];
    [view updateFocusMode];
}

RCT_CUSTOM_VIEW_PROPERTY(focusDepth, NSNumber, RCTCamera)
{
    [view setFocusDepth:[RCTConvert float:json]];
    [view updateFocusDepth];
}

RCT_CUSTOM_VIEW_PROPERTY(zoom, NSNumber, RCTCamera)
{
    [view setZoom:[RCTConvert CGFloat:json]];
    [view updateZoom];
}

RCT_CUSTOM_VIEW_PROPERTY(whiteBalance, NSInteger, RCTCamera)
{
    [view setWhiteBalance: [RCTConvert NSInteger:json]];
    [view updateWhiteBalance];
}

RCT_CUSTOM_VIEW_PROPERTY(faceDetectorEnabled, BOOL, RCTCamera)
{
    [view updateFaceDetecting:json];
}

RCT_CUSTOM_VIEW_PROPERTY(faceDetectionMode, NSInteger, RCTCamera)
{
    [view updateFaceDetectionMode:json];
}

RCT_CUSTOM_VIEW_PROPERTY(faceDetectionLandmarks, NSString, RCTCamera)
{
    [view updateFaceDetectionLandmarks:json];
}

RCT_CUSTOM_VIEW_PROPERTY(faceDetectionClassifications, NSString, RCTCamera)
{
    [view updateFaceDetectionClassifications:json];
}

RCT_CUSTOM_VIEW_PROPERTY(barCodeScannerEnabled, BOOL, RCTCamera)
{
    
    view.barCodeReading = [RCTConvert BOOL:json];
    [view setupOrDisableBarcodeScanner];
}

RCT_CUSTOM_VIEW_PROPERTY(barCodeTypes, NSArray, RCTCamera)
{
    [view setBarCodeTypes:[RCTConvert NSArray:json]];
}

RCT_REMAP_METHOD(takePicture,
                 options:(NSDictionary *)options
                 reactTag:(nonnull NSNumber *)reactTag
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)
{
#if TARGET_IPHONE_SIMULATOR
    NSMutableDictionary *response = [[NSMutableDictionary alloc] init];
    float quality = [options[@"quality"] floatValue];
    NSString *path = [RCTFileSystem generatePathInDirectory:[self.bridge.scopedModules.fileSystem.cachesDirectory stringByAppendingPathComponent:@"Camera"] withExtension:@".jpg"];
    UIImage *generatedPhoto = [RCTImageUtils generatePhotoOfSize:CGSizeMake(200, 200)];
    NSData *photoData = UIImageJPEGRepresentation(generatedPhoto, quality);
    response[@"uri"] = [RCTImageUtils writeImage:photoData toPath:path];
    response[@"width"] = @(generatedPhoto.size.width);
    response[@"height"] = @(generatedPhoto.size.height);
    if ([options[@"base64"] boolValue]) {
        response[@"base64"] = [photoData base64EncodedStringWithOptions:0];
    }
    resolve(response);
#else
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, RCTCamera *> *viewRegistry) {
        RCTCamera *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[RCTCamera class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting RCTCamera, got: %@", view);
        } else {
            [view takePicture:options resolve:resolve reject:reject];
        }
    }];
#endif
}

RCT_REMAP_METHOD(record,
                 withOptions:(NSDictionary *)options
                 reactTag:(nonnull NSNumber *)reactTag
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)
{
#if TARGET_IPHONE_SIMULATOR
    reject(@"E_RECORDING_FAILED", @"Video recording is not supported on a simulator.", nil);
    return;
#endif
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, RCTCamera *> *viewRegistry) {
        RCTCamera *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[RCTCamera class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting RCTCamera, got: %@", view);
        } else {
            [view record:options resolve:resolve reject:reject];
        }
    }];
}

RCT_REMAP_METHOD(stopRecording, reactTag:(nonnull NSNumber *)reactTag)
{
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, RCTCamera *> *viewRegistry) {
        RCTCamera *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[RCTCamera class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting RCTCamera, got: %@", view);
        } else {
            [view stopRecording];
        }
    }];
}

@end
