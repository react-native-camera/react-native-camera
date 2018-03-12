//
//  RNFaceDetectorUtils.h
//  RCTCamera
//
//  Created by Joao Guilherme Daros Fidelis on 21/01/18.
//

#if __has_include(<GoogleMobileVision/GoogleMobileVision.h>)
#import <UIKit/UIKit.h>
#import <CoreMedia/CoreMedia.h>
#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>
#import <GoogleMVDataOutput/GoogleMVDataOutput.h>

typedef NS_ENUM(NSInteger, RNFaceDetectionMode) {
    RNFaceDetectionFastMode = GMVDetectorFaceFastMode,
    RNFaceDetectionAccurateMode = GMVDetectorFaceAccurateMode
};

typedef NS_ENUM(NSInteger, RNFaceDetectionLandmarks) {
    RNFaceDetectAllLandmarks = GMVDetectorFaceLandmarkAll,
    RNFaceDetectNoLandmarks = GMVDetectorFaceLandmarkNone
};

typedef NS_ENUM(NSInteger, RNFaceDetectionClassifications) {
    RNFaceRunAllClassifications = GMVDetectorFaceClassificationAll,
    RNFaceRunNoClassifications = GMVDetectorFaceClassificationNone
};

@interface RNFaceDetectorUtils : NSObject

+ (NSDictionary *)constantsToExport;

+ (CGAffineTransform)transformFromDeviceOutput:(GMVDataOutput *)dataOutput toInterfaceVideoOrientation:(AVCaptureVideoOrientation)interfaceVideoOrientation;

@end
#endif
