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
    [self.motionManager startAccelerometerUpdatesToQueue:[NSOperationQueue new]
                                             withHandler:^(CMAccelerometerData  *accelerometerData, NSError *error) {
                                                 if (!error) {
                                                     self.orientation = [weakSelf getOrientationBy:accelerometerData.acceleration];
                                                 }
                                                 if (self.orientationCallback) {
                                                     self.orientationCallback(self.orientation);
                                                 }
                                             }];
}

- (void)pause
{
    [self.motionManager stopAccelerometerUpdates];
}

- (void)getDeviceOrientationWithBlock:(RNSensorCallback)callback
{
    __weak __typeof(self) weakSelf = self;
    self.orientationCallback = ^(UIInterfaceOrientation orientation) {
        if (callback) {
            callback(orientation);
        }
        weakSelf.orientationCallback = nil;
        [weakSelf pause];
    };
    [self resume];
}

- (UIInterfaceOrientation)getOrientationBy:(CMAcceleration)acceleration
{
    if(acceleration.x >= 0.75) {
        return UIInterfaceOrientationLandscapeLeft;
    }
    if(acceleration.x <= -0.75) {
        return UIInterfaceOrientationLandscapeRight;
    }
    if(acceleration.y <= -0.75) {
        return UIInterfaceOrientationPortrait;
    }
    if(acceleration.y >= 0.75) {
        return UIInterfaceOrientationPortraitUpsideDown;
    }
    return [[UIApplication sharedApplication] statusBarOrientation];
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
