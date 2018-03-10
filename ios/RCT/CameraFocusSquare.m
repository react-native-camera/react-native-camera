#import "CameraFocusSquare.h"
#import <QuartzCore/QuartzCore.h>

const float squareLength = 80.0f;
@implementation RCTCameraFocusSquare

- (id)initWithFrame:(CGRect)frame 
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code

        [self setBackgroundColor:[UIColor clearColor]];
        [self.layer setBorderWidth:1.0];
        [self.layer setBorderColor:[UIColor whiteColor].CGColor];
        
        CGSize size = frame.size;
        CGRect rect = CGRectMake(0, 0, size.width, size.height);
        
        UIBezierPath *endPath = [UIBezierPath bezierPath];
        [endPath moveToPoint:CGPointMake(CGRectGetMinX(rect) + size.width / 2.0, CGRectGetMinY(rect))];
        [endPath addLineToPoint:CGPointMake(CGRectGetMinX(rect) + size.width / 2.0, CGRectGetMinY(rect) + 5.0)];
        [endPath moveToPoint:CGPointMake(CGRectGetMaxX(rect), CGRectGetMinY(rect) + size.height / 2.0)];
        [endPath addLineToPoint:CGPointMake(CGRectGetMaxX(rect) - 5.0, CGRectGetMinY(rect) + size.height / 2.0)];
        
        [endPath moveToPoint:CGPointMake(CGRectGetMinX(rect) + size.width / 2.0, CGRectGetMaxY(rect))];
        [endPath addLineToPoint:CGPointMake(CGRectGetMinX(rect) + size.width / 2.0, CGRectGetMaxY(rect) - 5.0)];
        [endPath moveToPoint:CGPointMake(CGRectGetMinX(rect), CGRectGetMinY(rect) + size.height / 2.0)];
        [endPath addLineToPoint:CGPointMake(CGRectGetMinX(rect) + 5.0, CGRectGetMinY(rect) + size.height / 2.0)];

        CAShapeLayer *extraLayer = [CAShapeLayer layer];
        extraLayer.path = endPath.CGPath;
        extraLayer.fillColor = [UIColor clearColor].CGColor;
        extraLayer.strokeColor = [UIColor colorWithRed:1.0 green:0.83 blue:0 alpha:0.95].CGColor;
        extraLayer.lineWidth = 1.0;
        [self.layer addSublayer:extraLayer];
        
        CABasicAnimation* selectionAnimation = [CABasicAnimation
                                                animationWithKeyPath:@"borderColor"];
        selectionAnimation.toValue = (id)[UIColor colorWithRed:1.0 green:0.83 blue:0 alpha:0.95].CGColor;
        selectionAnimation.repeatCount = 8;
        [self.layer addAnimation:selectionAnimation
                          forKey:@"selectionAnimation"];

    }
    return self;
}
@end
