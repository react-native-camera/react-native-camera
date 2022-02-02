#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
#import "CameraFocusSquare.h"

@class RCTCameraManager;

@interface RCTCamera : UIView

- (id)initWithManager:(RCTCameraManager*)manager bridge:(RCTBridge *)bridge;

@property (nonatomic, strong) RCTCameraFocusSquare *camFocus;
@end
