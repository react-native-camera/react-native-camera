//
//  RNSensorOrientationChecker.h
//  RNCamera
//
//  Created by Radu Popovici on 24/03/16.
//
//

#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>


@interface RNSensorOrientationChecker : NSObject

- (void) start;
- (void) stop;
- (UIInterfaceOrientation)getDeviceOrientation;
- (AVCaptureVideoOrientation)convertToAVCaptureVideoOrientation:(UIInterfaceOrientation)orientation;

@end
