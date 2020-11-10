//
//  RNImageUtils.h
//  RCTCamera
//
//  Created by Joao Guilherme Daros Fidelis on 19/01/18.
//

#import <UIKit/UIKit.h>
#import <CoreMedia/CoreMedia.h>
#import <Foundation/Foundation.h>

@interface RNImageUtils : NSObject

+ (UIImage *)generatePhotoOfSize:(CGSize)size;
+ (UIImage *)cropImage:(UIImage *)image toRect:(CGRect)rect;
+ (UIImage *)mirrorImage:(UIImage *)image;
+ (UIImage *)forceUpOrientation:(UIImage *)image;
+ (NSString *)writeImage:(NSData *)image toPath:(NSString *)path;
+ (UIImage *) scaleImage:(UIImage*)image toWidth:(NSInteger)width;
+ (UIImage *)scaleImage:(UIImage *)image convertToSize:(CGSize)size; 
+ (UIImage *)scaleToRect:(UIImage *)image atX:(float)x atY:(float)y withSize:(CGRect)size ;
+ (void)updatePhotoMetadata:(CMSampleBufferRef)imageSampleBuffer withAdditionalData:(NSDictionary *)additionalData inResponse:(NSMutableDictionary *)response;
+ (UIImage *)invertColors:(UIImage *)image;
+ (NSData *)getArrayOfImage:(UIImage *)image;
+ (void)printData:(UInt8*)data width:(NSInteger)width height:(NSInteger)height bytesPerRow:(size_t)bytesPerRow;
+ (void)printGrayData:(UInt8*)data width:(NSInteger)width height:(NSInteger)height bytesPerRow:(size_t)bytesPerRow;
+ (UInt8 *)rawDataCopyWithImage:(UIImage*)image;
+ (void)rawDataCopyWithImageRef:(CGImageRef )imageRef width:(NSInteger) imgWidth height:(NSInteger )imgHeight  ;
+ (void)rawDataDrawWithImage:(UIImage*)image;
+ (UIImage *)convertImageToGrayScale:(UIImage *)image;
+ (UIImage*)loadImage;
+ (NSData *) RGBImageDataToGrayScaleArray:(UInt8 *)data width:(NSInteger)width height:(NSInteger)height bytesPerRow:(size_t)bytesPerRow;

@end

