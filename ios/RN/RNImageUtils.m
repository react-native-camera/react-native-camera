//
//  RNImageUtils.m
//  RCTCamera
//
//  Created by Joao Guilherme Daros Fidelis on 19/01/18.
//

#import "RNImageUtils.h"
#import "RNCameraUtils.h"
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
        // RCTLogInfo(@"RNImageUtiles > cropImage");  //only warn or error get response from react log.
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
        // RCTLogInfo(@"RNImageUtiles > mirrorImage flip image");  //only warn or error get response from react log.
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
        // RCTLogInfo(@"RNImageUtiles > writeImage success path = %@",path);  //only warn or error get response from react log.
// ================================================  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ 
    return [fileURL absoluteString];
}
// ========================================== <<<<<<<<<<<<<<<<<<<<<<<<<   check from here
// todo: use this to scale image to the size we want
+ (UIImage *) scaleImage:(UIImage*)image toWidth:(NSInteger)width
{
// ================================================  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        // RCTLogInfo(@"RNImageUtiles > scaleImage toWidth: %d",width);  //only warn or error get response from react log.
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
    // RCTLogInfo(@"RNImageUtiles > scaleImage convertToSize ...."); 
    UIGraphicsBeginImageContext(size);
    UIGraphicsBeginImageContextWithOptions(size, NO, 0.0);
    [image drawInRect:CGRectMake(0, 0, size.width, size.height)];
    UIImage *destImage = UIGraphicsGetImageFromCurrentImageContext();    
    UIGraphicsEndImageContext();
    return [UIImage imageWithCGImage:[destImage CGImage]  scale:1.0 orientation:(destImage.imageOrientation)];
}

+ (UIImage *)scaleToRect:(UIImage *)image atX:(float)x atY:(float)y withSize:(CGSize)size {
    // RCTLogInfo(@"RNImageUtiles > scaleImage convertToSize ...."); 
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

    if (gps) {
        for (NSString *gpsKey in gps) {
            metadata[[@"GPS" stringByAppendingString:gpsKey]] = gps[gpsKey];
        }
    }

    response[@"exif"] = metadata;
}

+ (UIImage *)invertColors:(UIImage *)image
{
// ================================================  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        // RCTLogInfo(@"RNImageUtiles > invertColors");  //only warn or error get response from react log.
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


+ (UIImage *) convertImageToGrayScale:(UIImage *)image
{
    //   RCTLogInfo(@"RNImageUtils > convertImageToGrayScale > input image data...");
  // Create image rectangle with current image width/height
  CGRect imageRect = CGRectMake(0, 0, image.size.width, image.size.height);
  // Grayscale color space
  CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceGray();
  // Create bitmap content with current image size and grayscale colorspace 8 bpp,  
  CGContextRef context = CGBitmapContextCreate(nil, image.size.width, image.size.height, 8, 0, colorSpace, kCGImageAlphaNone);
  // Draw image into current context, with specified rectangle
  // using previously defined context (with grayscale colorspace)
  CGContextDrawImage(context, imageRect, [image CGImage]);
 
  // Create bitmap image info from pixel data in current context
  CGImageRef imageRef = CGBitmapContextCreateImage(context);
  // todo: output this imageRef, including size, to be converted into array
  RCTLogInfo(@"RNImageUtils > convertImageToGrayScale > print grayscale imageRef bitmap...");
  // Create a new UIImage object  
  UIImage *newImage = [UIImage imageWithCGImage:imageRef];
    // todo: output this imageRef, including size, to be converted into array
  RCTLogInfo(@"RNImageUtils > convertImageToGrayScale > print grayscale newgrayImage bitmap...");
//   [self rawDataCopyWithImageRef:newImage.CGImage width:image.size.width height:image.size.height];
  [self rawDataCopyWithImage:newImage];
  // Release colorspace, context and bitmap information
  CGColorSpaceRelease(colorSpace);
  CGContextRelease(context);
  CFRelease(imageRef);
  // Return the new grayscale image
  return newImage;
}

+ (void)rawDataCopyWithImageRef:(CGImageRef )imageRef width:(NSInteger) imgWidth height:(NSInteger )imgHeight 
{
    CGDataProviderRef dataProvider = CGImageGetDataProvider(imageRef);
    
    __block CFDataRef dataRef;
    dataRef = CGDataProviderCopyData(dataProvider);
    
    UInt8* buffer = (UInt8*)CFDataGetBytePtr(dataRef);
    size_t bytesPerRow = CGImageGetBytesPerRow(imageRef);
    RCTLogInfo(@"RNImageUtils > rawDataCopyWithImageRef > print buffer data... width*height*bytesPerRow = %d * %d * %d",
        imgWidth,imgHeight,bytesPerRow);
    [self printData:buffer width:imgWidth height:imgHeight bytesPerRow:bytesPerRow];
   

}



+ (NSData *)getArrayOfImage:(UIImage *)image
{
  CGImageRef  imageRef = image.CGImage;

      CGDataProviderRef dataProvider = CGImageGetDataProvider(imageRef);
    
    __block CFDataRef dataRef;
    dataRef = CGDataProviderCopyData(dataProvider);
    
    UInt8* buffer = (UInt8*)CFDataGetBytePtr(dataRef);
    size_t bytesPerRow = CGImageGetBytesPerRow(imageRef);
     RCTLogInfo(@"RNImageUtils > getArrayOfImage > CGColorSpaceCreateDeviceGray print buffer data... width*height*bytesPerRow = %d * %d * %d",
        image.size.width,image.size.height,bytesPerRow);
    [self rawDataCopyWithImageRef:imageRef width:image.size.width height:image.size.height];
    NSMutableData *result = [self RGBImageDataToGrayScaleArray:buffer width:image.size.width height:image.size.height bytesPerRow:bytesPerRow];

  // Release colorspace, context and bitmap information
//   CGColorSpaceRelease(colorSpace);
//   CGContextRelease(context);
  CFRelease(imageRef);
  //todo : remove this after debug
  result = [[NSMutableData alloc] initWithLength:50176] ;
  [result resetBytesInRange:NSMakeRange(0, 50176)];
  return result;
}




+ (UInt8 *)rawDataCopyWithImage:(UIImage*)image
{
    RCTLogInfo(@"RNImageUtils > rawDataCopyWithImage ...");
    CGImageRef  imageRef = image.CGImage;
    CGDataProviderRef dataProvider = CGImageGetDataProvider(imageRef);
    
    __block CFDataRef dataRef;
    dataRef = CGDataProviderCopyData(dataProvider);
    
    UInt8* buffer = (UInt8*)CFDataGetBytePtr(dataRef);
    size_t bytesPerRow = CGImageGetBytesPerRow(imageRef);

    [self printData:buffer width:image.size.width height:image.size.height bytesPerRow:bytesPerRow];
    
    CFRelease(dataRef);
    return buffer;
}

// get raw bitmap data from UIImage sung a context to draw imageRef
+ (void)rawDataDrawWithImage:(UIImage*)image
{
    RCTLogInfo(@"RNImageUtils > rawDataDrawWithImage ...");
    CGImageRef imageRef = [image CGImage];
    NSUInteger width = CGImageGetWidth(imageRef);
    NSUInteger height = CGImageGetHeight(imageRef);
    
    CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
    unsigned char *rawData = (unsigned char*) calloc(height * width * 4, sizeof(unsigned char));
    NSUInteger bytesPerPixel = 4;
    NSUInteger bytesPerRow = bytesPerPixel * width;
    NSUInteger bitsPerComponent = 8;
    CGContextRef context = CGBitmapContextCreate(rawData, width, height,
                                                 bitsPerComponent, bytesPerRow, colorSpace,
                                                 kCGImageAlphaPremultipliedLast | kCGBitmapByteOrder32Big);
    CGContextDrawImage(context, CGRectMake(0, 0, width, height), imageRef);
    //  [self printData:rawData width:width height:height bytesPerRow:bytesPerRow];
    // get array data as grayscale image
    NSData * data = [self RGBImageDataToGrayScaleArray:rawData width:width height:height bytesPerRow:bytesPerRow];
    // print out the data for testing
    NSUInteger size = [data length] / sizeof(float);
    RCTLogInfo(@" gray rawData size: %d",size);
    rawData = (unsigned char*) [data bytes];
    [self printGrayData:rawData width:width height:height bytesPerRow:bytesPerRow];
    CGColorSpaceRelease(colorSpace);
    CGContextRelease(context);  
}


+ (void)printData:(UInt8*)data width:(NSInteger)width height:(NSInteger)height bytesPerRow:(size_t)bytesPerRow
{
    RCTLogInfo(@"RNImageUtils > printData ...");
    for (NSInteger x=0; x<width; x++)
    {
        for (NSInteger y=0; y<height; y++)
        {
            // ピクセルのポインタを取得する
            UInt8*  pixelPtr = data + (int)(y) * bytesPerRow + (int)(x) * 4;
            
            // 色情報を取得する
            UInt8 r = *(pixelPtr + 2);  // 赤
            UInt8 g = *(pixelPtr + 1);  // 緑
            UInt8 b = *(pixelPtr + 0);  // 青
            
            NSLog(@"x:%ld y:%ld R:%d G:%d B:%d", (long)x, (long)y, r, g, b);
        }
    }
}

 + (void)printGrayData:(UInt8 *)data width:(NSInteger)width height:(NSInteger)height bytesPerRow:(size_t)bytesPerRow
{
    RCTLogInfo(@"RNImageUtils > printGrayData ...");
    for (NSInteger x=0; x<width; x++)
    {
        for (NSInteger y=0; y<height; y++)
        {
            UInt8*  pixelPtr = data + (int)(y) * bytesPerRow + (int)(x) * 4;
            float fReadValue = 0;
            memcpy(&fReadValue, pixelPtr, sizeof(fReadValue));
            NSLog(@"x:%ld y:%ld gray=%f", (long)x, (long)y, fReadValue);
        }
    }
}

// convert image rawdata into grayscale data
+ (NSData *) RGBImageDataToGrayScaleArray:(UInt8 *)data width:(NSInteger)width height:(NSInteger)height bytesPerRow:(size_t)bytesPerRow
{
    NSMutableData *result = [[NSMutableData alloc] initWithLength:0] ; // demo data
     RCTLogInfo(@"RNImageUtils > RGBImageDataToGrayScaleArray ...");
    for (NSInteger x=0; x<width; x++)
    {
        for (NSInteger y=0; y<height; y++)
        {
            // ピクセルのポインタを取得する
            UInt8*  pixelPtr = data + (int)(y) * bytesPerRow + (int)(x) * 4;
            
            // 色情報を取得する
            UInt8 r = *(pixelPtr + 2);  // 赤
            UInt8 g = *(pixelPtr + 1);  // 緑
            UInt8 b = *(pixelPtr + 0);  // 青
            
            // float yGray = (0.2126 * r + 0.7152 * g + 0.0722 * b );
            float yGray = (0.2126 * r + 0.7152 * g + 0.0722 * b )/255;
            [result appendBytes:&yGray length:sizeof(float)];
        }
    }
    return result;
}

+ (UIImage*)loadImage
{
    return [UIImage imageNamed:@"colorpattern.jpg"];
}

+ (void)testGetPixelDataFromCGImageRefExample {
    
    UIImage *image = [self loadImage];
    [self rawDataCopyWithImage:image];
    [self rawDataDrawWithImage:image];
}
@end

