#import "FaceDetectorManagerMlkit.h"
#import <React/RCTConvert.h>
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
    self.options.contourMode = FIRVisionFaceDetectorContourModeNone;
    self.options.classificationMode = FIRVisionFaceDetectorClassificationModeNone;
    
    self.vision = [FIRVision vision];
    self.faceRecognizer = [_vision faceDetectorWithOptions:_options];
  }
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
             @"Contours" : @{
                     @"all" : @(RNFaceDetectAllContours),
                     @"none" : @(RNFaceDetectNoContours)
                     },
             @"Classifications" : @{
                     @"all" : @(RNFaceRunAllClassifications),
                     @"none" : @(RNFaceRunNoClassifications)
                     }
             };
}

- (void)setTracking:(id)json queue:(dispatch_queue_t)sessionQueue 
{
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

- (void)setContoursMode:(id)json queue:(dispatch_queue_t)sessionQueue
{
    long requestedValue = [RCTConvert NSInteger:json];
    if (requestedValue != self.options.contourMode) {
        if (sessionQueue) {
            dispatch_async(sessionQueue, ^{
                self.options.contourMode = requestedValue;
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
    FIRVisionImage *image = [[FIRVisionImage alloc] initWithImage:uiImage];
    NSMutableArray *emptyResult = [[NSMutableArray alloc] init];
    [_faceRecognizer
     processImage:image
     completion:^(NSArray<FIRVisionFace *> *faces, NSError *error) {
         if (error != nil || faces == nil) {
             completed(emptyResult);
         } else {
             completed([self processFaces:faces]);
         }
     }];
}

- (NSArray *)processFaces:(NSArray *)faces 
{
    NSMutableArray *result = [[NSMutableArray alloc] init];
    for (FIRVisionFace *face in faces) {
        NSMutableDictionary *resultDict =
        [[NSMutableDictionary alloc] initWithCapacity:21];
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

        if (self.options.contourMode == FIRVisionFaceDetectorContourModeAll) {
            NSDictionary *contours = [self processContours:face];
            [resultDict setObject:contours forKey:@"contours"];
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
    return result;
}

- (NSDictionary *)processBounds:(CGRect)bounds 
{
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

- (NSDictionary *)processContours:(FIRVisionFace *)face {
    NSDictionary *contours = @{
        @"all" : [self contourToPoints:[face contourOfType:FIRFaceContourTypeAll]],
        @"face" : [self contourToPoints:[face contourOfType:FIRFaceContourTypeFace]],
        @"leftEye" : [self contourToPoints:[face contourOfType:FIRFaceContourTypeLeftEye]],
        @"leftEyebrowTop" : [self contourToPoints:[face contourOfType:FIRFaceContourTypeLeftEyebrowTop]],
        @"leftEyebrowBottom" : [self contourToPoints:[face contourOfType:FIRFaceContourTypeLeftEyebrowBottom]],
        @"rightEye" : [self contourToPoints:[face contourOfType:FIRFaceContourTypeRightEye]],
        @"rightEyebrowTop" : [self contourToPoints:[face contourOfType:FIRFaceContourTypeRightEyebrowTop]],
        @"rightEyebrowBottom" : [self contourToPoints:[face contourOfType:FIRFaceContourTypeRightEyebrowBottom]],
        @"upperLipTop" : [self contourToPoints:[face contourOfType:FIRFaceContourTypeUpperLipTop]],
        @"upperLipBottom" : [self contourToPoints:[face contourOfType:FIRFaceContourTypeUpperLipBottom]],
        @"lowerLipTop" : [self contourToPoints:[face contourOfType:FIRFaceContourTypeLowerLipTop]],
        @"lowerLipBottom" : [self contourToPoints:[face contourOfType:FIRFaceContourTypeLowerLipBottom]],
        @"noseBridge" : [self contourToPoints:[face contourOfType:FIRFaceContourTypeNoseBridge]],
        @"noseBottom" : [self contourToPoints:[face contourOfType:FIRFaceContourTypeNoseBottom]],
    };
    return contours;
}

- (NSArray *)contourToPoints:(FIRVisionFaceContour *)visionFaceContour {
    NSMutableArray *pointsFormatted = [[NSMutableArray alloc] init];
    if (visionFaceContour == nil) {
        return pointsFormatted;
    }

    for (FIRVisionPoint *point in visionFaceContour.points) {
        [pointsFormatted addObject:[self processPoint:point]];
    }

    return pointsFormatted;
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

- (void)setContoursMode:(id)json:(dispatch_queue_t)sessionQueue
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
             @"Contours" : @{},
             @"Classifications" : @{}
             };
}

@end
#endif
