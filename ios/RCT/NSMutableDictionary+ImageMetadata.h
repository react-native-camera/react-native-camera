//
//  NSMutableDictionary+ImageMetadata.m
//  RCTCamera
//
//  Created by Nick Hodapp on 5/1/20.
//
//

#import <Foundation/Foundation.h>

@interface NSMutableDictionary(ImageMetadata)
- (void)mergeMetadata:(NSDictionary *)inputMetadata;
@end
