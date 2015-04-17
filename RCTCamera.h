#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>

@class RCTCameraManager;

@interface RCTCamera : UIView

@property (nonatomic) RCTCameraManager *manager;

- (id)initWithManager:(RCTCameraManager*)manager;

@end
