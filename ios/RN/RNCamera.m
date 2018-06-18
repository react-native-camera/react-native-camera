#import "RNCamera.h"
#import "RNCameraUtils.h"
#import "RNImageUtils.h"
#import "RNFileSystem.h"
#import <React/RCTEventDispatcher.h>
#import <React/RCTLog.h>
#import <React/RCTUtils.h>
#import <React/UIView+React.h>

@interface RNCamera ()

@property (nonatomic, weak) RCTBridge *bridge;

@property (nonatomic, assign, getter=isSessionPaused) BOOL paused;
@property (nonatomic, strong) id faceDetectorManager;

@property (nonatomic, copy) RCTDirectEventBlock onCameraReady;
@property (nonatomic, copy) RCTDirectEventBlock onMountError;
@property (nonatomic, copy) RCTDirectEventBlock onBarCodeRead;
@property (nonatomic, copy) RCTDirectEventBlock onFacesDetected;
@property (nonatomic, copy) RCTDirectEventBlock onPictureSaved;

@end

@implementation RNCamera

static NSDictionary *defaultFaceDetectorOptions = nil;

- (id)initWithBridge:(RCTBridge *)bridge
{
    if ((self = [super init])) {
        self.bridge = bridge;
        self.session = [AVCaptureSession new];
        self.sessionQueue = dispatch_queue_create("cameraQueue", DISPATCH_QUEUE_SERIAL);
        self.frameBufferQueue = dispatch_queue_create("frameQueue", DISPATCH_QUEUE_SERIAL);
        self.faceDetectorManager = [self createFaceDetectorManager];
#if !(TARGET_IPHONE_SIMULATOR)
        self.previewLayer =
        [AVCaptureVideoPreviewLayer layerWithSession:self.session];
        self.previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill;
        self.previewLayer.needsDisplayOnBoundsChange = YES;
#endif
        self.paused = NO;
        [self changePreviewOrientation:[UIApplication sharedApplication].statusBarOrientation];
        [self initializeCaptureSessionInput];
        [self startSession];
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(orientationChanged:)
                                                     name:UIDeviceOrientationDidChangeNotification
                                                   object:nil];
        self.autoFocus = -1;
    }
    return self;
}

- (UIColor *)backgroundColor
{
    return [UIColor blackColor];
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

- (void)layoutSubviews
{
    [super layoutSubviews];
    self.previewLayer.frame = self.bounds;
    [self.layer insertSublayer:self.previewLayer atIndex:0];
}

- (void)insertReactSubview:(UIView *)view atIndex:(NSInteger)atIndex
{
    [self insertSubview:view atIndex:atIndex + 1];
    [super insertReactSubview:view atIndex:atIndex];
    return;
}

- (void)removeReactSubview:(UIView *)subview
{
    [subview removeFromSuperview];
    [super removeReactSubview:subview];
    return;
}

- (void)removeFromSuperview
{
    [self stopSession];
    [super removeFromSuperview];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIDeviceOrientationDidChangeNotification object:nil];
}

-(void)updateType
{
    dispatch_async(self.sessionQueue, ^{
        [self initializeCaptureSessionInput];
        if (!self.session.isRunning) {
            [self startSession];
        }
    });
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
                if ([device isTorchModeSupported:AVCaptureTorchModeOff]) {
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
    [self updateSessionPreset:self.pictureSize];
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

- (void)takePicture:(NSDictionary *)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    AVCaptureConnection *connection = [self.stillImageOutput connectionWithMediaType:AVMediaTypeVideo];
    [connection setVideoOrientation:[RNCameraUtils videoOrientationForInterfaceOrientation:[[UIApplication sharedApplication] statusBarOrientation]]];

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
            response[@"uri"] = [RNImageUtils writeImage:takenImageData toPath:path];
            response[@"width"] = @(takenImage.size.width);
            response[@"height"] = @(takenImage.size.height);

            if ([options[@"base64"] boolValue]) {
                response[@"base64"] = [takenImageData base64EncodedStringWithOptions:0];
            }

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

- (void)record:(NSDictionary *)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
#if __has_include(<GoogleMobileVision/GoogleMobileVision.h>)
    [_faceDetectorManager stopFaceDetection];
#endif

    NSError *error = nil;
    NSString *path = [RNFileSystem generatePathInDirectory:[[RNFileSystem cacheDirectoryPath] stringByAppendingPathComponent:@"Camera"] withExtension:@".mov"];
    NSURL *outputURL = [[NSURL alloc] initFileURLWithPath:path];
    self.videoWriter = [[AVAssetWriter alloc] initWithURL:outputURL fileType:AVFileTypeQuickTimeMovie error:&error];
    if (error != nil) {
        NSLog(@"%@", error);
        reject(RCTErrorUnspecified, nil, RCTErrorWithMessage(error.description));
        return;
    }

    NSDictionary *outputSettings = [self.videoOutput recommendedVideoSettingsForAssetWriterWithOutputFileType:AVFileTypeQuickTimeMovie];
    self.writerInput = [AVAssetWriterInput assetWriterInputWithMediaType:AVMediaTypeVideo outputSettings:outputSettings];
    [self.videoWriter addInput:self.writerInput];

    // Base transformation that rotates the video based on device orientation.
    CGAffineTransform videoTransform = [RNCameraUtils videoTransformForOrientation:[[UIApplication sharedApplication] statusBarOrientation]];

    if (options[@"quality"]) {
        [self updateSessionPreset:[RNCameraUtils captureSessionPresetForVideoResolution:(RNCameraVideoResolution)[options[@"quality"] integerValue]]];
    }

    [self updateSessionAudioIsMuted:!!options[@"mute"]];

    AVCaptureConnection *connection = [self.movieFileOutput connectionWithMediaType:AVMediaTypeVideo];
    if (self.videoStabilizationMode != 0) {
        if (connection.isVideoStabilizationSupported == NO) {
            RCTLogWarn(@"%s: Video Stabilization is not supported on this device.", __func__);
        } else {
            [connection setPreferredVideoStabilizationMode:self.videoStabilizationMode];
        }
    }

    if ([options[@"mirrorImage"] boolValue]) {
        videoTransform = CGAffineTransformScale(videoTransform, 1, -1);
    }
    [connection setVideoOrientation:[RNCameraUtils videoOrientationForDeviceOrientation:[[UIDevice currentDevice] orientation]]];

    if (options[@"codec"]) {
      if (@available(iOS 10, *)) {
        AVVideoCodecType videoCodecType = options[@"codec"];
        if ([self.movieFileOutput.availableVideoCodecTypes containsObject:videoCodecType]) {
          [self.movieFileOutput setOutputSettings:@{AVVideoCodecKey:videoCodecType} forConnection:connection];
          self.videoCodecType = videoCodecType;
        } else {
          RCTLogWarn(@"%s: Video Codec '%@' is not supported on this device.", __func__, videoCodecType);
        }
      } else {
        RCTLogWarn(@"%s: Setting videoCodec is only supported above iOS version 10.", __func__);
      }
    }

    [self updateSessionAudioIsMuted:!!options[@"mute"]];

    [self.writerInput setTransform:videoTransform];

    // @TODO: maxFileSize
    self.canAppendBuffer = YES;
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(self.maxDuration * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        self.canAppendBuffer = NO;

        [self.writerInput markAsFinished];
        [self.videoWriter finishWritingWithCompletionHandler:^{
            if (self.videoWriter.status == AVAssetWriterStatusFailed) {
                return reject(@"E_RECORDING_FAILED", @"An error occurred while recording a video.", nil);
            }

            resolve(@{ @"uri": self.videoWriter.outputURL.absoluteString });
        }];
    });
}

- (void)stopRecording
{
    self.canAppendBuffer = NO;
    [self.writerInput markAsFinished];
    [self.videoWriter finishWritingWithCompletionHandler:^{
        NSLog(@"%@", self.videoWriter.outputURL);
    }];
}

- (void)captureOutput:(AVCaptureOutput *)output didOutputSampleBuffer:(CMSampleBufferRef)sampleBuffer fromConnection:(AVCaptureConnection *)connection
{
    if (!self.canAppendBuffer) return;

    if (self.videoWriter.status != AVAssetWriterStatusWriting) {
        [self.videoWriter startWriting];
        [self.videoWriter startSessionAtSourceTime:CMSampleBufferGetPresentationTimeStamp(sampleBuffer)];
    }

    if (self.writerInput.readyForMoreMediaData) {
        [self.writerInput appendSampleBuffer:sampleBuffer];
    }
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
    //    NSDictionary *cameraPermissions = [EXCameraPermissionRequester permissions];
    //    if (![cameraPermissions[@"status"] isEqualToString:@"granted"]) {
    //        [self onMountingError:@{@"message": @"Camera permissions not granted - component could not be rendered."}];
    //        return;
    //    }
    self.canAppendBuffer = NO;
    dispatch_async(self.sessionQueue, ^{
        if (self.presetCamera == AVCaptureDevicePositionUnspecified) {
            return;
        }

        self.session.sessionPreset = AVCaptureSessionPresetPhoto;

        AVCaptureStillImageOutput *stillImageOutput = [[AVCaptureStillImageOutput alloc] init];
        if ([self.session canAddOutput:stillImageOutput]) {
            stillImageOutput.outputSettings = @{AVVideoCodecKey : AVVideoCodecJPEG};
            [self.session addOutput:stillImageOutput];
            [stillImageOutput setHighResolutionStillImageOutputEnabled:YES];
            self.stillImageOutput = stillImageOutput;
        }

#if __has_include(<GoogleMobileVision/GoogleMobileVision.h>)
        [_faceDetectorManager maybeStartFaceDetectionOnSession:_session withPreviewLayer:_previewLayer];
#endif

        [self setupVideoStreamCapture];
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
            });
        }]];

        [self.session startRunning];
        [self onReady:nil];
    });
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
    dispatch_async(self.sessionQueue, ^{
        [self.session beginConfiguration];

        NSError *error = nil;
        AVCaptureDevice *captureDevice = [RNCameraUtils deviceWithMediaType:AVMediaTypeVideo preferringPosition:self.presetCamera];
        AVCaptureDeviceInput *captureDeviceInput = [AVCaptureDeviceInput deviceInputWithDevice:captureDevice error:&error];

        if (error || captureDeviceInput == nil) {
            RCTLog(@"%s: %@", __func__, error);
            return;
        }

        [self.session removeInput:self.videoCaptureDeviceInput];
        if ([self.session canAddInput:captureDeviceInput]) {
            [self.session addInput:captureDeviceInput];

            self.videoCaptureDeviceInput = captureDeviceInput;
            [self updateFlashMode];
            [self updateZoom];
            [self updateFocusMode];
            [self updateFocusDepth];
            [self updateWhiteBalance];
            [self.previewLayer.connection setVideoOrientation:orientation];
            [self _updateMetadataObjectsToRecognize];
        }

        [self.session commitConfiguration];
    });
}

#pragma mark - internal

- (void)updateSessionPreset:(AVCaptureSessionPreset)preset
{
#if !(TARGET_IPHONE_SIMULATOR)
    if ([preset integerValue] < 0) {
        return;
    }
    if (preset) {
        if (self.isDetectingFaces && [preset isEqual:AVCaptureSessionPresetPhoto]) {
            RCTLog(@"AVCaptureSessionPresetPhoto not supported during face detection. Falling back to AVCaptureSessionPresetHigh");
            preset = AVCaptureSessionPresetHigh;
        }
        dispatch_async(self.sessionQueue, ^{
            [self.session beginConfiguration];
            if ([self.session canSetSessionPreset:preset]) {
                self.session.sessionPreset = preset;
            }
            [self.session commitConfiguration];
        });
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

# pragma mark - AVAssetWriter

- (void)setupVideoStreamCapture
{
    // @TODO: videoCodecType
    self.videoOutput = [[AVCaptureVideoDataOutput alloc] init];
    self.videoOutput.videoSettings = @{ (id)kCVPixelBufferPixelFormatTypeKey : [NSNumber numberWithInteger:kCVPixelFormatType_32BGRA]};

    _frameBufferQueue = dispatch_queue_create("frameQueue", DISPATCH_QUEUE_SERIAL);
    [self.videoOutput setSampleBufferDelegate:self queue:_frameBufferQueue];
    self.videoOutput.alwaysDiscardsLateVideoFrames = YES;

    [self.session beginConfiguration];
    if ([self.session canAddOutput:self.videoOutput]){
        [self.session addOutput:self.videoOutput];
    }
    [self.session commitConfiguration];
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

@end
