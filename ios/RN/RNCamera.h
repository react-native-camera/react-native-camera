#import <AVFoundation/AVFoundation.h>
#import <React/RCTBridge.h>
#import <React/RCTBridgeModule.h>
#import <UIKit/UIKit.h>

#if __has_include("RNFaceDetectorManager.h")
#import "RNFaceDetectorManager.h"
#else
#import "RNFaceDetectorManagerStub.h"
#endif

#if __has_include("TextDetectorManager.h")
#import "TextDetectorManager.h"
#else
#import "TextDetectorManagerStub.h"
#endif

#import "RNAssetWriter.h"

@class RNCamera;

@interface RNCamera : UIView <AVCaptureMetadataOutputObjectsDelegate, RNFaceDetectorDelegate, AVCaptureVideoDataOutputSampleBufferDelegate, AVCaptureAudioDataOutputSampleBufferDelegate>

@property(nonatomic, strong) dispatch_queue_t sessionQueue;
@property(nonatomic, strong) RNAssetWriter* rnAssetWriter;
@property(nonatomic, strong) AVCaptureSession *session;
@property(nonatomic, strong) AVCaptureDeviceInput *videoCaptureDeviceInput;
@property(nonatomic, strong) AVCaptureDeviceInput *audioCaptureDeviceInput;
@property(nonatomic, strong) AVCaptureStillImageOutput *stillImageOutput;
@property(nonatomic, strong) AVCaptureMetadataOutput *metadataOutput;
@property(nonatomic, strong) AVCaptureVideoDataOutput *videoDataOutput;
@property(nonatomic, strong) AVCaptureAudioDataOutput *audioDataOutput;
@property(nonatomic, strong) id runtimeErrorHandlingObserver;
@property(nonatomic, strong) AVCaptureVideoPreviewLayer *previewLayer;
@property(nonatomic, strong) NSArray *barCodeTypes;

@property(nonatomic, assign) NSInteger presetCamera;
@property (assign, nonatomic) NSInteger flashMode;
@property (assign, nonatomic) BOOL isRecording;
@property (assign, nonatomic) BOOL faceDetectingWhileRecording;
@property (assign, nonatomic) BOOL mirrorVideo;
@property (assign, nonatomic) CGFloat zoom;
@property (assign, nonatomic) NSInteger autoFocus;
@property (copy, nonatomic) NSDictionary *autoFocusPointOfInterest;
@property (copy, nonatomic) NSDictionary *audioSettings;
@property (copy, nonatomic) NSDictionary *videoSettings;
@property (assign, nonatomic) float focusDepth;
@property (assign, nonatomic) NSInteger whiteBalance;
@property (assign, nonatomic) AVCaptureSessionPreset pictureSize;
@property (assign, nonatomic) AVCaptureSessionPreset videoSize;
@property (nonatomic, assign) BOOL isReadingBarCodes;
@property (nonatomic, assign) BOOL isDetectingFaces;
@property (nonatomic, assign) BOOL canReadText;
@property(assign, nonatomic) AVVideoCodecType videoCodecType;
@property (assign, nonatomic) AVCaptureVideoStabilizationMode videoStabilizationMode;

- (id)initWithBridge:(RCTBridge *)bridge;
- (void)updateType;
- (void)updateFlashMode;
- (void)updateFocusMode;
- (void)updateFocusDepth;
- (void)updateAutoFocusPointOfInterest;
- (void)updateZoom;
- (void)updateWhiteBalance;
- (void)updatePictureSize;
- (void)updateFaceDetecting:(id)isDetectingFaces;
- (void)updateFaceDetectionMode:(id)requestedMode;
- (void)updateFaceDetectionLandmarks:(id)requestedLandmarks;
- (void)updateFaceDetectionClassifications:(id)requestedClassifications;
- (void)takePicture:(NSDictionary *)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
- (void)takePictureWithOrientation:(NSDictionary *)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
- (void)record:(NSDictionary *)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
- (void)recordWithOrientation:(NSDictionary *)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
- (void)stopRecording;
- (void)resumePreview;
- (void)pausePreview;
- (void)setupOrDisableBarcodeScanner;
- (void)setupOrDisableTextDetector;
- (void)onReady:(NSDictionary *)event;
- (void)onMountingError:(NSDictionary *)event;
- (void)onCodeRead:(NSDictionary *)event;
- (void)onFacesDetected:(NSDictionary *)event;
- (void)onPictureSaved:(NSDictionary *)event;
- (void)onText:(NSDictionary *)event;
- (void)updateVideoSettings:(UIInterfaceOrientation)orientation;

@end

