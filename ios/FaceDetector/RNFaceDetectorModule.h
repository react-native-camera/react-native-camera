//
//  RNFaceDetectorModule.h
//  RCTCamera
//
//  Created by Joao Guilherme Daros Fidelis on 21/01/18.
//

#import <React/RCTBridgeModule.h>
#if __has_include(<GoogleMobileVision/GoogleMobileVision.h>)
#import <GoogleMobileVision/GoogleMobileVision.h>
#endif

@interface RNFaceDetectorModule : NSObject <RCTBridgeModule>
@end
