//
//  PoseEstimator.h
//  react-native-camera
//
//  Created by Kasper Dissing Bargsteen on 16/04/2019.
//

#ifndef PoseEstimator_h
#define PoseEstimator_h

@interface PoseEstimator : NSObject
typedef void(^postRecognitionBlock)(NSArray *heatmap);
typedef struct detections {
    int array[14][2];
} PoseCoordinates;

- (instancetype)init;
- (BOOL)isRealDetector;
- (void)setupModelForPoseEstimation;
- (void)estimatePoseOnDeviceInImage:(CGImageRef)image completed:(postRecognitionBlock)completed;

@end


#endif /* PoseEstimator_h */
