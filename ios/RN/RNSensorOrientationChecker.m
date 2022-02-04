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
        self.motionManager.deviceMotionUpdateInterval = 0.2;
    }
    
    
    return self;
}

- (void)dealloc
{
    [self stop];
}

- (void)start
{
    [self.motionManager startDeviceMotionUpdates];
}

- (void)stop
{
    [self.motionManager stopDeviceMotionUpdates];
}

- (UIInterfaceOrientation)getDeviceOrientation
{
    CMDeviceMotion* data = self.motionManager.deviceMotion;
    return [self getOrientationBy:data];
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
