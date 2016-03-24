//
//  RCTSensorOrientationChecker.h
//  RCTCamera
//
//  Created by Radu Popovici on 24/03/16.
//
//

#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>

typedef void (^RCTSensorCallback) (UIInterfaceOrientation orientation);

@interface RCTSensorOrientationChecker : NSObject

@property (assign, nonatomic) UIInterfaceOrientation orientation;

- (void)getDeviceOrientationWithBlock:(RCTSensorCallback)callback;
- (AVCaptureVideoOrientation)convertToAVCaptureVideoOrientation:(UIInterfaceOrientation)orientation;

@end
