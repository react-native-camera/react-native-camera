#import "RCTCameraManager.h"
#import "RCTCamera.h"
#import "RCTBridge.h"
#import "RCTEventDispatcher.h"
#import "RCTUtils.h"
#import "RCTLog.h"
#import "UIView+React.h"
#import "UIImage+Resize.h"
#import <AssetsLibrary/ALAssetsLibrary.h>
#import <AVFoundation/AVFoundation.h>

@implementation RCTCameraManager

RCT_EXPORT_MODULE();

- (UIView *)view
{
    return [[RCTCamera alloc] initWithManager:self];
}

RCT_EXPORT_VIEW_PROPERTY(aspect, NSInteger);
RCT_EXPORT_VIEW_PROPERTY(type, NSInteger);
RCT_EXPORT_VIEW_PROPERTY(orientation, NSInteger);
RCT_EXPORT_VIEW_PROPERTY(flashMode, NSInteger);
RCT_EXPORT_VIEW_PROPERTY(torchMode, NSInteger);

- (NSDictionary *)constantsToExport
{
    return @{
      @"Aspect": @{
        @"stretch": @(RCTCameraAspectStretch),
        @"fit": @(RCTCameraAspectFit),
        @"fill": @(RCTCameraAspectFill)
      },
      @"BarCodeType": @{
        @"upce": AVMetadataObjectTypeUPCECode,
        @"code39": AVMetadataObjectTypeCode39Code,
        @"code39mod43": AVMetadataObjectTypeCode39Mod43Code,
        @"ean13": AVMetadataObjectTypeEAN13Code,
        @"ean8":  AVMetadataObjectTypeEAN8Code,
        @"code93": AVMetadataObjectTypeCode93Code,
        @"code138": AVMetadataObjectTypeCode128Code,
        @"pdf417": AVMetadataObjectTypePDF417Code,
        @"qr": AVMetadataObjectTypeQRCode,
        @"aztec": AVMetadataObjectTypeAztecCode
      },
      @"Type": @{
        @"front": @(RCTCameraTypeFront),
        @"back": @(RCTCameraTypeBack)
      },
      @"CaptureMode": @{
        @"still": @(RCTCameraCaptureModeStill),
        @"video": @(RCTCameraCaptureModeVideo)
      },
      @"CaptureTarget": @{
        @"memory": @(RCTCameraCaptureTargetMemory),
        @"disk": @(RCTCameraCaptureTargetDisk),
        @"cameraRoll": @(RCTCameraCaptureTargetCameraRoll)
      },
      @"Orientation": @{
        @"auto": @(RCTCameraOrientationAuto),
        @"landscapeLeft": @(RCTCameraOrientationLandscapeLeft),
        @"landscapeRight": @(RCTCameraOrientationLandscapeRight),
        @"portrait": @(RCTCameraOrientationPortrait),
        @"portraitUpsideDown": @(RCTCameraOrientationPortraitUpsideDown)
      },
      @"FlashMode": @{
        @"off": @(RCTCameraFlashModeOff),
        @"on": @(RCTCameraFlashModeOn),
        @"auto": @(RCTCameraFlashModeAuto)
      },
      @"TorchMode": @{
        @"off": @(RCTCameraTorchModeOff),
        @"on": @(RCTCameraTorchModeOn),
        @"auto": @(RCTCameraTorchModeAuto)
      }
    };
}

- (id)init {

    if ((self = [super init])) {

        self.session = [AVCaptureSession new];
        self.session.sessionPreset = AVCaptureSessionPresetHigh;

        self.previewLayer = [AVCaptureVideoPreviewLayer layerWithSession:self.session];
        self.previewLayer.needsDisplayOnBoundsChange = YES;

        self.sessionQueue = dispatch_queue_create("cameraManagerQueue", DISPATCH_QUEUE_SERIAL);

        dispatch_async(self.sessionQueue, ^{
            NSError *error = nil;

            if (self.presetCamera == AVCaptureDevicePositionUnspecified) {
                self.presetCamera = AVCaptureDevicePositionBack;
            }

            AVCaptureDevice *captureDevice = [self deviceWithMediaType:AVMediaTypeVideo preferringPosition:self.presetCamera];
            if (captureDevice != nil) {
                AVCaptureDeviceInput *captureDeviceInput = [AVCaptureDeviceInput deviceInputWithDevice:captureDevice error:&error];

                if (error)
                {
                    NSLog(@"%@", error);
                }

                if ([self.session canAddInput:captureDeviceInput])
                {
                    [self.session addInput:captureDeviceInput];
                    self.captureDeviceInput = captureDeviceInput;
                }
            }

            AVCaptureStillImageOutput *stillImageOutput = [[AVCaptureStillImageOutput alloc] init];
            if ([self.session canAddOutput:stillImageOutput])
            {
                stillImageOutput.outputSettings = @{AVVideoCodecKey : AVVideoCodecJPEG};
                [self.session addOutput:stillImageOutput];
                self.stillImageOutput = stillImageOutput;
            }

            AVCaptureMetadataOutput *metadataOutput = [[AVCaptureMetadataOutput alloc] init];
            if ([self.session canAddOutput:metadataOutput]) {
                [metadataOutput setMetadataObjectsDelegate:self queue:self.sessionQueue];
                [self.session addOutput:metadataOutput];
                [metadataOutput setMetadataObjectTypes:metadataOutput.availableMetadataObjectTypes];
                self.metadataOutput = metadataOutput;
            }

            __weak RCTCameraManager *weakSelf = self;
            [self setRuntimeErrorHandlingObserver:[NSNotificationCenter.defaultCenter addObserverForName:AVCaptureSessionRuntimeErrorNotification object:self.session queue:nil usingBlock:^(NSNotification *note) {
                RCTCameraManager *strongSelf = weakSelf;
                dispatch_async(strongSelf.sessionQueue, ^{
                    // Manually restarting the session since it must have been stopped due to an error.
                    [strongSelf.session startRunning];
                });
            }]];

            [self.session startRunning];
        });
    }
    return self;
}

RCT_EXPORT_METHOD(checkDeviceAuthorizationStatus:(RCTResponseSenderBlock) callback)
{
    NSString *mediaType = AVMediaTypeVideo;

    [AVCaptureDevice requestAccessForMediaType:mediaType completionHandler:^(BOOL granted) {
        callback(@[[NSNull null], @(granted)]);
    }];
}

RCT_EXPORT_METHOD(changeFlashMode:(NSInteger)flashMode) {
    AVCaptureDevice *currentCaptureDevice = [self.captureDeviceInput device];
    [self setFlashMode:flashMode forDevice:currentCaptureDevice];
}

RCT_EXPORT_METHOD(changeCamera:(NSInteger)camera) {
    AVCaptureDevice *currentCaptureDevice = [self.captureDeviceInput device];
    AVCaptureDevicePosition position = (AVCaptureDevicePosition)camera;
    AVCaptureDevice *captureDevice = [self deviceWithMediaType:AVMediaTypeVideo preferringPosition:(AVCaptureDevicePosition)position];

    if (captureDevice == nil) {
        return;
    }

    NSError *error = nil;
    AVCaptureDeviceInput *captureDeviceInput = [AVCaptureDeviceInput deviceInputWithDevice:captureDevice error:&error];

    if (error)
    {
        NSLog(@"%@", error);
        return;
    }

    [self.session beginConfiguration];

    [self.session removeInput:self.captureDeviceInput];

    if ([self.session canAddInput:captureDeviceInput])
    {
        [NSNotificationCenter.defaultCenter removeObserver:self name:AVCaptureDeviceSubjectAreaDidChangeNotification object:currentCaptureDevice];

        [NSNotificationCenter.defaultCenter addObserver:self selector:@selector(subjectAreaDidChange:) name:AVCaptureDeviceSubjectAreaDidChangeNotification object:captureDevice];
        [self.session addInput:captureDeviceInput];
        self.captureDeviceInput = captureDeviceInput;
    }
    else
    {
        [self.session addInput:self.captureDeviceInput];
    }

    [self.session commitConfiguration];
}

RCT_EXPORT_METHOD(changeAspect:(NSString *)aspect) {
    self.previewLayer.videoGravity = aspect;
}

RCT_EXPORT_METHOD(changeOrientation:(NSInteger)orientation) {
    self.previewLayer.connection.videoOrientation = orientation;
}

RCT_EXPORT_METHOD(changeTorchMode:(NSInteger)torchMode) {
    AVCaptureDevice *device = [self.captureDeviceInput device];
    NSError *error = nil;
    
    if ([device hasTorch]) {
        if ([device lockForConfiguration:&error])
        {
            [device setTorchMode: torchMode];
            [device unlockForConfiguration];
        }
        else
        {
            NSLog(@"%@", error);
        }
    }
}

RCT_EXPORT_METHOD(capture:(NSDictionary *)options callback:(RCTResponseSenderBlock)callback) {
    NSInteger captureMode = [[options valueForKey:@"mode"] intValue];
    NSInteger captureTarget = [[options valueForKey:@"target"] intValue];

    if (captureMode == RCTCameraCaptureModeStill) {
        [self captureStill:captureTarget callback:callback];
    }
    else if (captureMode == RCTCameraCaptureModeVideo) {
        // waiting for incoming PRs
    }
}

- (void)captureStill:(NSInteger)target callback:(RCTResponseSenderBlock)callback {
    if ([[[UIDevice currentDevice].model lowercaseString] rangeOfString:@"simulator"].location != NSNotFound){
    
        CGSize size = CGSizeMake(720, 1280);
        UIGraphicsBeginImageContextWithOptions(size, YES, 0);
            [[UIColor whiteColor] setFill];
            UIRectFill(CGRectMake(0, 0, size.width, size.height));
            UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        
        [self storeImage:image target:target callback:callback];
        
    } else {
    
        [[self.stillImageOutput connectionWithMediaType:AVMediaTypeVideo] setVideoOrientation:self.previewLayer.connection.videoOrientation];
        
        [self.stillImageOutput captureStillImageAsynchronouslyFromConnection:[self.stillImageOutput connectionWithMediaType:AVMediaTypeVideo] completionHandler:^(CMSampleBufferRef imageDataSampleBuffer, NSError *error) {
            NSData *imageData = [AVCaptureStillImageOutput jpegStillImageNSDataRepresentation:imageDataSampleBuffer];
            UIImage *image = [UIImage imageWithData:imageData];
            if (image)
            {
                [self storeImage:image target:target callback:callback];
            }
            else {
                callback(@[RCTMakeError(error.description, nil, nil)]);
            }
        }];
    }
}

- (void)storeImage:(UIImage*)image target:(NSInteger)target callback:(RCTResponseSenderBlock)callback {
    UIImage *rotatedImage = [image resizedImage:CGSizeMake(image.size.width, image.size.height) interpolationQuality:kCGInterpolationDefault];

    NSString *responseString;
    
    if (target == RCTCameraCaptureTargetMemory) {
        responseString = [UIImageJPEGRepresentation(rotatedImage, 1.0) base64EncodedStringWithOptions:0];
    }
    else if (target == RCTCameraCaptureTargetDisk) {
        responseString = [self saveImage:rotatedImage withName:[[NSUUID UUID] UUIDString]];
    }
    else if (target == RCTCameraCaptureTargetCameraRoll) {
        [[[ALAssetsLibrary alloc] init] writeImageToSavedPhotosAlbum:rotatedImage.CGImage metadata:nil completionBlock:^(NSURL* url, NSError* error) {
            if (error == nil) {
                callback(@[[NSNull null], [url absoluteString]]);
            }
            else {
                callback(@[RCTMakeError(error.description, nil, nil)]);
            }
        }];
        return;
    }
    callback(@[[NSNull null], responseString]);
}

- (NSString *)saveImage:(UIImage *)image withName:(NSString *)name {
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths firstObject];

    NSData *data = UIImageJPEGRepresentation(image, 1.0);
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSString *fullPath = [[documentsDirectory stringByAppendingPathComponent:name] stringByAppendingPathExtension:@"jpg"];

    [fileManager createFileAtPath:fullPath contents:data attributes:nil];
    return fullPath;
}

- (void)captureOutput:(AVCaptureOutput *)captureOutput didOutputMetadataObjects:(NSArray *)metadataObjects fromConnection:(AVCaptureConnection *)connection {

    NSArray *barcodeTypes = @[
        AVMetadataObjectTypeUPCECode,
        AVMetadataObjectTypeCode39Code,
        AVMetadataObjectTypeCode39Mod43Code,
        AVMetadataObjectTypeEAN13Code,
        AVMetadataObjectTypeEAN8Code,
        AVMetadataObjectTypeCode93Code,
        AVMetadataObjectTypeCode128Code,
        AVMetadataObjectTypePDF417Code,
        AVMetadataObjectTypeQRCode,
        AVMetadataObjectTypeAztecCode
    ];

    for (AVMetadataMachineReadableCodeObject *metadata in metadataObjects) {
        for (id barcodeType in barcodeTypes) {
            if (metadata.type == barcodeType) {

                [self.bridge.eventDispatcher sendDeviceEventWithName:@"CameraBarCodeRead"
                    body:@{
                        @"type": metadata.type,
                        @"data": metadata.stringValue,
                        @"bounds": @{
                            @"origin": @{
                                @"x": [NSString stringWithFormat:@"%f", metadata.bounds.origin.x],
                                @"y": [NSString stringWithFormat:@"%f", metadata.bounds.origin.y]
                            },
                            @"size": @{
                                @"height": [NSString stringWithFormat:@"%f", metadata.bounds.size.height],
                                @"width": [NSString stringWithFormat:@"%f", metadata.bounds.size.width],
                            }
                        }
                    }];
            }
        }
    }
}


- (AVCaptureDevice *)deviceWithMediaType:(NSString *)mediaType preferringPosition:(AVCaptureDevicePosition)position
{
    NSArray *devices = [AVCaptureDevice devicesWithMediaType:mediaType];
    AVCaptureDevice *captureDevice = [devices firstObject];

    for (AVCaptureDevice *device in devices)
    {
        if ([device position] == position)
        {
            captureDevice = device;
            break;
        }
    }

    return captureDevice;
}


- (void)setFlashMode:(AVCaptureFlashMode)flashMode forDevice:(AVCaptureDevice *)device
{
    if (device.hasFlash && [device isFlashModeSupported:flashMode])
    {
        NSError *error = nil;
        if ([device lockForConfiguration:&error])
        {
            [device setFlashMode:flashMode];
            [device unlockForConfiguration];
        }
        else
        {
            NSLog(@"%@", error);
        }
    }
}

- (void)subjectAreaDidChange:(NSNotification *)notification
{
    CGPoint devicePoint = CGPointMake(.5, .5);
    [self focusWithMode:AVCaptureFocusModeContinuousAutoFocus exposeWithMode:AVCaptureExposureModeContinuousAutoExposure atDevicePoint:devicePoint monitorSubjectAreaChange:NO];
}

- (void)focusWithMode:(AVCaptureFocusMode)focusMode exposeWithMode:(AVCaptureExposureMode)exposureMode atDevicePoint:(CGPoint)point monitorSubjectAreaChange:(BOOL)monitorSubjectAreaChange
{
    dispatch_async([self sessionQueue], ^{
        AVCaptureDevice *device = [[self captureDeviceInput] device];
        NSError *error = nil;
        if ([device lockForConfiguration:&error])
        {
            if ([device isFocusPointOfInterestSupported] && [device isFocusModeSupported:focusMode])
            {
                [device setFocusMode:focusMode];
                [device setFocusPointOfInterest:point];
            }
            if ([device isExposurePointOfInterestSupported] && [device isExposureModeSupported:exposureMode])
            {
                [device setExposureMode:exposureMode];
                [device setExposurePointOfInterest:point];
            }
            [device setSubjectAreaChangeMonitoringEnabled:monitorSubjectAreaChange];
            [device unlockForConfiguration];
        }
        else
        {
            NSLog(@"%@", error);
        }
    });
}


@end
