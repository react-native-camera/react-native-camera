#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
#import "CameraFocusSquare.h"
#import <ZXingObjC/ZXingObjC.h>

@class RCTCameraManager;

@interface RCTCamera : UIView

- (id)initWithManager:(RCTCameraManager*)manager bridge:(RCTBridge *)bridge;

@end
