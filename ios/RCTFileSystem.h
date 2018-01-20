//
//  RCTFileSystem.h
//  RCTCamera
//
//  Created by Joao Guilherme Daros Fidelis on 19/01/18.
//

#import <Foundation/Foundation.h>

@interface RCTFileSystem : NSObject

+ (BOOL)ensureDirExistsWithPath:(NSString *)path;
+ (NSString *)generatePathInDirectory:(NSString *)directory withExtension:(NSString *)extension;

@end

