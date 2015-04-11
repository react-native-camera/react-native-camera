#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
#import "ViewfinderView.h"

@class RCTCameraManager;

@interface RCTCamera : UIView

@property (nonatomic) ViewfinderView *viewfinder;
@property (nonatomic) RCTCameraManager *manager;

- (id)initWithManager:(RCTCameraManager*)manager;

@end
