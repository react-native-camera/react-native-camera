//
//  RNFaceDetectorStub.m
//  RCTCamera
//
//  Created by Joao Guilherme Daros Fidelis on 24/01/18.
//

#import "RNFaceDetectorManagerStub.h"
#import <React/RCTLog.h>

@implementation RNFaceDetectorManagerStub

- (NSDictionary *)constantsToExport {
    return [[self class] constants];
}

+ (NSDictionary *)constants {
    return @{@"Mode" : @{},
             @"Landmarks" : @{},
             @"Classifications" : @{}};
}

- (instancetype)initWithSessionQueue:(dispatch_queue_t)sessionQueue delegate:(id <RNFaceDetectorDelegate>)delegate {
    self = [super init];
    return self;
}

- (void)setIsEnabled:(id)json { }
- (void)setLandmarksDetected:(id)json { }
- (void)setClassificationsDetected:(id)json { }
- (void)setMode:(id)json { }

- (void)maybeStartFaceDetectionOnSession:(AVCaptureSession *)session withPreviewLayer:(AVCaptureVideoPreviewLayer *)previewLayer {
    RCTLogWarn(@"FaceDetector not integrated, stub used!");
}
- (void)stopFaceDetection { }

@end

