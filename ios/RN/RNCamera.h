#import <AVFoundation/AVFoundation.h>
#import <React/RCTBridge.h>
#import <React/RCTBridgeModule.h>
#import <UIKit/UIKit.h>

#import "FaceDetectorManagerMlkit.h"
#import "BarcodeDetectorManagerMlkit.h"
#import "TextDetectorManager.h"

@class RNCamera;

@interface RNCamera : UIView <AVCaptureMetadataOutputObjectsDelegate,
                              AVCaptureFileOutputRecordingDelegate,
                              AVCaptureVideoDataOutputSampleBufferDelegate>

@property(nonatomic, strong) dispatch_queue_t sessionQueue;
@property(nonatomic, strong) AVCaptureSession *session;
@property(nonatomic, strong) AVCaptureDeviceInput *videoCaptureDeviceInput;
@property(nonatomic, strong) AVCaptureStillImageOutput *stillImageOutput;
@property(nonatomic, strong) AVCaptureMovieFileOutput *movieFileOutput;
@property(nonatomic, strong) AVCaptureMetadataOutput *metadataOutput;
@property(nonatomic, strong) AVCaptureVideoDataOutput *videoDataOutput;
@property(nonatomic, strong) id runtimeErrorHandlingObserver;
@property(nonatomic, strong) AVCaptureVideoPreviewLayer *previewLayer;
@property(nonatomic, strong) NSArray *barCodeTypes;
@property(nonatomic, strong) NSArray *googleVisionBarcodeTypes;

@property(nonatomic, assign) NSInteger presetCamera;
@property(assign, nonatomic) NSInteger flashMode;
@property(assign, nonatomic) CGFloat zoom;
@property(assign, nonatomic) NSInteger autoFocus;
@property(copy, nonatomic) NSDictionary *autoFocusPointOfInterest;
@property(assign, nonatomic) float focusDepth;
@property(assign, nonatomic) NSInteger whiteBalance;
@property(assign, nonatomic) AVCaptureSessionPreset pictureSize;
@property(nonatomic, assign) BOOL isReadingBarCodes;
@property(nonatomic, assign) BOOL isRecording;
@property(nonatomic, assign) BOOL isRecordingInterrupted;
@property(nonatomic, assign) BOOL isDetectingFaces;
@property(nonatomic, assign) BOOL canReadText;
@property(nonatomic, assign) BOOL canDetectFaces;
@property(nonatomic, assign) BOOL canDetectBarcodes;
@property(assign, nonatomic) AVVideoCodecType videoCodecType;
@property(assign, nonatomic)
    AVCaptureVideoStabilizationMode videoStabilizationMode;
@property(assign, nonatomic, nullable) NSNumber *defaultVideoQuality;
@property(assign, nonatomic, nullable) NSNumber *deviceOrientation;
@property(assign, nonatomic, nullable) NSNumber *orientation;

- (id)initWithBridge:(RCTBridge *)bridge;
- (void)updateType;
- (void)updateFlashMode;
- (void)updateFocusMode;
- (void)updateFocusDepth;
- (void)updateAutoFocusPointOfInterest;
- (void)updateZoom;
- (void)updateWhiteBalance;
- (void)updatePictureSize;
// Face Detection props
- (void)updateTrackingEnabled:(id)requestedTracking;
- (void)updateFaceDetectionMode:(id)requestedMode;
- (void)updateFaceDetectionLandmarks:(id)requestedLandmarks;
- (void)updateFaceDetectionClassifications:(id)requestedClassifications;
// google Barcode props
- (void)updateGoogleVisionBarcodeType:(id)requestedTypes;

- (void)takePicture:(NSDictionary *)options
            resolve:(RCTPromiseResolveBlock)resolve
             reject:(RCTPromiseRejectBlock)reject;
- (void)takePictureWithOrientation:(NSDictionary *)options
                           resolve:(RCTPromiseResolveBlock)resolve
                            reject:(RCTPromiseRejectBlock)reject;
- (void)record:(NSDictionary *)options
       resolve:(RCTPromiseResolveBlock)resolve
        reject:(RCTPromiseRejectBlock)reject;
- (void)recordWithOrientation:(NSDictionary *)options
                      resolve:(RCTPromiseResolveBlock)resolve
                       reject:(RCTPromiseRejectBlock)reject;
- (void)stopRecording;
- (void)resumePreview;
- (void)pausePreview;
- (void)setupOrDisableBarcodeScanner;
- (void)setupOrDisableTextDetector;
- (void)setupOrDisableFaceDetector;
- (void)setupOrDisableBarcodeDetector;
- (void)onReady:(NSDictionary *)event;
- (void)onMountingError:(NSDictionary *)event;
- (void)onCodeRead:(NSDictionary *)event;
- (void)onFacesDetected:(NSDictionary *)event;
- (void)onPictureSaved:(NSDictionary *)event;
- (void)onText:(NSDictionary *)event;
- (void)onBarcodesDetected:(NSDictionary *)event;
- (bool)isRecording;

@end
