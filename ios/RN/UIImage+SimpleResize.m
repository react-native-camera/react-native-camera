//  UIImage+SimpleResize.m
//
//  Created by Robert Ryan on 5/19/11.

#import "UIImage+SimpleResize.h"

@implementation UIImage (SimpleResize)

- (UIImage *)imageByScalingToSize:(CGSize)size contentMode:(UIViewContentMode)contentMode {
    return [self imageByScalingToSize:size contentMode:contentMode scale:0];
}

- (UIImage *)imageByScalingToSize:(CGSize)size contentMode:(UIViewContentMode)contentMode scale:(CGFloat)scale {
    if (contentMode == UIViewContentModeScaleToFill) {
        return [self imageByScalingToFillSize:size];
    }
    else if ((contentMode == UIViewContentModeScaleAspectFill) ||
             (contentMode == UIViewContentModeScaleAspectFit)) {
        CGFloat horizontalRatio   = self.size.width  / size.width;
        CGFloat verticalRatio     = self.size.height / size.height;
        CGFloat ratio;
        
        if (contentMode == UIViewContentModeScaleAspectFill)
            ratio = MIN(horizontalRatio, verticalRatio);
        else
            ratio = MAX(horizontalRatio, verticalRatio);
        
        CGSize  sizeForAspectScale = CGSizeMake(self.size.width / ratio, self.size.height / ratio);
        
        UIImage *image = [self imageByScalingToFillSize:sizeForAspectScale scale:scale];
        
        // if we're doing aspect fill, then the image still needs to be cropped
        
        if (contentMode == UIViewContentModeScaleAspectFill) {
            CGRect  subRect = CGRectMake(floor((sizeForAspectScale.width - size.width) / 2.0),
                                         floor((sizeForAspectScale.height - size.height) / 2.0),
                                         size.width,
                                         size.height);
            image = [image imageByCroppingToBounds:subRect];
        }
        
        return image;
    }
    
    return nil;
}

- (UIImage *)imageByCroppingToBounds:(CGRect)bounds {
    return [self imageByCroppingToBounds:bounds scale:0];
}

- (UIImage *)imageByCroppingToBounds:(CGRect)bounds scale:(CGFloat)scale {
    if (scale == 0) {
        scale = [[UIScreen mainScreen] scale];
    }
    CGRect rect = CGRectMake(bounds.origin.x * scale, bounds.origin.y * scale, bounds.size.width * scale, bounds.size.height * scale);
    CGImageRef imageRef = CGImageCreateWithImageInRect([self CGImage], rect);
    UIImage *croppedImage = [UIImage imageWithCGImage:imageRef scale:scale orientation:self.imageOrientation];
    CGImageRelease(imageRef);
    return croppedImage;
}

- (UIImage *)imageByScalingToFillSize:(CGSize)size {
    return [self imageByScalingToFillSize:size scale:0];
}

- (UIImage *)imageByScalingToFillSize:(CGSize)size scale:(CGFloat)scale {
    UIGraphicsBeginImageContextWithOptions(size, false, scale);
    [self drawInRect:CGRectMake(0, 0, size.width, size.height)];
    UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    return image;
}

- (UIImage *)imageByScalingAspectFillSize:(CGSize)size {
    return [self imageByScalingAspectFillSize:size scale:0];
}

- (UIImage *)imageByScalingAspectFillSize:(CGSize)size scale:(CGFloat)scale {
    return [self imageByScalingToSize:size contentMode:UIViewContentModeScaleAspectFill scale:scale];
}

- (UIImage *)imageByScalingAspectFitSize:(CGSize)size {
    return [self imageByScalingAspectFitSize:size scale:0];
}

- (UIImage *)imageByScalingAspectFitSize:(CGSize)size scale:(CGFloat)scale {
    return [self imageByScalingToSize:size contentMode:UIViewContentModeScaleAspectFit scale:scale];
}
@end
