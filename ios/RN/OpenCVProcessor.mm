#import "OpenCVProcessor.hpp"
#import <opencv2/opencv.hpp>
#import <opencv2/objdetect.hpp>

@implementation OpenCVProcessor

- (id) init {
    NSString *path = [[NSBundle mainBundle] pathForResource:@"haarcascade_frontalface_alt.xml"
                                                     ofType:nil];
    std::string cascade_path = (char *)[path UTF8String];
    if (!cascade.load(cascade_path)) {
        NSLog(@"Couldn't load haar cascade file.");
    }
    
    if (self = [super init]) {
        // Initialize self
    }
    return self;
}

- (id) initWithDelegate:(id)delegateObj {
    delegate = delegateObj;
    return self;
}

# pragma mark - OpenCV-Processing

#ifdef __cplusplus
- (void)processImage:(Mat&)image;
{
    cv::Mat grayMat;
    cv::cvtColor(image, grayMat, CV_BGR2GRAY);
    
    cv::equalizeHist(grayMat, grayMat);
    
    objects.clear();
    cascade.detectMultiScale(grayMat, objects,
                             4.6, 1,
                             CV_HAAR_SCALE_IMAGE,
                             cv::Size(40, 40));
    
    for(size_t i = 0; i < objects.size(); ++i) {
        [delegate onFacesDetected:[NSArray new]];
    }
}


- (void)captureOutput:(AVCaptureOutput *)captureOutput didOutputSampleBuffer:(CMSampleBufferRef)sampleBuffer fromConnection:(AVCaptureConnection *)connection
{
    
    (void)captureOutput;
    (void)connection;
    
    // convert from Core Media to Core Video
    CVImageBufferRef imageBuffer = CMSampleBufferGetImageBuffer(sampleBuffer);
    CVPixelBufferLockBaseAddress(imageBuffer, 0);
    
    void* bufferAddress;
    size_t width;
    size_t height;
    size_t bytesPerRow;
    
    CGColorSpaceRef colorSpace;
    CGContextRef context;
    
    int format_opencv;
    
    OSType format = CVPixelBufferGetPixelFormatType(imageBuffer);
    if (format == kCVPixelFormatType_420YpCbCr8BiPlanarFullRange) {
        
        format_opencv = CV_8UC1;
        
        bufferAddress = CVPixelBufferGetBaseAddressOfPlane(imageBuffer, 0);
        width = CVPixelBufferGetWidthOfPlane(imageBuffer, 0);
        height = CVPixelBufferGetHeightOfPlane(imageBuffer, 0);
        bytesPerRow = CVPixelBufferGetBytesPerRowOfPlane(imageBuffer, 0);
        
    } else { // expect kCVPixelFormatType_32BGRA
        
        format_opencv = CV_8UC4;
        
        bufferAddress = CVPixelBufferGetBaseAddress(imageBuffer);
        width = CVPixelBufferGetWidth(imageBuffer);
        height = CVPixelBufferGetHeight(imageBuffer);
        bytesPerRow = CVPixelBufferGetBytesPerRow(imageBuffer);
        
    }
    
    // delegate image processing to the delegate
    cv::Mat image((int)height, (int)width, format_opencv, bufferAddress, bytesPerRow);
    
    CGImage* dstImage;
    
    [self processImage:image];
    
    // check if matrix data pointer or dimensions were changed by the delegate
    bool iOSimage = false;
    if (height == (size_t)image.rows && width == (size_t)image.cols && format_opencv == image.type() && bufferAddress == image.data && bytesPerRow == image.step) {
        iOSimage = true;
    }
    
    
    // (create color space, create graphics context, render buffer)
    CGBitmapInfo bitmapInfo;
    
    // basically we decide if it's a grayscale, rgb or rgba image
    if (image.channels() == 1) {
        colorSpace = CGColorSpaceCreateDeviceGray();
        bitmapInfo = kCGImageAlphaNone;
    } else if (image.channels() == 3) {
        colorSpace = CGColorSpaceCreateDeviceRGB();
        bitmapInfo = kCGImageAlphaNone;
        if (iOSimage) {
            bitmapInfo |= kCGBitmapByteOrder32Little;
        } else {
            bitmapInfo |= kCGBitmapByteOrder32Big;
        }
    } else {
        colorSpace = CGColorSpaceCreateDeviceRGB();
        bitmapInfo = kCGImageAlphaPremultipliedFirst;
        if (iOSimage) {
            bitmapInfo |= kCGBitmapByteOrder32Little;
        } else {
            bitmapInfo |= kCGBitmapByteOrder32Big;
        }
    }
    
    if (iOSimage) {
        context = CGBitmapContextCreate(bufferAddress, width, height, 8, bytesPerRow, colorSpace, bitmapInfo);
        dstImage = CGBitmapContextCreateImage(context);
        CGContextRelease(context);
    } else {
        
        NSData *data = [NSData dataWithBytes:image.data length:image.elemSize()*image.total()];
        CGDataProviderRef provider = CGDataProviderCreateWithCFData((__bridge CFDataRef)data);
        
        // Creating CGImage from cv::Mat
        dstImage = CGImageCreate(image.cols,                                 // width
                                 image.rows,                                 // height
                                 8,                                          // bits per component
                                 8 * image.elemSize(),                       // bits per pixel
                                 image.step,                                 // bytesPerRow
                                 colorSpace,                                 // colorspace
                                 bitmapInfo,                                 // bitmap info
                                 provider,                                   // CGDataProviderRef
                                 NULL,                                       // decode
                                 false,                                      // should interpolate
                                 kCGRenderingIntentDefault                   // intent
                                 );
        
        CGDataProviderRelease(provider);
    }
    
    
    // render buffer
    dispatch_sync(dispatch_get_main_queue(), ^{
        //      self.customPreviewLayer.contents = (__bridge id)dstImage;
    });
    
    
    //    recordingCountDown--;
    //    if (self.recordVideo == YES && recordingCountDown < 0) {
    //      lastSampleTime = CMSampleBufferGetPresentationTimeStamp(sampleBuffer);
    //      //      CMTimeShow(lastSampleTime);
    //      if (self.recordAssetWriter.status != AVAssetWriterStatusWriting) {
    //        [self.recordAssetWriter startWriting];
    //        [self.recordAssetWriter startSessionAtSourceTime:lastSampleTime];
    //        if (self.recordAssetWriter.status != AVAssetWriterStatusWriting) {
    //          NSLog(@"[Camera] Recording Error: asset writer status is not writing: %@", self.recordAssetWriter.error);
    //          return;
    //        } else {
    //          NSLog(@"[Camera] Video recording started");
    //        }
    //      }
    //
    //      if (self.recordAssetWriterInput.readyForMoreMediaData) {
    //        CVImageBufferRef pixelBuffer = [self pixelBufferFromCGImage:dstImage];
    //        if (! [self.recordPixelBufferAdaptor appendPixelBuffer:pixelBuffer
    //                                          withPresentationTime:lastSampleTime] ) {
    //          NSLog(@"Video Writing Error");
    //        }
    //        if (pixelBuffer != nullptr)
    //          CVPixelBufferRelease(pixelBuffer);
    //      }
    //
    //    }
    
    
    // cleanup
    CGImageRelease(dstImage);
    
    CGColorSpaceRelease(colorSpace);
    
    CVPixelBufferUnlockBaseAddress(imageBuffer, 0);
    
}
#endif

@end
