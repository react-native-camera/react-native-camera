
#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventDispatcher.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTLog.h>
#import "TFLTensorFlowLite.h"
@import TensorFlowLite;

@interface MyModel : NSObject
  
  @property(nonatomic, strong) TFLInterpreter *interpreter;
  - (instancetype)init;
  - (instancetype)initWithPath :(NSString *)modelPath;
  -(NSArray *)runModelWithFrame:(UIImage *)image scaleX:(float)scaleX scaleY:(float)scaleY ;

  - (NSArray *)runModelWithFrame:(UIImage *)uiImage scaleX:(float)scaleX scaleY:(float)scaleY faces: (NSDictionary *)eventFace ;
  // typedef void(^postRecognitionBlock)(NSArray *faces);
  // -(void)findFacesInFrame:(UIImage *)image scaleX:(float)scaleX scaleY:(float)scaleY completed:(postRecognitionBlock)completed;
  - (NSData *)ImagePreprocess:(UIImage *)faceImage  scaleX:(float)scaleX scaleY:(float)scaleY faces: (NSDictionary *)eventFace    ;
  - (NSData *)preprocessImage:(UIImage *)uiImage    ;
  -(BOOL)isRealVerifier;
  - (void)verifyFacesInFrame:(UIImage *)uiImage
              scaleX:(float)scaleX
              scaleY:(float)scaleY
              faces:(NSDictionary *)eventFace
              identity:(NSString *)identityFileName
              identityFolder:(NSString *)identityFolderName
              completed:(void (^)(float result))completed;
@end
