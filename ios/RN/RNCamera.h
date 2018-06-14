#import <AVFoundation/AVFoundation.h>
#import <React/RCTBridge.h>
#import <React/RCTBridgeModule.h>
#import <UIKit/UIKit.h>

#if __has_include("RNFaceDetectorManager.h")
#import "RNFaceDetectorManager.h"
#else
#import "RNFaceDetectorManagerStub.h"
#endif

@class RNCamera;

@interface RNCamera : UIView <AVCaptureMetadataOutputObjectsDelegate, RNFaceDetectorDelegate, AVCaptureVideoDataOutputSampleBufferDelegate>

@property(nonatomic, strong) dispatch_queue_t sessionQueue;
@property(nonatomic, strong) AVCaptureSession *session;
@property(nonatomic, strong) AVCaptureDeviceInput *videoCaptureDeviceInput;
@property(nonatomic, strong) AVCaptureStillImageOutput *stillImageOutput;
@property(nonatomic, strong) AVCaptureVideoDataOutput* videoOutput;
@property(nonatomic, strong) AVCaptureMetadataOutput *metadataOutput;
@property(nonatomic, strong) id runtimeErrorHandlingObserver;
@property(nonatomic, strong) AVCaptureVideoPreviewLayer *previewLayer;
@property(nonatomic, strong) NSArray *barCodeTypes;

@property(nonatomic, assign) NSInteger presetCamera;
@property(nonatomic, assign) NSInteger flashMode;
@property(nonatomic, assign) CGFloat zoom;
@property(nonatomic, assign) NSInteger autoFocus;
@property(nonatomic, assign) float focusDepth;
@property(nonatomic, assign) NSInteger whiteBalance;
@property(nonatomic, assign) AVCaptureSessionPreset pictureSize;
@property(nonatomic, assign) BOOL isReadingBarCodes;
@property(nonatomic, assign) BOOL isDetectingFaces;
@property(nonatomic, assign) AVVideoCodecType videoCodecType;
@property(nonatomic, assign) AVCaptureVideoStabilizationMode videoStabilizationMode;

@property(nonatomic, strong) dispatch_queue_t frameBufferQueue;
@property(nonatomic, strong) AVAssetWriter *videoWriter;
@property(nonatomic, strong) AVAssetWriterInput* writerInput;
@property(nonatomic, assign) BOOL canAppendBuffer;
@property(nonatomic, assign) CMTime bufferTimestamp;
@property(nonatomic, assign) Float64 maxDuration;

- (id)initWithBridge:(RCTBridge *)bridge;
- (void)updateType;
- (void)updateFlashMode;
- (void)updateFocusMode;
- (void)updateFocusDepth;
- (void)updateZoom;
- (void)updateWhiteBalance;
- (void)updatePictureSize;
- (void)updateFaceDetecting:(id)isDetectingFaces;
- (void)updateFaceDetectionMode:(id)requestedMode;
- (void)updateFaceDetectionLandmarks:(id)requestedLandmarks;
- (void)updateFaceDetectionClassifications:(id)requestedClassifications;
- (void)takePicture:(NSDictionary *)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
- (void)record:(NSDictionary *)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
- (void)stopRecording;
- (void)resumePreview;
- (void)pausePreview;
- (void)setupOrDisableBarcodeScanner;
- (void)onReady:(NSDictionary *)event;
- (void)onMountingError:(NSDictionary *)event;
- (void)onCodeRead:(NSDictionary *)event;
- (void)onFacesDetected:(NSDictionary *)event;
- (void)onPictureSaved:(NSDictionary *)event;

@end
