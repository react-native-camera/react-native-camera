#import "OpenCVProcessor.hpp"
#import <opencv2/opencv.hpp>
#import <opencv2/objdetect.hpp>

@implementation OpenCVProcessor{
    BOOL saveDemoFrame;
    int processedFrames;
}

- (id) init {
    
    saveDemoFrame = false;
    processedFrames = 0;
    
    NSString *path = [[NSBundle mainBundle] pathForResource:@"lbpcascade_frontalface_improved.xml"
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

- (void)saveImageToDisk:(Mat&)image;
{
    NSLog(@"----------------SAVE IMAGE-----------------");
    saveDemoFrame = false;
    
    NSData *data = [NSData dataWithBytes:image.data length:image.elemSize()*image.total()];
    CGColorSpaceRef colorSpace;
    
    if (image.elemSize() == 1) {
        colorSpace = CGColorSpaceCreateDeviceGray();
    } else {
        colorSpace = CGColorSpaceCreateDeviceRGB();
    }
    
    CGDataProviderRef provider = CGDataProviderCreateWithCFData((__bridge CFDataRef)data);
    
    // Creating CGImage from cv::Mat
    CGImageRef imageRef = CGImageCreate(image.cols,                                 //width
                                        image.rows,                                 //height
                                        8,                                          //bits per component
                                        8 * image.elemSize(),                       //bits per pixel
                                        image.step[0],                            //bytesPerRow
                                        colorSpace,                                 //colorspace
                                        kCGImageAlphaNone|kCGBitmapByteOrderDefault,// bitmap info
                                        provider,                                   //CGDataProviderRef
                                        NULL,                                       //decode
                                        false,                                      //should interpolate
                                        kCGRenderingIntentDefault                   //intent
                                        );
    
    
    // Getting UIImage from CGImage
    UIImage *finalImage = [UIImage imageWithCGImage:imageRef];
    CGImageRelease(imageRef);
    CGDataProviderRelease(provider);
    CGColorSpaceRelease(colorSpace);
    
    UIImageWriteToSavedPhotosAlbum(finalImage, nil, nil, nil);
}

- (void)processImage:(Mat&)image;
{
    //cv::equalizeHist(image, image);
    
    // rotate image according to device-rotation
    UIDeviceOrientation interfaceOrientation = [[UIDevice currentDevice] orientation];
    if (interfaceOrientation == UIDeviceOrientationPortrait) {
        transpose(image, image);
        flip(image, image,1);
    } else  if (interfaceOrientation == UIDeviceOrientationPortraitUpsideDown) {
        transpose(image, image);
        flip(image, image,0);
    } else  if (interfaceOrientation == UIDeviceOrientationLandscapeLeft) {
        flip(image, image,-1);
    }
    
    cv::resize(image, image, cv::Size(0,0), 360./(float)image.cols, 360./(float)image.cols, cv::INTER_CUBIC);
    
    if(saveDemoFrame){
        [self saveImageToDisk:image];
    }
    
    objects.clear();
    cascade.detectMultiScale(image,
                             objects,
                             2.0,
                             3,
                             CV_HAAR_SCALE_IMAGE,
                             cv::Size(30, 30));
    
    if(objects.size() > 0){
        NSMutableArray *faces = [[NSMutableArray alloc] initWithCapacity:objects.size()];
        for( int i = 0; i < objects.size(); i++ )
        {
            cv::Rect face = objects[i];
            id objects[] = { @(face.x), @(face.y), @(face.width), @(face.height) };
            id keys[] = { @"x", @"y", @"width", @"height" };
            NSUInteger count = sizeof(objects) / sizeof(id);
            NSDictionary *faceDescriptor = [NSDictionary dictionaryWithObjects:objects
                                                                       forKeys:keys count:count];
            [faces addObject:faceDescriptor];
        }
        [delegate onFacesDetected:faces];
    }
}


- (void)captureOutput:(AVCaptureOutput *)captureOutput didOutputSampleBuffer:(CMSampleBufferRef)sampleBuffer fromConnection:(AVCaptureConnection *)connection
{
    // https://github.com/opencv/opencv/blob/master/modules/videoio/src/cap_ios_video_camera.mm
    if(processedFrames % 10 == 0){
        (void)captureOutput;
        (void)connection;
        
        // convert from Core Media to Core Video
        CVImageBufferRef imageBuffer = CMSampleBufferGetImageBuffer(sampleBuffer);
        CVPixelBufferLockBaseAddress(imageBuffer, 0);
        
        void* bufferAddress;
        size_t width;
        size_t height;
        size_t bytesPerRow;
        
        int format_opencv = CV_8UC1;
        
        bufferAddress = CVPixelBufferGetBaseAddressOfPlane(imageBuffer, 0);
        width = CVPixelBufferGetWidthOfPlane(imageBuffer, 0);
        height = CVPixelBufferGetHeightOfPlane(imageBuffer, 0);
        bytesPerRow = CVPixelBufferGetBytesPerRowOfPlane(imageBuffer, 0);
        
        // delegate image processing to the delegate
        cv::Mat image((int)height, (int)width, format_opencv, bufferAddress, bytesPerRow);
        
        [self processImage:image];
        
        // cleanup
        CVPixelBufferUnlockBaseAddress(imageBuffer, 0);
    }
    processedFrames++;
}
#endif

@end

