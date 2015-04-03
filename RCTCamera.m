#import "RCTBridge.h"
#import "RCTCamera.h"
#import "RCTCameraManager.h"
#import "RCTLog.h"
#import "ViewfinderView.h"
#import <AVFoundation/AVFoundation.h>

@implementation RCTCamera

- (void)setAspect:(NSString *)aspect
{
    [(AVCaptureVideoPreviewLayer *)[[self viewfinder] layer] setVideoGravity:aspect];
}

- (void)setCamera:(NSInteger)camera
{
    [[self cameraManager] setCamera:camera];
}

- (void)setOrientation:(NSInteger)orientation
{
    [[self cameraManager] setOrientation:orientation];
}

- (id)init
{
    if ((self = [super init])) {
        [self setCameraManager:[RCTCameraManager sharedManager]];
        [self setViewfinder:[[ViewfinderView alloc] init]];

        [[self viewfinder] setSession:[[self cameraManager] session]];
        [self addSubview:[self viewfinder]];
    }
    return self;
}

- (NSArray *)reactSubviews
{
    NSArray *subviews = @[[self viewfinder]];
    return subviews;
}

- (void)layoutSubviews
{
    [super layoutSubviews];
    [[self viewfinder] setFrame:[self bounds]];
}

- (void)insertReactSubview:(UIView *)view atIndex:(NSInteger)atIndex
{
    [[self viewfinder] insertSubview:view atIndex:atIndex + 1];
    return;
}

- (void)removeReactSubview:(UIView *)subview
{
    [subview removeFromSuperview];
    return;
}

@end
