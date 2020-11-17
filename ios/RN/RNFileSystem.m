//
//  RNFileSystem.m
//  RCTCamera
//
//  Created by Joao Guilherme Daros Fidelis on 19/01/18.
//

#import "RNFileSystem.h"
#import <React/RCTLog.h>
@implementation RNFileSystem

+ (BOOL)ensureDirExistsWithPath:(NSString *)path
{
    BOOL isDir = NO;
    NSError *error;
    BOOL exists = [[NSFileManager defaultManager] fileExistsAtPath:path isDirectory:&isDir];
    if (!(exists && isDir)) {
        [[NSFileManager defaultManager] createDirectoryAtPath:path withIntermediateDirectories:YES attributes:nil error:&error];
        if (error) {
            return NO;
        }
    }
    return YES;
}
+ (BOOL)checkExistFilesInDir:(NSString *)folderName
{
    //Accessing the default documents directory
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths objectAtIndex:0];

    //Appending the name of your custom folder, if you have any
    NSString *path = [documentsDirectory stringByAppendingPathComponent:folderName]; 

    NSFileManager *fileManager = [NSFileManager defaultManager];

    if ([fileManager fileExistsAtPath:path]) { // Directory exists
        NSArray *listOfFiles = [fileManager contentsOfDirectoryAtPath:path error:nil];
        RCTLogInfo(@"check folder %@ : contains: %@",folderName,listOfFiles);
        int amount = [listOfFiles count] ;
        if(amount > 0){
            return true;
        }else{
            return false;
        }
    }
    return false;
}
+ (BOOL)checkFileInDocumentDir:(NSString *)folderName withFileName:(NSString *)fileName
{
    //Accessing the default documents directory
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths objectAtIndex:0];

    //Appending the name of your custom folder, if you have any
    NSString *path = [documentsDirectory stringByAppendingPathComponent:folderName]; 
    path = [path stringByAppendingPathComponent:fileName];

    NSFileManager *fileManager = [NSFileManager defaultManager];
    return [fileManager fileExistsAtPath:path];
}
+ (BOOL)checkExistedFilesInDocumentDir
{
    //Accessing the default documents directory
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths objectAtIndex:0];

    //Appending the name of your custom folder, if you have any
    // NSString *path = [documentsDirectory stringByAppendingPathComponent:folderName]; 

    NSFileManager *fileManager = [NSFileManager defaultManager];

    if ([fileManager fileExistsAtPath:documentsDirectory]) { // Directory exists
        NSArray *listOfFiles = [fileManager contentsOfDirectoryAtPath:documentsDirectory error:nil];
        RCTLogInfo(@"check folder %@ : contains: %@",documentsDirectory,listOfFiles);
        int amount = [listOfFiles count] ;
        if(amount > 0){
            return true;
        }else{
            return false;
        }
    }
    return false;
}

+ (NSString *)generatePathInDirectory:(NSString *)directory withExtension:(NSString *)extension
{
    NSString *fileName = [[[NSUUID UUID] UUIDString] stringByAppendingString:extension];
    [RNFileSystem ensureDirExistsWithPath:directory];
    return [directory stringByAppendingPathComponent:fileName];
}

+ (NSString *)generatePathInDirectory:(NSString *)directory withFileName:(NSString *)fileName
{
    [RNFileSystem ensureDirExistsWithPath:directory];
    return [directory stringByAppendingPathComponent:fileName];
}
+ (NSString *)cacheDirectoryPath
{
    NSArray *array = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
    return [array objectAtIndex:0];
}

+ (NSString *)documentDirectoryPath
{
    NSArray *array = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    return [array objectAtIndex:0];
}



+ (void)purgeDocumentsDirectory
{
    NSLog(@"Purging Documents Directory...");
    NSString *folderPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    NSError *error = nil;
    for (NSString *file in [[NSFileManager defaultManager] contentsOfDirectoryAtPath:folderPath error:&error]) {
        [[NSFileManager defaultManager] removeItemAtPath:[folderPath stringByAppendingPathComponent:file] error:&error];
    }
}
+ (void)CopyFile :(NSString *)fileName fromPath:(NSString *)originPath toPath:(NSString *)destPath
{
    [self purgeDocumentsDirectory];
    BOOL success;
    BOOL exist;
    NSFileManager *fileManager = [NSFileManager defaultManager];
    originPath = [originPath stringByAppendingPathComponent:fileName];
    exist = [fileManager fileExistsAtPath:originPath];
    if(!exist){
        RCTLogWarn(@"file does not exist at path %@",originPath);
        return;
    }
    [self ensureDirExistsWithPath:destPath];
    // destPath = [destPath stringByAppendingPathComponent:fileName];
    
    success = [fileManager fileExistsAtPath:[destPath stringByAppendingPathComponent:fileName]];
    // NSString *FileDB = [[[NSBundle mainBundle]resourcePath]stringByAppendingPathComponent:DataName];
    if (success)
    {
        NSLog(@"File Exist");
        return;
    }
    else
    {
        // RCTLogInfo(@"copy file to path %@",destPath);
        [fileManager copyItemAtPath:originPath toPath:destPath error:nil];
        
        NSArray *listOfFiles = [fileManager contentsOfDirectoryAtPath:destPath error:nil];
        RCTLogInfo(@"copy file to path %@",destPath);
        RCTLogInfo(@"check folder %@ : contains: %@",destPath,listOfFiles);   
    }
}

@end

