#import "OpenCVProcessor.hpp"
#import <opencv2/opencv.hpp>
#import <opencv2/objdetect.hpp>

@implementation OpenCVProcessor{
    BOOL saveDemoFrame;
    int processedFrames;
    NSInteger expectedFaceOrientation;
    NSInteger objectsToDetect;
}

- (id) init {
    
    saveDemoFrame = false;
    processedFrames = 0;
    expectedFaceOrientation = -1;
    objectsToDetect = 0; // face
    
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

- (void)setExpectedFaceOrientation:(NSInteger)expectedOrientation
{
    expectedFaceOrientation = expectedOrientation;
}

- (void)updateObjectsToDetect:(NSInteger)givenObjectsToDetect
{
    objectsToDetect = givenObjectsToDetect;
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

- (int)rotateImage:(Mat&)image;
{
    int orientation = 3;
    //cv::equalizeHist(image, image);
    
    if(expectedFaceOrientation != -1){
        orientation = expectedFaceOrientation;
    } else {
        // rotate image according to device-orientation
        UIDeviceOrientation interfaceOrientation = [[UIDevice currentDevice] orientation];
        if (interfaceOrientation == UIDeviceOrientationPortrait) {
            orientation = 0;
        } else  if (interfaceOrientation == UIDeviceOrientationPortraitUpsideDown) {
            orientation = 2;
        } else  if (interfaceOrientation == UIDeviceOrientationLandscapeLeft) {
            orientation = 1;
        }
    }
    
    switch(orientation){
        case 0:
            transpose(image, image);
            flip(image, image,1);
            break;
        case 1:
            flip(image, image,-1);
            break;
        case 2:
            transpose(image, image);
            flip(image, image,0);
            break;
    }
    
    return orientation;
}

- (float)resizeImage:(Mat&)image width:(float)width;
{
    float scale = width / (float)image.cols;
    
    cv::resize(image, image, cv::Size(0,0), scale, scale, cv::INTER_CUBIC);
    
    return scale;
}

- (void)processImageFaces:(Mat&)image;
{
    int orientation = [self rotateImage:image];
    
    float imageWidth = 480.;
    int scale = [self resizeImage:image width:imageWidth];
    float imageHeight = (float)image.rows;
    
    if(saveDemoFrame){
        [self saveImageToDisk:image];
    }
    
    objects.clear();
    cascade.detectMultiScale(image,
                             objects,
                             1.2,
                             3,
                             0,
                             cv::Size(10, 10));
    
    NSMutableArray *faces = [[NSMutableArray alloc] initWithCapacity:objects.size()];
    if(objects.size() > 0){
        for( int i = 0; i < objects.size(); i++ )
        {
            cv::Rect face = objects[i];
            
            NSDictionary *faceDescriptor = @{
                                             @"x" : [NSNumber numberWithFloat:face.x / imageWidth],
                                             @"y" : [NSNumber numberWithFloat:face.y / imageHeight],
                                             @"width": [NSNumber numberWithFloat:face.width / imageWidth],
                                             @"height": [NSNumber numberWithFloat:face.height / imageHeight],
                                             @"orientation": @(orientation)
                                             };
            
            [faces addObject:faceDescriptor];
        }
    }
    [delegate onFacesDetected:faces];
}

- (BOOL) compareContourAreasReverse: (std::vector<cv::Point>) contour1 contour2:(std::vector<cv::Point>) contour2  {
    double i = fabs( contourArea(cv::Mat(contour1)) );
    double j = fabs( contourArea(cv::Mat(contour2)) );
    return ( i > j );
}

- (void)processImageTextBlocks:(Mat&)image;
{
    int orientation = [self rotateImage:image];
    
    float algorithmWidth = 1080.;
    float imageWidth = 480.;
    float algorithmScale = imageWidth / algorithmWidth;
    float scale = [self resizeImage:image width:imageWidth];
    
    float imageHeight = image.rows;
    
    float rectKernX = 17. * algorithmScale;
    float rectKernY = 6. * algorithmScale;
    float sqKernXY = 40. * algorithmScale;
    float minSize = 3000. * algorithmScale;
    float maxSize = 100000. * algorithmScale;
    
    cv::Mat processedImage = image.clone();
    
    // initialize a rectangular and square structuring kernel
    //float factor = (float)min(image.rows, image.cols) / 600.;
    Mat rectKernel = getStructuringElement(MORPH_RECT, cv::Size(rectKernX, rectKernY));
    Mat rectKernel2 = getStructuringElement(MORPH_RECT, cv::Size(sqKernXY, (int)(0.666666*sqKernXY)));
    
    // Smooth the image using a 3x3 Gaussian, then apply the blackhat morphological
    // operator to find dark regions on a light background
    GaussianBlur(processedImage, processedImage, cv::Size(3, 3), 0);
    morphologyEx(processedImage, processedImage, MORPH_BLACKHAT, rectKernel);
    
    
    // Compute the Scharr gradient of the blackhat image
    Mat imageGrad;
    Sobel(processedImage, imageGrad, CV_32F, 1, 0, CV_SCHARR);
    convertScaleAbs(imageGrad/8, processedImage);
    
    // Apply a closing operation using the rectangular kernel to close gaps in between
    // letters, then apply Otsu's thresholding method
    morphologyEx(processedImage, processedImage, MORPH_CLOSE, rectKernel);
    threshold(processedImage, processedImage, 0, 255, THRESH_BINARY | THRESH_OTSU);
    erode(processedImage, processedImage, Mat(), cv::Point(-1, -1), 2, 1, 1);
    
    
    // Perform another closing operation, this time using the square kernel to close gaps
    // between lines of TextBlocks
    morphologyEx(processedImage, processedImage, MORPH_CLOSE, rectKernel2);
    
    
    // Find contours in the thresholded image and sort them by size
    float minContourArea = minSize;
    float maxContourArea = maxSize;
    std::vector< std::vector<cv::Point> > contours;
    std::vector<Vec4i> hierarchy;
    findContours(processedImage, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
    
    // Create a result vector
    std::vector<RotatedRect> minRects;
    for (int i = 0, I = contours.size(); i < I; ++i) {
        // Filter by provided area limits
        if (contourArea(contours[i]) > minContourArea && contourArea(contours[i]) < maxContourArea)
            minRects.push_back(minAreaRect(Mat(contours[i])));
    }
    
    if(saveDemoFrame){
        cv::Mat debugDrawing = image.clone();
        for (int i = 0, I = minRects.size(); i < I; ++i) {
            Point2f rect_points[4]; minRects[i].points( rect_points );
            for( int j = 0; j < 4; ++j )
                line( debugDrawing, rect_points[j], rect_points[(j+1)%4], Scalar(255,0,0), 1, 8 );
        }
        
        [self saveImageToDisk:debugDrawing];
    }
    
    NSMutableArray *detectedObjects = [[NSMutableArray alloc] init];
    if(minRects.size() > 0){
        for(int i = 0, I = minRects.size(); i < I; ++i){
            Point2f rect_points[4];
            minRects[i].points( rect_points );
            
            float xRel = rect_points[1].x / imageWidth;
            float yRel = rect_points[1].y / imageHeight;
            float widthRel = fabsf(rect_points[3].x - rect_points[1].x) / imageWidth;
            float heightRel = fabsf(rect_points[3].y - rect_points[1].y) / imageHeight;
            float sizeRel = fabsf(widthRel * heightRel);
            float ratio =  fabsf(rect_points[3].x - rect_points[1].x) / fabsf(rect_points[3].y - rect_points[1].y);
            
            // if object large enough
            if(sizeRel >= 0.025 & ratio >= 5.5 & ratio <= 8.5){
                NSDictionary *objectDescriptor = @{
                                                   @"x" : [NSNumber numberWithFloat:xRel],
                                                   @"y" : [NSNumber numberWithFloat:yRel],
                                                   @"width": [NSNumber numberWithFloat:widthRel],
                                                   @"height": [NSNumber numberWithFloat:heightRel],
                                                   @"orientation": @(orientation)
                                                   };
                
                [detectedObjects addObject:objectDescriptor];
            }
        }
    }
    [delegate onFacesDetected:detectedObjects];
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
        
        switch(objectsToDetect){
            case 0:
                [self processImageFaces:image];
                break;
            case 1:
                [self processImageTextBlocks:image];
                break;
        }
        
        // cleanup
        CVPixelBufferUnlockBaseAddress(imageBuffer, 0);
    }
    processedFrames++;
}
#endif

@end

