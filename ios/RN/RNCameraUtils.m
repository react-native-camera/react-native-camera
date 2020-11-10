//
//  RNCameraUtils.m
//  RCTCamera
//
//  Created by Joao Guilherme Daros Fidelis on 19/01/18.
//

#import "RNCameraUtils.h"

@implementation RNCameraUtils

# pragma mark - Camera utilities

+ (AVCaptureDevice *)deviceWithMediaType:(AVMediaType)mediaType preferringPosition:(AVCaptureDevicePosition)position
{
    NSArray *devices = [AVCaptureDevice devicesWithMediaType:mediaType];
    AVCaptureDevice *captureDevice = [devices firstObject];
    
    for (AVCaptureDevice *device in devices) {
        if ([device position] == position) {
            captureDevice = device;
            break;
        }
    }
    
    return captureDevice;
}

+ (AVCaptureDevice *)deviceWithCameraId:(NSString *)cameraId
{
    AVCaptureDevice *device = [AVCaptureDevice deviceWithUniqueID:cameraId];
    return device;
}

# pragma mark - Enum conversion

+ (AVCaptureVideoOrientation)videoOrientationForInterfaceOrientation:(UIInterfaceOrientation)orientation
{
    switch (orientation) {
        case UIInterfaceOrientationPortrait:
            return AVCaptureVideoOrientationPortrait;
        case UIInterfaceOrientationPortraitUpsideDown:
            return AVCaptureVideoOrientationPortraitUpsideDown;
        case UIInterfaceOrientationLandscapeRight:
            return AVCaptureVideoOrientationLandscapeRight;
        case UIInterfaceOrientationLandscapeLeft:
            return AVCaptureVideoOrientationLandscapeLeft;
        default:
            return 0;
    }
}

+ (AVCaptureVideoOrientation)videoOrientationForDeviceOrientation:(UIDeviceOrientation)orientation
{
    switch (orientation) {
        case UIDeviceOrientationPortrait:
            return AVCaptureVideoOrientationPortrait;
        case UIDeviceOrientationPortraitUpsideDown:
            return AVCaptureVideoOrientationPortraitUpsideDown;
        case UIDeviceOrientationLandscapeLeft:
            return AVCaptureVideoOrientationLandscapeRight;
        case UIDeviceOrientationLandscapeRight:
            return AVCaptureVideoOrientationLandscapeLeft;
        default:
            return AVCaptureVideoOrientationPortrait;
    }
}

+ (float)temperatureForWhiteBalance:(RNCameraWhiteBalance)whiteBalance
{
    switch (whiteBalance) {
        case RNCameraWhiteBalanceSunny: default:
            return 5200;
        case RNCameraWhiteBalanceCloudy:
            return 6000;
        case RNCameraWhiteBalanceShadow:
            return 7000;
        case RNCameraWhiteBalanceIncandescent:
            return 3000;
        case RNCameraWhiteBalanceFluorescent:
            return 4200;
    }
}

+ (NSString *)captureSessionPresetForVideoResolution:(RNCameraVideoResolution)resolution
{
    switch (resolution) {
        case RNCameraVideo2160p:
            return AVCaptureSessionPreset3840x2160;
        case RNCameraVideo1080p:
            return AVCaptureSessionPreset1920x1080;
        case RNCameraVideo720p:
            return AVCaptureSessionPreset1280x720;
        case RNCameraVideo4x3:
            return AVCaptureSessionPreset640x480;
        case RNCameraVideo288p:
            return AVCaptureSessionPreset352x288;
        default:
            return AVCaptureSessionPresetHigh;
    }
}
        // --------------------------------------  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  check this
        // todo: how to convert to gray image???
+ (UIImage *)convertBufferToUIImage:(CMSampleBufferRef)sampleBuffer previewSize:(CGSize)previewSize position:(NSInteger)position
{
// ================================================  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        RCTLogInfo(@"RNCameraUtils > convertBufferToUIImage ");  //only warn or error get response from react log.
// ================================================  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ 

    CVImageBufferRef imageBuffer = CMSampleBufferGetImageBuffer(sampleBuffer);
    CIImage *ciImage = [CIImage imageWithCVPixelBuffer:imageBuffer];
    // set correct orientation
    __block UIInterfaceOrientation orientation;
    dispatch_sync(dispatch_get_main_queue(), ^{
        orientation = [[UIApplication sharedApplication] statusBarOrientation];
    });
    UIInterfaceOrientation curOrientation = orientation;
    NSInteger orientationToApply = 1;
    BOOL isBackCamera = position == 1;
    if (curOrientation == UIInterfaceOrientationLandscapeLeft){
        orientationToApply = isBackCamera ? kCGImagePropertyOrientationDown : kCGImagePropertyOrientationUpMirrored;
    } else if (curOrientation == UIInterfaceOrientationLandscapeRight){
        orientationToApply = isBackCamera ? kCGImagePropertyOrientationUp  : kCGImagePropertyOrientationDownMirrored;
    } else if (curOrientation == UIInterfaceOrientationPortrait){
        orientationToApply = isBackCamera ? kCGImagePropertyOrientationRight : kCGImagePropertyOrientationLeftMirrored;
    } else if (curOrientation == UIInterfaceOrientationPortraitUpsideDown){
        orientationToApply = isBackCamera ? kCGImagePropertyOrientationLeft : kCGImagePropertyOrientationRightMirrored;
    }
    ciImage = [ciImage imageByApplyingOrientation:orientationToApply];

    // scale down CIImage
    float bufferWidth = CVPixelBufferGetWidth(imageBuffer);
    float bufferHeight = CVPixelBufferGetHeight(imageBuffer);
    float scale = scale = bufferHeight>bufferWidth ? 720 / bufferWidth : 720 / bufferHeight;;
    if (position == 1) {
        scale = bufferHeight>bufferWidth ? 400 / bufferWidth : 400 / bufferHeight;
    }
    CIFilter* scaleFilter = [CIFilter filterWithName:@"CILanczosScaleTransform"];
    [scaleFilter setValue:ciImage forKey:kCIInputImageKey];
    [scaleFilter setValue:@(scale) forKey:kCIInputScaleKey];
    [scaleFilter setValue:@(1) forKey:kCIInputAspectRatioKey];
    ciImage = scaleFilter.outputImage;

    // convert to UIImage and crop to preview aspect ratio
    NSDictionary *contextOptions = @{kCIContextUseSoftwareRenderer : @(false)};
    CIContext *temporaryContext = [CIContext contextWithOptions:contextOptions];
    CGImageRef videoImage;
    CGRect boundingRect;
    if (curOrientation == UIInterfaceOrientationLandscapeLeft || curOrientation == UIInterfaceOrientationLandscapeRight) {
        boundingRect = CGRectMake(0, 0, bufferWidth*scale, bufferHeight*scale);
    } else {
        boundingRect = CGRectMake(0, 0, bufferHeight*scale, bufferWidth*scale);
    }
    videoImage = [temporaryContext createCGImage:ciImage fromRect:boundingRect];
    CGRect croppedSize = AVMakeRectWithAspectRatioInsideRect(previewSize, boundingRect);
    CGImageRef croppedCGImage = CGImageCreateWithImageInRect(videoImage, croppedSize);
    UIImage *image = [[UIImage alloc] initWithCGImage:videoImage];
    // ================================================  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        RCTLogInfo(@"RNCameraUtils > convertBufferToUIImage imageinfo: width=%f height=%f,  scale=%f",image.size.width,image.size.height,image.scale);
// ================================================  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ 
    CGImageRelease(videoImage);
    CGImageRelease(croppedCGImage);
    return image;
}


+ (UIImage *)convertImageToGrayScale:(UIImage *)image
{
  // Create image rectangle with current image width/height
  CGRect imageRect = CGRectMake(0, 0, image.size.width, image.size.height);
 
  // Grayscale color space
  CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceGray();
 
  // Create bitmap content with current image size and grayscale colorspace
  CGContextRef context = CGBitmapContextCreate(nil, image.size.width, image.size.height, 8, 0, colorSpace, kCGImageAlphaNone);
 
  // Draw image into current context, with specified rectangle
  // using previously defined context (with grayscale colorspace)
  CGContextDrawImage(context, imageRect, [image CGImage]);
 
  // Create bitmap image info from pixel data in current context
  CGImageRef imageRef = CGBitmapContextCreateImage(context);
 
  // Create a new UIImage object  
  UIImage *newImage = [UIImage imageWithCGImage:imageRef];
 
  // Release colorspace, context and bitmap information
  CGColorSpaceRelease(colorSpace);
  CGContextRelease(context);
  CFRelease(imageRef);
 
  // Return the new grayscale image
  return newImage;
}

// - (UIImage*)toGrayscale:(UIImage *)image {  
//     // Create image rectangle with current image width/height.  
//     CGRect imageRect = CGRectMake (0, 0, image.size.width * image.scale, image.size.height * image.scale);  
//     NSInteger width = imageRect.size.width;
//     NSInteger height = imageRect.size.height;  
//     // The pixels will be painted to this array.
//     uint32_t* pixels = (uint32_t*)malloc (width * height * sizeof (uint32_t));  
//     // Clear the pixels so any transparency is preserved.  
//     memset (pixels, 0, width * height * sizeof (uint32_t));
//     // Create a context with RGBA pixels.  
//     CGContextRef context = CGBitmapContextCreate (pixels, width, height, BITS_PER_SAMPLE, width * sizeof (uint32_t),
//     CGColorSpaceCreateDeviceRGB (), kCGBitmapByteOrder32Little | kCGImageAlphaPremultipliedLast);  
//     // Paint the bitmap to our context which will fill in the pixels array.  
//     CGContextDrawImage (context, CGRectMake (0, 0, width, height), [image CGImage]);
//     for (NSInteger y = 0; y < height; y++) {
//         for (NSInteger x = 0; x < width; x++) {
//            uint8_t* rgbaPixel = (uint8_t*)(&PIXEL (x, y));
//            // Convert to grayscale using recommended method.
//            // http://en.wikipedia.org/wiki/Grayscale#Converting_color_to_grayscale
//            uint8_t gray = (uint8_t)((30 * rgbaPixel[RED] + 59 * rgbaPixel[GREEN] + 11 * rgbaPixel[BLUE]) / 100);
//            // set the pixels to gray
//            rgbaPixel[RED] = gray;
//            rgbaPixel[GREEN] = gray;
//            rgbaPixel[BLUE] = gray;
//         }
//     }
//     // Create a new CGImageRef from our context with the modified pixels.  
//     CGImageRef image = CGBitmapContextCreateImage (context);   
//     // We're done with the context, color space, and pixels.
//     CGContextRelease (context);
//     free (pixels);  
//     // Make a new UIImage to return.  
//     UIImage* resultUIImage = [UIImage imageWithCGImage:image scale:self.scale orientation:UIImageOrientationUp];   
//     // We're done with image now too.
//     CGImageRelease (image);
//     return resultUIImage;
// }
@end
// import Accelerate
// class ViewController: UIViewController {
    
//     @IBOutlet var imageView: UIImageView!
    
//     /*
//      The Core Graphics image representation of the source asset.
//      */
//     let cgImage: CGImage = {
//         guard let cgImage = #imageLiteral(resourceName: "Food_4.JPG").cgImage else {
//             fatalError("Unable to get CGImage")
//         }
        
//         return cgImage
//     }()
    
//     /*
//      The format of the source asset.
//      */
//     lazy var format: vImage_CGImageFormat = {
//         guard
//             let format = vImage_CGImageFormat(cgImage: cgImage) else {
//                 fatalError("Unable to create format.")
//         }
        
//         return format
//     }()
    
//     /*
//      The vImage buffer containing a scaled down copy of the source asset.
//      */
//     lazy var sourceBuffer: vImage_Buffer = {
//         guard
//             var sourceImageBuffer = try? vImage_Buffer(cgImage: cgImage,
//                                                        format: format),
            
//             var scaledBuffer = try? vImage_Buffer(width: Int(sourceImageBuffer.height / 3),
//                                                   height: Int(sourceImageBuffer.width / 3),
//                                                   bitsPerPixel: format.bitsPerPixel) else {
//                                                     fatalError("Unable to create source buffers.")
//         }
        
//         defer {
//             sourceImageBuffer.free()
//         }
        
//         vImageScale_ARGB8888(&sourceImageBuffer,
//                              &scaledBuffer,
//                              nil,
//                              vImage_Flags(kvImageNoFlags))
        
//         return scaledBuffer
//     }()
    
//     /*
//      The 1-channel, 8-bit vImage buffer used as the operation destination.
//      */
//     lazy var destinationBuffer: vImage_Buffer = {
//         guard var destinationBuffer = try? vImage_Buffer(width: Int(sourceBuffer.width),
//                                               height: Int(sourceBuffer.height),
//                                               bitsPerPixel: 8) else {
//                                                 fatalError("Unable to create destination buffers.")
//         }
        
//         return destinationBuffer
//     }()
    
//     override func viewDidLoad() {
//         super.viewDidLoad()
        
//         // Declare the three coefficients that model the eye's sensitivity
//         // to color.
//         let redCoefficient: Float = 0.2126
//         let greenCoefficient: Float = 0.7152
//         let blueCoefficient: Float = 0.0722
        
//         // Create a 1D matrix containing the three luma coefficients that
//         // specify the color-to-grayscale conversion.
//         let divisor: Int32 = 0x1000
//         let fDivisor = Float(divisor)
        
//         var coefficientsMatrix = [
//             Int16(redCoefficient * fDivisor),
//             Int16(greenCoefficient * fDivisor),
//             Int16(blueCoefficient * fDivisor)
//         ]
        
//         // Use the matrix of coefficients to compute the scalar luminance by
//         // returning the dot product of each RGB pixel and the coefficients
//         // matrix.
//         let preBias: [Int16] = [0, 0, 0, 0]
//         let postBias: Int32 = 0
        
//         vImageMatrixMultiply_ARGB8888ToPlanar8(&sourceBuffer,
//                                                &destinationBuffer,
//                                                &coefficientsMatrix,
//                                                divisor,
//                                                preBias,
//                                                postBias,
//                                                vImage_Flags(kvImageNoFlags))
        
//         // Create a 1-channel, 8-bit grayscale format that's used to
//         // generate a displayable image.
//         guard let monoFormat = vImage_CGImageFormat(
//             bitsPerComponent: 8,
//             bitsPerPixel: 8,
//             colorSpace: CGColorSpaceCreateDeviceGray(),
//             bitmapInfo: CGBitmapInfo(rawValue: CGImageAlphaInfo.none.rawValue),
//             renderingIntent: .defaultIntent) else {
//                 return
//         }
        
//         // Create a Core Graphics image from the grayscale destination buffer.
//         let result = try? destinationBuffer.createCGImage(format: monoFormat)
        
//         // Display the grayscale result.
//         if let result = result {
//             imageView.image = UIImage(cgImage: result)
//         }
//     }
// }
