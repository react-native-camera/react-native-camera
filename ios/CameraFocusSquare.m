#import "CameraFocusSquare.h"
#import <QuartzCore/QuartzCore.h>

const float squareLength = 36.0f;
@implementation RCTCameraFocusSquare

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
        [self setBackgroundColor:[UIColor clearColor]];
        [self.layer setBorderWidth:0.5];
        [self.layer setBorderColor:[UIColor colorWithRed:1.00 green:0.84 blue:0.00 alpha:1.0].CGColor];
        [self.layer setCornerRadius:0.0];
        
        CABasicAnimation* selectionAnimation = [CABasicAnimation animationWithKeyPath:@"borderColor"];
        selectionAnimation.toValue = (id)[UIColor colorWithRed:1.00 green:0.84 blue:0.00 alpha:0.5].CGColor;
        selectionAnimation.repeatCount = 4;
        selectionAnimation.duration = 0.3;
        
        [self.layer addAnimation:selectionAnimation forKey:@"selectionAnimation"];
    }
    return self;
}
@end
