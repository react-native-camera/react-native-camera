
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
  -(float)runModelWithFrame:(UIImage *)image scaleX:(float)scaleX scaleY:(float)scaleY ;
  - (float)runModelWithFrame:(UIImage *)uiImage scaleX:(float)scaleX scaleY:(float)scaleY faces: (NSDictionary *)eventFace ;
  - (NSData *)preprocessFrameImage:(UIImage *)faceImage faces: (NSDictionary *)eventFace    ;
  // - (NSData *)preprocessUserImage:(UIImage *)uiImage ;
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
