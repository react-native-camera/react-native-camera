//
//  RNFaceDetectorPointTransformCalculator.h
//  RCTCamera
//
//  Created by Joao Guilherme Daros Fidelis on 21/01/18.
//

#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>

@interface RNFaceDetectorPointTransformCalculator : NSObject

- (instancetype)initToTransformFromOrientation:(AVCaptureVideoOrientation)orientation toOrientation:(AVCaptureVideoOrientation)toOrientation forVideoWidth:(CGFloat)videoWidth andVideoHeight:(CGFloat)videoHeight;

- (CGAffineTransform)transform;

@end
