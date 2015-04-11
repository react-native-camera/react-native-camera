#import "RCTBridge.h"
#import "RCTCamera.h"
#import "RCTCameraManager.h"
#import "RCTLog.h"
#import "RCTUtils.h"
#import "ViewfinderView.h"

#import <AVFoundation/AVFoundation.h>

@implementation RCTCamera

- (void)setAspect:(NSString *)aspect
{
    [(AVCaptureVideoPreviewLayer *)[_viewfinder layer] setVideoGravity:aspect];
}

- (void)setType:(NSInteger)camera
{
    if (self.manager.session.isRunning) {
        [self.manager changeCamera:camera];
    }
    else {
        self.manager.presetCamera = camera;
    }
}

- (void)setOrientation:(NSInteger)orientation
{
    [self.manager changeOrientation:orientation];
}

- (id)initWithManager:(RCTCameraManager*)manager
{
    if ((self = [super init])) {
        self.manager = manager;
        self.viewfinder = [[ViewfinderView alloc] init];
        self.viewfinder.session = self.manager.session;
    }
    return self;
}

- (NSArray *)reactSubviews
{
    NSArray *subviews = @[self.viewfinder];
    return subviews;
}

- (void)layoutSubviews
{
    [super layoutSubviews];
    [self.viewfinder setFrame:self.bounds];
}

- (void)insertReactSubview:(UIView *)view atIndex:(NSInteger)atIndex
{
    [self insertSubview:view atIndex:atIndex + 1];
    return;
}

- (void)removeReactSubview:(UIView *)subview
{
    [subview removeFromSuperview];
    return;
}

@end
