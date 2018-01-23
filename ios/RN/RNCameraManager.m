#import "RNCamera.h"
#import "RNCameraManager.h"
#import "RNFileSystem.h"
#import "RNImageUtils.h"
#import <React/RCTBridge.h>
#import <React/RCTUIManager.h>
#import <React/RCTEventDispatcher.h>
#import <React/RCTLog.h>
#import <React/RCTUtils.h>
#import <React/UIView+React.h>

@implementation RNCameraManager

RCT_EXPORT_MODULE(RNCameraManager);
//RCT_EXPORT_MODULE(RNCamera);
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
    return [[RNCamera alloc] initWithBridge:self.bridge];
}

- (NSDictionary *)constantsToExport
{
    return @{
             @"Type" :
                 @{@"front" : @(RNCameraTypeFront), @"back" : @(RNCameraTypeBack)},
             @"FlashMode" : @{
                     @"off" : @(RNCameraFlashModeOff),
                     @"on" : @(RNCameraFlashModeOn),
                     @"auto" : @(RNCameraFlashModeAuto),
                     @"torch" : @(RNCameraFlashModeTorch)
                     },
             @"AutoFocus" :
                 @{@"on" : @(RNCameraAutoFocusOn), @"off" : @(RNCameraAutoFocusOff)},
             @"WhiteBalance" : @{
                     @"auto" : @(RNCameraWhiteBalanceAuto),
                     @"sunny" : @(RNCameraWhiteBalanceSunny),
                     @"cloudy" : @(RNCameraWhiteBalanceCloudy),
                     @"shadow" : @(RNCameraWhiteBalanceShadow),
                     @"incandescent" : @(RNCameraWhiteBalanceIncandescent),
                     @"fluorescent" : @(RNCameraWhiteBalanceFluorescent)
                     },
             @"VideoQuality": @{
                     @"2160p": @(RNCameraVideo2160p),
                     @"1080p": @(RNCameraVideo1080p),
                     @"720p": @(RNCameraVideo720p),
                     @"480p": @(RNCameraVideo4x3),
                     @"4:3": @(RNCameraVideo4x3),
                     },
             @"BarCodeType" : [[self class] validBarCodeTypes],
             @"FaceDetection" : [[self  class] faceDetectorConstants]
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
    return [RNFaceDetectorManager constants];
}

RCT_CUSTOM_VIEW_PROPERTY(type, NSInteger, RNCamera)
{
    if (view.presetCamera != [RCTConvert NSInteger:json]) {
        [view setPresetCamera:[RCTConvert NSInteger:json]];
        [view updateType];
    }
}

RCT_CUSTOM_VIEW_PROPERTY(flashMode, NSInteger, RNCamera)
{
    [view setFlashMode:[RCTConvert NSInteger:json]];
    [view updateFlashMode];
}

RCT_CUSTOM_VIEW_PROPERTY(autoFocus, NSInteger, RNCamera)
{
    [view setAutoFocus:[RCTConvert NSInteger:json]];
    [view updateFocusMode];
}

RCT_CUSTOM_VIEW_PROPERTY(focusDepth, NSNumber, RNCamera)
{
    [view setFocusDepth:[RCTConvert float:json]];
    [view updateFocusDepth];
}

RCT_CUSTOM_VIEW_PROPERTY(zoom, NSNumber, RNCamera)
{
    [view setZoom:[RCTConvert CGFloat:json]];
    [view updateZoom];
}

RCT_CUSTOM_VIEW_PROPERTY(whiteBalance, NSInteger, RNCamera)
{
    [view setWhiteBalance: [RCTConvert NSInteger:json]];
    [view updateWhiteBalance];
}

RCT_CUSTOM_VIEW_PROPERTY(faceDetectorEnabled, BOOL, RNCamera)
{
    [view updateFaceDetecting:json];
}

RCT_CUSTOM_VIEW_PROPERTY(faceDetectionMode, NSInteger, RNCamera)
{
    [view updateFaceDetectionMode:json];
}

RCT_CUSTOM_VIEW_PROPERTY(faceDetectionLandmarks, NSString, RNCamera)
{
    [view updateFaceDetectionLandmarks:json];
}

RCT_CUSTOM_VIEW_PROPERTY(faceDetectionClassifications, NSString, RNCamera)
{
    [view updateFaceDetectionClassifications:json];
}

RCT_CUSTOM_VIEW_PROPERTY(barCodeScannerEnabled, BOOL, RNCamera)
{
    
    view.barCodeReading = [RCTConvert BOOL:json];
    [view setupOrDisableBarcodeScanner];
}

RCT_CUSTOM_VIEW_PROPERTY(barCodeTypes, NSArray, RNCamera)
{
    [view setBarCodeTypes:[RCTConvert NSArray:json]];
}

RCT_REMAP_METHOD(takePicture,
                 options:(NSDictionary *)options
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)
{
#if TARGET_IPHONE_SIMULATOR
    NSMutableDictionary *response = [[NSMutableDictionary alloc] init];
    float quality = [options[@"quality"] floatValue];
    //    NSString *path = [RCTFileSystem generatePathInDirectory:[self.bridge.scopedModules.fileSystem.cachesDirectory stringByAppendingPathComponent:@"Camera"] withExtension:@".jpg"];
    UIImage *generatedPhoto = [RNImageUtils generatePhotoOfSize:CGSizeMake(200, 200)];
    NSData *photoData = UIImageJPEGRepresentation(generatedPhoto, quality);
    //    response[@"uri"] = [RCTImageUtils writeImage:photoData toPath:path];
    response[@"width"] = @(generatedPhoto.size.width);
    response[@"height"] = @(generatedPhoto.size.height);
    if ([options[@"base64"] boolValue]) {
        response[@"base64"] = [photoData base64EncodedStringWithOptions:0];
    }
    resolve(response);
#else
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
        RNCamera *view = nil;
        for (NSNumber *reactTag in viewRegistry) {
            UIView *reactView = viewRegistry[reactTag];
            if ([reactView isKindOfClass:[RNCamera class]]) {
                view = (RNCamera *)reactView;
                break;
            }
        }
        if (!view) {
            RCTLogError(@"Could not find RNCamera view on viewRegistry");
        } else {
            [view takePicture:options resolve:resolve reject:reject];
        }
    }];
#endif
}

RCT_REMAP_METHOD(record,
                 withOptions:(NSDictionary *)options
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)
{
#if TARGET_IPHONE_SIMULATOR
    reject(@"E_RECORDING_FAILED", @"Video recording is not supported on a simulator.", nil);
    return;
#endif
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
        RNCamera *view = nil;
        for (NSNumber *reactTag in viewRegistry) {
            UIView *reactView = viewRegistry[reactTag];
            if ([reactView isKindOfClass:[RNCamera class]]) {
                view = (RNCamera *)reactView;
                break;
            }
        }
        if (!view) {
            RCTLogError(@"Could not find RNCamera view on viewRegistry");
        } else {
            [view record:options resolve:resolve reject:reject];
        }
    }];
}

RCT_REMAP_METHOD(stopRecording)
{
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
        RNCamera *view = nil;
        for (NSNumber *reactTag in viewRegistry) {
            UIView *reactView = viewRegistry[reactTag];
            if ([reactView isKindOfClass:[RNCamera class]]) {
                view = (RNCamera *)reactView;
                break;
            }
        }
        if (!view) {
            RCTLogError(@"Could not find RNCamera view on viewRegistry");
        } else {
            [view stopRecording];
        }
    }];
}

@end

