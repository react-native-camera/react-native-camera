//
//  MyModel.m
//  RNCamera
//
//  Created by Donbosco on 10/12/20.
//


#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventDispatcher.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTLog.h>
#import "MyModel.h"
#import "RNImageUtils.h"
#import "RNFileSystem.h"
#import "RNCameraUtils.h"
#import "TFLTensorFlowLite.h"
#import "FaceDetectorManagerMlkit.h"
@import TensorFlowLite;

@implementation MyModel
// init with model file added to resource bundle
- (instancetype)init 
{
  if (self = [super init]) {
    NSString *modelPath = [[NSBundle mainBundle] pathForResource:@"mymodel"
                                                      ofType:@"tflite"];                                                
    NSError *error;
      // Initialize an interpreter with the model.
    TFLInterpreter *myinterpreter = [[TFLInterpreter alloc] initWithModelPath:modelPath error:&error];
                      
    if (error != nil) { 
    /* Error handling... */ 
      // RCTLogWarn(@"MyModel > init error ...");
      
    }
    self.interpreter = myinterpreter;
  }
        // RCTLogInfo(@"MyModel > init finish"); 
  return self;
}

// init model with provided path
- (instancetype)initWithPath :(NSString *)modelPath
{
  if (self = [super init]) {                                               
    NSError *error;
      // Initialize an interpreter with the model.
    TFLInterpreter *myinterpreter = [[TFLInterpreter alloc] initWithModelPath:modelPath error:&error];
                      
    if (error != nil) { 
    /* Error handling... */ 
      // RCTLogWarn(@"MyModel > init error ...");
    }
    // RCTLogInfo(@"MyModel > init success with %@",modelPath);
    self.interpreter = myinterpreter;
  }
        // RCTLogInfo(@"MyModel > init finish"); 
  return self;
}

- (float)runModelWithFrame:(UIImage *)uiImage
                  scaleX:(float)scaleX
                  scaleY:(float)scaleY
               
{
  CGImageRef imageRef = [uiImage CGImage];
  NSUInteger width = CGImageGetWidth(imageRef);
  NSUInteger height = CGImageGetHeight(imageRef);
  NSData *imageData = UIImagePNGRepresentation(uiImage);
  // NSLog(@"runModelWithFrame > image data length : %d, width=%d, height=%d, scale=%f x %f", 
  //       [imageData length],width,height,scaleX,scaleY );
  // RCTLogInfo(@"MyModel > runModelWithFrame...");  
  NSError *error;
  // Allocate memory for the model's input `TFLTensor`s.
  [self.interpreter allocateTensorsWithError:&error];
  if (error != nil) { 
    /* Error handling... */ 
    // RCTLogWarn(@"MyModel > runModelWithFrame > allocateTensorsWithError : error ...%@",error);
    return   404;
  }
  // preprocess input
  // RCTLogInfo(@"MyModel > runModelWithFrame > imagePreProcess...");
  NSData *inputData = [RNImageUtils getArrayOfImage:uiImage];
  // RCTLogInfo(@"MyModel > runModelWithFrame > originalImageData...");
  UIImage * originalImage = [UIImage imageNamed:@"true_img.png"];
  // UIImage * originalImage = [UIImage imageNamed:@"fake_img.png"];
  NSData *originData = [self preprocessImage:originalImage];

  // Copy the input data to the input `TFLTensor`.
  [[self.interpreter  inputTensorAtIndex:0 error:&error] copyData:inputData error:&error];
  if (error != nil) { 
    /* Error handling... */ 
    //  RCTLogInfo(@"MyModel > runModelWithFrame > copy input 1 data error: %@",error);
     return   404;
  }
   [[self.interpreter  inputTensorAtIndex:1 error:&error] copyData:originData error:&error];
  if (error != nil) { 
    /* Error handling... */ 
    //  RCTLogInfo(@"MyModel > runModelWithFrame > copy input 2 data error: %@",error);
     return   404;
  }
  // Run inference by invoking the `TFLInterpreter`.
  [self.interpreter invokeWithError:&error];
  if (error != nil) { 
    /* Error handling... */ 
    //  RCTLogInfo(@"MyModel > runModelWithFrame > invokeWithError : error : %@",error);
     return   404;
  }

  // Get the output `TFLTensor`
  TFLTensor *outputTensor = [self.interpreter outputTensorAtIndex:0 error:&error];
  if (error != nil) { 
    /* Error handling... */ 
        // RCTLogInfo(@"MyModel > runModelWithFrame > outputTensor error : %@",error);
        return   404;
  }

  // Copy output to `NSData` to process the inference results.
  NSData *outputData = [outputTensor dataWithError:&error];
  // RCTLogInfo(@"MyModel > runModelWithFrame > outputData  ...%@",outputData);
  if (error != nil) { 
    /* Error handling... */ 
    //  RCTLogInfo(@"MyModel > runModelWithFrame > get ouput data error : %@",error);
     return   404;
  }
  float *p = (float*)[outputData bytes];   // -bytes returns a void* that points to the data
  float f = *p;
  // RCTLogInfo(@"MyModel > runModelWithFrame > result = %f",f);
  return f; 
}

- (float)runModelWithFrame:(UIImage *)uiImage scaleX:(float)scaleX scaleY:(float)scaleY faces:(NSDictionary *)eventFace             
{
    CGImageRef imageRef = [uiImage CGImage];
    NSUInteger width = CGImageGetWidth(imageRef);
    NSUInteger height = CGImageGetHeight(imageRef);
    NSData *imageData = UIImagePNGRepresentation(uiImage);
    // NSLog(@"runModelWithFrame > image data length : %d, width=%d, height=%d, scale=%f x %f", 
    //       [imageData length],width,height,scaleX,scaleY );
    if ([eventFace[@"faces"] count] < 1) {
      NSLog(@"runModelWithFrame > emptyface");
      return   404;
    }
    else {
      // get the first face to run model
      NSDictionary *firstFace = eventFace[@"faces"][0] ;
      int faceX = (int) [[[firstFace valueForKeyPath:@"bounds.origin"] objectForKey:@"x"] floatValue];
      int faceY = (int) [[[firstFace valueForKeyPath:@"bounds.origin"] objectForKey:@"y"] floatValue];
      int faceWidth = (int) [[[firstFace valueForKeyPath:@"bounds.size"] objectForKey:@"width"] floatValue];
      int faceHeight = (int) [[[firstFace valueForKeyPath:@"bounds.size"] objectForKey:@"height"] floatValue];
      // NSLog(@"runModelWithFrame > first face: x:y:w:h  %d x %d ; %d x %d", faceX,faceY,faceWidth,faceHeight);
      // preprocess: cut face, scale face, 
      int maxLength = faceHeight;
      if (faceHeight < faceWidth) {
        maxLength = faceWidth;
      }
      uiImage = [RNImageUtils cropImage:uiImage toRect:CGRectMake(faceX, faceY, maxLength , maxLength)];
      // NSLog(@"runModelWithFrame > crop image to face width=%d, height=%d", 
            // CGImageGetWidth([uiImage CGImage]),CGImageGetHeight([uiImage CGImage]));
      uiImage = [RNImageUtils scaleImage:uiImage convertToSize:CGSizeMake(112, 112) ];
      NSData *imageData = UIImagePNGRepresentation(uiImage);
      // NSLog(@"runModelWithFrame > scaled image data length = %d, width=%d, height=%d", 
      //       [imageData length],CGImageGetWidth([uiImage CGImage]),CGImageGetHeight([uiImage CGImage]));
      // [RNImageUtils rawDataDrawWithImage:uiImage ];
      // get the image array data 
      // NSData * imageGrayArrayData = [RNImageUtils getArrayOfImage:uiImage] ;
    }
    NSError *error;
    [self.interpreter allocateTensorsWithError:&error];
    if (error != nil) { 
    /* Error handling... */ 
      // RCTLogInfo(@"MyModel > runModelWithFrame > allocateTensorsWithError : error ...%@",error);
      return   404;
    }

    // RCTLogInfo(@"MyModel > runModelWithFrame > imagePreProcess...");
    NSData *inputData = [RNImageUtils getArrayOfImage:uiImage];
    // RCTLogInfo(@"MyModel > runModelWithFrame > originalImageData...");
    UIImage * originalImage = [UIImage imageNamed:@"true_img.png"];
    // UIImage * originalImage = [UIImage imageNamed:@"fake_img.png"];
    NSData *originData = [self preprocessImage:originalImage];

  

  // Copy the input data to the input `TFLTensor`.
    [[self.interpreter  inputTensorAtIndex:0 error:&error] copyData:inputData error:&error];
    if (error != nil) { 
    /* Error handling... */ 
      // RCTLogInfo(@"MyModel > runModelWithFrame > copyData : error ...%@",error);
      return   404;
    }
    [[self.interpreter  inputTensorAtIndex:1 error:&error] copyData:originData error:&error];
    if (error != nil) { 
    /* Error handling... */ 
      // RCTLogInfo(@"MyModel > runModelWithFrame > copyData : error ...%@",error);
      return   404;
    }
  // Run inference by invoking the `TFLInterpreter`.
    [self.interpreter invokeWithError:&error];
    if (error != nil) { 
    /* Error handling... */ 
      // RCTLogInfo(@"MyModel > runModelWithFrame > invokeWithError : error ...%@",error);
      return   404;
    }

  // Get the output `TFLTensor`
    TFLTensor *outputTensor = [self.interpreter outputTensorAtIndex:0 error:&error];
    if (error != nil) { 
    /* Error handling... */ 
      // RCTLogInfo(@"MyModel > runModelWithFrame > outputTensorAtIndex : error ...%@",error);
      return   404;
    }

  // Copy output to `NSData` to process the inference results.
    NSData *outputData = [outputTensor dataWithError:&error];
    // RCTLogInfo(@"MyModel > runModelWithFrame > outputData  ...%@",outputData);
    if (error != nil) { 
    /* Error handling... */ 
      // RCTLogInfo(@"MyModel > runModelWithFrame > dataWithError : error ...%@",error);
      return   404;
    }
    float *p = (float*)[outputData bytes];   // -bytes returns a void* that points to the data
    float f = *p;
    // RCTLogInfo(@"MyModel > runModelWithFrame > outputData  ...%f",f);
    return f;
}

// callback version
- (void)verifyFacesInFrame:(UIImage *)uiImage
              scaleX:(float)scaleX
              scaleY:(float)scaleY
              faces:(NSDictionary *)eventFace
              identity:(NSString *)identityFileName
              identityFolder:(NSString *)identityFolderName
              completed:(void (^)(float result))completed {
    NSData *inputData;
    NSData *originData;
    if ([eventFace[@"faces"] count] < 1) {
      NSLog(@"verifyFacesInFrame > emptyface");
      completed(  404);
      return;
    }
    else {
      inputData = [self preprocessFrameImage:uiImage faces:eventFace];
      // RCTLogInfo(@"MyModel > runModelWithFrame > originalImageData...");
      NSString *originalImagePath = [RNFileSystem documentDirectoryPath];
      originalImagePath = [originalImagePath stringByAppendingPathComponent:identityFolderName];
      originalImagePath = [originalImagePath stringByAppendingPathComponent:identityFileName];
      originData = [self preprocessImage:[RNImageUtils loadImage:originalImagePath]];
    }
    NSError *error;
    [self.interpreter allocateTensorsWithError:&error];
    if (error != nil) { 
    /* Error handling... */ 
      // RCTLogInfo(@"MyModel > runModelWithFrame > allocateTensorsWithError : error ...%@",error);
      completed(  404);
      return;
    }


  // Copy the input data to the input `TFLTensor`.
    [[self.interpreter  inputTensorAtIndex:0 error:&error] copyData:inputData error:&error];
    if (error != nil) { 
    /* Error handling... */ 
      // RCTLogInfo(@"MyModel > runModelWithFrame > input 1 error : %@",error);
      completed(  404);
      return;
    }
    [[self.interpreter  inputTensorAtIndex:1 error:&error] copyData:originData error:&error];
    if (error != nil) { 
    /* Error handling... */ 
      // RCTLogInfo(@"MyModel > runModelWithFrame > input 2 error : %@",error);
      completed(  404);
      return;
    }
  // Run inference by invoking the `TFLInterpreter`.
    [self.interpreter invokeWithError:&error];
    if (error != nil) { 
    /* Error handling... */ 
      // RCTLogInfo(@"MyModel > runModelWithFrame > invokeWithError : error ...%@",error);
      completed(  404);
      return;
    }

  // Get the output `TFLTensor`
    TFLTensor *outputTensor = [self.interpreter outputTensorAtIndex:0 error:&error];
    if (error != nil) { 
    /* Error handling... */ 
      // RCTLogInfo(@"MyModel > runModelWithFrame > outputTensorAtIndex : error ...%@",error);
      completed(  404);
      return;
    }

  // Copy output to `NSData` to process the inference results.
    NSData *outputData = [outputTensor dataWithError:&error];
    // RCTLogInfo(@"MyModel > runModelWithFrame > outputData  ...%@",outputData);
    if (error != nil) { 
    /* Error handling... */ 
      // RCTLogInfo(@"MyModel > runModelWithFrame > output error : %@",error);
      completed(  404);
      return;
    }
    float *p = (float*)[outputData bytes];   // -bytes returns a void* that points to the data
    float f = *p;
    // RCTLogInfo(@"MyModel > runModelWithFrame > result = %f",f);
    completed(f);
  }


- (NSData *)preprocessFrameImage:(UIImage *)uiImage  
                  // scaleX:(float)scaleX 
                  // scaleY:(float)scaleY 
                  faces:(NSDictionary *)eventFace                 
{
    NSData *imageData = UIImagePNGRepresentation(uiImage);
    if ([eventFace[@"faces"] count] < 1) {
      NSLog(@"MyModel > preprocessFrameImage emptyface");
      return [self randomData];
    }
    else {
      NSDictionary *firstFace = eventFace[@"faces"][0] ;
      // NSLog(@"runModelWithFrame > first face: %@", firstFace);
      int faceX = (int) [[[firstFace valueForKeyPath:@"bounds.origin"] objectForKey:@"x"] floatValue];
      int faceY = (int) [[[firstFace valueForKeyPath:@"bounds.origin"] objectForKey:@"y"] floatValue];
      int faceWidth = (int) [[[firstFace valueForKeyPath:@"bounds.size"] objectForKey:@"width"] floatValue];
      int faceHeight = (int) [[[firstFace valueForKeyPath:@"bounds.size"] objectForKey:@"height"] floatValue];
      // NSLog(@"runModelWithFrame > first face: x:y:w:h  %d x %d ; %d x %d", faceX,faceY,faceWidth,faceHeight);
      int maxLength = faceHeight;
      if (faceHeight < faceWidth) {
        maxLength = faceWidth;
      }
      uiImage = [RNImageUtils cropImage:uiImage toRect:CGRectMake(faceX, faceY, maxLength , maxLength)];
      uiImage = [RNImageUtils scaleImage:uiImage convertToSize:CGSizeMake(112, 112) ];
      // get the image array data 
      NSData * imageGrayArrayData = [RNImageUtils getArrayOfImage:uiImage] ;
      return imageGrayArrayData;
    }
}


- (NSData *)preprocessImage:(UIImage *)uiImage               
{

    uiImage = [RNImageUtils scaleImage:uiImage convertToSize:CGSizeMake(112, 112) ];
    NSData * imageGrayArrayData = [RNImageUtils getArrayOfImage:uiImage] ;
    return imageGrayArrayData;
}

- (NSData *) randomData{
    // int maxLength = 41216; //92*112*4-bytes; float	4 byte
  int maxLength = 50176; //112*112*4-bytes; float	4 byte
  NSMutableData *originalImageData = [[NSMutableData alloc] initWithLength:maxLength] ; 
  NSData * result = originalImageData;
  return result;
}




- (BOOL)isRealVerifier 
{
  return true;
}
@end
