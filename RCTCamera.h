#import <UIKit/UIKit.h>
#import "ViewfinderView.h"

@class AVCaptureSession;

@interface RCTCamera : UIView

@property (nonatomic) AVCaptureSession *session;
@property (nonatomic) ViewfinderView *viewfinder;

@end