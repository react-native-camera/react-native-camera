//
//  RNAssetWriter.h
//  RNCamera
//
//  Created by Marius Reimer on 10/6/18.
//

#import <AVFoundation/AVFoundation.h>
#import <React/RCTBridge.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTLog.h>
#import <UIKit/UIKit.h>

@class RNAssetWriter;

@interface RNAssetWriter : UIView

@property (nonatomic)       int64_t         maxRecordedFileSize;
@property (nonatomic)       double          maxDuration;
@property (nonatomic)       BOOL            audioIsMuted;
@property (nonatomic, copy) AVFileType      outputFileType;
@property (nonatomic, copy) NSURL           *outputURL;
@property (nonatomic, copy) NSDictionary    *videoSettings;
@property (nonatomic, copy) NSDictionary    *audioSettings;

- (BOOL)initRecording;
- (NSString*)logAndGetAssetWriterError;
- (BOOL)addVideoData:(CVImageBufferRef)imageBuffer;
- (BOOL)addAudioData:(CMSampleBufferRef)audioBuffer;
- (void)finishWritingWithCompletionHandler:(void (^)(void))handler NS_AVAILABLE(10_9, 6_0);

@end
