//
//  PoseEstimator.m
//  react-native-camera
//
//  Created by Kasper Dissing Bargsteen on 16/04/2019.
//

#import "PoseEstimator.h"
#import <FirebaseMLCommon/FIRLocalModelSource.h>
#import <FirebaseMLCommon/FIRModelManager.h>
#import <FirebaseMLModelInterpreter/FIRModelOptions.h>
#import <FirebaseMLModelInterpreter/FirebaseMLModelInterpreter.h>
#import <FirebaseMLModelInterpreter/FIRModelInputOutputOptions.h>
#import "UIImage+SimpleResize.h"

@interface PoseEstimator ()

@property(nonatomic, assign) CGContextRef context;
@property NSMutableData *inputData;
@property (nonatomic, strong) FIRModelInterpreter *interpreter;
@property (nonatomic, strong) FIRModelInputOutputOptions *ioOptions;

@end

@implementation PoseEstimator

- (instancetype)init
{
    if (self = [super init]) {
        [self setupModelForPoseEstimation];
    }
    return self;
}

- (BOOL)isRealDetector{
    return true;
}

const int IN_DIM = 192;
const int OUT_DIM = 96;
const size_t FL32SIZE = 4;
const int COLORS_PER_PIXEL = 3;

- (void)setupModelForPoseEstimation{
    
    // Load model
    NSString *modelPath = [NSBundle.mainBundle pathForResource:@"model-cpm-192"
                                                        ofType:@"tflite"];
    FIRLocalModelSource *localModelSource = [[FIRLocalModelSource alloc] initWithName:@"my_local_model"
                                                                                 path:modelPath];
    
    [[FIRModelManager modelManager] registerLocalModelSource:localModelSource];
    
    
    // Create interpreter from model
    FIRModelOptions *options = [[FIRModelOptions alloc] initWithCloudModelName:nil
                                                                localModelName:@"my_local_model"];
    _interpreter = [FIRModelInterpreter modelInterpreterWithOptions:options];
    
    
    // Specify input and output sizes
    _ioOptions = [[FIRModelInputOutputOptions alloc] init];
    NSError *error;
    [_ioOptions setInputFormatForIndex:0
                                      type:FIRModelElementTypeFloat32
                                dimensions:@[@1, @192, @192, @3]
                                     error:&error];
    if (error != nil) { return; }
    [_ioOptions setOutputFormatForIndex:0
                                       type:FIRModelElementTypeFloat32
                                 dimensions:@[@1, @96, @96, @14]
                                      error:&error];
    if (error != nil) { return; }
}



- (void)estimatePoseOnDeviceInImage:(UIImage *)uiImage completed: (void (^)(NSArray * result)) completed
{
    // Used for testing the static image. Uncomment, and change the resizedImage to use staticImage
    
    //    UIImage *staticImage = [UIImage imageNamed:@"dancer"
    //                                inBundle:[NSBundle bundleForClass:self.class]
    //                  compatibleWithTraitCollection:nil];
    
    //                                                       Half size, as it draws from center
   
    UIImage *resizedImage = [uiImage imageByScalingToFillSize:CGSizeMake(IN_DIM/2, IN_DIM/2)];
    CGImageRef cgImage = resizedImage.CGImage;
    
    long imageWidth = CGImageGetWidth(cgImage);
    long imageHeight = CGImageGetHeight(cgImage);
    
    if(_context == nil){
        _context = CGBitmapContextCreate(nil,
                                         imageWidth, imageHeight,
                                         8,
                                         imageWidth * FL32SIZE,
                                         CGColorSpaceCreateDeviceRGB(),
                                         kCGImageAlphaNoneSkipFirst);
        _inputData = [[NSMutableData alloc] initWithCapacity:IN_DIM*IN_DIM*COLORS_PER_PIXEL*FL32SIZE];
    }
    CGContextDrawImage(_context, CGRectMake(0, 0, imageWidth, imageHeight), cgImage);
    UInt8 *imageData = CGBitmapContextGetData(_context);
    
    uiImage = nil;
    resizedImage = nil;
    cgImage = nil;
    
    FIRModelInputs *inputs = [[FIRModelInputs alloc] init];
    
    // Input and output should be COLUMN Major
    for (int col = 0; col < IN_DIM; col++) {
        for (int row = 0; row < IN_DIM; row++) {
            long offset = FL32SIZE * (col * imageWidth + row);

            Float32 red = imageData[offset+1];
            Float32 green = imageData[offset+2];
            Float32 blue = imageData[offset+3];
            
            long outOffset = FL32SIZE * COLORS_PER_PIXEL * (col * IN_DIM + row);
            
            [_inputData replaceBytesInRange:NSMakeRange(outOffset, FL32SIZE) withBytes:&red];
            [_inputData replaceBytesInRange:NSMakeRange(outOffset + 1 * FL32SIZE, FL32SIZE) withBytes:&green];
            [_inputData replaceBytesInRange:NSMakeRange(outOffset + 2 * FL32SIZE, FL32SIZE) withBytes:&blue];
        }
    }
    NSError *error;
    
    [inputs addInput:_inputData error:&error];
    if (error != nil) { return; }
    
    [_interpreter runWithInputs:inputs
                            options:_ioOptions
                         completion:^(FIRModelOutputs * _Nullable outputs,
                                      NSError * _Nullable error) {
                             if (error != nil || outputs == nil) {
                                 NSLog(@"%@", error);
                                 return;
                             }
                             NSError *outputError;
                             
                             // Outputs has 4 dimensions, outer-most is each interpretation, and we only run it on one input
                             // therefore we return the inner three dimensions.
                             NSArray *heatmap = [outputs outputAtIndex:0 error:&outputError];
                             completed(heatmap);
                         }];
}

@end
