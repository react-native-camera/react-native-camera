#import "RCTViewManager.h"
#import <AVFoundation/AVFoundation.h>

@class RCTCamera;

@interface RCTCameraManager : RCTViewManager<AVCaptureMetadataOutputObjectsDelegate>

@property (nonatomic) dispatch_queue_t sessionQueue;
@property (nonatomic) AVCaptureSession *session;
@property (nonatomic) AVCaptureDeviceInput *captureDeviceInput;
@property (nonatomic) AVCaptureStillImageOutput *stillImageOutput;
@property (nonatomic) AVCaptureMetadataOutput *metadataOutput;
@property (nonatomic) id runtimeErrorHandlingObserver;
@property (nonatomic) NSInteger presetCamera;
@property (nonatomic) AVCaptureVideoPreviewLayer *previewLayer;

- (void)changeAspect:(NSString *)aspect;
- (void)changeCamera:(NSInteger)camera;
- (void)changeOrientation:(NSInteger)orientation;
- (AVCaptureDevice *)deviceWithMediaType:(NSString *)mediaType preferringPosition:(AVCaptureDevicePosition)position;
- (void)takePicture:(RCTResponseSenderBlock)callback;

@end
