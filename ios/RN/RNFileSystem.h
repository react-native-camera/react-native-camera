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
+ (NSString *)generatePathInDirectory:(NSString *)directory withFileName:(NSString *)fileName;
+ (NSString *)cacheDirectoryPath;
+ (NSString *)documentDirectoryPath;
+ (BOOL)checkExistFilesInDir:(NSString *)folderName;
+ (BOOL)checkFileInDocumentDir:(NSString *)folderName withFileName:(NSString *)fileName;
+ (BOOL)checkExistedFilesInDocumentDir;
+ (void)purgeDocumentsDirectory;
+ (void)CopyFile :(NSString *)fileName fromPath:(NSString *)originPath toPath:(NSString *)destPath;
@end

