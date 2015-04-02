#import "RCTBridge.h"
#import "RCTCamera.h"
#import "RCTCameraManager.h"
#import "RCTLog.h"
#import "ViewfinderView.h"
#import <AVFoundation/AVFoundation.h>

@implementation RCTCamera

- (void)setAspect:(NSString *)aspect
{
    RCTCameraManager *cameraManager = [RCTCameraManager sharedManager];
    [cameraManager setAspect:aspect];
}

- (void)setCamera:(NSInteger)camera
{
    RCTCameraManager *cameraManager = [RCTCameraManager sharedManager];
    [cameraManager setCamera:camera];
}

- (void)setOrientation:(NSInteger)orientation
{
    RCTCameraManager *cameraManager = [RCTCameraManager sharedManager];
    [cameraManager setOrientation:orientation];
}

- (id)init
{
    if ((self = [super init])) {
        [self setCameraManager:[RCTCameraManager sharedManager]];
        _viewfinder = [[ViewfinderView alloc] init];

        [[self viewfinder] setSession:_cameraManager.session];
        [self addSubview:_viewfinder];
    }
    return self;
}

- (NSArray *)reactSubviews
{
    NSArray *subviews = @[_viewfinder];
    return subviews;
}

- (void)layoutSubviews
{
    [super layoutSubviews];
    _viewfinder.frame = self.bounds;
}

- (void)insertReactSubview:(UIView *)view atIndex:(NSInteger)atIndex
{
    RCTLogError(@"Camera does not support subviews");
    return;
}

- (void)removeReactSubview:(UIView *)subview
{
    RCTLogError(@"Camera does not support subviews");
    return;
}

@end
