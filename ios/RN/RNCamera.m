#import "RNCamera.h"
#import "RNCameraUtils.h"
#import "RNImageUtils.h"
#import "RNFileSystem.h"
#import <React/RCTEventDispatcher.h>
#import <React/RCTLog.h>
#import <React/RCTUtils.h>
#import <React/UIView+React.h>
#import  "RNSensorOrientationChecker.h"
@interface RNCamera ()

@property (nonatomic, weak) RCTBridge *bridge;
@property (nonatomic,strong) RNSensorOrientationChecker * sensorOrientationChecker;
@property (nonatomic, assign, getter=isSessionPaused) BOOL paused;

@property (nonatomic, strong) RCTPromiseResolveBlock videoRecordedResolve;
@property (nonatomic, strong) RCTPromiseRejectBlock videoRecordedReject;
@property (nonatomic, strong) id faceDetectorManager;
@property (nonatomic, strong) id textDetector;

@property (nonatomic, copy) RCTDirectEventBlock onCameraReady;
@property (nonatomic, copy) RCTDirectEventBlock onMountError;
@property (nonatomic, copy) RCTDirectEventBlock onBarCodeRead;
@property (nonatomic, copy) RCTDirectEventBlock onTextRecognized;
@property (nonatomic, copy) RCTDirectEventBlock onFacesDetected;
@property (nonatomic, copy) RCTDirectEventBlock onPictureSaved;
@property (nonatomic, assign) BOOL finishedReadingText;
@property (nonatomic, copy) NSDate *start;

@end

@implementation RNCamera

static NSDictionary *defaultFaceDetectorOptions = nil;

- (id)initWithBridge:(RCTBridge *)bridge
{
    if ((self = [super init])) {
        self.bridge = bridge;
        self.session = [AVCaptureSession new];
        self.sessionQueue = dispatch_queue_create("com.mariusreimer.sessionQueue", DISPATCH_QUEUE_SERIAL);
        self.sensorOrientationChecker = [RNSensorOrientationChecker new];
        self.textDetector = [self createTextDetector];
        self.finishedReadingText = true;
        self.faceDetectingWhileRecording = false;
        self.mirrorVideo = false;
        
        self.videoCodecType = AVVideoCodecH264;
        self.start = [NSDate date];
        self.faceDetectorManager = [self createFaceDetectorManager];
#if !(TARGET_IPHONE_SIMULATOR)
        self.previewLayer =
        [AVCaptureVideoPreviewLayer layerWithSession:self.session];
        self.previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill;
        self.previewLayer.needsDisplayOnBoundsChange = YES;
#endif
        self.paused = NO;
        [self initAssetWriter];
        [self changePreviewOrientation:[UIApplication sharedApplication].statusBarOrientation];
        [self updateVideoSettings:[UIApplication sharedApplication].statusBarOrientation];
        
        [self initializeAudioSessionInput];
        [self updateFlashMode];
        
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(orientationChanged:)
                                                     name:UIDeviceOrientationDidChangeNotification
                                                   object:nil];
        
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(bridgeDidBackground:)
                                                     name:UIApplicationDidEnterBackgroundNotification
                                                   object:nil];

        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(bridgeDidForeground:)
                                                     name:UIApplicationWillEnterForegroundNotification
                                                   object:nil];

        self.autoFocus = -1;

    }
    return self;
}

- (void)onReady:(NSDictionary *)event
{
    if (_onCameraReady) {
        _onCameraReady(nil);
    }
}

- (void)onMountingError:(NSDictionary *)event
{
    if (_onMountError) {
        _onMountError(event);
    }
}

- (void)onCodeRead:(NSDictionary *)event
{
    if (_onBarCodeRead) {
        _onBarCodeRead(event);
    }
}

- (void)onPictureSaved:(NSDictionary *)event
{
    if (_onPictureSaved) {
        _onPictureSaved(event);
    }
}

- (void)onText:(NSDictionary *)event
{
    if (_onTextRecognized && _session) {
        _onTextRecognized(event);
    }
}

- (void)layoutSubviews
{
    [super layoutSubviews];
    self.previewLayer.frame = self.bounds;
    [self setBackgroundColor:[UIColor clearColor]];
    [self.layer insertSublayer:self.previewLayer atIndex:0];
}

- (void)insertReactSubview:(UIView *)view atIndex:(NSInteger)atIndex
{
    [self insertSubview:view atIndex:atIndex + 1];
    [super insertReactSubview:view atIndex:atIndex];
}

- (void)removeReactSubview:(UIView *)subview
{
    [subview removeFromSuperview];
    [super removeReactSubview:subview];
}

- (void)removeFromSuperview
{
    [super removeFromSuperview];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIDeviceOrientationDidChangeNotification object:nil];
    [self stopSession];
}

-(void)updateType
{
    [_session commitConfiguration];
    [self initializeCaptureSessionInput];
    [self startSession];
}

- (void)updateFlashMode
{
    AVCaptureDevice *device = [self.videoCaptureDeviceInput device];
    NSError *error = nil;

    if (self.flashMode == RNCameraFlashModeTorch) {
        if (![device hasTorch])
            return;
        if (![device lockForConfiguration:&error]) {
            if (error) {
                RCTLogError(@"%s: %@", __func__, error);
            }
            return;
        }
        if (device.hasTorch && [device isTorchModeSupported:AVCaptureTorchModeOn])
        {
            NSError *error = nil;
            if ([device lockForConfiguration:&error]) {
                [device setFlashMode:AVCaptureFlashModeOff];
                [device setTorchMode:AVCaptureTorchModeOn];
                [device unlockForConfiguration];
            } else {
                if (error) {
                    RCTLogError(@"%s: %@", __func__, error);
                }
            }
        }
    } else {
        if (![device hasFlash])
            return;
        if (![device lockForConfiguration:&error]) {
            if (error) {
                RCTLogError(@"%s: %@", __func__, error);
            }
            return;
        }
        if (device.hasFlash && [device isFlashModeSupported:self.flashMode])
        {
            NSError *error = nil;
            if ([device lockForConfiguration:&error]) {
                if ([device isTorchActive]) {
                    [device setTorchMode:AVCaptureTorchModeOff];
                }
                [device setFlashMode:self.flashMode];
                [device unlockForConfiguration];
            } else {
                if (error) {
                    RCTLogError(@"%s: %@", __func__, error);
                }
            }
        }
    }

    [device unlockForConfiguration];
}

- (void)updateAutoFocusPointOfInterest
{
    AVCaptureDevice *device = [self.videoCaptureDeviceInput device];
    NSError *error = nil;

    if (![device lockForConfiguration:&error]) {
        if (error) {
            RCTLogError(@"%s: %@", __func__, error);
        }
        return;
    }

    if ([self.autoFocusPointOfInterest objectForKey:@"x"] && [self.autoFocusPointOfInterest objectForKey:@"y"]) {
        float xValue = [self.autoFocusPointOfInterest[@"x"] floatValue];
        float yValue = [self.autoFocusPointOfInterest[@"y"] floatValue];
        if ([device isFocusPointOfInterestSupported] && [device isFocusModeSupported:AVCaptureFocusModeContinuousAutoFocus]) {

            CGPoint autofocusPoint = CGPointMake(xValue, yValue);
            [device setFocusPointOfInterest:autofocusPoint];
            [device setFocusMode:AVCaptureFocusModeContinuousAutoFocus];
          }
        else {
            RCTLogWarn(@"AutoFocusPointOfInterest not supported");
        }
    }

    [device unlockForConfiguration];
}

- (void)updateFocusMode
{
    AVCaptureDevice *device = [self.videoCaptureDeviceInput device];
    NSError *error = nil;

    if (![device lockForConfiguration:&error]) {
        if (error) {
            RCTLogError(@"%s: %@", __func__, error);
        }
        return;
    }

    if ([device isFocusModeSupported:self.autoFocus]) {
        if ([device lockForConfiguration:&error]) {
            [device setFocusMode:self.autoFocus];
        } else {
            if (error) {
                RCTLogError(@"%s: %@", __func__, error);
            }
        }
    }

    [device unlockForConfiguration];
}

- (void)updateFocusDepth
{
    AVCaptureDevice *device = [self.videoCaptureDeviceInput device];
    NSError *error = nil;

    if (device == nil || self.autoFocus < 0 || device.focusMode != RNCameraAutoFocusOff || device.position == RNCameraTypeFront) {
        return;
    }

    if (![device respondsToSelector:@selector(isLockingFocusWithCustomLensPositionSupported)] || ![device isLockingFocusWithCustomLensPositionSupported]) {
        RCTLogWarn(@"%s: Setting focusDepth isn't supported for this camera device", __func__);
        return;
    }

    if (![device lockForConfiguration:&error]) {
        if (error) {
            RCTLogError(@"%s: %@", __func__, error);
        }
        return;
    }

    __weak __typeof__(device) weakDevice = device;
    [device setFocusModeLockedWithLensPosition:self.focusDepth completionHandler:^(CMTime syncTime) {
        [weakDevice unlockForConfiguration];
    }];
}

- (void)updateZoom {
    AVCaptureDevice *device = [self.videoCaptureDeviceInput device];
    NSError *error = nil;

    if (![device lockForConfiguration:&error]) {
        if (error) {
            RCTLogError(@"%s: %@", __func__, error);
        }
        return;
    }

    device.videoZoomFactor = (device.activeFormat.videoMaxZoomFactor - 1.0) * self.zoom + 1.0;

    [device unlockForConfiguration];
}

- (void)updateWhiteBalance
{
    AVCaptureDevice *device = [self.videoCaptureDeviceInput device];
    NSError *error = nil;

    if (![device lockForConfiguration:&error]) {
        if (error) {
            RCTLogError(@"%s: %@", __func__, error);
        }
        return;
    }

    if (self.whiteBalance == RNCameraWhiteBalanceAuto) {
        [device setWhiteBalanceMode:AVCaptureWhiteBalanceModeContinuousAutoWhiteBalance];
        [device unlockForConfiguration];
    } else {
        AVCaptureWhiteBalanceTemperatureAndTintValues temperatureAndTint = {
            .temperature = [RNCameraUtils temperatureForWhiteBalance:self.whiteBalance],
            .tint = 0,
        };
        AVCaptureWhiteBalanceGains rgbGains = [device deviceWhiteBalanceGainsForTemperatureAndTintValues:temperatureAndTint];
        __weak __typeof__(device) weakDevice = device;
        if ([device lockForConfiguration:&error]) {
            [device setWhiteBalanceModeLockedWithDeviceWhiteBalanceGains:rgbGains completionHandler:^(CMTime syncTime) {
                [weakDevice unlockForConfiguration];
            }];
        } else {
            if (error) {
                RCTLogError(@"%s: %@", __func__, error);
            }
        }
    }

    [device unlockForConfiguration];
}

- (void)updatePictureSize
{
    // [reime005] we may not need this anymore...
//    [self updateSessionPreset:self.pictureSize withoutCommit:NO];
}

#if __has_include(<GoogleMobileVision/GoogleMobileVision.h>)
- (void)updateFaceDetecting:(id)faceDetecting
{
    [_faceDetectorManager setIsEnabled:faceDetecting];
}

- (void)updateFaceDetectionMode:(id)requestedMode
{
    [_faceDetectorManager setMode:requestedMode];
}

- (void)updateFaceDetectionLandmarks:(id)requestedLandmarks
{
    [_faceDetectorManager setLandmarksDetected:requestedLandmarks];
}

- (void)updateFaceDetectionClassifications:(id)requestedClassifications
{
    [_faceDetectorManager setClassificationsDetected:requestedClassifications];
}
#endif


- (void)takePictureWithOrientation:(NSDictionary *)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject{
    [self.sensorOrientationChecker getDeviceOrientationWithBlock:^(UIInterfaceOrientation orientation) {
        NSMutableDictionary *tmpOptions = [options mutableCopy];
        tmpOptions[@"orientation"]=[NSNumber numberWithInteger:[self.sensorOrientationChecker convertToAVCaptureVideoOrientation: orientation]];
        [self takePicture:tmpOptions resolve:resolve reject:reject];

    }];
}
- (void)takePicture:(NSDictionary *)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    int orientation;
    if ([options[@"orientation"] integerValue]) {
        orientation = [options[@"orientation"] integerValue];
    } else {
        [self takePictureWithOrientation:options resolve:resolve reject:reject];
        return;
    }
    
    // change preset quality for takePicture quality
    [self updateSessionPreset:self.pictureSize withoutCommit:NO];
    
    AVCaptureConnection *connection = [self.stillImageOutput connectionWithMediaType:AVMediaTypeVideo];
    [connection setVideoOrientation:orientation];
    [self.stillImageOutput captureStillImageAsynchronouslyFromConnection:connection completionHandler: ^(CMSampleBufferRef imageSampleBuffer, NSError *error) {
        if (imageSampleBuffer && !error) {
            BOOL useFastMode = options[@"fastMode"] && [options[@"fastMode"] boolValue];
            if (useFastMode) {
                resolve(nil);
            }
            NSData *imageData = [AVCaptureStillImageOutput jpegStillImageNSDataRepresentation:imageSampleBuffer];

            UIImage *takenImage = [UIImage imageWithData:imageData];

            CGImageRef takenCGImage = takenImage.CGImage;
            CGSize previewSize;
            if (UIInterfaceOrientationIsPortrait([[UIApplication sharedApplication] statusBarOrientation])) {
                previewSize = CGSizeMake(self.previewLayer.frame.size.height, self.previewLayer.frame.size.width);
            } else {
                previewSize = CGSizeMake(self.previewLayer.frame.size.width, self.previewLayer.frame.size.height);
            }
            CGRect cropRect = CGRectMake(0, 0, CGImageGetWidth(takenCGImage), CGImageGetHeight(takenCGImage));
            CGRect croppedSize = AVMakeRectWithAspectRatioInsideRect(previewSize, cropRect);
            takenImage = [RNImageUtils cropImage:takenImage toRect:croppedSize];

            if ([options[@"mirrorImage"] boolValue]) {
                takenImage = [RNImageUtils mirrorImage:takenImage];
            }
            if ([options[@"forceUpOrientation"] boolValue]) {
                takenImage = [RNImageUtils forceUpOrientation:takenImage];
            }

            if ([options[@"width"] integerValue]) {
                takenImage = [RNImageUtils scaleImage:takenImage toWidth:[options[@"width"] integerValue]];
            }

            NSMutableDictionary *response = [[NSMutableDictionary alloc] init];
            float quality = [options[@"quality"] floatValue];
            NSData *takenImageData = UIImageJPEGRepresentation(takenImage, quality);
            NSString *path = [RNFileSystem generatePathInDirectory:[[RNFileSystem cacheDirectoryPath] stringByAppendingPathComponent:@"Camera"] withExtension:@".jpg"];
            if (![options[@"doNotSave"] boolValue]) {
                response[@"uri"] = [RNImageUtils writeImage:takenImageData toPath:path];
            }
            response[@"width"] = @(takenImage.size.width);
            response[@"height"] = @(takenImage.size.height);

            if ([options[@"base64"] boolValue]) {
                response[@"base64"] = [takenImageData base64EncodedStringWithOptions:0];
            }

            // reset preset quality for video recording quality
            [self updateSessionPreset:self.videoSize withoutCommit:NO];

            if ([options[@"exif"] boolValue]) {
                int imageRotation;
                switch (takenImage.imageOrientation) {
                    case UIImageOrientationLeft:
                    case UIImageOrientationRightMirrored:
                        imageRotation = 90;
                        break;
                    case UIImageOrientationRight:
                    case UIImageOrientationLeftMirrored:
                        imageRotation = -90;
                        break;
                    case UIImageOrientationDown:
                    case UIImageOrientationDownMirrored:
                        imageRotation = 180;
                        break;
                    case UIImageOrientationUpMirrored:
                    default:
                        imageRotation = 0;
                        break;
                }
                [RNImageUtils updatePhotoMetadata:imageSampleBuffer withAdditionalData:@{ @"Orientation": @(imageRotation) } inResponse:response]; // TODO
            }

            if (useFastMode) {
                [self onPictureSaved:@{@"data": response, @"id": options[@"id"]}];
            } else {
                resolve(response);
            }
        } else {
            reject(@"E_IMAGE_CAPTURE_FAILED", @"Image could not be captured", error);
        }
    }];
}
- (void)recordWithOrientation:(NSDictionary *)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject{
    [self.sensorOrientationChecker getDeviceOrientationWithBlock:^(UIInterfaceOrientation orientation) {
        NSMutableDictionary *tmpOptions = [options mutableCopy];
        tmpOptions[@"orientation"]=[NSNumber numberWithInteger:[self.sensorOrientationChecker convertToAVCaptureVideoOrientation: orientation]];
        [self record:tmpOptions resolve:resolve reject:reject];

    }];
}
- (void)record:(NSDictionary *)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    int orientation;
    if ([options[@"orientation"] integerValue]) {
        orientation = [options[@"orientation"] integerValue];
    } else {
        [self recordWithOrientation:options resolve:resolve reject:reject];
        return;
    }
    if (_videoDataOutput == nil || _audioDataOutput == nil) {
#if __has_include(<GoogleMobileVision/GoogleMobileVision.h>)
        [_faceDetectorManager stopFaceDetection];
        [self stopTextRecognition];
#endif
        [self setupVideoAudioData];
    }

    if (_isRecording || _videoRecordedResolve != nil || _videoRecordedReject != nil) {
      return;
    }

    if (options[@"maxDuration"]) {
        Float64 maxDuration = [options[@"maxDuration"] floatValue];
        [_rnAssetWriter setMaxDuration:maxDuration];
    }

    if (options[@"maxFileSize"]) {
        [_rnAssetWriter setMaxRecordedFileSize:[options[@"maxFileSize"] integerValue]];
    }
    
    if (options[@"mute"]) {
        [_rnAssetWriter setAudioIsMuted:[options[@"mute"] boolValue]];
    }

    if (options[@"codec"]) {
      if (@available(iOS 10, *)) {
        AVVideoCodecType videoCodecType = options[@"codec"];
        if ([self.videoDataOutput.availableVideoCodecTypes containsObject:videoCodecType]) {
            NSDictionary* videoSettings = _videoSettings;
            [videoSettings setValue:videoCodecType forKey:AVVideoCodecKey];
            [_rnAssetWriter setVideoSettings:videoSettings];
            self.videoCodecType = videoCodecType;
        } else {
            RCTLogWarn(@"%s: Setting videoCodec is only supported above iOS version 10.", __func__);
        }
      }
    } else if (self.videoCodecType == nil) {
        self.videoCodecType = AVVideoCodecH264;
    }
    
    NSString *path = nil;
    if (options[@"path"]) {
        path = options[@"path"];
    }
    else {
        path = [RNFileSystem generatePathInDirectory:[[RNFileSystem cacheDirectoryPath] stringByAppendingPathComponent:@"Camera"] withExtension:@".mov"];
    }

    NSURL *outputURL = [[NSURL alloc] initFileURLWithPath:path];
    [_rnAssetWriter setOutputURL:outputURL];
    [_rnAssetWriter setVideoSettings:_videoSettings];
    [_rnAssetWriter setAudioSettings:_audioSettings];
    self.videoRecordedResolve = resolve;
    self.videoRecordedReject = reject;
    [_rnAssetWriter initRecording];
    self.isRecording = true;
    
    double delayInSeconds = [_rnAssetWriter maxDuration];
    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delayInSeconds * NSEC_PER_SEC));
    dispatch_after(popTime, _sessionQueue, ^(void) {
        [self stopRecording];
        
        @try {
            [_rnAssetWriter finishWritingWithCompletionHandler:^{
                [self didFinishRecordingToOutputFileAtURL:[_rnAssetWriter outputURL] error:nil];
            }];
        } @catch (NSException *exception) {
            RCTLogWarn(@"%@", exception.reason);
        }
    });
}

- (void)stopRecording
{
    self.isRecording = false;
}

- (void)resumePreview
{
    [[self.previewLayer connection] setEnabled:YES];
}

- (void)pausePreview
{
    [[self.previewLayer connection] setEnabled:NO];
}

- (void)startSession
{
#if TARGET_IPHONE_SIMULATOR
    return;
#endif
    if (self.presetCamera == AVCaptureDevicePositionUnspecified) {
        return;
    }
    
    [_session beginConfiguration];
    
    [self updateSessionPreset:self.videoSize withoutCommit:YES];

    AVCaptureStillImageOutput *stillImageOutput = [[AVCaptureStillImageOutput alloc] init];
    if ([self.session canAddOutput:stillImageOutput]) {
        stillImageOutput.outputSettings = @{AVVideoCodecKey : AVVideoCodecJPEG};
        [self.session addOutput:stillImageOutput];
        [stillImageOutput setHighResolutionStillImageOutputEnabled:YES];
        self.stillImageOutput = stillImageOutput;
    }
    
    [self setupVideoAudioData];
    [_session commitConfiguration];

#if __has_include(<GoogleMobileVision/GoogleMobileVision.h>)
//        [_faceDetectorManager maybeStartFaceDetectionOnSession:_session withPreviewLayer:_previewLayer];
    if ([self.textDetector isRealDetector]) {
        [self setupOrDisableTextDetector];
    }
#endif
    [self setupOrDisableBarcodeScanner];

    __weak RNCamera *weakSelf = self;
    [self setRuntimeErrorHandlingObserver:
     [NSNotificationCenter.defaultCenter addObserverForName:AVCaptureSessionRuntimeErrorNotification object:self.session queue:nil usingBlock:^(NSNotification *note) {
        RNCamera *strongSelf = weakSelf;
        dispatch_async(strongSelf.sessionQueue, ^{
            // Manually restarting the session since it must
            // have been stopped due to an error.
            [strongSelf.session startRunning];
            [strongSelf onReady:nil];
            RCTLogWarn(@"Session interrupted %@", note.userInfo);
        });
    }]];

    [self.session startRunning];
    [self onReady:nil];
}

- (void)stopSession
{
#if TARGET_IPHONE_SIMULATOR
    return;
#endif
    dispatch_async(self.sessionQueue, ^{
#if __has_include(<GoogleMobileVision/GoogleMobileVision.h>)
        [_faceDetectorManager stopFaceDetection];
#endif
        if ([self.textDetector isRealDetector]) {
            [self stopTextRecognition];
        }
        [self.previewLayer removeFromSuperlayer];
        [self.session commitConfiguration];
        [self.session stopRunning];
        for (AVCaptureInput *input in self.session.inputs) {
            [self.session removeInput:input];
        }

        for (AVCaptureOutput *output in self.session.outputs) {
            [self.session removeOutput:output];
        }
    });
}

- (void)initializeCaptureSessionInput
{
    if (self.videoCaptureDeviceInput.device.position == self.presetCamera) {
        return;
    }
    __block UIInterfaceOrientation interfaceOrientation;

    void (^statusBlock)() = ^() {
        interfaceOrientation = [[UIApplication sharedApplication] statusBarOrientation];
    };
    if ([NSThread isMainThread]) {
        statusBlock();
    } else {
        dispatch_sync(dispatch_get_main_queue(), statusBlock);
    }

    AVCaptureVideoOrientation orientation = [RNCameraUtils videoOrientationForInterfaceOrientation:interfaceOrientation];
    
    NSError *error = nil;
    AVCaptureDevice *captureDevice = [RNCameraUtils deviceWithMediaType:AVMediaTypeVideo preferringPosition:self.presetCamera];
    AVCaptureDeviceInput *captureDeviceInput = [AVCaptureDeviceInput deviceInputWithDevice:captureDevice error:&error];

    if (error || captureDeviceInput == nil) {
        RCTLog(@"%s: %@", __func__, error);
        return;
    }
    
    [self.session beginConfiguration];
    
    if ([_session canSetSessionPreset:AVCaptureSessionPresetHigh]) {
        _session.sessionPreset = AVCaptureSessionPresetHigh;
    } else {
        RCTLogWarn(@"cannot set video session preset");
    }
    
    [self.session removeInput:self.videoCaptureDeviceInput];
    if ([self.session canAddInput:captureDeviceInput]) {
        self.videoCaptureDeviceInput = captureDeviceInput;
        [self.session addInput:captureDeviceInput];

        [self updateFlashMode];
        [self updateZoom];
        [self updateFocusMode];
        [self updateFocusDepth];
        [self updateAutoFocusPointOfInterest];
        [self updateWhiteBalance];
        [self.previewLayer.connection setVideoOrientation:orientation];
        [self _updateMetadataObjectsToRecognize];
    } else {
        RCTLogWarn(@"cannot set video session preset");
    }
    
    [self updateSessionPreset:self.videoSize withoutCommit:YES];

    [self.session commitConfiguration];
    
    error = nil;
    captureDevice = [RNCameraUtils deviceWithMediaType:AVMediaTypeVideo preferringPosition:self.presetCamera];
    captureDeviceInput = [AVCaptureDeviceInput deviceInputWithDevice:captureDevice error:&error];
    
    if (error || captureDeviceInput == nil) {
        RCTLog(@"%s: %@", __func__, error);
        return;
    }
}

- (void)initializeAudioSessionInput
{
    NSError *error = nil;
    
    AVCaptureDevice *audioCaptureDevice = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeAudio];
    AVCaptureDeviceInput *audioDeviceInput = [AVCaptureDeviceInput deviceInputWithDevice:audioCaptureDevice error:&error];
    
    if (error || audioDeviceInput == nil) {
        RCTLogWarn(@"%s: %@", __func__, error);
        return;
    }
    
    [self.session removeInput:self.audioCaptureDeviceInput];
    if ([self.session canAddInput:audioDeviceInput]) {
        [self.session addInput:audioDeviceInput];
        self.audioCaptureDeviceInput = audioDeviceInput;
    } else {
        RCTLogWarn(@"cannot set audio session input");
    }
}

#pragma mark - internal

- (void)updateSessionPreset:(AVCaptureSessionPreset)preset withoutCommit:(BOOL)withoutCommit
{
#if !(TARGET_IPHONE_SIMULATOR)
    if ([preset integerValue] < 0) {
        return;
    }
    if (preset) {
        if (![self.session canSetSessionPreset:preset]) {
            RCTLogWarn(@"cannot set preset!");
        } else {
            if (!withoutCommit) {
                [self.session beginConfiguration];
            }
            
            self.session.sessionPreset = preset;
            
            if (!withoutCommit) {
                [self.session commitConfiguration];
            }
        }
    }
#endif
}

- (void)updateSessionAudioIsMuted:(BOOL)isMuted
{
    dispatch_async(self.sessionQueue, ^{
        [self.session beginConfiguration];

        for (AVCaptureDeviceInput* input in [self.session inputs]) {
            if ([input.device hasMediaType:AVMediaTypeAudio]) {
                if (isMuted) {
                    [self.session removeInput:input];
                }
                [self.session commitConfiguration];
                return;
            }
        }

        if (!isMuted) {
            NSError *error = nil;

            AVCaptureDevice *audioCaptureDevice = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeAudio];
            AVCaptureDeviceInput *audioDeviceInput = [AVCaptureDeviceInput deviceInputWithDevice:audioCaptureDevice error:&error];

            if (error || audioDeviceInput == nil) {
                RCTLogWarn(@"%s: %@", __func__, error);
                return;
            }

            if ([self.session canAddInput:audioDeviceInput]) {
                [self.session addInput:audioDeviceInput];
            }
        }

        [self.session commitConfiguration];
    });
}

- (void)bridgeDidForeground:(NSNotification *)notification
{
    if (![self.session isRunning] && [self isSessionPaused]) {
        self.paused = NO;
        dispatch_async( self.sessionQueue, ^{
            [self.session startRunning];
        });
    }
}

- (void)bridgeDidBackground:(NSNotification *)notification
{
    if ([self.session isRunning] && ![self isSessionPaused]) {
        self.paused = YES;
        dispatch_async( self.sessionQueue, ^{
            [self.session stopRunning];
        });
    }
}

- (void)orientationChanged:(NSNotification *)notification
{
    UIInterfaceOrientation orientation = [[UIApplication sharedApplication] statusBarOrientation];
    [self changePreviewOrientation:orientation];
    [self updateVideoSettings:orientation];
    [self initializeAudioSessionInput];
    [self updateType];
}

- (void)updateVideoSettings:(UIInterfaceOrientation)orientation {
    CGSize size = [RNCameraUtils sizeForSessionPreset:self.videoSize];
    
    if (UIInterfaceOrientationIsLandscape(orientation)) {
        CGSize size2 = size;
        size = CGSizeMake(size2.height, size2.width);
    }
    
    self.videoSettings = [NSDictionary dictionaryWithObjectsAndKeys:
                          self.videoCodecType, AVVideoCodecKey,
                          [NSNumber numberWithInt:size.width], AVVideoWidthKey,
                          [NSNumber numberWithInt:size.height], AVVideoHeightKey,
                          nil];
}

- (void)changePreviewOrientation:(UIInterfaceOrientation)orientation
{
    __weak typeof(self) weakSelf = self;
    AVCaptureVideoOrientation videoOrientation = [RNCameraUtils videoOrientationForInterfaceOrientation:orientation];
    dispatch_async(dispatch_get_main_queue(), ^{
        __strong typeof(self) strongSelf = weakSelf;
        if (strongSelf && strongSelf.previewLayer.connection.isVideoOrientationSupported) {
            [strongSelf.previewLayer.connection setVideoOrientation:videoOrientation];
        }
    });
}

# pragma mark - AVCaptureMetadataOutput

- (void)setupOrDisableBarcodeScanner
{
    [self _setupOrDisableMetadataOutput];
    [self _updateMetadataObjectsToRecognize];
}

- (void)_setupOrDisableMetadataOutput
{
    if ([self isReadingBarCodes] && (_metadataOutput == nil || ![self.session.outputs containsObject:_metadataOutput])) {
        AVCaptureMetadataOutput *metadataOutput = [[AVCaptureMetadataOutput alloc] init];
        if ([self.session canAddOutput:metadataOutput]) {
            [metadataOutput setMetadataObjectsDelegate:self queue:self.sessionQueue];
            [self.session addOutput:metadataOutput];
            self.metadataOutput = metadataOutput;
        }
    } else if (_metadataOutput != nil && ![self isReadingBarCodes]) {
        [self.session removeOutput:_metadataOutput];
        _metadataOutput = nil;
    }
}

- (void)_updateMetadataObjectsToRecognize
{
    if (_metadataOutput == nil) {
        return;
    }

    NSArray<AVMetadataObjectType> *availableRequestedObjectTypes = [[NSArray alloc] init];
    NSArray<AVMetadataObjectType> *requestedObjectTypes = [NSArray arrayWithArray:self.barCodeTypes];
    NSArray<AVMetadataObjectType> *availableObjectTypes = _metadataOutput.availableMetadataObjectTypes;

    for(AVMetadataObjectType objectType in requestedObjectTypes) {
        if ([availableObjectTypes containsObject:objectType]) {
            availableRequestedObjectTypes = [availableRequestedObjectTypes arrayByAddingObject:objectType];
        }
    }

    [_metadataOutput setMetadataObjectTypes:availableRequestedObjectTypes];
}

- (void)captureOutput:(AVCaptureOutput *)captureOutput didOutputMetadataObjects:(NSArray *)metadataObjects
       fromConnection:(AVCaptureConnection *)connection
{
    for(AVMetadataObject *metadata in metadataObjects) {
        if([metadata isKindOfClass:[AVMetadataMachineReadableCodeObject class]]) {
            AVMetadataMachineReadableCodeObject *codeMetadata = (AVMetadataMachineReadableCodeObject *) metadata;
            for (id barcodeType in self.barCodeTypes) {
                if ([metadata.type isEqualToString:barcodeType]) {
                    AVMetadataMachineReadableCodeObject *transformed = (AVMetadataMachineReadableCodeObject *)[_previewLayer transformedMetadataObjectForMetadataObject:metadata];
                    NSDictionary *event = @{
                                            @"type" : codeMetadata.type,
                                            @"data" : codeMetadata.stringValue,
                                            @"bounds": @{
                                                @"origin": @{
                                                    @"x": [NSString stringWithFormat:@"%f", transformed.bounds.origin.x],
                                                    @"y": [NSString stringWithFormat:@"%f", transformed.bounds.origin.y]
                                                },
                                                @"size": @{
                                                    @"height": [NSString stringWithFormat:@"%f", transformed.bounds.size.height],
                                                    @"width": [NSString stringWithFormat:@"%f", transformed.bounds.size.width]
                                                }
                                            }
                                            };

                    [self onCodeRead:event];
                }
            }
        }
    }
}

# pragma mark - AVCaptureVideoAudioDataOutput
         
- (void)setupVideoAudioData
{
    [_session removeOutput:_videoDataOutput];
    _videoDataOutput = [[AVCaptureVideoDataOutput alloc] init];
    
    dispatch_queue_t videoQueue = dispatch_queue_create("com.mariusreimer.videoQueue", dispatch_queue_attr_make_with_qos_class(DISPATCH_QUEUE_SERIAL, QOS_CLASS_USER_INITIATED, 0));
    
    // discard if the data output queue is blocked (as we process the still image
    [_videoDataOutput setAlwaysDiscardsLateVideoFrames:YES];
    
    [_videoDataOutput setSampleBufferDelegate:self queue:videoQueue];
    [_videoDataOutput setVideoSettings:@{ (NSString *)kCVPixelBufferPixelFormatTypeKey : @(kCVPixelFormatType_420YpCbCr8BiPlanarFullRange) }];
    
    if ([self.session canAddOutput:_videoDataOutput]) {
        [self.session addOutput:_videoDataOutput];
    } else {
        RCTLogWarn(@"Failed to setup video data output");
    }
    
    AVCaptureConnection *connection = [_videoDataOutput connectionWithMediaType:AVMediaTypeVideo];
    
    if (connection != nil) {
        if (self.videoStabilizationMode != 0) {
            if (connection.isVideoStabilizationSupported == NO) {
                RCTLogWarn(@"%s: Video Stabilization is not supported on this device.", __func__);
            } else {
                [connection setPreferredVideoStabilizationMode:self.videoStabilizationMode];
            }
        }
        
        if (self.mirrorVideo) {
            if ([connection isVideoMirroringSupported]) {
                [connection setAutomaticallyAdjustsVideoMirroring:NO];
                [connection setVideoMirrored:YES];
            }
        }
        
        [self updateFlashMode];
        
        [connection setVideoOrientation:[RNCameraUtils videoOrientationForInterfaceOrientation:[[UIApplication sharedApplication] statusBarOrientation]]];
    } else {
        RCTLogWarn(@"no video connection!");
    }
    
    [self setupAudioData];
}

- (void) setupAudioData {
    [_session removeOutput:_audioDataOutput];
    _audioDataOutput = [[AVCaptureAudioDataOutput alloc] init];
    
    dispatch_queue_t audioQueue = dispatch_queue_create("com.mariusreimer.audioQueue", dispatch_queue_attr_make_with_qos_class(DISPATCH_QUEUE_SERIAL, QOS_CLASS_USER_INITIATED, 0));
    [_audioDataOutput setSampleBufferDelegate:self queue:audioQueue];
    
    if ([self.session canAddOutput:_audioDataOutput]) {
        [self.session addOutput:_audioDataOutput];
    } else {
        RCTLogWarn(@"Failed to setup audio data output");
    }
    NSDictionary *audioSettings = [_audioDataOutput recommendedAudioSettingsForAssetWriterWithOutputFileType:AVFileTypeMPEG4];
    
    if (audioSettings == nil) {
        audioSettings = @{
                          AVEncoderBitRateKey: @(64000),
                          AVFormatIDKey: @(kAudioFormatMPEG4AAC),
                          AVSampleRateKey: @([[AVAudioSession sharedInstance] sampleRate]),
                          AVNumberOfChannelsKey: @(1)
                          };
    }
    
    self.audioSettings = audioSettings;
}

- (void) initAssetWriter
{
    if (self.rnAssetWriter == nil) {
        self.rnAssetWriter = [[RNAssetWriter alloc] init];
    }
}

- (void)didFinishRecordingToOutputFileAtURL:(NSURL *)outputFileURL error:(NSError *)error
{
    BOOL success = YES;
    if ([error code] != noErr) {
        NSNumber *value = [[error userInfo] objectForKey:AVErrorRecordingSuccessfullyFinishedKey];
        if (value) {
            success = [value boolValue];
        }
    }
    if (success && self.videoRecordedResolve != nil) {
      if (@available(iOS 10, *)) {
          AVVideoCodecType videoCodec = self.videoCodecType;
          if (videoCodec == nil) {
              videoCodec = [self.videoDataOutput.availableVideoCodecTypes firstObject];
          }
          self.videoRecordedResolve(@{ @"uri": outputFileURL.absoluteString, @"codec":videoCodec });
      } else {
          self.videoRecordedResolve(@{ @"uri": outputFileURL.absoluteString });
      }
    } else if (self.videoRecordedReject != nil) {
        self.videoRecordedReject(@"E_RECORDING_FAILED", @"An error occurred while recording a video.", error);
    }

    [self cleanupCamera];
}

- (void)cleanupCamera {
    self.videoRecordedResolve = nil;
    self.videoRecordedReject = nil;
//    self.videoCodecType = nil;

#if __has_include(<GoogleMobileVision/GoogleMobileVision.h>)
    // If face detection has been running prior to recording to file
    // we reenable it here (see comment in -record).
//    [_faceDetectorManager maybeStartFaceDetectionOnSession:_session withPreviewLayer:_previewLayer];
#endif
    [_session commitConfiguration];
    [self updateSessionPreset:self.videoSize withoutCommit:NO];

    if ([self.textDetector isRealDetector]) {
        [self setupOrDisableTextDetector];
    }
}

- (void)mirrorVideo:(NSURL *)inputURL completion:(void (^)(NSURL* outputUR))completion {
    AVAsset* videoAsset = [AVAsset assetWithURL:inputURL];
    AVAssetTrack* clipVideoTrack = [[videoAsset tracksWithMediaType:AVMediaTypeVideo] firstObject];

    AVMutableComposition* composition = [[AVMutableComposition alloc] init];
    [composition addMutableTrackWithMediaType:AVMediaTypeVideo preferredTrackID:kCMPersistentTrackID_Invalid];

    AVMutableVideoComposition* videoComposition = [[AVMutableVideoComposition alloc] init];
    videoComposition.renderSize = CGSizeMake(clipVideoTrack.naturalSize.height, clipVideoTrack.naturalSize.width);
    videoComposition.frameDuration = CMTimeMake(1, 30);

    AVMutableVideoCompositionLayerInstruction* transformer = [AVMutableVideoCompositionLayerInstruction videoCompositionLayerInstructionWithAssetTrack:clipVideoTrack];

    AVMutableVideoCompositionInstruction* instruction = [[AVMutableVideoCompositionInstruction alloc] init];
    instruction.timeRange = CMTimeRangeMake(kCMTimeZero, CMTimeMakeWithSeconds(60, 30));

    CGAffineTransform transform = CGAffineTransformMakeScale(-1.0, 1.0);
    transform = CGAffineTransformTranslate(transform, -clipVideoTrack.naturalSize.width, 0);
    transform = CGAffineTransformRotate(transform, M_PI/2.0);
    transform = CGAffineTransformTranslate(transform, 0.0, -clipVideoTrack.naturalSize.width);

    [transformer setTransform:transform atTime:kCMTimeZero];

    [instruction setLayerInstructions:@[transformer]];
    [videoComposition setInstructions:@[instruction]];

    // Export
    AVAssetExportSession* exportSession = [AVAssetExportSession exportSessionWithAsset:videoAsset presetName:AVAssetExportPreset640x480];
    NSString* filePath = [RNFileSystem generatePathInDirectory:[[RNFileSystem cacheDirectoryPath] stringByAppendingString:@"CameraFlip"] withExtension:@".mp4"];
    NSURL* outputURL = [NSURL fileURLWithPath:filePath];
    [exportSession setOutputURL:outputURL];
    [exportSession setOutputFileType:AVFileTypeMPEG4];
    [exportSession setVideoComposition:videoComposition];
    [exportSession exportAsynchronouslyWithCompletionHandler:^{
        if (exportSession.status == AVAssetExportSessionStatusCompleted) {
            dispatch_async(dispatch_get_main_queue(), ^{
                completion(outputURL);
            });
        } else {
            NSLog(@"Export failed %@", exportSession.error);
        }
    }];
}

# pragma mark - Face detector

- (id)createFaceDetectorManager
{
    Class faceDetectorManagerClass = NSClassFromString(@"RNFaceDetectorManager");
    Class faceDetectorManagerStubClass = NSClassFromString(@"RNFaceDetectorManagerStub");

#if __has_include(<GoogleMobileVision/GoogleMobileVision.h>)
    if (faceDetectorManagerClass) {
        return [[faceDetectorManagerClass alloc] initWithSessionQueue:_sessionQueue delegate:self];
    } else if (faceDetectorManagerStubClass) {
        return [[faceDetectorManagerStubClass alloc] init];
    }
#endif

    return nil;
}

- (void)onFacesDetected:(NSArray<NSDictionary *> *)faces
{
    if (_onFacesDetected) {
        _onFacesDetected(@{
                           @"type": @"face",
                           @"faces": faces
                           });
    }
}

# pragma mark - TextDetector

-(id)createTextDetector
{
    Class textDetectorManagerClass = NSClassFromString(@"TextDetectorManager");
    Class textDetectorManagerStubClass =
        NSClassFromString(@"TextDetectorManagerStub");

#if __has_include(<GoogleMobileVision/GoogleMobileVision.h>)
    if (textDetectorManagerClass) {
        return [[textDetectorManagerClass alloc] init];
    } else if (textDetectorManagerStubClass) {
        return [[textDetectorManagerStubClass alloc] init];
    }
#endif

    return nil;
}

- (void)setupOrDisableTextDetector
{
    if ([self canReadText] && [self.textDetector isRealDetector]){
        if (self.videoDataOutput == nil) {
            self.videoDataOutput = [[AVCaptureVideoDataOutput alloc] init];
            
            if (![self.session canAddOutput:_videoDataOutput]) {
                NSLog(@"Failed to setup video data output");
                [self stopTextRecognition];
                return;
            }
        }
        
        NSDictionary *rgbOutputSettings = [NSDictionary
            dictionaryWithObject:[NSNumber numberWithInt:kCMPixelFormat_32BGRA]
                            forKey:(id)kCVPixelBufferPixelFormatTypeKey];
        [self.videoDataOutput setVideoSettings:rgbOutputSettings];
        [self.videoDataOutput setAlwaysDiscardsLateVideoFrames:YES];
        [self.videoDataOutput setSampleBufferDelegate:self queue:self.sessionQueue];
        [self.session addOutput:_videoDataOutput];
    } else {
        [self stopTextRecognition];
    }
}

- (void)captureOutput:(AVCaptureOutput *)captureOutput
    didOutputSampleBuffer:(CMSampleBufferRef)sampleBuffer
           fromConnection:(AVCaptureConnection *)connection
{
    CFRetain(sampleBuffer);
    CVImageBufferRef imageBuffer = CMSampleBufferGetImageBuffer(sampleBuffer);
    CVPixelBufferLockBaseAddress(imageBuffer, 0);
    
    if (_isRecording) {
        if (captureOutput == _audioDataOutput) {
            [_rnAssetWriter addAudioData:sampleBuffer];
        } else if (captureOutput == _videoDataOutput) {
            [_rnAssetWriter addVideoData:imageBuffer];
            
            if (_faceDetectingWhileRecording) {
                //TODO
            }
        }
    } else if (!_isRecording && captureOutput == _videoDataOutput) {
        //TODO
    }
    
    CVPixelBufferUnlockBaseAddress(imageBuffer, 0);
    
    CFRelease(sampleBuffer);
    
    if (![self.textDetector isRealDetector]) {
        return;
    }

    // Do not submit image for text recognition too often:
    // 1. we only dispatch events every 500ms anyway
    // 2. wait until previous recognition is finished
    // 3. let user disable text recognition, e.g. onTextRecognized={someCondition ? null : this.textRecognized}
    NSDate *methodFinish = [NSDate date];
    NSTimeInterval timePassed = [methodFinish timeIntervalSinceDate:self.start];
    if (timePassed > 0.5 && _finishedReadingText && [self canReadText]) {
        CGSize previewSize = CGSizeMake(_previewLayer.frame.size.width, _previewLayer.frame.size.height);
        UIImage *image = [RNCameraUtils convertBufferToUIImage:sampleBuffer previewSize:previewSize];
        // take care of the fact that preview dimensions differ from the ones of the image that we submit for text detection
        float scaleX = _previewLayer.frame.size.width / image.size.width;
        float scaleY = _previewLayer.frame.size.height / image.size.height;

        // find text features
        _finishedReadingText = false;
        self.start = [NSDate date];
        NSArray *textBlocks = [self.textDetector findTextBlocksInFrame:image scaleX:scaleX scaleY:scaleY];
        NSDictionary *eventText = @{@"type" : @"TextBlock", @"textBlocks" : textBlocks};
        [self onText:eventText];

        _finishedReadingText = true;
    }
}

- (void)stopTextRecognition
{
    if (self.videoDataOutput) {
        [self.session removeOutput:self.videoDataOutput];
    }
    self.videoDataOutput = nil;
}

@end
