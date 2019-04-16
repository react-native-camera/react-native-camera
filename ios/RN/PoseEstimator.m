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

- (void)setupModelForPoseEstimation{
    
    // Load model
    NSString *modelPath = [NSBundle.mainBundle pathForResource:@"mv2-cpm-224"
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
                                dimensions:@[@1, @224, @224, @3]
                                     error:&error];
    if (error != nil) { return; }
    [_ioOptions setOutputFormatForIndex:0
                                       type:FIRModelElementTypeFloat32
                                 dimensions:@[@1, @112, @112, @14]
                                      error:&error];
    if (error != nil) { return; }
}

- (void)estimatePoseOnDeviceInImage:(CGImageRef)image completed: (void (^)(NSArray * result)) completed
{
    long imageWidth = CGImageGetWidth(image);
    long imageHeight = CGImageGetHeight(image);
    
    if(_context == nil){
        _context = CGBitmapContextCreate(nil,
                                        imageWidth, imageHeight,
                                        8,
                                        imageWidth * 4,
                                        CGColorSpaceCreateDeviceRGB(),
                                        kCGImageAlphaNoneSkipFirst);
        _inputData = [[NSMutableData alloc] initWithCapacity:224*224*3*4]; // 224 * 244 * 3 * 4 = 602112
    }
    CGContextDrawImage(_context, CGRectMake(0, 0, imageWidth, imageHeight), image);
    UInt8 *imageData = CGBitmapContextGetData(_context);
    
    FIRModelInputs *inputs = [[FIRModelInputs alloc] init];
    
    //NSArray *inputData = [NSArray with]
    
    for (int row = 0; row < 224; row++) {
        for (int col = 0; col < 224; col++) {
            long offset = 4 * (col * imageWidth + row);
            // Normalize channel values to [0.0, 1.0]. This requirement varies
            // by model. For example, some models might require values to be
            // normalized to the range [-1.0, 1.0] instead, and others might
            // require fixed-point values or the original bytes.
            // (Ignore offset 0, the unused alpha channel)
            Float32 red = imageData[offset+1] / 255.0f;
            Float32 green = imageData[offset+2] / 255.0f;
            Float32 blue = imageData[offset+3] / 255.0f;
            
            long outOffset = 4 * 3 * (col + row * 224);
            
            [_inputData replaceBytesInRange:NSMakeRange(outOffset, sizeof(red)) withBytes:&red];
            [_inputData replaceBytesInRange:NSMakeRange(outOffset+1*4, sizeof(green)) withBytes:&green];
            [_inputData replaceBytesInRange:NSMakeRange(outOffset+2*4, sizeof(blue)) withBytes:&blue];
            
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
                             NSArray *heatmap = [outputs outputAtIndex:0 error:&outputError][0];
                             completed(heatmap);
                         }];
}

@end
