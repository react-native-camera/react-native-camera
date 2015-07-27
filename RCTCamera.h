#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
#import "CameraFocusSquare.h"

@class RCTCameraManager;

@interface RCTCamera : UIView

@property (nonatomic) RCTCameraManager *manager;
@property (nonatomic) RCTCameraFocusSquare *camFocus;
@property (nonatomic) BOOL multipleTouches;

- (id)initWithManager:(RCTCameraManager*)manager;

@end
