#import "RCTCameraManager.h"
#import "RCTCamera.h"
#import "RCTBridge.h"
#import "RCTEventDispatcher.h"
#import "RCTUtils.h"
#import "RCTLog.h"
#import "UIView+React.h"
#import "NSMutableDictionary+ImageMetadata.m"
#import <AssetsLibrary/ALAssetsLibrary.h>
#import <AVFoundation/AVFoundation.h>
#import <ImageIO/ImageIO.h>

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

- (NSArray *)getBarCodeTypes {
  return @[
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
}

- (id)init {

  if ((self = [super init])) {

    self.session = [AVCaptureSession new];
    self.session.sessionPreset = AVCaptureSessionPresetHigh;

    self.previewLayer = [AVCaptureVideoPreviewLayer layerWithSession:self.session];
    self.previewLayer.needsDisplayOnBoundsChange = YES;

    self.sessionQueue = dispatch_queue_create("cameraManagerQueue", DISPATCH_QUEUE_SERIAL);

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

RCT_EXPORT_METHOD(changeCamera:(NSInteger)camera) {
  dispatch_async(self.sessionQueue, ^{
    AVCaptureDevice *currentCaptureDevice = [self.videoCaptureDeviceInput device];
    AVCaptureDevicePosition position = (AVCaptureDevicePosition)camera;
    AVCaptureDevice *captureDevice = [self deviceWithMediaType:AVMediaTypeVideo preferringPosition:(AVCaptureDevicePosition)position];

    if (captureDevice == nil) {
      return;
    }

    self.presetCamera = camera;

    NSError *error = nil;
    AVCaptureDeviceInput *captureDeviceInput = [AVCaptureDeviceInput deviceInputWithDevice:captureDevice error:&error];

    if (error || captureDeviceInput == nil)
    {
      NSLog(@"%@", error);
      return;
    }

    [self.session beginConfiguration];

    [self.session removeInput:self.videoCaptureDeviceInput];

    if ([self.session canAddInput:captureDeviceInput])
    {
      [self.session addInput:captureDeviceInput];

      [NSNotificationCenter.defaultCenter removeObserver:self name:AVCaptureDeviceSubjectAreaDidChangeNotification object:currentCaptureDevice];

      [NSNotificationCenter.defaultCenter addObserver:self selector:@selector(subjectAreaDidChange:) name:AVCaptureDeviceSubjectAreaDidChangeNotification object:captureDevice];
      self.videoCaptureDeviceInput = captureDeviceInput;
    }
    else
    {
      [self.session addInput:self.videoCaptureDeviceInput];
    }

    [self.session commitConfiguration];
  });
}

RCT_EXPORT_METHOD(changeAspect:(NSString *)aspect) {
  self.previewLayer.videoGravity = aspect;
}

RCT_EXPORT_METHOD(changeFlashMode:(NSInteger)flashMode) {
  AVCaptureDevice *device = [self.videoCaptureDeviceInput device];
  NSError *error = nil;

  if (![device hasFlash]) return;
  if (![device lockForConfiguration:&error]) {
    NSLog(@"%@", error);
    return;
  }
  [self setFlashMode:flashMode forDevice:device];
  [device unlockForConfiguration];
}

RCT_EXPORT_METHOD(changeOrientation:(NSInteger)orientation) {
  self.previewLayer.connection.videoOrientation = orientation;
}

RCT_EXPORT_METHOD(changeTorchMode:(NSInteger)torchMode) {
  AVCaptureDevice *device = [self.videoCaptureDeviceInput device];
  NSError *error = nil;

  if (![device hasTorch]) return;
  if (![device lockForConfiguration:&error]) {
    NSLog(@"%@", error);
    return;
  }
  [device setTorchMode: torchMode];
  [device unlockForConfiguration];
}

RCT_EXPORT_METHOD(capture:(NSDictionary *)options callback:(RCTResponseSenderBlock)callback) {
  NSInteger captureMode = [[options valueForKey:@"mode"] intValue];
  NSInteger captureTarget = [[options valueForKey:@"target"] intValue];

  if (captureMode == RCTCameraCaptureModeStill) {
    [self captureStill:captureTarget options:options callback:callback];
  }
  else if (captureMode == RCTCameraCaptureModeVideo) {
    [self captureVideo:captureTarget options:options callback:callback];
  }
}

RCT_EXPORT_METHOD(stopCapture) {
  if (self.movieFileOutput.recording) {
    [self.movieFileOutput stopRecording];
  }
}

- (void)startSession {
  dispatch_async(self.sessionQueue, ^{
    if (self.presetCamera == AVCaptureDevicePositionUnspecified) {
      self.presetCamera = AVCaptureDevicePositionBack;
    }
    
    AVCaptureStillImageOutput *stillImageOutput = [[AVCaptureStillImageOutput alloc] init];
    if ([self.session canAddOutput:stillImageOutput])
    {
      stillImageOutput.outputSettings = @{AVVideoCodecKey : AVVideoCodecJPEG};
      [self.session addOutput:stillImageOutput];
      self.stillImageOutput = stillImageOutput;
    }
    
    AVCaptureMovieFileOutput *movieFileOutput = [[AVCaptureMovieFileOutput alloc] init];
    if ([self.session canAddOutput:movieFileOutput])
    {
      [self.session addOutput:movieFileOutput];
      self.movieFileOutput = movieFileOutput;
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

- (void)stopSession {
  dispatch_async(self.sessionQueue, ^{
    [self.previewLayer removeFromSuperlayer];
    [self.session stopRunning];
    for(AVCaptureInput *input in self.session.inputs) {
      [self.session removeInput:input];
    }
    
    for(AVCaptureOutput *output in self.session.outputs) {
      [self.session removeOutput:output];
    }
  });
}

- (void)initializeCaptureSessionInput:(NSString *)type {
  dispatch_async(self.sessionQueue, ^{
    
    [self.session beginConfiguration];
    
    NSError *error = nil;
    AVCaptureDevice *captureDevice;
    
    if (type == AVMediaTypeAudio) {
      captureDevice = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeAudio];
    }
    else if (type == AVMediaTypeVideo) {
      captureDevice = [self deviceWithMediaType:AVMediaTypeVideo preferringPosition:self.presetCamera];
    }
    
    if (captureDevice == nil) {
      return;
    }

    AVCaptureDeviceInput *captureDeviceInput = [AVCaptureDeviceInput deviceInputWithDevice:captureDevice error:&error];

    if (error || captureDeviceInput == nil) {
      NSLog(@"%@", error);
      return;
    }

    if (type == AVMediaTypeAudio) {
      [self.session removeInput:self.audioCaptureDeviceInput];
    }
    else if (type == AVMediaTypeVideo) {
      [self.session removeInput:self.videoCaptureDeviceInput];
    }

    if ([self.session canAddInput:captureDeviceInput]) {
      [self.session addInput:captureDeviceInput];

      if (type == AVMediaTypeAudio) {
        self.audioCaptureDeviceInput = captureDeviceInput;
      }
      else if (type == AVMediaTypeVideo) {
        self.videoCaptureDeviceInput = captureDeviceInput;
      }
      [self.metadataOutput setMetadataObjectTypes:self.metadataOutput.availableMetadataObjectTypes];
    }

    [self.session commitConfiguration];
  });
}


- (void)captureStill:(NSInteger)target options:(NSDictionary *)options callback:(RCTResponseSenderBlock)callback {
  dispatch_async(self.sessionQueue, ^{
    if ([[[UIDevice currentDevice].model lowercaseString] rangeOfString:@"simulator"].location != NSNotFound){

      CGSize size = CGSizeMake(720, 1280);
      UIGraphicsBeginImageContextWithOptions(size, YES, 0);
        [[UIColor whiteColor] setFill];
        UIRectFill(CGRectMake(0, 0, size.width, size.height));
        UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
      UIGraphicsEndImageContext();

      NSData *imageData = UIImageJPEGRepresentation(image, 1.0);
      [self saveImage:imageData target:target metadata:nil callback:callback];

    } else {

      [[self.stillImageOutput connectionWithMediaType:AVMediaTypeVideo] setVideoOrientation:self.previewLayer.connection.videoOrientation];

      [self.stillImageOutput captureStillImageAsynchronouslyFromConnection:[self.stillImageOutput connectionWithMediaType:AVMediaTypeVideo] completionHandler:^(CMSampleBufferRef imageDataSampleBuffer, NSError *error) {

        if (imageDataSampleBuffer) {
          NSData *imageData = [AVCaptureStillImageOutput jpegStillImageNSDataRepresentation:imageDataSampleBuffer];

          // Create image source
          CGImageSourceRef source = CGImageSourceCreateWithData((CFDataRef)imageData, NULL);
          //get all the metadata in the image
          NSMutableDictionary *imageMetadata = [(NSDictionary *) CFBridgingRelease(CGImageSourceCopyPropertiesAtIndex(source, 0, NULL)) mutableCopy];

          // create cgimage
          CGImageRef CGImage;
          CGImage = CGImageSourceCreateImageAtIndex(source, 0, NULL);

          // Rotate it
          CGImageRef rotatedCGImage;
          if ([options objectForKey:@"rotation"]) {
            float rotation = [[options objectForKey:@"rotation"] floatValue];
            rotatedCGImage = [self newCGImageRotatedByAngle:CGImage angle:rotation];
          } else {
            // Get metadata orientation
            int metadataOrientation = [[imageMetadata objectForKey:(NSString *)kCGImagePropertyOrientation] intValue];

            if (metadataOrientation == 6) {
              rotatedCGImage = [self newCGImageRotatedByAngle:CGImage angle:270];
            } else if (metadataOrientation == 1) {
              rotatedCGImage = [self newCGImageRotatedByAngle:CGImage angle:0];
            } else if (metadataOrientation == 3) {
              rotatedCGImage = [self newCGImageRotatedByAngle:CGImage angle:180];
            } else {
              rotatedCGImage = [self newCGImageRotatedByAngle:CGImage angle:0];
            }
          }
          CGImageRelease(CGImage);

          // Erase metadata orientation
          [imageMetadata removeObjectForKey:(NSString *)kCGImagePropertyOrientation];
          // Erase stupid TIFF stuff
          [imageMetadata removeObjectForKey:(NSString *)kCGImagePropertyTIFFDictionary];

          // Add input metadata
          [imageMetadata mergeMetadata:[options objectForKey:@"metadata"]];

          // Create destination thing
          NSMutableData *rotatedImageData = [NSMutableData data];
          CGImageDestinationRef destination = CGImageDestinationCreateWithData((CFMutableDataRef)rotatedImageData, CGImageSourceGetType(source), 1, NULL);
          CFRelease(source);
          // add the image to the destination, reattaching metadata
          CGImageDestinationAddImage(destination, rotatedCGImage, (CFDictionaryRef) imageMetadata);
          // And write
          CGImageDestinationFinalize(destination);
          CFRelease(destination);

          [self saveImage:rotatedImageData target:target metadata:imageMetadata callback:callback];

          CGImageRelease(rotatedCGImage);
        }
        else {
          callback(@[RCTMakeError(error.description, nil, nil)]);
        }
      }];
    }
  });
}


- (void)saveImage:(NSData*)imageData target:(NSInteger)target metadata:(NSDictionary *)metadata callback:(RCTResponseSenderBlock)callback {
  NSString *responseString;

  if (target == RCTCameraCaptureTargetMemory) {
    responseString = [imageData base64EncodedStringWithOptions:0];
  }

  else if (target == RCTCameraCaptureTargetDisk) {
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths firstObject];

    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSString *fullPath = [[documentsDirectory stringByAppendingPathComponent:[[NSUUID UUID] UUIDString]] stringByAppendingPathExtension:@"jpg"];

    [fileManager createFileAtPath:fullPath contents:imageData attributes:nil];
    responseString = fullPath;
  }

  else if (target == RCTCameraCaptureTargetCameraRoll) {
    [[[ALAssetsLibrary alloc] init] writeImageDataToSavedPhotosAlbum:imageData metadata:metadata completionBlock:^(NSURL* url, NSError* error) {
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

- (CGImageRef)newCGImageRotatedByAngle:(CGImageRef)imgRef angle:(CGFloat)angle
{
  CGFloat angleInRadians = angle * (M_PI / 180);
  CGFloat width = CGImageGetWidth(imgRef);
  CGFloat height = CGImageGetHeight(imgRef);

  CGRect imgRect = CGRectMake(0, 0, width, height);
  CGAffineTransform transform = CGAffineTransformMakeRotation(angleInRadians);
  CGRect rotatedRect = CGRectApplyAffineTransform(imgRect, transform);

  CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
  CGContextRef bmContext = CGBitmapContextCreate(NULL, rotatedRect.size.width, rotatedRect.size.height, 8, 0, colorSpace, (CGBitmapInfo) kCGImageAlphaPremultipliedFirst);

  CGContextSetAllowsAntialiasing(bmContext, TRUE);
  CGContextSetInterpolationQuality(bmContext, kCGInterpolationNone);

  CGColorSpaceRelease(colorSpace);

  CGContextTranslateCTM(bmContext, +(rotatedRect.size.width/2), +(rotatedRect.size.height/2));
  CGContextRotateCTM(bmContext, angleInRadians);
  CGContextTranslateCTM(bmContext, -(rotatedRect.size.width/2), -(rotatedRect.size.height/2));

  CGContextDrawImage(bmContext, CGRectMake((rotatedRect.size.width-width)/2.0f, (rotatedRect.size.height-height)/2.0f, width, height), imgRef);

  CGImageRef rotatedImage = CGBitmapContextCreateImage(bmContext);
  CFRelease(bmContext);
  return rotatedImage;
}

-(void)captureVideo:(NSInteger)target options:(NSDictionary *)options callback:(RCTResponseSenderBlock)callback {

  if (self.movieFileOutput.recording) {
    callback(@[RCTMakeError(@"Already Recording", nil, nil)]);
    return;
  }

  if ([options valueForKey:@"audio"]) {
    [self initializeCaptureSessionInput:AVMediaTypeAudio];
  }

  Float64 totalSeconds = [[options valueForKey:@"totalSeconds"] floatValue];
  if (totalSeconds > -1) {
    int32_t preferredTimeScale = [[options valueForKey:@"preferredTimeScale"] intValue];
    CMTime maxDuration = CMTimeMakeWithSeconds(totalSeconds, preferredTimeScale);
    self.movieFileOutput.maxRecordedDuration = maxDuration;
  }

  dispatch_async(self.sessionQueue, ^{
    [[self.movieFileOutput connectionWithMediaType:AVMediaTypeVideo] setVideoOrientation:self.previewLayer.connection.videoOrientation];

    //Create temporary URL to record to
    NSString *outputPath = [[NSString alloc] initWithFormat:@"%@%@", NSTemporaryDirectory(), @"output.mov"];
    NSURL *outputURL = [[NSURL alloc] initFileURLWithPath:outputPath];
    NSFileManager *fileManager = [NSFileManager defaultManager];
    if ([fileManager fileExistsAtPath:outputPath]) {
      NSError *error;
      if ([fileManager removeItemAtPath:outputPath error:&error] == NO) {
        callback(@[RCTMakeError(error.description, nil, nil)]);
        return;
      }
    }

    //Start recording
    [self.movieFileOutput startRecordingToOutputFileURL:outputURL recordingDelegate:self];

    self.videoCallback = callback;
    self.videoTarget = target;
  });
}

- (void)captureOutput:(AVCaptureFileOutput *)captureOutput
didFinishRecordingToOutputFileAtURL:(NSURL *)outputFileURL
      fromConnections:(NSArray *)connections
                error:(NSError *)error
{

  BOOL recordSuccess = YES;
  if ([error code] != noErr) {
    // A problem occurred: Find out if the recording was successful.
    id value = [[error userInfo] objectForKey:AVErrorRecordingSuccessfullyFinishedKey];
    if (value) {
      recordSuccess = [value boolValue];
    }
  }
  if (!recordSuccess) {
    self.videoCallback(@[RCTMakeError(@"Error while recording", nil, nil)]);
    return;
  }

  if (self.videoTarget == RCTCameraCaptureTargetCameraRoll) {
    ALAssetsLibrary *library = [[ALAssetsLibrary alloc] init];
    if ([library videoAtPathIsCompatibleWithSavedPhotosAlbum:outputFileURL]) {
      [library writeVideoAtPathToSavedPhotosAlbum:outputFileURL
                                  completionBlock:^(NSURL *assetURL, NSError *error) {
                                    if (error) {
                                      self.videoCallback(@[RCTMakeError(error.description, nil, nil)]);
                                      return;
                                    }

                                    self.videoCallback(@[[NSNull null], [assetURL absoluteString]]);
                                  }];
    }
  }
  else if (self.videoTarget == RCTCameraCaptureTargetDisk) {
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths firstObject];
    NSString *fullPath = [[documentsDirectory stringByAppendingPathComponent:[[NSUUID UUID] UUIDString]] stringByAppendingPathExtension:@"mov"];

    NSFileManager * fileManager = [NSFileManager defaultManager];
    NSError * error = nil;

    //copying destination
    if (!([fileManager copyItemAtPath:[outputFileURL path] toPath:fullPath error:&error])) {
      self.videoCallback(@[RCTMakeError(error.description, nil, nil)]);
      return;
    }
    self.videoCallback(@[[NSNull null], fullPath]);
  }
  else {
    self.videoCallback(@[RCTMakeError(@"Target not supported", nil, nil)]);
  }
}

- (void)captureOutput:(AVCaptureOutput *)captureOutput didOutputMetadataObjects:(NSArray *)metadataObjects fromConnection:(AVCaptureConnection *)connection {

  for (AVMetadataMachineReadableCodeObject *metadata in metadataObjects) {
    for (id barcodeType in [self getBarCodeTypes]) {
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
    AVCaptureDevice *device = [[self videoCaptureDeviceInput] device];
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
