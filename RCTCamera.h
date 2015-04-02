#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
#import "ViewfinderView.h"

@class RCTCameraManager;

@interface RCTCamera : UIView

@property (nonatomic) RCTCameraManager *cameraManager;
@property (nonatomic) ViewfinderView *viewfinder;

@end
