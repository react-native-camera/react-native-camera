#import "RCTCameraManager.h"
#import "RCTCamera.h"
#import <React/RCTBridge.h>
#import <React/RCTEventDispatcher.h>
#import <React/RCTUtils.h>
#import <React/RCTLog.h>
#import <React/UIView+React.h>
#import "NSMutableDictionary+ImageMetadata.m"
#import <AssetsLibrary/ALAssetsLibrary.h>
#import <AVFoundation/AVFoundation.h>
#import <ImageIO/ImageIO.h>
#import "RCTSensorOrientationChecker.h"

@interface RCTCameraManager ()

@property (strong, nonatomic) RCTSensorOrientationChecker * sensorOrientationChecker;
@property (assign, nonatomic) NSInteger* flashMode;

@end

@implementation RCTCameraManager

RCT_EXPORT_MODULE();

- (UIView *)viewWithProps:(__unused NSDictionary *)props
{
    self.presetCamera = ((NSNumber *)props[@"type"]).integerValue;
    return [self view];
}

- (UIView *)view
{
  self.session = [AVCaptureSession new];
  #if !(TARGET_IPHONE_SIMULATOR)
    self.previewLayer = [AVCaptureVideoPreviewLayer layerWithSession:self.session];
    self.previewLayer.needsDisplayOnBoundsChange = YES;
  #endif

  if(!self.camera){
    self.camera = [[RCTCamera alloc] initWithManager:self bridge:self.bridge];
  }
  return self.camera;
}

- (NSDictionary *)constantsToExport
{

    NSMutableDictionary * runtimeBarcodeTypes = [NSMutableDictionary dictionary];
    [runtimeBarcodeTypes setDictionary:@{
                                         @"upce": AVMetadataObjectTypeUPCECode,
                                         @"code39": AVMetadataObjectTypeCode39Code,
                                         @"code39mod43": AVMetadataObjectTypeCode39Mod43Code,
                                         @"ean13": AVMetadataObjectTypeEAN13Code,
                                         @"ean8":  AVMetadataObjectTypeEAN8Code,
                                         @"code93": AVMetadataObjectTypeCode93Code,
                                         @"code128": AVMetadataObjectTypeCode128Code,
                                         @"pdf417": AVMetadataObjectTypePDF417Code,
                                         @"qr": AVMetadataObjectTypeQRCode,
                                         @"aztec": AVMetadataObjectTypeAztecCode
                                         }];

    if (&AVMetadataObjectTypeInterleaved2of5Code != NULL) {
        [runtimeBarcodeTypes setObject:AVMetadataObjectTypeInterleaved2of5Code forKey:@"interleaved2of5"];
    }

    if(&AVMetadataObjectTypeITF14Code != NULL){
        [runtimeBarcodeTypes setObject:AVMetadataObjectTypeITF14Code forKey:@"itf14"];
    }

    if(&AVMetadataObjectTypeDataMatrixCode != NULL){
        [runtimeBarcodeTypes setObject:AVMetadataObjectTypeDataMatrixCode forKey:@"datamatrix"];
    }


  return @{
           @"Aspect": @{
               @"stretch": @(RCTCameraAspectStretch),
               @"fit": @(RCTCameraAspectFit),
               @"fill": @(RCTCameraAspectFill)
               },
           @"BarCodeType": runtimeBarcodeTypes,
           @"Type": @{
               @"front": @(RCTCameraTypeFront),
               @"back": @(RCTCameraTypeBack)
               },
           @"CaptureMode": @{
               @"still": @(RCTCameraCaptureModeStill),
               @"video": @(RCTCameraCaptureModeVideo)
               },
           @"CaptureQuality": @{
               @"low": @(RCTCameraCaptureSessionPresetLow),
               @"AVCaptureSessionPresetLow": @(RCTCameraCaptureSessionPresetLow),
               @"medium": @(RCTCameraCaptureSessionPresetMedium),
               @"AVCaptureSessionPresetMedium": @(RCTCameraCaptureSessionPresetMedium),
               @"high": @(RCTCameraCaptureSessionPresetHigh),
               @"AVCaptureSessionPresetHigh": @(RCTCameraCaptureSessionPresetHigh),
               @"photo": @(RCTCameraCaptureSessionPresetPhoto),
               @"AVCaptureSessionPresetPhoto": @(RCTCameraCaptureSessionPresetPhoto),
               @"480p": @(RCTCameraCaptureSessionPreset480p),
               @"AVCaptureSessionPreset640x480": @(RCTCameraCaptureSessionPreset480p),
               @"720p": @(RCTCameraCaptureSessionPreset720p),
               @"AVCaptureSessionPreset1280x720": @(RCTCameraCaptureSessionPreset720p),
               @"1080p": @(RCTCameraCaptureSessionPreset1080p),
               @"AVCaptureSessionPreset1920x1080": @(RCTCameraCaptureSessionPreset1080p)
               },
           @"CaptureTarget": @{
               @"memory": @(RCTCameraCaptureTargetMemory),
               @"disk": @(RCTCameraCaptureTargetDisk),
               @"temp": @(RCTCameraCaptureTargetTemp),
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

RCT_EXPORT_VIEW_PROPERTY(orientation, NSInteger);
RCT_EXPORT_VIEW_PROPERTY(defaultOnFocusComponent, BOOL);
RCT_EXPORT_VIEW_PROPERTY(onFocusChanged, BOOL);
RCT_EXPORT_VIEW_PROPERTY(onZoomChanged, BOOL);

RCT_CUSTOM_VIEW_PROPERTY(captureQuality, NSInteger, RCTCamera) {
  NSInteger quality = [RCTConvert NSInteger:json];
  NSString *qualityString;
  switch (quality) {
    default:
    case RCTCameraCaptureSessionPresetHigh:
      qualityString = AVCaptureSessionPresetHigh;
      break;
    case RCTCameraCaptureSessionPresetMedium:
      qualityString = AVCaptureSessionPresetMedium;
      break;
    case RCTCameraCaptureSessionPresetLow:
      qualityString = AVCaptureSessionPresetLow;
      break;
    case RCTCameraCaptureSessionPresetPhoto:
      qualityString = AVCaptureSessionPresetPhoto;
      break;
    case RCTCameraCaptureSessionPreset1080p:
      qualityString = AVCaptureSessionPreset1920x1080;
      break;
    case RCTCameraCaptureSessionPreset720p:
      qualityString = AVCaptureSessionPreset1280x720;
      break;
    case RCTCameraCaptureSessionPreset480p:
      qualityString = AVCaptureSessionPreset640x480;
      break;
  }

  [self setCaptureQuality:qualityString];
}

RCT_CUSTOM_VIEW_PROPERTY(aspect, NSInteger, RCTCamera) {
  NSInteger aspect = [RCTConvert NSInteger:json];
  NSString *aspectString;
  switch (aspect) {
    default:
    case RCTCameraAspectFill:
      aspectString = AVLayerVideoGravityResizeAspectFill;
      break;
    case RCTCameraAspectFit:
      aspectString = AVLayerVideoGravityResizeAspect;
      break;
    case RCTCameraAspectStretch:
      aspectString = AVLayerVideoGravityResize;
      break;
  }

  self.previewLayer.videoGravity = aspectString;
}

RCT_CUSTOM_VIEW_PROPERTY(type, NSInteger, RCTCamera) {
  NSInteger type = [RCTConvert NSInteger:json];

  self.presetCamera = type;
  if (self.session.isRunning) {
    dispatch_async(self.sessionQueue, ^{
      AVCaptureDevice *currentCaptureDevice = [self.videoCaptureDeviceInput device];
      AVCaptureDevicePosition position = (AVCaptureDevicePosition)type;
      AVCaptureDevice *captureDevice = [self deviceWithMediaType:AVMediaTypeVideo preferringPosition:(AVCaptureDevicePosition)position];

      if (captureDevice == nil) {
        return;
      }

      self.presetCamera = type;

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
        [self setFlashMode];
      }
      else
      {
        [self.session addInput:self.videoCaptureDeviceInput];
      }

      [self.session commitConfiguration];
    });
  }
  [self initializeCaptureSessionInput:AVMediaTypeVideo];
}

RCT_CUSTOM_VIEW_PROPERTY(flashMode, NSInteger, RCTCamera) {
    self.flashMode = [RCTConvert NSInteger:json];
    [self setFlashMode];
}

- (void)setFlashMode {
    AVCaptureDevice *device = [self.videoCaptureDeviceInput device];
    NSError *error = nil;

    if (![device hasFlash]) return;
    if (![device lockForConfiguration:&error]) {
        NSLog(@"%@", error);
        return;
    }
    if (device.hasFlash && [device isFlashModeSupported:self.flashMode])
    {
        NSError *error = nil;
        if ([device lockForConfiguration:&error])
        {
            [device setFlashMode:self.flashMode];
            [device unlockForConfiguration];
        }
        else
        {
            NSLog(@"%@", error);
        }
    }
    [device unlockForConfiguration];
}

RCT_CUSTOM_VIEW_PROPERTY(torchMode, NSInteger, RCTCamera) {
  dispatch_async(self.sessionQueue, ^{
    NSInteger *torchMode = [RCTConvert NSInteger:json];
    AVCaptureDevice *device = [self.videoCaptureDeviceInput device];
    NSError *error = nil;

    if (![device hasTorch]) return;
    if (![device lockForConfiguration:&error]) {
      NSLog(@"%@", error);
      return;
    }
    [device setTorchMode: torchMode];
    [device unlockForConfiguration];
  });
}

RCT_CUSTOM_VIEW_PROPERTY(keepAwake, BOOL, RCTCamera) {
  BOOL enabled = [RCTConvert BOOL:json];
  [UIApplication sharedApplication].idleTimerDisabled = enabled;
}

RCT_CUSTOM_VIEW_PROPERTY(mirrorImage, BOOL, RCTCamera) {
  self.mirrorImage = [RCTConvert BOOL:json];
}

RCT_CUSTOM_VIEW_PROPERTY(barCodeTypes, NSArray, RCTCamera) {
  self.barCodeTypes = [RCTConvert NSArray:json];
}

RCT_CUSTOM_VIEW_PROPERTY(captureAudio, BOOL, RCTCamera) {
  BOOL captureAudio = [RCTConvert BOOL:json];
  if (captureAudio) {
    RCTLog(@"capturing audio");
    [self initializeCaptureSessionInput:AVMediaTypeAudio];
  }
}

- (NSArray *)customDirectEventTypes
{
    return @[
      @"focusChanged",
      @"zoomChanged",
    ];
}

- (id)init {
  if ((self = [super init])) {
    self.mirrorImage = false;

    self.sessionQueue = dispatch_queue_create("cameraManagerQueue", DISPATCH_QUEUE_SERIAL);

    self.sensorOrientationChecker = [RCTSensorOrientationChecker new];
  }
  return self;
}

RCT_EXPORT_METHOD(checkDeviceAuthorizationStatus:(RCTPromiseResolveBlock)resolve
                  reject:(__unused RCTPromiseRejectBlock)reject) {
  __block NSString *mediaType = AVMediaTypeVideo;

  [AVCaptureDevice requestAccessForMediaType:mediaType completionHandler:^(BOOL granted) {
    if (!granted) {
      resolve(@(granted));
    }
    else {
      mediaType = AVMediaTypeAudio;
      [AVCaptureDevice requestAccessForMediaType:mediaType completionHandler:^(BOOL granted) {
        resolve(@(granted));
      }];
    }
  }];
}


RCT_EXPORT_METHOD(checkVideoAuthorizationStatus:(RCTPromiseResolveBlock)resolve
                  reject:(__unused RCTPromiseRejectBlock)reject) {
    __block NSString *mediaType = AVMediaTypeVideo;

    [AVCaptureDevice requestAccessForMediaType:mediaType completionHandler:^(BOOL granted) {
        resolve(@(granted));
    }];
}

RCT_EXPORT_METHOD(checkAudioAuthorizationStatus:(RCTPromiseResolveBlock)resolve
                  reject:(__unused RCTPromiseRejectBlock)reject) {
    __block NSString *mediaType = AVMediaTypeAudio;

    [AVCaptureDevice requestAccessForMediaType:mediaType completionHandler:^(BOOL granted) {
        resolve(@(granted));
    }];
}

RCT_EXPORT_METHOD(changeOrientation:(NSInteger)orientation) {
  [self setOrientation:orientation];
}

RCT_EXPORT_METHOD(capture:(NSDictionary *)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
  NSInteger captureMode = [[options valueForKey:@"mode"] intValue];
  NSInteger captureTarget = [[options valueForKey:@"target"] intValue];

  if (captureMode == RCTCameraCaptureModeStill) {
    [self captureStill:captureTarget options:options resolve:resolve reject:reject];
  }
  else if (captureMode == RCTCameraCaptureModeVideo) {
    [self captureVideo:captureTarget options:options resolve:resolve reject:reject];
  }
}

RCT_EXPORT_METHOD(stopCapture) {
  if (self.movieFileOutput.recording) {
    [self.movieFileOutput stopRecording];
  }
}

RCT_EXPORT_METHOD(getFOV:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
  NSArray *devices = [AVCaptureDevice devices];
  AVCaptureDevice *frontCamera;
  AVCaptureDevice *backCamera;
  double frontFov = 0.0;
  double backFov = 0.0;

  for (AVCaptureDevice *device in devices) {

      NSLog(@"Device name: %@", [device localizedName]);

      if ([device hasMediaType:AVMediaTypeVideo]) {

          if ([device position] == AVCaptureDevicePositionBack) {
              NSLog(@"Device position : back");
              backCamera = device;
              backFov = backCamera.activeFormat.videoFieldOfView;
          }
          else {
              NSLog(@"Device position : front");
              frontCamera = device;
              frontFov = frontCamera.activeFormat.videoFieldOfView;
          }
      }
  }

  resolve(@{
    [NSNumber numberWithInt:RCTCameraTypeBack]: [NSNumber numberWithDouble: backFov],
    [NSNumber numberWithInt:RCTCameraTypeFront]: [NSNumber numberWithDouble: frontFov]
  });
}

RCT_EXPORT_METHOD(hasFlash:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    AVCaptureDevice *device = [self.videoCaptureDeviceInput device];
    resolve(@(device.hasFlash));
}

- (void)startSession {
#if TARGET_IPHONE_SIMULATOR
  return;
#endif
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
      [metadataOutput setMetadataObjectTypes:self.barCodeTypes];
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
#if TARGET_IPHONE_SIMULATOR
  self.camera = nil;
  return;
#endif
  dispatch_async(self.sessionQueue, ^{
    self.camera = nil;
    [self.previewLayer removeFromSuperlayer];
    [self.session commitConfiguration];
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
    if (type == AVMediaTypeAudio) {
      for (AVCaptureDeviceInput* input in [self.session inputs]) {
        if ([input.device hasMediaType:AVMediaTypeAudio]) {
          // If an audio input has been configured we don't need to set it up again
          return;
        }
      }
    }

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

    if (type == AVMediaTypeVideo) {
      [self.session removeInput:self.videoCaptureDeviceInput];
    }

    if ([self.session canAddInput:captureDeviceInput]) {
      [self.session addInput:captureDeviceInput];

      if (type == AVMediaTypeAudio) {
        self.audioCaptureDeviceInput = captureDeviceInput;
      }
      else if (type == AVMediaTypeVideo) {
        self.videoCaptureDeviceInput = captureDeviceInput;
        [self setFlashMode];
      }
      [self.metadataOutput setMetadataObjectTypes:self.metadataOutput.availableMetadataObjectTypes];
    }

    [self.session commitConfiguration];
  });
}

- (void)captureStill:(NSInteger)target options:(NSDictionary *)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    AVCaptureVideoOrientation orientation = options[@"orientation"] != nil ? [options[@"orientation"] integerValue] : self.orientation;
    if (orientation == RCTCameraOrientationAuto) {
        #if TARGET_IPHONE_SIMULATOR
            [self captureStill:target options:options orientation:self.previewLayer.connection.videoOrientation resolve:resolve reject:reject];
        #else
            [self.sensorOrientationChecker getDeviceOrientationWithBlock:^(UIInterfaceOrientation orientation) {
                [self captureStill:target options:options orientation:[self.sensorOrientationChecker convertToAVCaptureVideoOrientation: orientation] resolve:resolve reject:reject];
            }];
        #endif
    } else {
        [self captureStill:target options:options orientation:orientation resolve:resolve reject:reject];
    }
}

- (void)captureStill:(NSInteger)target options:(NSDictionary *)options orientation:(AVCaptureVideoOrientation)orientation resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
  dispatch_async(self.sessionQueue, ^{
#if TARGET_IPHONE_SIMULATOR
      CGSize size = CGSizeMake(720, 1280);
      UIGraphicsBeginImageContextWithOptions(size, YES, 0);
          // Thanks https://gist.github.com/kylefox/1689973
          CGFloat hue = ( arc4random() % 256 / 256.0 );  //  0.0 to 1.0
          CGFloat saturation = ( arc4random() % 128 / 256.0 ) + 0.5;  //  0.5 to 1.0, away from white
          CGFloat brightness = ( arc4random() % 128 / 256.0 ) + 0.5;  //  0.5 to 1.0, away from black
          UIColor *color = [UIColor colorWithHue:hue saturation:saturation brightness:brightness alpha:1];
          [color setFill];
          UIRectFill(CGRectMake(0, 0, size.width, size.height));
          NSDate *currentDate = [NSDate date];
          NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
          [dateFormatter setDateFormat:@"dd.MM.YY HH:mm:ss"];
          NSString *text = [dateFormatter stringFromDate:currentDate];
          UIFont *font = [UIFont systemFontOfSize:40.0];
          NSDictionary *attributes = [NSDictionary dictionaryWithObjects:
                                      @[font, [UIColor blackColor]]
                                                                 forKeys:
                                      @[NSFontAttributeName, NSForegroundColorAttributeName]];
          [text drawAtPoint:CGPointMake(size.width/3, size.height/2) withAttributes:attributes];
          UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
      UIGraphicsEndImageContext();

      NSData *imageData = UIImageJPEGRepresentation(image, 1.0);
      [self saveImage:imageData target:target metadata:nil resolve:resolve reject:reject];
#else
      [[self.stillImageOutput connectionWithMediaType:AVMediaTypeVideo] setVideoOrientation:orientation];

      [self.stillImageOutput captureStillImageAsynchronouslyFromConnection:[self.stillImageOutput connectionWithMediaType:AVMediaTypeVideo] completionHandler:^(CMSampleBufferRef imageDataSampleBuffer, NSError *error) {

        if (imageDataSampleBuffer) {
          NSData *imageData = [AVCaptureStillImageOutput jpegStillImageNSDataRepresentation:imageDataSampleBuffer];

          // Create image source
          CGImageSourceRef source = CGImageSourceCreateWithData((CFDataRef)imageData, NULL);
          //get all the metadata in the image
          NSMutableDictionary *imageMetadata = [(NSDictionary *) CFBridgingRelease(CGImageSourceCopyPropertiesAtIndex(source, 0, NULL)) mutableCopy];

          // create cgimage
          CGImageRef cgImage = CGImageSourceCreateImageAtIndex(source, 0, NULL);

          // Rotate it
          CGImageRef rotatedCGImage;
          if ([options objectForKey:@"rotation"]) {
            float rotation = [[options objectForKey:@"rotation"] floatValue];
            rotatedCGImage = [self newCGImageRotatedByAngle:cgImage angle:rotation];
          } else if ([[options objectForKey:@"fixOrientation"] boolValue] == YES) {
            // Get metadata orientation
            int metadataOrientation = [[imageMetadata objectForKey:(NSString *)kCGImagePropertyOrientation] intValue];

            bool rotated = false;
            //see http://www.impulseadventure.com/photo/exif-orientation.html
            if (metadataOrientation == 6) {
              rotatedCGImage = [self newCGImageRotatedByAngle:cgImage angle:270];
              rotated = true;
            } else if (metadataOrientation == 3) {
              rotatedCGImage = [self newCGImageRotatedByAngle:cgImage angle:180];
              rotated = true;
            } else {
              rotatedCGImage = cgImage;
            }

            if(rotated) {
              [imageMetadata setObject:[NSNumber numberWithInteger:1] forKey:(NSString *)kCGImagePropertyOrientation];
              CGImageRelease(cgImage);
            }
          } else {
            rotatedCGImage = cgImage;
          }

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

          [self saveImage:rotatedImageData target:target metadata:imageMetadata resolve:resolve reject:reject];

          CGImageRelease(rotatedCGImage);
        }
        else {
          reject(RCTErrorUnspecified, nil, RCTErrorWithMessage(error.description));
        }
      }];
#endif
  });
}


- (void)saveImage:(NSData*)imageData target:(NSInteger)target metadata:(NSDictionary *)metadata resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
  NSString *responseString;

  if (target == RCTCameraCaptureTargetMemory) {
    resolve(@{@"data":[imageData base64EncodedStringWithOptions:0]});
    return;
  }

  else if (target == RCTCameraCaptureTargetDisk) {
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths firstObject];

    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSString *fullPath = [[documentsDirectory stringByAppendingPathComponent:[[NSUUID UUID] UUIDString]] stringByAppendingPathExtension:@"jpg"];

    [fileManager createFileAtPath:fullPath contents:imageData attributes:nil];
    responseString = fullPath;
  }

  else if (target == RCTCameraCaptureTargetTemp) {
    NSString *fileName = [[NSProcessInfo processInfo] globallyUniqueString];
    NSString *fullPath = [NSString stringWithFormat:@"%@%@.jpg", NSTemporaryDirectory(), fileName];

    [imageData writeToFile:fullPath atomically:YES];
    responseString = fullPath;
  }

  else if (target == RCTCameraCaptureTargetCameraRoll) {
    [[[ALAssetsLibrary alloc] init] writeImageDataToSavedPhotosAlbum:imageData metadata:metadata completionBlock:^(NSURL* url, NSError* error) {
      if (error == nil) {
        //path isn't really applicable here (this is an asset uri), but left it in for backward comparability
        resolve(@{@"path":[url absoluteString], @"mediaUri":[url absoluteString]});
      }
      else {
        reject(RCTErrorUnspecified, nil, RCTErrorWithMessage(error.description));
      }
    }];
    return;
  }
  resolve(@{@"path":responseString});
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

  if (self.mirrorImage) {
    CGAffineTransform transform = CGAffineTransformMakeTranslation(rotatedRect.size.width, 0.0);
    transform = CGAffineTransformScale(transform, -1.0, 1.0);
    CGContextConcatCTM(bmContext, transform);
  }

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

-(void)captureVideo:(NSInteger)target options:(NSDictionary *)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
    AVCaptureVideoOrientation orientation = options[@"orientation"] != nil ? [options[@"orientation"] integerValue] : self.orientation;
    if (orientation == RCTCameraOrientationAuto) {
        [self.sensorOrientationChecker getDeviceOrientationWithBlock:^(UIInterfaceOrientation orientation) {
            [self captureVideo:target options:options orientation:[self.sensorOrientationChecker convertToAVCaptureVideoOrientation: orientation] resolve:resolve reject:reject];
        }];
    } else {
        [self captureVideo:target options:options orientation:orientation resolve:resolve reject:reject];
    }
}

-(void)captureVideo:(NSInteger)target options:(NSDictionary *)options orientation:(AVCaptureVideoOrientation)orientation resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
  if (self.movieFileOutput.recording) {
    reject(RCTErrorUnspecified, nil, RCTErrorWithMessage(@"Already recording"));
    return;
  }

  if ([[options valueForKey:@"audio"] boolValue]) {
    [self initializeCaptureSessionInput:AVMediaTypeAudio];
  }

  Float64 totalSeconds = [[options valueForKey:@"totalSeconds"] floatValue];
  if (totalSeconds > -1) {
    int32_t preferredTimeScale = [[options valueForKey:@"preferredTimeScale"] intValue];
    CMTime maxDuration = CMTimeMakeWithSeconds(totalSeconds, preferredTimeScale);
    self.movieFileOutput.maxRecordedDuration = maxDuration;
  }

  dispatch_async(self.sessionQueue, ^{
    [[self.movieFileOutput connectionWithMediaType:AVMediaTypeVideo] setVideoOrientation:orientation];

    //Create temporary URL to record to
    NSString *outputPath = [[NSString alloc] initWithFormat:@"%@%@", NSTemporaryDirectory(), @"output.mov"];
    NSURL *outputURL = [[NSURL alloc] initFileURLWithPath:outputPath];
    NSFileManager *fileManager = [NSFileManager defaultManager];
    if ([fileManager fileExistsAtPath:outputPath]) {
        NSError *error;
        if ([fileManager removeItemAtPath:outputPath error:&error] == NO) {
          reject(RCTErrorUnspecified, nil, RCTErrorWithMessage(error.description));
          return;
        }
    }

    //Start recording
    [self.movieFileOutput startRecordingToOutputFileURL:outputURL recordingDelegate:self];

    self.videoResolve = resolve;
    self.videoReject = reject;
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
    self.videoReject(RCTErrorUnspecified, nil, RCTErrorWithMessage(@"Error while recording"));
    return;
  }

  AVURLAsset* videoAsAsset = [AVURLAsset URLAssetWithURL:outputFileURL options:nil];
  AVAssetTrack* videoTrack = [[videoAsAsset tracksWithMediaType:AVMediaTypeVideo] objectAtIndex:0];
  float videoWidth;
  float videoHeight;

  CGSize videoSize = [videoTrack naturalSize];
  CGAffineTransform txf = [videoTrack preferredTransform];

  if ((txf.tx == videoSize.width && txf.ty == videoSize.height) || (txf.tx == 0 && txf.ty == 0)) {
    // Video recorded in landscape orientation
    videoWidth = videoSize.width;
    videoHeight = videoSize.height;
  } else {
    // Video recorded in portrait orientation, so have to swap reported width/height
    videoWidth = videoSize.height;
    videoHeight = videoSize.width;
  }

  NSMutableDictionary *videoInfo = [NSMutableDictionary dictionaryWithDictionary:@{
     @"duration":[NSNumber numberWithFloat:CMTimeGetSeconds(videoAsAsset.duration)],
     @"width":[NSNumber numberWithFloat:videoWidth],
     @"height":[NSNumber numberWithFloat:videoHeight],
     @"size":[NSNumber numberWithLongLong:captureOutput.recordedFileSize],
  }];

  if (self.videoTarget == RCTCameraCaptureTargetCameraRoll) {
    ALAssetsLibrary *library = [[ALAssetsLibrary alloc] init];
    if ([library videoAtPathIsCompatibleWithSavedPhotosAlbum:outputFileURL]) {
      [library writeVideoAtPathToSavedPhotosAlbum:outputFileURL
                                  completionBlock:^(NSURL *assetURL, NSError *error) {
                                    if (error) {
                                      self.videoReject(RCTErrorUnspecified, nil, RCTErrorWithMessage(error.description));
                                      return;
                                    }
                                    [videoInfo setObject:[assetURL absoluteString] forKey:@"path"];
                                    self.videoResolve(videoInfo);
                                  }];
    }
  }
  else if (self.videoTarget == RCTCameraCaptureTargetDisk) {
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths firstObject];
    NSString *fullPath = [[documentsDirectory stringByAppendingPathComponent:[[NSUUID UUID] UUIDString]] stringByAppendingPathExtension:@"mov"];

    NSFileManager * fileManager = [NSFileManager defaultManager];
    NSError * error = nil;

    //moving to destination
    if (!([fileManager moveItemAtPath:[outputFileURL path] toPath:fullPath error:&error])) {
      self.videoReject(RCTErrorUnspecified, nil, RCTErrorWithMessage(error.description));
      return;
    }
    [videoInfo setObject:fullPath forKey:@"path"];
    self.videoResolve(videoInfo);
  }
  else if (self.videoTarget == RCTCameraCaptureTargetTemp) {
    NSString *fileName = [[NSProcessInfo processInfo] globallyUniqueString];
    NSString *fullPath = [NSString stringWithFormat:@"%@%@.mov", NSTemporaryDirectory(), fileName];

    NSFileManager * fileManager = [NSFileManager defaultManager];
    NSError * error = nil;

    //moving to destination
    if (!([fileManager moveItemAtPath:[outputFileURL path] toPath:fullPath error:&error])) {
        self.videoReject(RCTErrorUnspecified, nil, RCTErrorWithMessage(error.description));
        return;
    }
    [videoInfo setObject:fullPath forKey:@"path"];
    self.videoResolve(videoInfo);
  }
  else {
    self.videoReject(RCTErrorUnspecified, nil, RCTErrorWithMessage(@"Target not supported"));
  }
}

- (void)captureOutput:(AVCaptureOutput *)captureOutput didOutputMetadataObjects:(NSArray *)metadataObjects fromConnection:(AVCaptureConnection *)connection {

  for (AVMetadataMachineReadableCodeObject *metadata in metadataObjects) {
    for (id barcodeType in self.barCodeTypes) {
      if ([metadata.type isEqualToString:barcodeType]) {
        // Transform the meta-data coordinates to screen coords
        AVMetadataMachineReadableCodeObject *transformed = (AVMetadataMachineReadableCodeObject *)[_previewLayer transformedMetadataObjectForMetadataObject:metadata];

        NSDictionary *event = @{
          @"type": metadata.type,
          @"data": metadata.stringValue,
          @"bounds": @{
            @"origin": @{
              @"x": [NSString stringWithFormat:@"%f", transformed.bounds.origin.x],
              @"y": [NSString stringWithFormat:@"%f", transformed.bounds.origin.y]
            },
            @"size": @{
              @"height": [NSString stringWithFormat:@"%f", transformed.bounds.size.height],
              @"width": [NSString stringWithFormat:@"%f", transformed.bounds.size.width],
            }
          }
        };

        [self.bridge.eventDispatcher sendAppEventWithName:@"CameraBarCodeRead" body:event];
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

- (void)focusAtThePoint:(CGPoint) atPoint;
{
    Class captureDeviceClass = NSClassFromString(@"AVCaptureDevice");
    if (captureDeviceClass != nil) {
        dispatch_async([self sessionQueue], ^{
            AVCaptureDevice *device = [[self videoCaptureDeviceInput] device];
            if([device isFocusPointOfInterestSupported] &&
               [device isFocusModeSupported:AVCaptureFocusModeAutoFocus]) {
                CGRect cameraViewRect = [[self camera] bounds];
                double cameraViewWidth = cameraViewRect.size.width;
                double cameraViewHeight = cameraViewRect.size.height;
                double focus_x = atPoint.x/cameraViewWidth;
                double focus_y = atPoint.y/cameraViewHeight;
                CGPoint cameraViewPoint = CGPointMake(focus_x, focus_y);
                if([device lockForConfiguration:nil]) {
                    [device setFocusPointOfInterest:cameraViewPoint];
                    [device setFocusMode:AVCaptureFocusModeAutoFocus];
                    if ([device isExposurePointOfInterestSupported] && [device isExposureModeSupported:AVCaptureExposureModeAutoExpose]) {
                        [device setExposureMode:AVCaptureExposureModeAutoExpose];
                        [device setExposurePointOfInterest:cameraViewPoint];
                    }
                    [device unlockForConfiguration];
                }
            }
        });
    }
}

- (void)zoom:(CGFloat)velocity reactTag:(NSNumber *)reactTag{
    if (isnan(velocity)) {
        return;
    }
    const CGFloat pinchVelocityDividerFactor = 20.0f; // TODO: calibrate or make this component's property
    NSError *error = nil;
    AVCaptureDevice *device = [[self videoCaptureDeviceInput] device];
    if ([device lockForConfiguration:&error]) {
        CGFloat zoomFactor = device.videoZoomFactor + atan(velocity / pinchVelocityDividerFactor);
        if (zoomFactor > device.activeFormat.videoMaxZoomFactor) {
            zoomFactor = device.activeFormat.videoMaxZoomFactor;
        } else if (zoomFactor < 1) {
            zoomFactor = 1.0f;
        }

        NSDictionary *event = @{
          @"target": reactTag,
          @"zoomFactor": [NSNumber numberWithDouble:zoomFactor],
          @"velocity": [NSNumber numberWithDouble:velocity]
        };

        [self.bridge.eventDispatcher sendInputEventWithName:@"zoomChanged" body:event];

        device.videoZoomFactor = zoomFactor;
        [device unlockForConfiguration];
    } else {
        NSLog(@"error: %@", error);
    }
}

- (void)setCaptureQuality:(NSString *)quality
{
    #if !(TARGET_IPHONE_SIMULATOR)
        if (quality) {
            [self.session beginConfiguration];
            if ([self.session canSetSessionPreset:quality]) {
                self.session.sessionPreset = quality;
            }
            [self.session commitConfiguration];
        }
    #endif
}

@end
