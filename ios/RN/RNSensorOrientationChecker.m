//
//  RNSensorOrientationChecker.m
//  RNCamera
//
//  Created by Radu Popovici on 24/03/16.
//
//

#import "RNSensorOrientationChecker.h"
#import <CoreMotion/CoreMotion.h>


@interface RNSensorOrientationChecker ()

@property (strong, nonatomic) CMMotionManager * motionManager;
@property (strong, nonatomic) RNSensorCallback orientationCallback;

@end

@implementation RNSensorOrientationChecker

- (instancetype)init
{
    self = [super init];
    if (self) {
        // Initialization code
        self.motionManager = [[CMMotionManager alloc] init];
        self.motionManager.accelerometerUpdateInterval = 0.2;
        self.motionManager.gyroUpdateInterval = 0.2;
        self.orientationCallback = nil;
    }
    return self;
}

- (void)dealloc
{
    [self pause];
}

- (void)resume
{
    __weak __typeof(self) weakSelf = self;
    [self.motionManager startDeviceMotionUpdatesToQueue:[NSOperationQueue new]
                                             withHandler:^(CMDeviceMotion  *data, NSError *error) {
                                                 if (!error) {
                                                     self.orientation = [weakSelf getOrientationBy:data];
                                                 }
                                                 if (self.orientationCallback) {
                                                     self.orientationCallback(self.orientation);
                                                 }
                                             }];
}

- (void)pause
{
    [self.motionManager stopDeviceMotionUpdates];
}

- (void)getDeviceOrientationWithBlock:(RNSensorCallback)callback
{
    __weak __typeof(self) weakSelf = self;
    self.orientationCallback = ^(UIInterfaceOrientation orientation) {
        // Synchronized because this might fire more than once
        // under some circumstances, causing a very bad loop
        // to people that uses it.
        @synchronized (weakSelf) {
            if (callback && weakSelf.orientationCallback) {
                callback(orientation);
            }
            weakSelf.orientationCallback = nil;
            [weakSelf pause];
        }
    };
    [self resume];
}

- (UIInterfaceOrientation)getOrientationBy:(CMDeviceMotion*)motion
{
    CMAcceleration gravity = motion.gravity;
    
    if (fabs(gravity.y) < fabs(gravity.x)) {
        if(gravity.x > 0){
            return UIInterfaceOrientationLandscapeLeft;
        }
        else{
            return UIInterfaceOrientationLandscapeRight;
        }
    } else {
        if(gravity.y > 0){
            return UIInterfaceOrientationPortraitUpsideDown;
        }
        else{
            return UIInterfaceOrientationPortrait;
        }
    }
    
//    __block UIInterfaceOrientation orientation;
//    dispatch_sync(dispatch_get_main_queue(), ^{
//        orientation = [[UIApplication sharedApplication] statusBarOrientation];
//    });
//    return orientation;
}

- (AVCaptureVideoOrientation)convertToAVCaptureVideoOrientation:(UIInterfaceOrientation)orientation
{
    switch (orientation) {
        case UIInterfaceOrientationPortrait:
            return AVCaptureVideoOrientationPortrait;
        case UIInterfaceOrientationPortraitUpsideDown:
            return AVCaptureVideoOrientationPortraitUpsideDown;
        case UIInterfaceOrientationLandscapeLeft:
            return AVCaptureVideoOrientationLandscapeLeft;
        case UIInterfaceOrientationLandscapeRight:
            return AVCaptureVideoOrientationLandscapeRight;
        default:
            return 0; // unknown
    }
}

@end
