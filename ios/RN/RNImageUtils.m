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

+ (UIImage *)cropImage:(UIImage *)image toRect:(CGRect)rect
{
    CGImageRef takenCGImage = image.CGImage;
    CGImageRef cropCGImage = CGImageCreateWithImageInRect(takenCGImage, rect);
    image = [UIImage imageWithCGImage:cropCGImage];
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
    // RCTLogInfo(@"RNImageUtils > writeImage : will save to path %@",path);  
    // overide by default
    [image writeToFile:path atomically:YES];
    NSURL *fileURL = [NSURL fileURLWithPath:path];
    RCTLogInfo(@"RNImageUtils > writeImage : apsoluteString %@",[fileURL absoluteString]);  
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

+ (UIImage *)scaleImage:(UIImage *)image convertToSize:(CGSize)size {
    // RCTLogInfo(@"RNImageUtiles > scaleImage convertToSize ...."); 
    UIGraphicsBeginImageContext(size);
    // UIGraphicsBeginImageContextWithOptions(size, NO, 0.0);
    [image drawInRect:CGRectMake(0, 0, size.width, size.height)];
    UIImage *destImage = UIGraphicsGetImageFromCurrentImageContext();    
    UIGraphicsEndImageContext();
    return destImage;
}

+ (UIImage *)scaleToRect:(UIImage *)image atX:(float)x atY:(float)y withSize:(CGSize)size {
    // RCTLogInfo(@"RNImageUtiles > scaleImage convertToSize ...."); 
    UIGraphicsBeginImageContext(size);
    [image drawInRect:CGRectMake(x, y, size.width, size.height)];
    UIImage *destImage = UIGraphicsGetImageFromCurrentImageContext();    
    UIGraphicsEndImageContext();
    return destImage;
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
            
        // todo: there is GPS info in the image metadata
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

// todo: check if useless: remove this
+ (UIImage *) convertImageToGrayScale:(UIImage *)image
{
  // Create image rectangle with current image width/height
  CGRect imageRect = CGRectMake(0, 0, image.size.width, image.size.height);
 
  // Grayscale color space
  CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceGray();
 
  // Create bitmap content with current image size and grayscale colorspace 8 bpp,  
  //bitsPerComponent The number of bits to use for each component of a pixel in memory. 
//For example, for a 32-bit pixel (4*8 bits; 4 byte) format and an RGB color space, you would specify a value of 8 bits per component.
//graycontext = CGContextRef CGBitmapContextCreate(
        //void *data, size_t width, size_t height, 
        //size_t bitsPerComponent, size_t bytesPerRow (0 for automatically.), 
        //CGColorSpaceRef space, uint32_t bitmapInfo);
  CGContextRef context = CGBitmapContextCreate(nil, image.size.width, image.size.height, 8, 0, colorSpace, kCGImageAlphaNone);
 
  // Draw image into current context, with specified rectangle
  // using previously defined context (with grayscale colorspace)
  CGContextDrawImage(context, imageRect, [image CGImage]);
 
  // Create bitmap image info from pixel data in current context
  CGImageRef imageRef = CGBitmapContextCreateImage(context);
  
  // Create a new UIImage object  
  UIImage *newImage = [UIImage imageWithCGImage:imageRef];
    
//   [self rawDataCopyWithImageRef:newImage.CGImage width:image.size.width height:image.size.height];
   [self rawDataCopyWithImage:newImage];
//  [self rawDataDrawWithImage:newImage ];
  // Release colorspace, context and bitmap information
  CGColorSpaceRelease(colorSpace);
  CGContextRelease(context);
  CFRelease(imageRef);
 
  // Return the new grayscale image
  return newImage;
}



// get grayscale array data from image
+ (NSData *)getArrayOfImage:(UIImage *)image
{
    // RCTLogInfo(@"RNImageUtils > getArrayOfImage ...");
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

    // convert to grayscale
    NSData * data = [self RGBImageDataToGrayScaleArray:rawData width:width height:height bytesPerRow:bytesPerRow];
    NSUInteger size = [data length] ;
    if(size != 50176) {
        void * bytes = malloc(50176);
        data = [NSData dataWithBytes:bytes length:50176];
        // free(bytes);
        // data = [NSMutableData dataWithCapacity:50176];
        
    }
    // RCTLogInfo(@" gray rawData size: %d",size);
    rawData = (unsigned char*) [data bytes];
    // [self printGrayData:rawData width:width height:height bytesPerRow:bytesPerRow];
    CGColorSpaceRelease(colorSpace);
    CGContextRelease(context);
    // free(rawData);  
    return data;
}



// demo function to get raw data from image using provider
+ (UInt8 *)rawDataCopyWithImage:(UIImage*)image
{
    // RCTLogInfo(@"RNImageUtils > rawDataCopyWithImage ...");
    CGImageRef  imageRef = image.CGImage;
    CGDataProviderRef dataProvider = CGImageGetDataProvider(imageRef);
    
    __block CFDataRef dataRef;
    dataRef = CGDataProviderCopyData(dataProvider);
    
    UInt8* buffer = (UInt8*)CFDataGetBytePtr(dataRef);
    size_t bytesPerRow = CGImageGetBytesPerRow(imageRef);
    // print it out
    [self printData:buffer width:image.size.width height:image.size.height bytesPerRow:bytesPerRow];
    
    CFRelease(dataRef);
    return buffer;
}

// demo function to get raw data from image by draw it out with pixel bitmap context
+ (void)rawDataDrawWithImage:(UIImage*)image
{
    // RCTLogInfo(@"RNImageUtils > rawDataDrawWithImage ...");
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



    // after fill/draw image in the context, rawData will be filled
    CGContextDrawImage(context, CGRectMake(0, 0, width, height), imageRef);
    // print it out
    //  [self printData:rawData width:width height:height bytesPerRow:bytesPerRow];
    // get grayscale array data from that raw data
    NSData * data = [self RGBImageDataToGrayScaleArray:rawData width:width height:height bytesPerRow:bytesPerRow];
    // NSUInteger size = [data length] / sizeof(float);
    NSUInteger size = [data length] ;
    // RCTLogInfo(@" gray rawData size: %d",size);
    rawData = (unsigned char*) [data bytes];
    // [self printGrayData:rawData width:width height:height bytesPerRow:bytesPerRow];



    CGColorSpaceRelease(colorSpace);
    CGContextRelease(context); 
}


//  CGImageRef  imageRef = image.CGImage;
// size_t bytesPerRow = CGImageGetBytesPerRow(imageRef);
// print RGB values from image data 
+ (void)printData:(UInt8*)data width:(NSInteger)width height:(NSInteger)height bytesPerRow:(size_t)bytesPerRow
{
    // RCTLogInfo(@"RNImageUtils > printData ...");
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

// print grayscale array data 
 + (void)printGrayData:(UInt8 *)data width:(NSInteger)width height:(NSInteger)height bytesPerRow:(size_t)bytesPerRow
{
    // RCTLogInfo(@"RNImageUtils > printGrayData ...");
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
// get grayscale array data, converted from RGB image data
+ (NSData *) RGBImageDataToGrayScaleArray:(UInt8 *)data width:(NSInteger)width height:(NSInteger)height bytesPerRow:(size_t)bytesPerRow
{
    NSMutableData *result = [[NSMutableData alloc] initWithLength:0] ; // demo data
    //  RCTLogInfo(@"RNImageUtils > RGBImageDataToGrayScaleArray ...");
    for (NSInteger x=0; x<width; x++)
    {
        for (NSInteger y=0; y<height; y++)
        {
            UInt8*  pixelPtr = data + (int)(y) * bytesPerRow + (int)(x) * 4;
            UInt8 r = *(pixelPtr + 2);  // 赤
            UInt8 g = *(pixelPtr + 1);  // 緑
            UInt8 b = *(pixelPtr + 0);  // 青
            float yGray = (0.2126 * r + 0.7152 * g + 0.0722 * b )/255;
            [result appendBytes:&yGray length:sizeof(float)];
        }
    }
    return result;
}
+ (NSString *)applicationDocumentsDirectory {
    return [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject];
}
+ (UIImage*)loadImage:(NSString *) imagePath
{
    return [UIImage imageWithContentsOfFile:imagePath];
    
}

// https://gist.github.com/3ign0n/43dd799c33331c3de603 
+ (void)testGetPixelDataFromCGImageRefExample {
    
    UIImage *image = [self loadImage:@"myimage.png"];
    [self rawDataCopyWithImage:image];
    [self rawDataDrawWithImage:image];
}
@end

