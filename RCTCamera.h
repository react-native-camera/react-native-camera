#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
#import "ViewfinderView.h"

@class AVCaptureSession;

@interface RCTCamera : UIView

@property (nonatomic) AVCaptureSession *session;
@property (nonatomic) ViewfinderView *viewfinder;
@property (nonatomic) AVCaptureDeviceInput *captureDeviceInput;

@end
