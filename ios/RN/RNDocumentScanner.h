
#import <UIKit/UIKit.h>


@interface RNDocumentScanner : NSObject
typedef void(^postDetectionBlock)(NSDictionary *document);

-(void)findDocumentInFrame:(UIImage *)image scaleX:(float)scaleX scaleY:(float)scaleY completed:(postDetectionBlock)completed;
-(UIImage *)getDocument:(UIImage *)image;

@end
