#import "RCTBridge.h"
#import "RCTCamera.h"
#import "RCTCameraManager.h"
#import "RCTLog.h"
#import "RCTUtils.h"

#import <AVFoundation/AVFoundation.h>

@implementation RCTCamera

- (void)setAspect:(NSString *)aspect
{
    [self.manager changeAspect:aspect];
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
    }
    return self;
}

- (void)layoutSubviews
{
    [super layoutSubviews];
    self.manager.previewLayer.frame = self.bounds;
    [self.layer insertSublayer:self.manager.previewLayer atIndex:0];
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
