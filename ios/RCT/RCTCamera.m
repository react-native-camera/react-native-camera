#import <React/RCTBridge.h>
#import "RCTCamera.h"
#import "RCTCameraManager.h"
#import <React/RCTLog.h>
#import <React/RCTUtils.h>
#import <React/RCTEventDispatcher.h>

#import <React/UIView+React.h>

#import <AVFoundation/AVFoundation.h>
#import "CameraFocusSquare.h"

@interface RCTCamera ()

@property (nonatomic, weak) RCTCameraManager *manager;
@property (nonatomic, weak) RCTBridge *bridge;

@end

@implementation RCTCamera
{
  BOOL _multipleTouches;
  BOOL _onFocusChanged;
  BOOL _defaultOnFocusComponent;
  BOOL _onZoomChanged;
  BOOL _previousIdleTimerDisabled;
}

- (void)setOrientation:(NSInteger)orientation
{
  [self.manager changeOrientation:orientation];

  if (orientation == RCTCameraOrientationAuto) {
    [self changePreviewOrientation:[UIApplication sharedApplication].statusBarOrientation];
    [[NSNotificationCenter defaultCenter] addObserver:self  selector:@selector(orientationChanged:)    name:UIDeviceOrientationDidChangeNotification  object:nil];
  }
  else {
    [[NSNotificationCenter defaultCenter]removeObserver:self name:UIDeviceOrientationDidChangeNotification object:nil];
    [self changePreviewOrientation:orientation];
  }
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
    _previousIdleTimerDisabled = [UIApplication sharedApplication].idleTimerDisabled;
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
  [UIApplication sharedApplication].idleTimerDisabled = _previousIdleTimerDisabled;
}

- (void)orientationChanged:(NSNotification *)notification{
  UIInterfaceOrientation orientation = [[UIApplication sharedApplication] statusBarOrientation];
  [self changePreviewOrientation:orientation];
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
        [self.bridge.eventDispatcher sendAppEventWithName:@"focusChanged" body:event];

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

- (void)changePreviewOrientation:(NSInteger)orientation
{
    dispatch_async(self.manager.sessionQueue, ^{
      dispatch_async(dispatch_get_main_queue(), ^{
        if (self.manager.previewLayer.connection.isVideoOrientationSupported) {
            self.manager.previewLayer.connection.videoOrientation = orientation;
        }
      });
    });
}

@end
