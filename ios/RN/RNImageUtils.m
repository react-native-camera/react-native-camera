//
//  RNImageUtils.m
//  RCTCamera
//
//  Created by Joao Guilherme Daros Fidelis on 19/01/18.
//

#import "RNImageUtils.h"
#import <React/RCTLog.h>
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
// ========================================== <<<<<<<<<<<<<<<<<<<<<<<<<   check from here
// todo: use this to crop face
+ (UIImage *)cropImage:(UIImage *)image toRect:(CGRect)rect
{
// ================================================  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        RCTLogInfo(@"RNImageUtiles > cropImage");  //only warn or error get response from react log.
// ================================================  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ 
    CGImageRef takenCGImage = image.CGImage;
    CGImageRef cropCGImage = CGImageCreateWithImageInRect(takenCGImage, rect);
    // image = [UIImage imageWithCGImage:cropCGImage scale:image.scale orientation:image.imageOrientation];
    image = [UIImage imageWithCGImage:cropCGImage];

    CGImageRelease(cropCGImage);
    return image;

}
        // --------------------------------------  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  check this
        // todo: why flip the image?
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
// ================================================  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        RCTLogInfo(@"RNImageUtiles > mirrorImage flip image");  //only warn or error get response from react log.
// ================================================  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ 
    UIImage * flippedImage = [UIImage imageWithCGImage:image.CGImage scale:image.scale orientation:flippedOrientation];
    return flippedImage;
}
// ========================================== <<<<<<<<<<<<<<<<<<<<<<<<<   check from here
// todo: use this to save image to file path
+ (NSString *)writeImage:(NSData *)image toPath:(NSString *)path
{
    [image writeToFile:path atomically:YES];
    NSURL *fileURL = [NSURL fileURLWithPath:path];
// ================================================  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        RCTLogInfo(@"RNImageUtiles > writeImage success path = %@",path);  //only warn or error get response from react log.
// ================================================  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ 
    return [fileURL absoluteString];
}
// ========================================== <<<<<<<<<<<<<<<<<<<<<<<<<   check from here
// todo: use this to scale image to the size we want
+ (UIImage *) scaleImage:(UIImage*)image toWidth:(NSInteger)width
{
// ================================================  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        RCTLogInfo(@"RNImageUtiles > scaleImage toWidth: %d",width);  //only warn or error get response from react log.
// ================================================  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ 
    width /= [UIScreen mainScreen].scale; // prevents image from being incorrectly resized on retina displays
    float scaleRatio = (float) width / (float) image.size.width;
    CGSize size = CGSizeMake(width, roundf(image.size.height * scaleRatio));
    
    UIGraphicsBeginImageContextWithOptions(size, NO, 0.0);
    [image drawInRect:CGRectMake(0, 0, size.width, size.height)];
    UIImage *newImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return [UIImage imageWithCGImage:[newImage CGImage]  scale:1.0 orientation:(newImage.imageOrientation)];
}
// ================================================  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
+ (UIImage *)scaleImage:(UIImage *)image convertToSize:(CGSize)size {
    RCTLogInfo(@"RNImageUtiles > scaleImage convertToSize ...."); 
    // UIGraphicsBeginImageContext(size);
      UIGraphicsBeginImageContextWithOptions(size, NO, 0.0);
    [image drawInRect:CGRectMake(0, 0, size.width, size.height)];
    UIImage *destImage = UIGraphicsGetImageFromCurrentImageContext();    
    UIGraphicsEndImageContext();
    return [UIImage imageWithCGImage:[destImage CGImage]  scale:1.0 orientation:(destImage.imageOrientation)];
    // return destImage;

    // CGFloat scale = MAX(size.width/image.size.width, size.height/image.size.height);
    // CGFloat width = image.size.width * scale;
    // CGFloat height = image.size.height * scale;
    // CGRect imageRect = CGRectMake((size.width - width)/2.0f,
    //                               (size.height - height)/2.0f,
    //                               width,
    //                               height);

    // UIGraphicsBeginImageContextWithOptions(size, NO, 0);
    // [image drawInRect:imageRect];
    // UIImage *newImage = UIGraphicsGetImageFromCurrentImageContext();
    // UIGraphicsEndImageContext();
    // return newImage;

}

+ (UIImage *)scaleToRect:(UIImage *)image atX:(float)x atY:(float)y withSize:(CGSize)size {
    RCTLogInfo(@"RNImageUtiles > scaleImage convertToSize ...."); 
    UIGraphicsBeginImageContext(size);
    [image drawInRect:CGRectMake(x, y, size.width, size.height)];
    UIImage *destImage = UIGraphicsGetImageFromCurrentImageContext();    
    UIGraphicsEndImageContext();
    return destImage;
}
// ================================================  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ 

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
            // --------------------------------------  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  check this
        // todo: there is GPS info in the image metadata
    if (gps) {
        for (NSString *gpsKey in gps) {
            metadata[[@"GPS" stringByAppendingString:gpsKey]] = gps[gpsKey];
        }
    }
// ================================================  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        RCTLogInfo(@"RNImageUtiles > updatePhotoMetadata metadata: %@",metadata);  //only warn or error get response from react log.
// ================================================  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  
    response[@"exif"] = metadata;
}
// ========================================== <<<<<<<<<<<<<<<<<<<<<<<<<   check from here
// todo: check if can convert to grayscale
// https://stackoverflow.com/questions/4627840/changing-rgb-color-image-to-grayscale-image-using-objective-c
// https://stackoverflow.com/questions/20149708/convert-an-array-to-a-one-channel-image
// https://stackoverflow.com/questions/39221007/how-to-convert-uiimagepicker-image-into-a-byte-array-objective-c/39221412#39221412
// https://stackoverflow.com/questions/33768066/get-pixel-data-as-array-from-uiimage-cgimage-in-swift
+ (UIImage *)invertColors:(UIImage *)image
{
// ================================================  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        RCTLogInfo(@"RNImageUtiles > invertColors");  //only warn or error get response from react log.
// ================================================  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ 
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

