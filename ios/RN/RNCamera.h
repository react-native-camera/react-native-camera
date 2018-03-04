#import "OpenCVProcessor.hpp"
#import <AVFoundation/AVFoundation.h>
#import <React/RCTBridge.h>
#import <React/RCTBridgeModule.h>
#import <UIKit/UIKit.h>
#import "RNCamera.h"

#if __has_include("RNFaceDetectorManager.h")
#import "RNFaceDetectorManager.h"
#else
#import "RNFaceDetectorManagerStub.h"
#endif

@class RNCamera;

@interface RNCamera : UIView <AVCaptureMetadataOutputObjectsDelegate, AVCaptureFileOutputRecordingDelegate, RNFaceDetectorDelegate, OpenCVProcessorFaceDetectorDelegate>

@property(nonatomic, strong) dispatch_queue_t sessionQueue;
@property(nonatomic, strong) AVCaptureSession *session;
@property(nonatomic, strong) AVCaptureDeviceInput *videoCaptureDeviceInput;
@property(nonatomic, strong) AVCaptureStillImageOutput *stillImageOutput;
@property(nonatomic, strong) AVCaptureMovieFileOutput *movieFileOutput;
@property(nonatomic, strong) AVCaptureMetadataOutput *metadataOutput;
@property(nonatomic, strong) id runtimeErrorHandlingObserver;
@property(nonatomic, strong) AVCaptureVideoPreviewLayer *previewLayer;
@property(nonatomic, strong) NSArray *barCodeTypes;

@property(nonatomic, assign) NSInteger presetCamera;
@property (assign, nonatomic) NSInteger flashMode;
@property (assign, nonatomic) CGFloat zoom;
@property (assign, nonatomic) NSInteger autoFocus;
@property (assign, nonatomic) float focusDepth;
@property (assign, nonatomic) NSInteger whiteBalance;
@property (nonatomic, assign, getter=isReadingBarCodes) BOOL barCodeReading;

- (id)initWithBridge:(RCTBridge *)bridge;
- (void)updateType;
- (void)updateFlashMode;
- (void)updateFocusMode;
- (void)updateFocusDepth;
- (void)updateZoom;
- (void)updateWhiteBalance;
- (void)updateFaceDetecting:(id)isDetectingFaces;
- (void)updateFaceDetectionExpectedOrientation:(NSInteger)faceDetectionExpectedOrientation;
- (void)updateObjectsToDetect:(NSInteger)objectsToDetect;
- (void)updateFaceDetectionMode:(id)requestedMode;
- (void)updateFaceDetectionLandmarks:(id)requestedLandmarks;
- (void)updateFaceDetectionClassifications:(id)requestedClassifications;
- (void)takePicture:(NSDictionary *)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
- (void)record:(NSDictionary *)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
- (void)stopRecording;
- (void)setupOrDisableBarcodeScanner;
- (void)onReady:(NSDictionary *)event;
- (void)onMountingError:(NSDictionary *)event;
- (void)onCodeRead:(NSDictionary *)event;
- (void)onFacesDetected:(NSDictionary *)event;

@end

