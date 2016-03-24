//
//  RCTSensorOrientationChecker.m
//  RCTCamera
//
//  Created by Radu Popovici on 24/03/16.
//
//

#import "RCTSensorOrientationChecker.h"
#import <CoreMotion/CoreMotion.h>

@interface RCTSensorOrientationChecker ()

@property (strong, nonatomic) CMMotionManager * motionManager;

@end

@implementation RCTSensorOrientationChecker

- (void)getDeviceOrientation:(void(^)(UIInterfaceOrientation orientation))callback
{
    self.motionManager = [[CMMotionManager alloc] init];
    self.motionManager.accelerometerUpdateInterval = DBL_MAX; // infinite delay, never update
    self.motionManager.gyroUpdateInterval = DBL_MAX;
    
    __weak CMMotionManager * weakMotionManager = self.motionManager;
    __weak __typeof(self) weakSelf = self;
    
    [self.motionManager startAccelerometerUpdatesToQueue:[NSOperationQueue new]
                                             withHandler:^(CMAccelerometerData  *accelerometerData, NSError *error) {
                                                 // stop after the first sample
                                                 [weakMotionManager stopAccelerometerUpdates];
                                                 
                                                 UIInterfaceOrientation deviceOrientation = UIInterfaceOrientationUnknown;
                                                 if (!error) {
                                                     deviceOrientation = [weakSelf getOrientationBy:accelerometerData.acceleration];
                                                 }
                                                 
                                                 if (callback) {
                                                     callback(deviceOrientation);
                                                 }
                                             }];
    
}

- (UIInterfaceOrientation)getOrientationBy:(CMAcceleration)acceleration
{
    if(acceleration.x >= 0.75) {
        return UIInterfaceOrientationLandscapeLeft;
    }
    if(acceleration.x <= -0.75) {
        return UIInterfaceOrientationLandscapeRight;
    }
    if(acceleration.y >= -0.75) {
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
