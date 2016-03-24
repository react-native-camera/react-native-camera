//
//  RCTSensorOrientationChecker.h
//  RCTCamera
//
//  Created by Radu Popovici on 24/03/16.
//
//

#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>

@interface RCTSensorOrientationChecker : NSObject

- (void)getDeviceOrientation:(void(^)(UIInterfaceOrientation orientation))callback;
- (AVCaptureVideoOrientation)convertToAVCaptureVideoOrientation:(UIInterfaceOrientation)orientation;

@end
