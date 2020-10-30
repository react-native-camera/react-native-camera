#import "FaceDetectorManagerMlkit.h"
#import <React/RCTConvert.h> 
#import <React/RCTLog.h>
#if __has_include(<FirebaseMLVision/FirebaseMLVision.h>)

@interface FaceDetectorManagerMlkit ()
@property(nonatomic, strong) FIRVisionFaceDetector *faceRecognizer;
@property(nonatomic, strong) FIRVision *vision;
@property(nonatomic, strong) FIRVisionFaceDetectorOptions *options;
@property(nonatomic, assign) float scaleX;
@property(nonatomic, assign) float scaleY;
@end

@implementation FaceDetectorManagerMlkit

- (instancetype)init 
{
  if (self = [super init]) {
    self.options = [[FIRVisionFaceDetectorOptions alloc] init];
    self.options.performanceMode = FIRVisionFaceDetectorPerformanceModeFast;
    self.options.landmarkMode = FIRVisionFaceDetectorLandmarkModeNone;
    self.options.classificationMode = FIRVisionFaceDetectorClassificationModeNone;
    
    self.vision = [FIRVision vision];
    self.faceRecognizer = [_vision faceDetectorWithOptions:_options];
  }
// ================================================  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        // RCTLogInfo(@"FaceDetectorManagerMlkit > init");  //only warn or error get response from react log.
// ================================================  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  
  return self;
}

- (BOOL)isRealDetector 
{
  return true;
}

+ (NSDictionary *)constants
{
    return @{
             @"Mode" : @{
                     @"fast" : @(RNFaceDetectionFastMode),
                     @"accurate" : @(RNFaceDetectionAccurateMode)
                     },
             @"Landmarks" : @{
                     @"all" : @(RNFaceDetectAllLandmarks),
                     @"none" : @(RNFaceDetectNoLandmarks)
                     },
             @"Classifications" : @{
                     @"all" : @(RNFaceRunAllClassifications),
                     @"none" : @(RNFaceRunNoClassifications)
                     }
             };
}

- (void)setTracking:(id)json queue:(dispatch_queue_t)sessionQueue 
{
    // ================================================  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        // RCTLogInfo(@"FaceDetectorManagerMlkit > setTracking");  //only warn or error get response from react log.
// ================================================  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  
  BOOL requestedValue = [RCTConvert BOOL:json];
  if (requestedValue != self.options.trackingEnabled) {
      if (sessionQueue) {
          dispatch_async(sessionQueue, ^{
              self.options.trackingEnabled = requestedValue;
              self.faceRecognizer =
              [self.vision faceDetectorWithOptions:self.options];
          });
      }
  }
}

- (void)setLandmarksMode:(id)json queue:(dispatch_queue_t)sessionQueue 
{
    // ================================================  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        // RCTLogInfo(@"FaceDetectorManagerMlkit > setLandmarksMode");  //only warn or error get response from react log.
// ================================================  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  
    long requestedValue = [RCTConvert NSInteger:json];
    if (requestedValue != self.options.landmarkMode) {
        if (sessionQueue) {
            dispatch_async(sessionQueue, ^{
                self.options.landmarkMode = requestedValue;
                self.faceRecognizer =
                [self.vision faceDetectorWithOptions:self.options];
            });
        }
    }
}

- (void)setPerformanceMode:(id)json queue:(dispatch_queue_t)sessionQueue 
{
    long requestedValue = [RCTConvert NSInteger:json];
    if (requestedValue != self.options.performanceMode) {
        if (sessionQueue) {
            dispatch_async(sessionQueue, ^{
                self.options.performanceMode = requestedValue;
                self.faceRecognizer =
                [self.vision faceDetectorWithOptions:self.options];
            });
        }
    }
}

- (void)setClassificationMode:(id)json queue:(dispatch_queue_t)sessionQueue 
{
    long requestedValue = [RCTConvert NSInteger:json];
    if (requestedValue != self.options.classificationMode) {
        if (sessionQueue) {
            dispatch_async(sessionQueue, ^{
                self.options.classificationMode = requestedValue;
                self.faceRecognizer =
                [self.vision faceDetectorWithOptions:self.options];
            });
        }
    }
}

- (void)findFacesInFrame:(UIImage *)uiImage
                  scaleX:(float)scaleX
                  scaleY:(float)scaleY
               completed:(void (^)(NSArray *result))completed 
{
    self.scaleX = scaleX;
    self.scaleY = scaleY;

// ================================================  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    // int control = 1;
        NSData *imageData = UIImagePNGRepresentation(uiImage);
        // NSLog(@"image data length : %d", [imageData length] );
        // RCTLogError(@"image data length : %d", [imageData length] );
        // RCTLogWarn(@"image data length : %d", [imageData length] );
        // RCTLog(@"image data length : %d", [imageData length] ) ;
        // RCTLogTrace(@"image data length : %d", [imageData length] );
        // RCTLogInfo(@"image data length : %d", [imageData length] ) ;
        // RCTLogAdvice(@"advice from bridge", @"image data length : %d", [imageData length] );
        NSString * base64String = [imageData base64EncodedStringWithOptions:NSDataBase64Encoding64CharacterLineLength];
       
        // NSLog((@"%@", base64String));
        //        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Test Message" 
        //                                           message:@"This is a sample"
        //                                          delegate:nil
        //                                 cancelButtonTitle:@"OK" 
        //                                 otherButtonTitles:nil];
        // [alert show];
// ================================================  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
// ================================================  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        RCTLogInfo(@"FaceDetectorManagerMlkit > findFacesInFrame");  //only warn or error get response from react log.
// ================================================  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  




    FIRVisionImage *image = [[FIRVisionImage alloc] initWithImage:uiImage];
    NSMutableArray *emptyResult = [[NSMutableArray alloc] init];
    [_faceRecognizer
     processImage:image
     completion:^(NSArray<FIRVisionFace *> *faces, NSError *error) {

         if (error != nil || faces == nil ) {
             completed(emptyResult);
         } else {
             int size = [faces count];
             if (size < 1) {
                 completed(emptyResult);
             }
                     // --------------------------------------  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  check this
        // todo: call other function, with the image included to cut faces, return face image or recognize image.
            //  completed([self processFaces:faces]);
            completed([self processFaces:faces inImage:base64String]);
         }
     }];
}
- (NSArray *)processFaces:(NSArray *)faces inImage:(NSString *)base64Image
{
// ================================================  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        RCTLogInfo(@"FaceDetectorManagerMlkit > processFaces");  //only warn or error get response from react log.
// ================================================  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  
    NSMutableArray *result = [[NSMutableArray alloc] init];

// ================================================  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
            // NSLog(@"faces length : %d", [faces count] );
        // RCTLogError(@"faces length : %d", [faces count]  );
        // RCTLogWarn(@"faces length : %d", [faces count] );
        // RCTLog(@"faces length : %d", [faces count]  ) ;
        // RCTLogTrace(@"faces length : %d", [faces count] );
        RCTLogInfo(@"faces length : %d", [faces count]  ) ;
        // RCTLogAdvice(@"advice from bridge", @"faces length : %d", [faces count] );
// ================================================  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    for (FIRVisionFace *face in faces) {
        NSMutableDictionary *resultDict =
        [[NSMutableDictionary alloc] initWithCapacity:20];
        //set imaga data
        // [resultDict setObject:base64Image forKey:@"base64"];
        // Boundaries of face in image
        NSDictionary *bounds = [self processBounds:face.frame];
        [resultDict setObject:bounds forKey:@"bounds"];
        // If face tracking was enabled:
        if (face.hasTrackingID) {
            NSInteger trackingID = face.trackingID;
            [resultDict setObject:@(trackingID) forKey:@"faceID"];
        }
        // Head is rotated to the right rotY degrees
        if (face.hasHeadEulerAngleY) {
            CGFloat rotY = face.headEulerAngleY;
            [resultDict setObject:@(rotY) forKey:@"yawAngle"];
        }
        // Head is tilted sideways rotZ degrees
        if (face.hasHeadEulerAngleZ) {
            CGFloat rotZ = -1 * face.headEulerAngleZ;
            [resultDict setObject:@(rotZ) forKey:@"rollAngle"];
        }
        
        // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
        // nose available):
        /** Midpoint of the left ear tip and left ear lobe. */
        FIRVisionFaceLandmark *leftEar =
        [face landmarkOfType:FIRFaceLandmarkTypeLeftEar];
        if (leftEar != nil) {
            [resultDict setObject:[self processPoint:leftEar.position]
                           forKey:@"leftEarPosition"];
        }
        /** Midpoint of the right ear tip and right ear lobe. */
        FIRVisionFaceLandmark *rightEar =
        [face landmarkOfType:FIRFaceLandmarkTypeRightEar];
        if (rightEar != nil) {
            [resultDict setObject:[self processPoint:rightEar.position]
                           forKey:@"rightEarPosition"];
        }
        /** Center of the bottom lip. */
        FIRVisionFaceLandmark *mouthBottom =
        [face landmarkOfType:FIRFaceLandmarkTypeMouthBottom];
        if (mouthBottom != nil) {
            [resultDict setObject:[self processPoint:mouthBottom.position]
                           forKey:@"bottomMouthPosition"];
        }
        /** Right corner of the mouth */
        FIRVisionFaceLandmark *mouthRight =
        [face landmarkOfType:FIRFaceLandmarkTypeMouthRight];
        if (mouthRight != nil) {
            [resultDict setObject:[self processPoint:mouthRight.position]
                           forKey:@"rightMouthPosition"];
        }
        /** Left corner of the mouth */
        FIRVisionFaceLandmark *mouthLeft =
        [face landmarkOfType:FIRFaceLandmarkTypeMouthLeft];
        if (mouthLeft != nil) {
            [resultDict setObject:[self processPoint:mouthLeft.position]
                           forKey:@"leftMouthPosition"];
        }
        /** Left eye. */
        FIRVisionFaceLandmark *eyeLeft =
        [face landmarkOfType:FIRFaceLandmarkTypeLeftEye];
        if (eyeLeft != nil) {
            [resultDict setObject:[self processPoint:eyeLeft.position]
                           forKey:@"leftEyePosition"];
        }
        /** Right eye. */
        FIRVisionFaceLandmark *eyeRight =
        [face landmarkOfType:FIRFaceLandmarkTypeRightEye];
        if (eyeRight != nil) {
            [resultDict setObject:[self processPoint:eyeRight.position]
                           forKey:@"rightEyePosition"];
        }
        /** Left cheek. */
        FIRVisionFaceLandmark *cheekLeft =
        [face landmarkOfType:FIRFaceLandmarkTypeLeftCheek];
        if (cheekLeft != nil) {
            [resultDict setObject:[self processPoint:cheekLeft.position]
                           forKey:@"leftCheekPosition"];
        }
        /** Right cheek. */
        FIRVisionFaceLandmark *cheekRight =
        [face landmarkOfType:FIRFaceLandmarkTypeRightCheek];
        if (cheekRight != nil) {
            [resultDict setObject:[self processPoint:cheekRight.position]
                           forKey:@"rightCheekPosition"];
        }
        /** Midpoint between the nostrils where the nose meets the face. */
        FIRVisionFaceLandmark *noseBase =
        [face landmarkOfType:FIRFaceLandmarkTypeNoseBase];
        if (noseBase != nil) {
            [resultDict setObject:[self processPoint:noseBase.position]
                           forKey:@"noseBasePosition"];
        }
        
        // If classification was enabled:
        if (face.hasSmilingProbability) {
            CGFloat smileProb = face.smilingProbability;
            [resultDict setObject:@(smileProb) forKey:@"smilingProbability"];
        }
        if (face.hasRightEyeOpenProbability) {
            CGFloat rightEyeOpenProb = face.rightEyeOpenProbability;
            [resultDict setObject:@(rightEyeOpenProb)
                           forKey:@"rightEyeOpenProbability"];
        }
        if (face.hasLeftEyeOpenProbability) {
            CGFloat leftEyeOpenProb = face.leftEyeOpenProbability;
            [resultDict setObject:@(leftEyeOpenProb)
                           forKey:@"leftEyeOpenProbability"];
        }
        [result addObject:resultDict];
    }
    // ================================================  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        // NSLog(@"face detection result: %@", result);
        // RCTLogError(@"face detection result: %@", result );
        RCTLogInfo(@"processFaces result: %@", result);  //only warn or error get response from react log.
        // RCTLog(@"face detection result: %@", result) ;
        // RCTLogTrace(@"face detection result: %@", result);
        // RCTLogInfo(@"face detection result: %@", result ) ;
        // RCTLogAdvice(@"advice from bridge", @"face detection result: %@", result );
// ================================================  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

    return result;
}
- (NSArray *)processFaces:(NSArray *)faces 
{
// ================================================  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        RCTLogInfo(@"FaceDetectorManagerMlkit > processFaces");  //only warn or error get response from react log.
// ================================================  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  
    NSMutableArray *result = [[NSMutableArray alloc] init];

// ================================================  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
            // NSLog(@"faces length : %d", [faces count] );
        // RCTLogError(@"faces length : %d", [faces count]  );
        // RCTLogWarn(@"faces length : %d", [faces count] );
        // RCTLog(@"faces length : %d", [faces count]  ) ;
        // RCTLogTrace(@"faces length : %d", [faces count] );
        RCTLogInfo(@"faces length : %d", [faces count]  ) ;
        // RCTLogAdvice(@"advice from bridge", @"faces length : %d", [faces count] );
// ================================================  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    for (FIRVisionFace *face in faces) {
        NSMutableDictionary *resultDict =
        [[NSMutableDictionary alloc] initWithCapacity:20];
        // Boundaries of face in image
        NSDictionary *bounds = [self processBounds:face.frame];
        [resultDict setObject:bounds forKey:@"bounds"];
        // If face tracking was enabled:
        if (face.hasTrackingID) {
            NSInteger trackingID = face.trackingID;
            [resultDict setObject:@(trackingID) forKey:@"faceID"];
        }
        // Head is rotated to the right rotY degrees
        if (face.hasHeadEulerAngleY) {
            CGFloat rotY = face.headEulerAngleY;
            [resultDict setObject:@(rotY) forKey:@"yawAngle"];
        }
        // Head is tilted sideways rotZ degrees
        if (face.hasHeadEulerAngleZ) {
            CGFloat rotZ = -1 * face.headEulerAngleZ;
            [resultDict setObject:@(rotZ) forKey:@"rollAngle"];
        }
        
        // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
        // nose available):
        /** Midpoint of the left ear tip and left ear lobe. */
        FIRVisionFaceLandmark *leftEar =
        [face landmarkOfType:FIRFaceLandmarkTypeLeftEar];
        if (leftEar != nil) {
            [resultDict setObject:[self processPoint:leftEar.position]
                           forKey:@"leftEarPosition"];
        }
        /** Midpoint of the right ear tip and right ear lobe. */
        FIRVisionFaceLandmark *rightEar =
        [face landmarkOfType:FIRFaceLandmarkTypeRightEar];
        if (rightEar != nil) {
            [resultDict setObject:[self processPoint:rightEar.position]
                           forKey:@"rightEarPosition"];
        }
        /** Center of the bottom lip. */
        FIRVisionFaceLandmark *mouthBottom =
        [face landmarkOfType:FIRFaceLandmarkTypeMouthBottom];
        if (mouthBottom != nil) {
            [resultDict setObject:[self processPoint:mouthBottom.position]
                           forKey:@"bottomMouthPosition"];
        }
        /** Right corner of the mouth */
        FIRVisionFaceLandmark *mouthRight =
        [face landmarkOfType:FIRFaceLandmarkTypeMouthRight];
        if (mouthRight != nil) {
            [resultDict setObject:[self processPoint:mouthRight.position]
                           forKey:@"rightMouthPosition"];
        }
        /** Left corner of the mouth */
        FIRVisionFaceLandmark *mouthLeft =
        [face landmarkOfType:FIRFaceLandmarkTypeMouthLeft];
        if (mouthLeft != nil) {
            [resultDict setObject:[self processPoint:mouthLeft.position]
                           forKey:@"leftMouthPosition"];
        }
        /** Left eye. */
        FIRVisionFaceLandmark *eyeLeft =
        [face landmarkOfType:FIRFaceLandmarkTypeLeftEye];
        if (eyeLeft != nil) {
            [resultDict setObject:[self processPoint:eyeLeft.position]
                           forKey:@"leftEyePosition"];
        }
        /** Right eye. */
        FIRVisionFaceLandmark *eyeRight =
        [face landmarkOfType:FIRFaceLandmarkTypeRightEye];
        if (eyeRight != nil) {
            [resultDict setObject:[self processPoint:eyeRight.position]
                           forKey:@"rightEyePosition"];
        }
        /** Left cheek. */
        FIRVisionFaceLandmark *cheekLeft =
        [face landmarkOfType:FIRFaceLandmarkTypeLeftCheek];
        if (cheekLeft != nil) {
            [resultDict setObject:[self processPoint:cheekLeft.position]
                           forKey:@"leftCheekPosition"];
        }
        /** Right cheek. */
        FIRVisionFaceLandmark *cheekRight =
        [face landmarkOfType:FIRFaceLandmarkTypeRightCheek];
        if (cheekRight != nil) {
            [resultDict setObject:[self processPoint:cheekRight.position]
                           forKey:@"rightCheekPosition"];
        }
        /** Midpoint between the nostrils where the nose meets the face. */
        FIRVisionFaceLandmark *noseBase =
        [face landmarkOfType:FIRFaceLandmarkTypeNoseBase];
        if (noseBase != nil) {
            [resultDict setObject:[self processPoint:noseBase.position]
                           forKey:@"noseBasePosition"];
        }
        
        // If classification was enabled:
        if (face.hasSmilingProbability) {
            CGFloat smileProb = face.smilingProbability;
            [resultDict setObject:@(smileProb) forKey:@"smilingProbability"];
        }
        if (face.hasRightEyeOpenProbability) {
            CGFloat rightEyeOpenProb = face.rightEyeOpenProbability;
            [resultDict setObject:@(rightEyeOpenProb)
                           forKey:@"rightEyeOpenProbability"];
        }
        if (face.hasLeftEyeOpenProbability) {
            CGFloat leftEyeOpenProb = face.leftEyeOpenProbability;
            [resultDict setObject:@(leftEyeOpenProb)
                           forKey:@"leftEyeOpenProbability"];
        }
        [result addObject:resultDict];
    }
    // ================================================  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        // NSLog(@"face detection result: %@", result);
        // RCTLogError(@"face detection result: %@", result );
        RCTLogInfo(@"processFaces result: %@", result);  //only warn or error get response from react log.
        // RCTLog(@"face detection result: %@", result) ;
        // RCTLogTrace(@"face detection result: %@", result);
        // RCTLogInfo(@"face detection result: %@", result ) ;
        // RCTLogAdvice(@"advice from bridge", @"face detection result: %@", result );
// ================================================  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

    return result;
}

- (NSDictionary *)processBounds:(CGRect)bounds 
{
// ================================================  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        RCTLogInfo(@"FaceDetectorManagerMlkit > processBounds");  //only warn or error get response from react log.
// todo: check scaleX, scaleY
// ================================================  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ 
    float width = bounds.size.width * _scaleX;
    float height = bounds.size.height * _scaleY;
    float originX = bounds.origin.x * _scaleX;
    float originY = bounds.origin.y * _scaleY;
    NSDictionary *boundsDict = @{
                                 @"size" : @{@"width" : @(width), @"height" : @(height)},
                                 @"origin" : @{@"x" : @(originX), @"y" : @(originY)}
                                 };
    return boundsDict;
}

- (NSDictionary *)processPoint:(FIRVisionPoint *)point 
{
    float originX = [point.x floatValue] * _scaleX;
    float originY = [point.y floatValue] * _scaleY;
    NSDictionary *pointDict = @{
                                
                                @"x" : @(originX),
                                @"y" : @(originY)
                                };
    return pointDict;
}

@end
#else

@interface FaceDetectorManagerMlkit ()
@end

@implementation FaceDetectorManagerMlkit

- (instancetype)init {
    // ================================================  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        RCTLogInfo(@"FaceDetectorManagerMlkit > init without FirebaseMLVision");  //only warn or error get response from react log.
// ================================================  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  
    self = [super init];
    return self;
}

- (BOOL)isRealDetector {
    return false;
}

- (NSArray *)findFacesInFrame:(UIImage *)image
                       scaleX:(float)scaleX
                       scaleY:(float)scaleY
                       completed:(void (^)(NSArray *result))completed;
{
    // ================================================  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        RCTLogInfo(@"FaceDetectorManagerMlkit > findFacesInFrame (without FirebaseMLVision) : not yet implemented");  //only warn or error get response from react log.
// ================================================  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  
    NSLog(@"FaceDetector not installed, stub used!");
    NSArray *features = @[ @"Error, Face Detector not installed" ];
    return features;
}

- (void)setTracking:(id)json:(dispatch_queue_t)sessionQueue 
{
    return;
}
- (void)setLandmarksMode:(id)json:(dispatch_queue_t)sessionQueue 
{
    return;
}

- (void)setPerformanceMode:(id)json:(dispatch_queue_t)sessionQueue 
{
    return;
}

- (void)setClassificationMode:(id)json:(dispatch_queue_t)sessionQueue 
{
    return;
}

+ (NSDictionary *)constantsToExport
{
    return @{
             @"Mode" : @{},
             @"Landmarks" : @{},
             @"Classifications" : @{}
             };
}

@end
#endif
