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
+ (UIImage *)scaleImage:(UIImage*)image toWidth:(NSInteger)width;
+ (void)updatePhotoMetadata:(CMSampleBufferRef)imageSampleBuffer withAdditionalData:(NSDictionary *)additionalData inResponse:(NSMutableDictionary *)response;

@end

