#import "RCTBridge.h"
#import "RCTCamera.h"
#import "RCTCameraManager.h"
#import "RCTLog.h"
#import "RCTUtils.h"
#import "RCTEventDispatcher.h"

#import "UIView+React.h"

#import <AVFoundation/AVFoundation.h>
#import "CameraFocusSquare.h"

@implementation RCTCamera
{
  BOOL _multipleTouches;
  BOOL _onFocusChanged;
  BOOL _defaultOnFocusComponent;
  BOOL _onZoomChanged;
}

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

- (void)setOnFocusChanged:(BOOL)enabled
{
  if (_onFocusChanged != enabled) {
    _onFocusChanged = enabled;
  }
}

- (void)setDefaultOnFocusComponent:(BOOL)enabled
{
  if (_defaultOnFocusComponent != enabled) {
    _defaultOnFocusComponent = enabled;
  }
}

- (void)setOnZoomChanged:(BOOL)enabled
{
  if (_onZoomChanged != enabled) {
    _onZoomChanged = enabled;
  }
}

- (id)initWithManager:(RCTCameraManager*)manager bridge:(RCTBridge *)bridge
{
  
  if ((self = [super init])) {
    self.manager = manager;
    self.bridge = bridge;
    UIPinchGestureRecognizer *pinchGesture = [[UIPinchGestureRecognizer alloc] initWithTarget:self action:@selector(handlePinchToZoomRecognizer:)];
    [self addGestureRecognizer:pinchGesture];
    [self.manager initializeCaptureSessionInput:AVMediaTypeVideo];
    [self.manager startSession];
    _multipleTouches = NO;
    _onFocusChanged = NO;
    _defaultOnFocusComponent = YES;
    _onZoomChanged = NO;
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


- (void) touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{
    // Update the touch state.
    if ([[event touchesForView:self] count] > 1) {
        _multipleTouches = YES;
    }

}

- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event
{
    if (!_onFocusChanged) return;

    BOOL allTouchesEnded = ([touches count] == [[event touchesForView:self] count]);

    // Do not conflict with zooming and etc.
    if (allTouchesEnded && !_multipleTouches) {
        UITouch *touch = [[event allTouches] anyObject];
        CGPoint touchPoint = [touch locationInView:touch.view];
        // Focus camera on this point
        [self.manager focusAtThePoint:touchPoint];

        if (self.camFocus)
        {
            [self.camFocus removeFromSuperview];
        }
        NSDictionary *event = @{
          @"target": self.reactTag,
          @"touchPoint": @{
            @"x": [NSNumber numberWithDouble:touchPoint.x],
            @"y": [NSNumber numberWithDouble:touchPoint.y]
          }
        };
        [self.bridge.eventDispatcher sendInputEventWithName:@"focusChanged" body:event];

        // Show animated rectangle on the touched area
        if (_defaultOnFocusComponent) {
            self.camFocus = [[RCTCameraFocusSquare alloc]initWithFrame:CGRectMake(touchPoint.x-40, touchPoint.y-40, 80, 80)];
            [self.camFocus setBackgroundColor:[UIColor clearColor]];
            [self addSubview:self.camFocus];
            [self.camFocus setNeedsDisplay];

            [UIView beginAnimations:nil context:NULL];
            [UIView setAnimationDuration:1.0];
            [self.camFocus setAlpha:0.0];
            [UIView commitAnimations];
        }
    }

    if (allTouchesEnded) {
        _multipleTouches = NO;
    }

}


-(void) handlePinchToZoomRecognizer:(UIPinchGestureRecognizer*)pinchRecognizer {
    if (!_onZoomChanged) return;

    if (pinchRecognizer.state == UIGestureRecognizerStateChanged) {
        [self.manager zoom:pinchRecognizer.velocity reactTag:self.reactTag];
    }
}


@end
