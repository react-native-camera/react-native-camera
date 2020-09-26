//
//  RNImageUtils.m
//  RCTCamera
//
//  Created by Joao Guilherme Daros Fidelis on 19/01/18.
//

#import "RNImageUtils.h"

@implementation RNImageUtils

+ (UIImage *)generatePhotoOfSize:(CGSize)size
{
    CGRect rect = CGRectMake(0, 0, size.width, size.height);
    UIImage *image;
    UIGraphicsBeginImageContextWithOptions(size, YES, 0);
    UIColor *color = [UIColor blackColor];
    [color setFill];
    UIRectFill(rect);
    NSDate *currentDate = [NSDate date];
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"dd.MM.YY HH:mm:ss"];
    NSString *text = [dateFormatter stringFromDate:currentDate];
    NSDictionary *attributes = [NSDictionary dictionaryWithObjects: @[[UIFont systemFontOfSize:18.0], [UIColor orangeColor]]
                                                           forKeys: @[NSFontAttributeName, NSForegroundColorAttributeName]];
    [text drawAtPoint:CGPointMake(size.width * 0.1, size.height * 0.9) withAttributes:attributes];
    image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return image;
}

+ (UIImage *)cropImage:(UIImage *)image toRect:(CGRect)rect
{
    CGImageRef takenCGImage = image.CGImage;
    CGImageRef cropCGImage = CGImageCreateWithImageInRect(takenCGImage, rect);
    image = [UIImage imageWithCGImage:cropCGImage scale:image.scale orientation:image.imageOrientation];
    CGImageRelease(cropCGImage);
    return image;
}

+ (UIImage *)mirrorImage:(UIImage *)image
{
    UIImageOrientation flippedOrientation = UIImageOrientationUpMirrored;
    switch (image.imageOrientation) {
        case UIImageOrientationDown:
            flippedOrientation = UIImageOrientationDownMirrored;
            break;
        case UIImageOrientationLeft:
            flippedOrientation = UIImageOrientationRightMirrored;
            break;
        case UIImageOrientationUp:
            flippedOrientation = UIImageOrientationUpMirrored;
            break;
        case UIImageOrientationRight:
            flippedOrientation = UIImageOrientationLeftMirrored;
            break;
        default:
            break;
    }
    UIImage * flippedImage = [UIImage imageWithCGImage:image.CGImage scale:image.scale orientation:flippedOrientation];
    return flippedImage;
}

+ (NSString *)writeImage:(NSData *)image toPath:(NSString *)path
{
    [image writeToFile:path atomically:YES];
    NSURL *fileURL = [NSURL fileURLWithPath:path];
    return [fileURL absoluteString];
}

+ (UIImage *) scaleImage:(UIImage*)image toWidth:(NSInteger)width
{
    width /= [UIScreen mainScreen].scale; // prevents image from being incorrectly resized on retina displays
    float scaleRatio = (float) width / (float) image.size.width;
    CGSize size = CGSizeMake(width, roundf(image.size.height * scaleRatio));
    
    UIGraphicsBeginImageContextWithOptions(size, NO, 0.0);
    [image drawInRect:CGRectMake(0, 0, size.width, size.height)];
    UIImage *newImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return [UIImage imageWithCGImage:[newImage CGImage]  scale:1.0 orientation:(newImage.imageOrientation)];
}

+ (UIImage *)forceUpOrientation:(UIImage *)image
{
    if (image.imageOrientation != UIImageOrientationUp) {
        UIGraphicsBeginImageContextWithOptions(image.size, NO, image.scale);
        [image drawInRect:CGRectMake(0, 0, image.size.width, image.size.height)];
        image = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
    }
    return image;
} 


+ (void)updatePhotoMetadata:(CMSampleBufferRef)imageSampleBuffer withAdditionalData:(NSDictionary *)additionalData inResponse:(NSMutableDictionary *)response
{
    CFDictionaryRef exifAttachments = CMGetAttachment(imageSampleBuffer, kCGImagePropertyExifDictionary, NULL);
    NSMutableDictionary *metadata = (__bridge NSMutableDictionary *)exifAttachments;
    metadata[(NSString *)kCGImagePropertyExifPixelYDimension] = response[@"width"];
    metadata[(NSString *)kCGImagePropertyExifPixelXDimension] = response[@"height"];
    
    for (id key in additionalData) {
        metadata[key] = additionalData[key];
    }
    
    NSDictionary *gps = metadata[(NSString *)kCGImagePropertyGPSDictionary];
    
    if (gps) {
        for (NSString *gpsKey in gps) {
            metadata[[@"GPS" stringByAppendingString:gpsKey]] = gps[gpsKey];
        }
    }
    
    response[@"exif"] = metadata;
}

+ (UIImage *)invertColors:(UIImage *)image
{
    CIImage *inputCIImage = [[CIImage alloc] initWithImage:image];

    // Invert colors
    CIFilter *filterColorInvert = [CIFilter filterWithName:@"CIColorInvert"];
    [filterColorInvert setValue:inputCIImage forKey:kCIInputImageKey];
    CIImage *outputCIImage = [filterColorInvert valueForKey:kCIOutputImageKey];

    // A UIImage initialized directly from CIImage has its CGImage property set to NULL. So it has
    // to be converted to a CGImage first.
    static CIContext *context = nil; if (!context) context = [CIContext contextWithOptions:nil];
    CGImageRef outputCGImage = [context createCGImage:outputCIImage fromRect:[outputCIImage extent]];

    UIImage *outputUIImage = [UIImage imageWithCGImage:outputCGImage];

    CGImageRelease(outputCGImage);

    return outputUIImage;
}

@end

