
#import <UIKit/UIKit.h>
#if __has_include(<MLKitBarcodeScanning/MLKitBarcodeScanning.h>)
  @import MLKitBarcodeScanning;
#endif

@interface BarcodeDetectorManagerMlkit : NSObject
typedef void(^postRecognitionBlock)(NSArray *barcodes);

- (instancetype)init;

-(BOOL)isRealDetector;
-(NSInteger)fetchDetectionMode;
-(void)setType:(id)json queue:(dispatch_queue_t)sessionQueue;
-(void)setMode:(id)json queue:(dispatch_queue_t)sessionQueue;
-(void)findBarcodesInFrame:(UIImage *)image scaleX:(float)scaleX scaleY:(float)scaleY completed:(postRecognitionBlock)completed;
+(NSDictionary *)constants;

@end 
