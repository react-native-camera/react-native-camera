#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
#import "CameraFocusSquare.h"

@class RCTCameraManager;

@interface RCTCamera : UIView

@property (nonatomic) RCTCameraManager *manager;
@property (nonatomic) RCTBridge *bridge;
@property (nonatomic) RCTCameraFocusSquare *camFocus;
@property (nonatomic) AVCaptureVideoPreviewLayer *previewLayer;

- (id)initWithManager:(RCTCameraManager*)manager bridge:(RCTBridge *)bridge;

@end
