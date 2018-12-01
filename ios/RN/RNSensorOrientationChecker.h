//
//  RNSensorOrientationChecker.h
//  RNCamera
//
//  Created by Radu Popovici on 24/03/16.
//
//

#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>

typedef void (^RNSensorCallback) (UIInterfaceOrientation orientation);

@interface RNSensorOrientationChecker : NSObject

@property (assign, nonatomic) UIInterfaceOrientation orientation;

- (void)getDeviceOrientationWithBlock:(RNSensorCallback)callback;
- (AVCaptureVideoOrientation)convertToAVCaptureVideoOrientation:(UIInterfaceOrientation)orientation;

@end
