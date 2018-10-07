//
//  RNAssetWriter.m
//  RNCamera
//
//  Created by Marius Reimer on 10/6/18.
//

#import "RNAssetWriter.h"

@implementation RNAssetWriter

{
    BOOL dualModeEnabled;
    BOOL bFirstAudio;
    BOOL bWriting;
    
    int64_t frameNumber;
    size_t bytesWritten;

    CMTime startTime; //TODO check this with original code! messed initializing self. !
    CMTime previousFrameTime;
    CMTime previousAudioTime;
    CMTime firstAudioTimeStamp;
    CMTime firstVideoTimestamp;
    
    NSDate* initDate;

    CMSampleBufferRef firstAudioBuffer;
    
    AVAssetWriter* assetWriter;
    AVAssetWriterInput* assetVideoWriterInput;
    AVAssetWriterInput* assetAudioWriterInput;
    AVAssetWriterInputPixelBufferAdaptor* pixelBufferAdaptor;
    
    dispatch_queue_t writerQueue;
}

- (BOOL)initRecording {
    writerQueue = dispatch_queue_create("com.mariusreimer.writerQueue", dispatch_queue_attr_make_with_qos_class(DISPATCH_QUEUE_SERIAL, QOS_CLASS_USER_INTERACTIVE, 0));
    
    [self initAssetWriter];
    [self initAssetWriterInputs];
    [self assignInputsToAssetWriter];
    
    frameNumber = 0;
    bFirstAudio = YES;
    bWriting = YES;
    
    startTime = kCMTimeZero;
    previousFrameTime = kCMTimeInvalid;
    previousAudioTime = kCMTimeInvalid;
    firstAudioTimeStamp = kCMTimeInvalid;
    firstVideoTimestamp = kCMTimeInvalid;
    initDate = [NSDate date];
    
    if (firstAudioBuffer) {
        CFRelease(firstAudioBuffer);
    }
    
    return YES;
}

- (void)initAssetWriter {
    NSError *error = nil;
    
    assetWriter = [[AVAssetWriter alloc] initWithURL:_outputURL fileType:_outputFileType error:&error];
    NSParameterAssert(assetWriter);
    
    if (error != nil) {
        RCTLogWarn(@"Asset writer init error - %@", error.description);
    }
}

- (void)initAssetWriterInputs {
    // Video writer
    assetVideoWriterInput = [AVAssetWriterInput
                              assetWriterInputWithMediaType:AVMediaTypeVideo
                              outputSettings:_videoSettings];
    assetVideoWriterInput.expectsMediaDataInRealTime = YES;
    
    CGAffineTransform transform = assetVideoWriterInput.transform;
    // note: degree to radians 'calculation'
    transform = CGAffineTransformRotate(transform, (M_PI * 90)/180);
    assetVideoWriterInput.transform = transform;
    
    NSParameterAssert(assetVideoWriterInput);
    
    // Pixel adaptor
    
    pixelBufferAdaptor = [[AVAssetWriterInputPixelBufferAdaptor alloc]
                           initWithAssetWriterInput:assetVideoWriterInput
                           sourcePixelBufferAttributes:
                           [NSDictionary dictionaryWithObjectsAndKeys:
                            [NSNumber numberWithInt:kCVPixelFormatType_420YpCbCr8BiPlanarFullRange],
                            kCVPixelBufferPixelFormatTypeKey,
                            nil]];
    
    NSParameterAssert(pixelBufferAdaptor);
    
    // Audio writer
    
    assetAudioWriterInput = [AVAssetWriterInput
                              assetWriterInputWithMediaType:AVMediaTypeAudio
                              outputSettings:_audioSettings ];
    assetAudioWriterInput.expectsMediaDataInRealTime = YES;
    
    NSParameterAssert(assetAudioWriterInput);
}

- (void) assignInputsToAssetWriter {
    if (assetWriter == nil) {
        RCTLogWarn(@"[RNAssetWriter] - not initialized!");
        return;
    }
    
    if ([assetWriter canAddInput:assetVideoWriterInput]) {
        [assetWriter addInput:assetVideoWriterInput];
    } else {
        [self logAndGetAssetWriterError];
    }
    
    if ([assetWriter canAddInput:assetAudioWriterInput]) {
        [assetWriter addInput:assetAudioWriterInput];
    } else {
        [self logAndGetAssetWriterError];
    }
}

- (NSString *) logAndGetAssetWriterError {
    if (assetWriter == nil) {
        return @"";
    }
    
    NSString * errorDesc = assetWriter.error.description;
    RCTLogWarn(@"[RNAssetWriter] - asset writer error: %@", errorDesc);
    
    return errorDesc;
}

- (BOOL) maxFileSizeReached {
    return bytesWritten >= _maxRecordedFileSize;
}

//TODO [reime005] use of pointers for better performance?
- (BOOL)addVideoData:(CVImageBufferRef)imageBuffer {
    if (bWriting == NO) {
        return NO;
    }
    
    if ([self maxFileSizeReached]) {
        bWriting = NO;
    }
    
    //TODO [reime005] improve performance by not using NSDate but CMTime only?
    NSTimeInterval secondsBetween = [[NSDate date] timeIntervalSinceDate:initDate];
    CMTime frameTime = CMTimeMakeWithSeconds(secondsBetween, NSEC_PER_SEC);
    
    if((CMTIME_IS_INVALID(frameTime)) ||
       (CMTIME_COMPARE_INLINE(frameTime, ==, previousFrameTime)) ||
       (CMTIME_IS_INDEFINITE(frameTime))) {
        return NO;
    }
    
    if ([assetWriter status] == AVAssetWriterStatusUnknown) {
        // need a video frame time to begin before writing
        // must also have the first audio frame to start writing
        if (CMTIME_IS_INVALID(firstVideoTimestamp) &&
            !CMTIME_IS_INVALID(firstAudioTimeStamp)) {
            firstVideoTimestamp = frameTime;
            
            [assetWriter startWriting];
            
            CMTime startTime = CMTimeAdd(firstAudioTimeStamp, firstVideoTimestamp);
            [assetWriter startSessionAtSourceTime:startTime];
            
            RCTLogWarn(@"BEGIN writing at %f", CMTimeGetSeconds(startTime));
        }
    }
    
    if ([assetWriter status] == AVAssetWriterStatusWriting) {
        if (assetVideoWriterInput.readyForMoreMediaData == YES &&
            !CMTIME_IS_INVALID(firstAudioTimeStamp)) {
            CMTime time = CMTimeAdd(firstAudioTimeStamp, frameTime);
            
            if (assetVideoWriterInput.readyForMoreMediaData == YES) {
                
                if ([pixelBufferAdaptor appendPixelBuffer:imageBuffer
                                      withPresentationTime:time]) {
                    CVImageBufferGetEncodedSize(imageBuffer);
                    frameNumber++;
                } else {
                    RCTLogWarn(@"error writing video buffer");
                    return NO; //TODO [reime005] could this cause a frame error?
                }
            } else {
                RCTLogWarn(@"[VideoWriter addVideo] - not ready for more media data");
                return NO; //TODO [reime005] could this cause a frame error?
            }
            previousFrameTime = time;
        }
    }
    
    if ([assetWriter status] == AVAssetWriterStatusFailed) {
        [self logAndGetAssetWriterError];
        return NO;
    }
    
    return YES;
}

//TODO [reime005] use of pointers for better performance?
- (BOOL)addAudioData:(CMSampleBufferRef)audioBuffer {
    if (bWriting == NO) {
        return NO;
    }
    
    if ([self maxFileSizeReached]) {
        bWriting = NO;
    }
    
    if (audioBuffer == nil) {
        RCTLogWarn(@"[VideoWriter addAudio] - audioBuffer was nil.");
        return NO;
    }
    
    if (CMTIME_IS_INVALID(firstAudioTimeStamp)) {
        firstAudioTimeStamp = CMSampleBufferGetPresentationTimeStamp(audioBuffer);
    }
    
    if (assetAudioWriterInput.readyForMoreMediaData == NO) {
        RCTLogWarn(@"[VideoWriter addAudio] - not ready for more media data");
        return NO;
    }
    
    CMTime newBufferTime = CMSampleBufferGetPresentationTimeStamp(audioBuffer);
    if (CMTIME_COMPARE_INLINE(newBufferTime, ==, previousAudioTime)) {
        RCTLogWarn(@"CMTIME_COMPARE_INLINE(newBufferTime, ==, previousAudioTime)");
        return NO;
    }
    
    previousAudioTime = newBufferTime;
    
    // hold onto the first buffer, until we've figured out when playback truly starts (which is
    // when the second buffer arrives)
    if (bFirstAudio) {
        CMSampleBufferCreateCopy(NULL, audioBuffer, &firstAudioBuffer);
        bFirstAudio = NO;
        return NO;
    }
    // if the incoming audio buffer has an earlier timestamp than the current "first" buffer, then
    // drop the current "first" buffer and store the new one instead
    else if (firstAudioBuffer && CMTIME_COMPARE_INLINE(CMSampleBufferGetPresentationTimeStamp(firstAudioBuffer), >, newBufferTime)) {
        CFRelease(firstAudioBuffer);
        CMSampleBufferCreateCopy(NULL, audioBuffer, &firstAudioBuffer);
        return NO;
    }
    
    //----------------------------------------------------------
    
    // note: sync since we need to release the buffers afterwards!
    dispatch_sync(writerQueue, ^{
        if (firstAudioBuffer) {
            if (!_audioIsMuted) {
                CMSampleBufferRef correctedFirstBuffer = [self copySampleBuffer:firstAudioBuffer withNewTime:previousFrameTime];
                [assetAudioWriterInput appendSampleBuffer:correctedFirstBuffer];
                [self appendBufferSize: CMSampleBufferGetTotalSampleSize(correctedFirstBuffer)];
                CFRelease(correctedFirstBuffer);
            }
            CFRelease(firstAudioBuffer);
            firstAudioBuffer = NULL;
        }
        
        if (!_audioIsMuted) {
            BOOL bOk = [assetAudioWriterInput appendSampleBuffer:audioBuffer];
            if (bOk == NO) {
                [self logAndGetAssetWriterError];
            } else {
                [self appendBufferSize: CMSampleBufferGetTotalSampleSize(audioBuffer)];
            }
        }
    });
    
    return YES;
}

- (void)appendBufferSize:(size_t)bytes {
    bytesWritten = bytesWritten + bytes;
}

//TODO [reime005] add callback for when really finished
- (void)finishWritingWithCompletionHandler:(void (^)(void))handler NS_AVAILABLE(10_9, 6_0) {
    if (assetWriter == nil) {
        RCTLogWarn(@"[RNAssetWriter] - not initialized!");
        return;
    }
    
    if ([assetWriter status] != AVAssetWriterStatusUnknown) {
        [assetWriter finishWritingWithCompletionHandler:^{
            bWriting = NO;
            RCTLogWarn(@"finished. fps: %.1f", frameNumber/_maxDuration);
            handler();
        }];
    } else {
        handler();
    }
}

#pragma mark Helpers

- (CMSampleBufferRef) copySampleBuffer:(CMSampleBufferRef)inBuffer withNewTime:(CMTime)time {
    
    CMSampleTimingInfo timingInfo;
    CMSampleBufferGetSampleTimingInfo(inBuffer, 0, &timingInfo);
    timingInfo.presentationTimeStamp = time;
    
    CMSampleBufferRef outBuffer;
    CMSampleBufferCreateCopyWithNewTiming(NULL, inBuffer, 1, &timingInfo, &outBuffer);
    return outBuffer;
}

@end
