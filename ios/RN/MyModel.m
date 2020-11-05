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
#import "RNImageUtils.h"
#import "RNCameraUtils.h"
#import "TFLTensorFlowLite.h"
@import TensorFlowLite;

// @interface FaceDetectorManagerMlkit ()
// @property(nonatomic, strong) FIRVisionFaceDetector *faceRecognizer;
// @property(nonatomic, strong) FIRVision *vision;
// @property(nonatomic, strong) FIRVisionFaceDetectorOptions *options;
// @property(nonatomic, assign) float scaleX;
// @property(nonatomic, assign) float scaleY;
// @end
@interface MyModel : NSObject
  // typedef void(^postRecognitionBlock)(NSArray *faces);
  @property(nonatomic, strong) TFLInterpreter *interpreter;
  - (instancetype)init;
  -(NSArray *)runModelWithFrame:(UIImage *)image scaleX:(float)scaleX scaleY:(float)scaleY ;

  // -(BOOL)isRealDetector;
  // -(void)setTracking:(id)json queue:(dispatch_queue_t)sessionQueue;
  // -(void)setLandmarksMode:(id)json queue:(dispatch_queue_t)sessionQueue;
  // -(void)setPerformanceMode:(id)json queue:(dispatch_queue_t)sessionQueue;
  // -(void)setClassificationMode:(id)json queue:(dispatch_queue_t)sessionQueue;
  // -(void)findFacesInFrame:(UIImage *)image scaleX:(float)scaleX scaleY:(float)scaleY completed:(postRecognitionBlock)completed;
  // +(NSDictionary *)constants;
@end
@implementation MyModel

// RCT_EXTERN_MODULE(MyModel, NSObject);
// RCT_EXTERN_MODULE(ReactNativeEventEmitter, RCTEventEmitter);

// RCT_EXTERN_METHOD(supportedEvents);
// RCT_EXTERN_METHOD(loadmodel);
////expose the method to get result
// RCT_EXTERN_METHOD(getmodel: (RCTResponseSenderBlock)callback);
// RCT_EXTERN_METHOD(doInterprete);
// RCT_EXTERN_METHOD(testEvent);
// RCT_EXTERN_METHOD(supportedEvents)

// RCT_EXTERN_METHOD(addEvent:(NSString *)name location:(NSString *)location date:(nonnull NSNumber *)date)
// - (dispatch_queue_t)methodQueue
// {
//   return dispatch_queue_create("com.facebook.React.AsyncLocalStorageQueue", DISPATCH_QUEUE_SERIAL);
// }
// RCT_EXPORT_METHOD(doSomethingExpensive:(NSString *)param callback:(RCTResponseSenderBlock)callback)
// {
//   dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
//     // Call long-running code on background thread
 
    
//     NSString *greeting =   @"Welcome, you an administrator.";
// //    ...
//     // You can invoke callback from any thread/queue
//     callback(@[greeting]);
//   });
// }

////{
////  backgroundQueue = dispatch_queue_create("anynameisok", NULL);
////  dispatch_async(backgroundQueue, ^{
////    NSLog(@"processing background");
////    // dispatch a event to the app , through the bridge, with a name, and a body
////    [self.bridge.eventDispatcher sendAppEventWithName:@"backgroundProgress" body:@{@"status": @"Loading"}];
////    // sleep 5 second as if running codes...
////    [NSThread sleepForTimeInterval:5];
////    NSLog(@"slept");
////    // notify system that task completed
////    dispatch_async(dispatch_get_main_queue(), ^{
////      NSLog(@"Done processing; main thread");
////        // dispatch a event to the app , through the bridge, with a name, and a body
////      [self.bridge.eventDispatcher sendAppEventWithName:@"backgroundProgress" body:@{@"status": @"Done"}];
////    });
////  });
////}
//
//
//
// + (BOOL)requiresMainQueueSetup
// {
// //  return YES;  // only do this if your module initialization relies on calling UIKit!
//    return NO;
// }
// @end


//@implementation MyModelManager
//
//RCT_EXPORT_MODULE();
//
//- (NSArray<NSString *> *)supportedEvents
//{
//  return @[@"EventReminder"];
//}
//
//- (void)calendarEventReminderReceived:(NSNotification *)notification
//{
//  NSString *eventName = notification.userInfo[@"name"];
//  [self sendEventWithName:@"EventReminder" body:@{@"name": eventName}];
//}
//
//  NSError *error ;

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
      RCTLogWarn(@"MyModel > init error ...");
      
    }
    self.interpreter = myinterpreter;
    // self.options = [[FIRVisionFaceDetectorOptions alloc] init];
    // self.options.performanceMode = FIRVisionFaceDetectorPerformanceModeFast;
    // self.options.landmarkMode = FIRVisionFaceDetectorLandmarkModeNone;
    // self.options.classificationMode = FIRVisionFaceDetectorClassificationModeNone;
    
    // self.vision = [FIRVision vision];
    // self.faceRecognizer = [_vision faceDetectorWithOptions:_options];
  }
// ================================================  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        // RCTLogInfo(@"MyModel > init finish");  //only warn or error get response from react log.
// ================================================  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  
  return self;
}


  // NSString *modelPath = [[NSBundle mainBundle] pathForResource:@"mymodel"
  //                                                       ofType:@"tflite"];
  // NSError *error;

  // // Initialize an interpreter with the model.
  // TFLInterpreter *interpreter = [[TFLInterpreter alloc] initWithModelPath:modelPath
  //                                                                   error:&error];
  // if (error != nil) { 
  //   /* Error handling... */ 
  //     RCTLogWarn(@"MyModel > init without FirebaseMLVision");
  //   }

  // // Allocate memory for the model's input `TFLTensor`s.
  // [interpreter allocateTensorsWithError:&error];
  // if (error != nil) { /* Error handling... */ }

  // NSMutableData *inputData;  // Should be initialized
  // // input data preparation...

  // // Copy the input data to the input `TFLTensor`.
  // [interpreter copyData:inputData toInputTensorAtIndex:0 error:&error];
  // if (error != nil) { /* Error handling... */ }

  // // Run inference by invoking the `TFLInterpreter`.
  // [interpreter invokeWithError:&error];
  // if (error != nil) { /* Error handling... */ }

  // // Get the output `TFLTensor`
  // TFLTensor *outputTensor = [interpreter outputTensorAtIndex:0 error:&error];
  // if (error != nil) { /* Error handling... */ }

  // // Copy output to `NSData` to process the inference results.
  // NSData *outputData = [outputTensor dataWithError:&amp;error];
  // if (error != nil) { /* Error handling... */ }




  - (NSArray *)runModelWithFrame:(UIImage *)uiImage
                  scaleX:(float)scaleX
                  scaleY:(float)scaleY
               
{
    // self.scaleX = scaleX;
    // self.scaleY = scaleY;

// ================================================  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    // int control = 1;
        CGImageRef imageRef = [uiImage CGImage];
            NSUInteger width = CGImageGetWidth(imageRef);
    NSUInteger height = CGImageGetHeight(imageRef);


        NSData *imageData = UIImagePNGRepresentation(uiImage);
        NSLog(@"runModelWithFrame > image data length : %d, width=%d, height=%d, scale=%f x %f", [imageData length],width,height,scaleX,scaleY );
        
  // [RNImageUtils rawDataCopyWithImage:uiImage];
            

        // RCTLogError(@"image data length : %d", [imageData length] );
        // RCTLogWarn(@"image data length : %d", [imageData length] );
        // RCTLog(@"image data length : %d", [imageData length] ) ;
        // RCTLogTrace(@"image data length : %d", [imageData length] );
        // RCTLogInfo(@"image data length : %d", [imageData length] ) ;
        // RCTLogAdvice(@"advice from bridge", @"image data length : %d", [imageData length] );
        // NSString * base64String = [imageData base64EncodedStringWithOptions:0];
        // NSLog((@"%@", base64String));
        //        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Test Message" 
        //                                           message:@"This is a sample"
        //                                          delegate:nil
        //                                 cancelButtonTitle:@"OK" 
        //                                 otherButtonTitles:nil];
        // [alert show];
// ================================================  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
// ================================================  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        RCTLogInfo(@"MyModel > runModelWithFrame...");  //only warn or error get response from react log.
// ================================================  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  
 NSError *error;
  // Allocate memory for the model's input `TFLTensor`s.
  [self.interpreter allocateTensorsWithError:&error];
  if (error != nil) { 
    /* Error handling... */ 
     RCTLogInfo(@"MyModel > runModelWithFrame > allocateTensorsWithError : error ...%@",error);
    }


   
  // input data preparation...
  //  NSData *inputData = [[NSMutableData alloc] initWithLength:41216]; ; // Should be initialized
  //  NSData *originData = inputData;
  //  NSData *inputData = [RNImageUtils getArrayOfImage:uiImage];
  RCTLogInfo(@"MyModel > runModelWithFrame > imagePreProcess...");
    NSData *inputData = [self OriginalImageData:uiImage];
    RCTLogInfo(@"MyModel > runModelWithFrame > originalImageData...");
    NSData *originData = [self OriginalImageData:uiImage];

  

  // Copy the input data to the input `TFLTensor`.
    [[self.interpreter  inputTensorAtIndex:0 error:&error] copyData:inputData error:&error];
  if (error != nil) { 
    /* Error handling... */ 
     RCTLogInfo(@"MyModel > runModelWithFrame > copyData : error ...%@",error);
    }
   [[self.interpreter  inputTensorAtIndex:1 error:&error] copyData:originData error:&error];
  if (error != nil) { 
    /* Error handling... */ 
     RCTLogInfo(@"MyModel > runModelWithFrame > copyData : error ...%@",error);
    }
  // Run inference by invoking the `TFLInterpreter`.
    [self.interpreter invokeWithError:&error];
  if (error != nil) { 
    /* Error handling... */ 
     RCTLogInfo(@"MyModel > runModelWithFrame > invokeWithError : error ...%@",error);
    }

  // Get the output `TFLTensor`
    TFLTensor *outputTensor = [self.interpreter outputTensorAtIndex:0 error:&error];
  if (error != nil) { 
    /* Error handling... */ 
        RCTLogInfo(@"MyModel > runModelWithFrame > outputTensorAtIndex : error ...%@",error);
    }

  // Copy output to `NSData` to process the inference results.
    NSData *outputData = [outputTensor dataWithError:&error];
   RCTLogInfo(@"MyModel > runModelWithFrame > outputData  ...%@",outputData);
  if (error != nil) { 
    /* Error handling... */ 
     RCTLogInfo(@"MyModel > runModelWithFrame > dataWithError : error ...%@",error);
    }
    float *p = (float*)[outputData bytes];   // -bytes returns a void* that points to the data
    float f = *p;
  RCTLogInfo(@"MyModel > runModelWithFrame > outputData  ...%f",f);
  NSArray *features = @[ @"Interprete finish" ];
  //  CGFloat rotY = face.headEulerAngleY;
  //           [resultDict setObject:@(rotY) forKey:@"yawAngle"];
    return features;

  
}

  - (NSArray *)runModelWithFrame:(UIImage *)uiImage scaleX:(float)scaleX scaleY:(float)scaleY faces: (NSDictionary *)eventFace             
{
    CGImageRef imageRef = [uiImage CGImage];
    NSUInteger width = CGImageGetWidth(imageRef);
    NSUInteger height = CGImageGetHeight(imageRef);
    NSData *imageData = UIImagePNGRepresentation(uiImage);
    NSLog(@"runModelWithFrame > image data length : %d, width=%d, height=%d, scale=%f x %f", [imageData length],width,height,scaleX,scaleY );
    // NSLog(@"runModelWithFrame > faces: %@", eventFace);
    // if ([eventFace forKey:@"faces"])
    if ([eventFace[@"faces"] count] < 1) {
       NSLog(@"runModelWithFrame > emptyface");
      return imageData;
    }
    else {
      NSDictionary *firstFace = eventFace[@"faces"][0] ;
      //  NSLog(@"runModelWithFrame > faces: %@", eventFace);
        // NSLog(@"runModelWithFrame > first face: %@", firstFace);
        int faceX = (int) [[[firstFace valueForKeyPath:@"bounds.origin"] objectForKey:@"x"] floatValue];
        int faceY = (int) [[[firstFace valueForKeyPath:@"bounds.origin"] objectForKey:@"y"] floatValue];
        int faceWidth = (int) [[[firstFace valueForKeyPath:@"bounds.size"] objectForKey:@"width"] floatValue];
        int faceHeight = (int) [[[firstFace valueForKeyPath:@"bounds.size"] objectForKey:@"height"] floatValue];
        NSLog(@"runModelWithFrame > first face: x:y:w:h  %d x %d ; %d x %d", faceX,faceY,faceWidth,faceHeight);
        // uiImage = [RNImageUtils cropImage:uiImage toRect:CGRectMake(faceX, faceY, faceWidth , faceHeight)];
        // NSLog(@"runModelWithFrame > crop image to face width=%d, height=%d", 
        //       CGImageGetWidth([uiImage CGImage]),CGImageGetHeight([uiImage CGImage]));
        int maxLength = faceHeight;
        if (faceHeight < faceWidth) {
          maxLength = faceWidth;
        }
        uiImage = [RNImageUtils cropImage:uiImage toRect:CGRectMake(faceX, faceY, maxLength , maxLength)];
        NSLog(@"runModelWithFrame > crop image to face width=%d, height=%d", 
              CGImageGetWidth([uiImage CGImage]),CGImageGetHeight([uiImage CGImage]));
        uiImage = [RNImageUtils scaleImage:uiImage convertToSize:CGSizeMake(112, 112) ];
        NSData *imageData = UIImagePNGRepresentation(uiImage);
        NSLog(@"runModelWithFrame > scaled image data length = %d, width=%d, height=%d", 
        [imageData length],CGImageGetWidth([uiImage CGImage]),CGImageGetHeight([uiImage CGImage]));
        // [RNImageUtils rawDataDrawWithImage:uiImage ];
        // get the image array data 
        // NSData * imageGrayArrayData = [RNImageUtils getArrayOfImage:uiImage] ;
    }
     NSError *error;
    [self.interpreter allocateTensorsWithError:&error];
  if (error != nil) { 
    /* Error handling... */ 
     RCTLogInfo(@"MyModel > runModelWithFrame > allocateTensorsWithError : error ...%@",error);
    }


   
  // input data preparation...
  //  NSData *inputData = [[NSMutableData alloc] initWithLength:41216]; ; // Should be initialized
  //  NSData *originData = inputData;
  
  RCTLogInfo(@"MyModel > runModelWithFrame > imagePreProcess...");
   NSData *inputData = [RNImageUtils getArrayOfImage:uiImage];
    // NSData *inputData = [self OriginalImageData:uiImage];
    RCTLogInfo(@"MyModel > runModelWithFrame > originalImageData...");
    // NSData *originData = inputData;
    NSData *originData = [self OriginalImageData:uiImage];

  

  // Copy the input data to the input `TFLTensor`.
    [[self.interpreter  inputTensorAtIndex:0 error:&error] copyData:inputData error:&error];
  if (error != nil) { 
    /* Error handling... */ 
     RCTLogInfo(@"MyModel > runModelWithFrame > copyData : error ...%@",error);
    }
   [[self.interpreter  inputTensorAtIndex:1 error:&error] copyData:originData error:&error];
  if (error != nil) { 
    /* Error handling... */ 
     RCTLogInfo(@"MyModel > runModelWithFrame > copyData : error ...%@",error);
    }
  // Run inference by invoking the `TFLInterpreter`.
    [self.interpreter invokeWithError:&error];
  if (error != nil) { 
    /* Error handling... */ 
     RCTLogInfo(@"MyModel > runModelWithFrame > invokeWithError : error ...%@",error);
    }

  // Get the output `TFLTensor`
    TFLTensor *outputTensor = [self.interpreter outputTensorAtIndex:0 error:&error];
  if (error != nil) { 
    /* Error handling... */ 
        RCTLogInfo(@"MyModel > runModelWithFrame > outputTensorAtIndex : error ...%@",error);
    }

  // Copy output to `NSData` to process the inference results.
    NSData *outputData = [outputTensor dataWithError:&error];
   RCTLogInfo(@"MyModel > runModelWithFrame > outputData  ...%@",outputData);
  if (error != nil) { 
    /* Error handling... */ 
     RCTLogInfo(@"MyModel > runModelWithFrame > dataWithError : error ...%@",error);
    }
    float *p = (float*)[outputData bytes];   // -bytes returns a void* that points to the data
    float f = *p;
  RCTLogInfo(@"MyModel > runModelWithFrame > outputData  ...%f",f);
  // NSArray *features = @[ @"Interprete finish" ];
  //  CGFloat rotY = face.headEulerAngleY;
  //           [resultDict setObject:@(rotY) forKey:@"yawAngle"];
    // return features;
    return imageData;
}

- (void)findFacesInFrame:(UIImage *)uiImage
                  scaleX:(float)scaleX
                  scaleY:(float)scaleY
               completed:(void (^)(NSArray *result))completed {
    NSMutableArray *emptyResult = [[NSMutableArray alloc] init];
    // [_faceRecognizer  processImage:image completion:^(NSArray<FIRVisionFace *> *faces, NSError *error) {
    //      if (error != nil || faces == nil ) {
             completed(emptyResult);
        //  } else {
        //      int size = [faces count];
        //      if (size < 1) {
        //          completed(emptyResult);
        //      }
                     // --------------------------------------  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  check this
        // todo: call other function, with the image included to cut faces, return face image or recognize image.
            //  completed([self processFaces:faces]);
        //     completed([self processFaces:faces inImage:base64String]);
        //  }
    //  }];
  }
  - (NSData *)ImagePreprocess:(UIImage *)uiImage  scaleX:(float)scaleX scaleY:(float)scaleY faces: (NSDictionary *)eventFace                 
{
  CGImageRef imageRef = [uiImage CGImage];
    NSUInteger width = CGImageGetWidth(imageRef);
    NSUInteger height = CGImageGetHeight(imageRef);
    NSData *imageData = UIImagePNGRepresentation(uiImage);
    NSLog(@"runModelWithFrame > image data length : %d, width=%d, height=%d, scale=%f x %f", [imageData length],width,height,scaleX,scaleY );
    // NSLog(@"runModelWithFrame > faces: %@", eventFace);
    // if ([eventFace forKey:@"faces"])
    if ([eventFace[@"faces"] count] < 1) {
       NSLog(@"runModelWithFrame > emptyface");
      return imageData;
    }
    else {
      NSDictionary *firstFace = eventFace[@"faces"][0] ;
      //  NSLog(@"runModelWithFrame > faces: %@", eventFace);
        // NSLog(@"runModelWithFrame > first face: %@", firstFace);
        int faceX = (int) [[[firstFace valueForKeyPath:@"bounds.origin"] objectForKey:@"x"] floatValue];
        int faceY = (int) [[[firstFace valueForKeyPath:@"bounds.origin"] objectForKey:@"y"] floatValue];
        int faceWidth = (int) [[[firstFace valueForKeyPath:@"bounds.size"] objectForKey:@"width"] floatValue];
        int faceHeight = (int) [[[firstFace valueForKeyPath:@"bounds.size"] objectForKey:@"height"] floatValue];
        NSLog(@"runModelWithFrame > first face: x:y:w:h  %d x %d ; %d x %d", faceX,faceY,faceWidth,faceHeight);
        // uiImage = [RNImageUtils cropImage:uiImage toRect:CGRectMake(faceX, faceY, faceWidth , faceHeight)];
        // NSLog(@"runModelWithFrame > crop image to face width=%d, height=%d", 
        //       CGImageGetWidth([uiImage CGImage]),CGImageGetHeight([uiImage CGImage]));
        int maxLength = faceHeight;
        if (faceHeight < faceWidth) {
          maxLength = faceWidth;
        }
        uiImage = [RNImageUtils cropImage:uiImage toRect:CGRectMake(faceX, faceY, maxLength , maxLength)];
        NSLog(@"runModelWithFrame > crop image to face width=%d, height=%d", 
              CGImageGetWidth([uiImage CGImage]),CGImageGetHeight([uiImage CGImage]));
        uiImage = [RNImageUtils scaleImage:uiImage convertToSize:CGSizeMake(112, 112) ];
        NSData *imageData = UIImagePNGRepresentation(uiImage);
        NSLog(@"runModelWithFrame > scaled image data length = %d, width=%d, height=%d", 
              [imageData length],CGImageGetWidth([uiImage CGImage]),CGImageGetHeight([uiImage CGImage]));
        // [RNImageUtils rawDataDrawWithImage:uiImage ];
        // get the image array data 
        NSData * imageGrayArrayData = [RNImageUtils getArrayOfImage:uiImage] ;
        return imageGrayArrayData;
    }
    
  
}

  - (NSData *)OriginalImageData :(UIImage *)faceImage                
{
  RCTLogInfo(@"MyModel > OriginalImageData...");
// get image from a location, preprocess and return
 
  // int maxLength = 41216; //92*112*4-bytes; float	4 byte
  int maxLength = 50176; //112*112*4-bytes; float	4 byte
  // int faceX = 0;
  // int faceY = 0;
  // int width = 112;
  // int height = 112;
   NSMutableData *originalImageData = [[NSMutableData alloc] initWithLength:maxLength] ; // demo data
    // CGRect cropRect = CGRectMake(faceX, faceY, width,height);
    //   RCTLogInfo(@"MyModel > cropImage 112*112 ...");
    // faceImage = [RNImageUtils cropImage:faceImage toRect:cropRect];
   
    //   RCTLogInfo(@"MyModel > convertImageToGrayScale...");
    // faceImage = [RNImageUtils convertImageToGrayScale:faceImage] ;
    
    //  NSData *imageData = UIImagePNGRepresentation(faceImage);
    //   NSUInteger len = [imageData length];
    //  Byte *byteData= (Byte*)malloc(len);
    //  memcpy(byteData, [imageData bytes], len);
    //  RCTLogInfo(@"MyModel > OriginalImageData faceImage bytes length  ...%d",len);
    //  [originalImageData resetBytesInRange:NSMakeRange(0,3398)];
    // [originalImageData appendBytes:[imageData bytes] length:4067 ];
  // long buf = 200;
  
  // [originalImageData replaceBytesInRange:NSMakeRange(0, 41216) withBytes:(const void *)&buf  length:sizeof(buf) ];
//   // const char *bytes = [originalImageData bytes];
//   for (int i = 0; i < 3; i++)
//   {
//       // [originalImageData replaceBytesInRange:NSMakeRange(i*len, i*len+len) withBytes:(const void *)byteData length:len ];
//     [originalImageData appendBytes:[imageData bytes] length:len ];
// //    [originalImageData appendData:[imageData bytes]];
  
//   }
//   int last = maxLength - [originalImageData length];
//   [originalImageData appendBytes:[imageData bytes] length:last  ];
  NSData * result = originalImageData;
  // free(byteData);
  RCTLogInfo(@"MyModel > OriginalImageData result bytes length  ...%d",[result length]);
  return result;
}
@end
