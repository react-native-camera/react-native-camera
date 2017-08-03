#import "CameraFocusSquare.h"
#import <QuartzCore/QuartzCore.h>

const float squareLength = 50.0f;
@implementation RCTCameraFocusSquare

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
        
        [self setBackgroundColor:[UIColor clearColor]];
        [self.layer setBorderWidth:2.0];
        [self.layer setCornerRadius:3.0];
        [self.layer setBorderColor:[UIColor colorWithRed:1.00 green:0.84 blue:0.00 alpha:1.0].CGColor];
        
        CABasicAnimation* selectionAnimation = [CABasicAnimation                                    animationWithKeyPath:@"borderColor"];
        selectionAnimation.toValue = (id)[UIColor whiteColor].CGColor;
        selectionAnimation.repeatCount = 5;
        [self.layer addAnimation:selectionAnimation
                          forKey:@"selectionAnimation"];
        
    }
    return self;
}
@end
