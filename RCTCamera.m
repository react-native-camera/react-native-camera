#import "RCTBridge.h"
#import "RCTCamera.h"
#import "RCTCameraManager.h"
#import "RCTLog.h"
#import "RCTUtils.h"

#import <AVFoundation/AVFoundation.h>

@implementation RCTCamera

- (void)setAspect:(NSInteger)aspect
{
  NSString *aspectString;
  switch (aspect) {
    default:
    case RCTCameraAspectFill:
      aspectString = AVLayerVideoGravityResizeAspectFill;
      break;
    case RCTCameraAspectFit:
      aspectString = AVLayerVideoGravityResizeAspect;
      break;
    case RCTCameraAspectStretch:
      aspectString = AVLayerVideoGravityResize;
      break;
  }
  [self.manager changeAspect:aspectString];
}

- (void)setType:(NSInteger)type
{
  if (self.manager.session.isRunning) {
    [self.manager changeCamera:type];
  }
  else {
    self.manager.presetCamera = type;
  }
  [self.manager initializeCaptureSessionInput:AVMediaTypeVideo];
}

- (void)setOrientation:(NSInteger)orientation
{
  if (orientation == RCTCameraOrientationAuto) {
    [self.manager changeOrientation:[UIApplication sharedApplication].statusBarOrientation];
    [[NSNotificationCenter defaultCenter] addObserver:self  selector:@selector(orientationChanged:)    name:UIDeviceOrientationDidChangeNotification  object:nil];
  }
  else {
    [[NSNotificationCenter defaultCenter]removeObserver:self name:UIDeviceOrientationDidChangeNotification object:nil];
    [self.manager changeOrientation:orientation];
  }
}

- (void)setFlashMode:(NSInteger)flashMode
{
  [self.manager changeFlashMode:flashMode];
}

- (void)setTorchMode:(NSInteger)torchMode
{
  [self.manager changeTorchMode:torchMode];
}

- (id)initWithManager:(RCTCameraManager*)manager
{
  
  if ((self = [super init])) {
    self.manager = manager;
    [self.manager initializeCaptureSessionInput:AVMediaTypeVideo];
    [self.manager startSession];
  }
  return self;
}

- (void)layoutSubviews
{
  [super layoutSubviews];
  self.manager.previewLayer.frame = self.bounds;
  [self setBackgroundColor:[UIColor blackColor]];
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

- (void)removeFromSuperview
{
  [self.manager stopSession];
  [super removeFromSuperview];
  [[NSNotificationCenter defaultCenter] removeObserver:self name:UIDeviceOrientationDidChangeNotification object:nil];
}

- (void)orientationChanged:(NSNotification *)notification{
  UIInterfaceOrientation orientation = [[UIApplication sharedApplication] statusBarOrientation];
  [self.manager changeOrientation:orientation];
}

@end
