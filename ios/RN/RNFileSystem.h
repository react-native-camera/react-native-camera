//
//  RNFileSystem.h
//  RCTCamera
//
//  Created by Joao Guilherme Daros Fidelis on 19/01/18.
//

#import <Foundation/Foundation.h>

@interface RNFileSystem : NSObject

+ (BOOL)ensureDirExistsWithPath:(NSString *)path;
+ (NSString *)generatePathInDirectory:(NSString *)directory withExtension:(NSString *)extension;
+ (NSString *)cacheDirectoryPath;

@end

