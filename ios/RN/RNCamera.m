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
@property (nonatomic, strong) id textDetector;
@property (nonatomic, strong) id faceDetector;
@property (nonatomic, strong) id barcodeDetector;

@property (nonatomic, copy) RCTDirectEventBlock onCameraReady;
@property (nonatomic, copy) RCTDirectEventBlock onMountError;
@property (nonatomic, copy) RCTDirectEventBlock onBarCodeRead;
@property (nonatomic, copy) RCTDirectEventBlock onTextRecognized;
@property (nonatomic, copy) RCTDirectEventBlock onFacesDetected;
@property (nonatomic, copy) RCTDirectEventBlock onGoogleVisionBarcodesDetected;
@property (nonatomic, copy) RCTDirectEventBlock onPictureSaved;
@property (nonatomic, assign) BOOL finishedReadingText;
@property (nonatomic, assign) BOOL finishedDetectingFace;
@property (nonatomic, assign) BOOL finishedDetectingBarcodes;
@property (nonatomic, copy) NSDate *startText;
@property (nonatomic, copy) NSDate *startFace;
@property (nonatomic, copy) NSDate *startBarcode;

@end

@implementation RNCamera

static NSDictionary *defaultFaceDetectorOptions = nil;

- (id)initWithBridge:(RCTBridge *)bridge
{
    if ((self = [super init])) {
        self.bridge = bridge;
        self.session = [AVCaptureSession new];
        self.sessionQueue = dispatch_queue_create("cameraQueue", DISPATCH_QUEUE_SERIAL);
        self.sensorOrientationChecker = [RNSensorOrientationChecker new];
        self.textDetector = [self createTextDetector];
        self.faceDetector = [self createFaceDetectorMlKit];
        self.barcodeDetector = [self createBarcodeDetectorMlKit];
        self.finishedReadingText = true;
        self.finishedDetectingFace = true;
        self.finishedDetectingBarcodes = true;
        self.startText = [NSDate date];
        self.startFace = [NSDate date];
        self.startBarcode = [NSDate date];
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

        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(bridgeDidBackground:)
                                                     name:UIApplicationDidEnterBackgroundNotification
                                                   object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                selector:@selector(bridgeDidForeground:)
                                                    name:UIApplicationWillEnterForegroundNotification
                                                object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(audioDidInterrupted:)
                                                     name:AVAudioSessionInterruptionNotification
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
    [self setBackgroundColor:[UIColor blackColor]];
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
    [super removeFromSuperview];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIDeviceOrientationDidChangeNotification object:nil];
    [self stopSession];
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
    [self updateSessionPreset:self.pictureSize];
}

- (void)takePictureWithOrientation:(NSDictionary *)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject{
    [self.sensorOrientationChecker getDeviceOrientationWithBlock:^(UIInterfaceOrientation orientation) {
        NSMutableDictionary *tmpOptions = [options mutableCopy];
        if ([tmpOptions valueForKey:@"orientation"] == nil) {
            tmpOptions[@"orientation"] = [NSNumber numberWithInteger:[self.sensorOrientationChecker convertToAVCaptureVideoOrientation:orientation]];
        }
        self.deviceOrientation = [NSNumber numberWithInteger:orientation];
        self.orientation = [NSNumber numberWithInteger:[tmpOptions[@"orientation"] integerValue]];
        [self takePicture:tmpOptions resolve:resolve reject:reject];
    }];
}
- (void)takePicture:(NSDictionary *)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    if (!self.deviceOrientation) {
        [self takePictureWithOrientation:options resolve:resolve reject:reject];
        return;
    }

    NSInteger orientation = [options[@"orientation"] integerValue];

    AVCaptureConnection *connection = [self.stillImageOutput connectionWithMediaType:AVMediaTypeVideo];
    [connection setVideoOrientation:orientation];
    [self.stillImageOutput captureStillImageAsynchronouslyFromConnection:connection completionHandler: ^(CMSampleBufferRef imageSampleBuffer, NSError *error) {
        if (imageSampleBuffer && !error) {
            if ([options[@"pauseAfterCapture"] boolValue]) {
                [[self.previewLayer connection] setEnabled:NO];
            }

            BOOL useFastMode = [options valueForKey:@"fastMode"] != nil && [options[@"fastMode"] boolValue];
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

            response[@"pictureOrientation"] = @([self.orientation integerValue]);
            response[@"deviceOrientation"] = @([self.deviceOrientation integerValue]);
            self.orientation = nil;
            self.deviceOrientation = nil;

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
        if ([tmpOptions valueForKey:@"orientation"] == nil) {
            tmpOptions[@"orientation"] = [NSNumber numberWithInteger:[self.sensorOrientationChecker convertToAVCaptureVideoOrientation: orientation]];
        }
        self.deviceOrientation = [NSNumber numberWithInteger:orientation];
        self.orientation = [NSNumber numberWithInteger:[tmpOptions[@"orientation"] integerValue]];
        [self record:tmpOptions resolve:resolve reject:reject];
    }];
}
- (void)record:(NSDictionary *)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    if (!self.deviceOrientation) {
        [self recordWithOrientation:options resolve:resolve reject:reject];
        return;
    }

    NSInteger orientation = [options[@"orientation"] integerValue];

    if (_movieFileOutput == nil) {
        // At the time of writing AVCaptureMovieFileOutput and AVCaptureVideoDataOutput (> GMVDataOutput)
        // cannot coexist on the same AVSession (see: https://stackoverflow.com/a/4986032/1123156).
        // We stop face detection here and restart it in when AVCaptureMovieFileOutput finishes recording.
        if ([self.textDetector isRealDetector]) {
            [self stopTextRecognition];
        }
        if ([self.faceDetector isRealDetector]) {
            [self stopFaceDetection];
        }
        if ([self.barcodeDetector isRealDetector]) {
            [self stopBarcodeDetection];
        }
        [self setupMovieFileCapture];
    }

    if (self.movieFileOutput == nil || self.movieFileOutput.isRecording || _videoRecordedResolve != nil || _videoRecordedReject != nil) {
      return;
    }

    if (options[@"maxDuration"]) {
        Float64 maxDuration = [options[@"maxDuration"] floatValue];
        self.movieFileOutput.maxRecordedDuration = CMTimeMakeWithSeconds(maxDuration, 30);
    }

    if (options[@"maxFileSize"]) {
        self.movieFileOutput.maxRecordedFileSize = [options[@"maxFileSize"] integerValue];
    }

    if (options[@"quality"]) {
        AVCaptureSessionPreset newQuality = [RNCameraUtils captureSessionPresetForVideoResolution:(RNCameraVideoResolution)[options[@"quality"] integerValue]];
        if (self.session.sessionPreset != newQuality) {
            [self updateSessionPreset:newQuality];
        }
    }

    // only update audio session when mute is not set or set to false, because otherwise there will be a flickering
    if ([options valueForKey:@"mute"] == nil || ([options valueForKey:@"mute"] != nil && ![options[@"mute"] boolValue])) {
        [self updateSessionAudioIsMuted:NO];
    }

    AVCaptureConnection *connection = [self.movieFileOutput connectionWithMediaType:AVMediaTypeVideo];
    if (self.videoStabilizationMode != 0) {
        if (connection.isVideoStabilizationSupported == NO) {
            RCTLogWarn(@"%s: Video Stabilization is not supported on this device.", __func__);
        } else {
            [connection setPreferredVideoStabilizationMode:self.videoStabilizationMode];
        }
    }
    [connection setVideoOrientation:orientation];

    if (options[@"codec"]) {
      if (@available(iOS 10, *)) {
        AVVideoCodecType videoCodecType = options[@"codec"];
        if ([self.movieFileOutput.availableVideoCodecTypes containsObject:videoCodecType]) {
          [self.movieFileOutput setOutputSettings:@{AVVideoCodecKey:videoCodecType} forConnection:connection];
          self.videoCodecType = videoCodecType;
        } else {
            RCTLogWarn(@"%s: Setting videoCodec is only supported above iOS version 10.", __func__);
        }
      }
    }

    dispatch_async(self.sessionQueue, ^{
        [self updateFlashMode];
        NSString *path = nil;
        if (options[@"path"]) {
            path = options[@"path"];
        }
        else {
            path = [RNFileSystem generatePathInDirectory:[[RNFileSystem cacheDirectoryPath] stringByAppendingPathComponent:@"Camera"] withExtension:@".mov"];
        }

        if ([options[@"mirrorVideo"] boolValue]) {
            if ([connection isVideoMirroringSupported]) {
                [connection setAutomaticallyAdjustsVideoMirroring:NO];
                [connection setVideoMirrored:YES];
            }
        }

        NSURL *outputURL = [[NSURL alloc] initFileURLWithPath:path];
        [self.movieFileOutput startRecordingToOutputFileURL:outputURL recordingDelegate:self];
        self.isRecording = YES;
        self.videoRecordedResolve = resolve;
        self.videoRecordedReject = reject;
    });
}

- (void)stopRecording
{
    if ([self.movieFileOutput isRecording]) {
        [self.movieFileOutput stopRecording];
    } else {
        RCTLogWarn(@"Video is not recording.");
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
    [self onReady:nil];
    return;
#endif
    dispatch_async(self.sessionQueue, ^{
        if (self.presetCamera == AVCaptureDevicePositionUnspecified) {
            return;
        }

        // Default video quality AVCaptureSessionPresetHigh if non is provided
        AVCaptureSessionPreset preset = ([self defaultVideoQuality]) ? [RNCameraUtils captureSessionPresetForVideoResolution:[[self defaultVideoQuality] integerValue]] : AVCaptureSessionPresetHigh;

        self.session.sessionPreset = preset == AVCaptureSessionPresetHigh ? AVCaptureSessionPresetPhoto: preset;

        AVCaptureStillImageOutput *stillImageOutput = [[AVCaptureStillImageOutput alloc] init];
        if ([self.session canAddOutput:stillImageOutput]) {
            stillImageOutput.outputSettings = @{AVVideoCodecKey : AVVideoCodecJPEG};
            [self.session addOutput:stillImageOutput];
            [stillImageOutput setHighResolutionStillImageOutputEnabled:YES];
            self.stillImageOutput = stillImageOutput;
        }

        // If AVCaptureVideoDataOutput is not required because of Google Vision
        // (see comment in -record), we go ahead and add the AVCaptureMovieFileOutput
        // to avoid an exposure rack on some devices that can cause the first few
        // frames of the recorded output to be underexposed.
        if (![self.faceDetector isRealDetector] && ![self.textDetector isRealDetector] && ![self.barcodeDetector isRealDetector]) {
            [self setupMovieFileCapture];
        }
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
        if ([self.textDetector isRealDetector]) {
            [self stopTextRecognition];
        }
        if ([self.faceDetector isRealDetector]) {
            [self stopFaceDetection];
        }
        if ([self.barcodeDetector isRealDetector]) {
            [self stopBarcodeDetection];
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
            [self updateAutoFocusPointOfInterest];
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
        if (self.canDetectFaces && [preset isEqual:AVCaptureSessionPresetPhoto]) {
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
    if (self.isRecording) {
        self.isRecordingInterrupted = YES;
    }
    if ([self.session isRunning] && ![self isSessionPaused]) {
        self.paused = YES;
        dispatch_async( self.sessionQueue, ^{
            [self.session stopRunning];
        });
    }
}

- (void)audioDidInterrupted:(NSNotification *)notification
{
    NSDictionary *userInfo = notification.userInfo;
    NSInteger type = [[userInfo valueForKey:AVAudioSessionInterruptionTypeKey] integerValue];
    switch (type) {
        case AVAudioSessionInterruptionTypeBegan:
            [self bridgeDidBackground: notification];
            break;
            
        case AVAudioSessionInterruptionTypeEnded:
            [self bridgeDidForeground: notification];
            break;
            
        default:
            break;
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
                    NSMutableDictionary *event = [NSMutableDictionary dictionaryWithDictionary:@{
                        @"type" : codeMetadata.type,
                        @"data" : [NSNull null],
                        @"rawData" : [NSNull null],
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
                        }
                    ];

                    NSData *rawData;
                    // If we're on ios11 then we can use `descriptor` to access the raw data of the barcode.
                    // If we're on an older version of iOS we're stuck using valueForKeyPath to peak at the
                    // data.
                    if (@available(iOS 11, *)) {
                        // descriptor is a CIBarcodeDescriptor which is an abstract base class with no useful fields.
                        // in practice it's a subclass, many of which contain errorCorrectedPayload which is the data we
                        // want. Instead of individually checking the class types, just duck type errorCorrectedPayload
                        if ([codeMetadata.descriptor respondsToSelector:@selector(errorCorrectedPayload)]) {
                            rawData = [codeMetadata.descriptor performSelector:@selector(errorCorrectedPayload)];
                        }
                    } else {
                        rawData = [codeMetadata valueForKeyPath:@"_internal.basicDescriptor.BarcodeRawData"];
                    }

                    // Now that we have the raw data of the barcode translate it into a hex string to pass to the JS
                    const unsigned char *dataBuffer = (const unsigned char *)[rawData bytes];
                    if (dataBuffer) {
                        NSMutableString     *rawDataHexString  = [NSMutableString stringWithCapacity:([rawData length] * 2)];
                        for (int i = 0; i < [rawData length]; ++i) {
                            [rawDataHexString appendString:[NSString stringWithFormat:@"%02lx", (unsigned long)dataBuffer[i]]];
                        }
                        [event setObject:[NSString stringWithString:rawDataHexString] forKey:@"rawData"];
                    }

                    // If we were able to extract a string representation of the barcode, attach it to the event as well
                    // else just send null along.
                    if (codeMetadata.stringValue) {
                        [event setObject:codeMetadata.stringValue forKey:@"data"];
                    }

                    // Only send the event if we were able to pull out a binary or string representation
                    if ([event objectForKey:@"data"] != [NSNull null] || [event objectForKey:@"rawData"] != [NSNull null]) {
                        [self onCodeRead:event];
                    }
                }
            }
        }
    }
}

# pragma mark - AVCaptureMovieFileOutput

- (void)setupMovieFileCapture
{
    AVCaptureMovieFileOutput *movieFileOutput = [[AVCaptureMovieFileOutput alloc] init];

    if ([self.session canAddOutput:movieFileOutput]) {
        [self.session addOutput:movieFileOutput];
        self.movieFileOutput = movieFileOutput;
    }
}

- (void)cleanupMovieFileCapture
{
    if ([_session.outputs containsObject:_movieFileOutput]) {
        [_session removeOutput:_movieFileOutput];
        _movieFileOutput = nil;
    }
}

- (void)captureOutput:(AVCaptureFileOutput *)captureOutput didFinishRecordingToOutputFileAtURL:(NSURL *)outputFileURL fromConnections:(NSArray *)connections error:(NSError *)error
{
    BOOL success = YES;
    if ([error code] != noErr) {
        NSNumber *value = [[error userInfo] objectForKey:AVErrorRecordingSuccessfullyFinishedKey];
        if (value) {
            success = [value boolValue];
        }
    }
    if (success && self.videoRecordedResolve != nil) {
        NSMutableDictionary *result = [[NSMutableDictionary alloc] init];

        void (^resolveBlock)(void) = ^() {
            self.videoRecordedResolve(result);
        };
        
        result[@"uri"] = outputFileURL.absoluteString;
        result[@"videoOrientation"] = @([self.orientation integerValue]);
        result[@"deviceOrientation"] = @([self.deviceOrientation integerValue]);
        result[@"isRecordingInterrupted"] = @(self.isRecordingInterrupted);


        if (@available(iOS 10, *)) {
            AVVideoCodecType videoCodec = self.videoCodecType;
            if (videoCodec == nil) {
                videoCodec = [self.movieFileOutput.availableVideoCodecTypes firstObject];
            }
            result[@"codec"] = videoCodec;

            if ([connections[0] isVideoMirrored]) {
                [self mirrorVideo:outputFileURL completion:^(NSURL *mirroredURL) {
                    result[@"uri"] = mirroredURL.absoluteString;
                    resolveBlock();
                }];
                return;
            }
        }

        resolveBlock();
    } else if (self.videoRecordedReject != nil) {
        self.videoRecordedReject(@"E_RECORDING_FAILED", @"An error occurred while recording a video.", error);
    }

    [self cleanupCamera];

}

- (void)cleanupCamera {
    self.videoRecordedResolve = nil;
    self.videoRecordedReject = nil;
    self.videoCodecType = nil;
    self.deviceOrientation = nil;
    self.orientation = nil;
    self.isRecording = NO;
    self.isRecordingInterrupted = NO;

    if ([self.textDetector isRealDetector] || [self.faceDetector isRealDetector]) {
        [self cleanupMovieFileCapture];
    }

    if ([self.textDetector isRealDetector]) {
        [self setupOrDisableTextDetector];
    }

    if ([self.faceDetector isRealDetector]) {
        [self setupOrDisableFaceDetector];
    }

    if ([self.barcodeDetector isRealDetector]) {
        [self setupOrDisableBarcodeDetector];
    }

    AVCaptureSessionPreset preset = [RNCameraUtils captureSessionPresetForVideoResolution:[self defaultVideoQuality]];
    if (self.session.sessionPreset != preset) {
        [self updateSessionPreset: preset == AVCaptureSessionPresetHigh ? AVCaptureSessionPresetPhoto: preset];
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

# pragma mark - FaceDetectorMlkit

-(id)createFaceDetectorMlKit
{
    Class faceDetectorManagerClassMlkit = NSClassFromString(@"FaceDetectorManagerMlkit");
    return [[faceDetectorManagerClassMlkit alloc] init];
}

- (void)setupOrDisableFaceDetector
{
    if (self.canDetectFaces && [self.faceDetector isRealDetector]){
        AVCaptureSessionPreset preset = ([self defaultVideoQuality]) ? [RNCameraUtils captureSessionPresetForVideoResolution:[[self defaultVideoQuality] integerValue]] : AVCaptureSessionPresetHigh;
        self.session.sessionPreset = preset;
        if (!self.videoDataOutput) {
            self.videoDataOutput = [[AVCaptureVideoDataOutput alloc] init];
            if (![self.session canAddOutput:_videoDataOutput]) {
                NSLog(@"Failed to setup video data output");
                [self stopFaceDetection];
                return;
            }
            
            NSDictionary *rgbOutputSettings = [NSDictionary
                dictionaryWithObject:[NSNumber numberWithInt:kCMPixelFormat_32BGRA]
                                forKey:(id)kCVPixelBufferPixelFormatTypeKey];
            [self.videoDataOutput setVideoSettings:rgbOutputSettings];
            [self.videoDataOutput setAlwaysDiscardsLateVideoFrames:YES];
            [self.videoDataOutput setSampleBufferDelegate:self queue:self.sessionQueue];
            [self.session addOutput:_videoDataOutput];
        }
    } else {
        [self stopFaceDetection];
    }
}

- (void)stopFaceDetection
{
    if (self.videoDataOutput && !self.canReadText) {
        [self.session removeOutput:self.videoDataOutput];
    }
    self.videoDataOutput = nil;
    AVCaptureSessionPreset preset = [RNCameraUtils captureSessionPresetForVideoResolution:[self defaultVideoQuality]];
    if (self.session.sessionPreset != preset) {
        [self updateSessionPreset: preset == AVCaptureSessionPresetHigh ? AVCaptureSessionPresetPhoto: preset];
    }
}

- (void)updateTrackingEnabled:(id)requestedTracking
{
    [self.faceDetector setTracking:requestedTracking queue:self.sessionQueue];
}

- (void)updateFaceDetectionMode:(id)requestedMode
{
    [self.faceDetector setPerformanceMode:requestedMode queue:self.sessionQueue];
}

- (void)updateFaceDetectionLandmarks:(id)requestedLandmarks
{   
    [self.faceDetector setLandmarksMode:requestedLandmarks queue:self.sessionQueue];
}

- (void)updateFaceDetectionClassifications:(id)requestedClassifications
{
    [self.faceDetector setClassificationMode:requestedClassifications queue:self.sessionQueue];
}

- (void)onFacesDetected:(NSDictionary *)event
{
    if (_onFacesDetected && _session) {
        _onFacesDetected(event);
    }
}

# pragma mark - BarcodeDetectorMlkit

-(id)createBarcodeDetectorMlKit
{
    Class barcodeDetectorManagerClassMlkit = NSClassFromString(@"BarcodeDetectorManagerMlkit");
    return [[barcodeDetectorManagerClassMlkit alloc] init];
}

- (void)setupOrDisableBarcodeDetector
{
    if (self.canDetectBarcodes && [self.barcodeDetector isRealDetector]){
        AVCaptureSessionPreset preset = ([self defaultVideoQuality]) ? [RNCameraUtils captureSessionPresetForVideoResolution:[[self defaultVideoQuality] integerValue]] : AVCaptureSessionPresetHigh;
        self.session.sessionPreset = preset;
        if (!self.videoDataOutput) {
            self.videoDataOutput = [[AVCaptureVideoDataOutput alloc] init];
            if (![self.session canAddOutput:_videoDataOutput]) {
                NSLog(@"Failed to setup video data output");
                [self stopBarcodeDetection];
                return;
            }
            
            NSDictionary *rgbOutputSettings = [NSDictionary
                                               dictionaryWithObject:[NSNumber numberWithInt:kCMPixelFormat_32BGRA]
                                               forKey:(id)kCVPixelBufferPixelFormatTypeKey];
            [self.videoDataOutput setVideoSettings:rgbOutputSettings];
            [self.videoDataOutput setAlwaysDiscardsLateVideoFrames:YES];
            [self.videoDataOutput setSampleBufferDelegate:self queue:self.sessionQueue];
            [self.session addOutput:_videoDataOutput];
        }
    } else {
        [self stopBarcodeDetection];
    }
}

- (void)stopBarcodeDetection
{
    if (self.videoDataOutput && !self.canReadText) {
        [self.session removeOutput:self.videoDataOutput];
    }
    self.videoDataOutput = nil;
    AVCaptureSessionPreset preset = [RNCameraUtils captureSessionPresetForVideoResolution:[self defaultVideoQuality]];
    if (self.session.sessionPreset != preset) {
        [self updateSessionPreset: preset == AVCaptureSessionPresetHigh ? AVCaptureSessionPresetPhoto: preset];
    }
}

- (void)updateGoogleVisionBarcodeType:(id)requestedTypes
{
    [self.barcodeDetector setType:requestedTypes queue:self.sessionQueue];
}

- (void)onBarcodesDetected:(NSDictionary *)event
{
    if (_onGoogleVisionBarcodesDetected && _session) {
        _onGoogleVisionBarcodesDetected(event);
    }
}

# pragma mark - TextDetector

-(id)createTextDetector
{
    Class textDetectorManagerClass = NSClassFromString(@"TextDetectorManager");
    return [[textDetectorManagerClass alloc] init];
}

- (void)setupOrDisableTextDetector
{
    if ([self canReadText] && [self.textDetector isRealDetector]){
        if (!self.videoDataOutput) {
            self.videoDataOutput = [[AVCaptureVideoDataOutput alloc] init];
            if (![self.session canAddOutput:_videoDataOutput]) {
                NSLog(@"Failed to setup video data output");
                [self stopTextRecognition];
                return;
            }
            NSDictionary *rgbOutputSettings = [NSDictionary
                dictionaryWithObject:[NSNumber numberWithInt:kCMPixelFormat_32BGRA]
                                forKey:(id)kCVPixelBufferPixelFormatTypeKey];
            [self.videoDataOutput setVideoSettings:rgbOutputSettings];
            [self.videoDataOutput setAlwaysDiscardsLateVideoFrames:YES];
            [self.videoDataOutput setSampleBufferDelegate:self queue:self.sessionQueue];
            [self.session addOutput:_videoDataOutput];
        }
    } else {
        [self stopTextRecognition];
    }
}

- (void)stopTextRecognition
{
    if (self.videoDataOutput && !self.canDetectFaces) {
        [self.session removeOutput:self.videoDataOutput];
    }
    self.videoDataOutput = nil;
}

# pragma mark - mlkit

- (void)captureOutput:(AVCaptureOutput *)captureOutput
    didOutputSampleBuffer:(CMSampleBufferRef)sampleBuffer
           fromConnection:(AVCaptureConnection *)connection
{
    if (![self.textDetector isRealDetector] && ![self.faceDetector isRealDetector] && ![self.barcodeDetector isRealDetector]) {
        NSLog(@"failing real check");
        return;
    }

    // Do not submit image for text/face recognition too often:
    // 1. we only dispatch events every 500ms anyway
    // 2. wait until previous recognition is finished
    // 3. let user disable text recognition, e.g. onTextRecognized={someCondition ? null : this.textRecognized}
    NSDate *methodFinish = [NSDate date];
    NSTimeInterval timePassedSinceSubmittingForText = [methodFinish timeIntervalSinceDate:self.startText];
    NSTimeInterval timePassedSinceSubmittingForFace = [methodFinish timeIntervalSinceDate:self.startFace];
    NSTimeInterval timePassedSinceSubmittingForBarcode = [methodFinish timeIntervalSinceDate:self.startBarcode];
    BOOL canSubmitForTextDetection = timePassedSinceSubmittingForText > 0.5 && _finishedReadingText && self.canReadText && [self.textDetector isRealDetector];
    BOOL canSubmitForFaceDetection = timePassedSinceSubmittingForFace > 0.5 && _finishedDetectingFace && self.canDetectFaces && [self.faceDetector isRealDetector];
    BOOL canSubmitForBarcodeDetection = timePassedSinceSubmittingForBarcode > 0.5 && _finishedDetectingBarcodes && self.canDetectBarcodes && [self.barcodeDetector isRealDetector];
    if (canSubmitForFaceDetection || canSubmitForTextDetection || canSubmitForBarcodeDetection) {
        CGSize previewSize = CGSizeMake(_previewLayer.frame.size.width, _previewLayer.frame.size.height);
        NSInteger position = self.videoCaptureDeviceInput.device.position;
        UIImage *image = [RNCameraUtils convertBufferToUIImage:sampleBuffer previewSize:previewSize position:position];
        // take care of the fact that preview dimensions differ from the ones of the image that we submit for text detection
        float scaleX = _previewLayer.frame.size.width / image.size.width;
        float scaleY = _previewLayer.frame.size.height / image.size.height;

        // find text features
        if (canSubmitForTextDetection) {
            _finishedReadingText = false;
            self.startText = [NSDate date];
            [self.textDetector findTextBlocksInFrame:image scaleX:scaleX scaleY:scaleY completed:^(NSArray * textBlocks) {
                NSDictionary *eventText = @{@"type" : @"TextBlock", @"textBlocks" : textBlocks};
                [self onText:eventText];
                self.finishedReadingText = true;
            }];
        }
        // find face features
        if (canSubmitForFaceDetection) {
            _finishedDetectingFace = false;
            self.startFace = [NSDate date];
            [self.faceDetector findFacesInFrame:image scaleX:scaleX scaleY:scaleY completed:^(NSArray * faces) {
                NSDictionary *eventFace = @{@"type" : @"face", @"faces" : faces};
                [self onFacesDetected:eventFace];
                self.finishedDetectingFace = true;
            }];
        }
        // find barcodes
        if (canSubmitForBarcodeDetection) {
            _finishedDetectingBarcodes = false;
            self.startBarcode = [NSDate date];
            [self.barcodeDetector findBarcodesInFrame:image scaleX:scaleX scaleY:scaleY completed:^(NSArray * barcodes) {
                NSDictionary *eventBarcode = @{@"type" : @"barcode", @"barcodes" : barcodes};
                [self onBarcodesDetected:eventBarcode];
                self.finishedDetectingBarcodes = true;
            }];
        }
    }
}

- (bool)isRecording {
    return self.movieFileOutput.isRecording;
}

@end
