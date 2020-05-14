// @flow
import { NativeModules } from 'react-native';

const faceDetectionDisabledMessage = 'Face detection has not been included in this build.';

const FaceDetectorModule: Object = NativeModules.RNFaceDetector || {
  stubbed: true,
  Mode: {},
  Landmarks: {},
  Classifications: {},
  Contours: {},
  detectFaces: () => new Promise((_, reject) => reject(faceDetectionDisabledMessage)),
};

type Point = { x: number, y: number };

export type FaceContours = {
  all: Point[],
  face: Point[],
  leftEye: Point[],
  leftEyebrowBottom: Point[],
  leftEyebrowTop: Point[],
  lowerLipBottom: Point[],
  lowerLipTop: Point[],
  noseBottom: Point[],
  noseBridge: Point[],
  rightEye: Point[],
  rightEyebrowBottom: Point[],
  rightEyebrowTop: Point[],
  upperLipBottom: Point[],
  upperLipTop: Point[],
};

export type FaceFeature = {
  bounds: {
    size: {
      width: number,
      height: number,
    },
    origin: Point,
  },
  smilingProbability?: number,
  leftEarPosition?: Point,
  rightEarPosition?: Point,
  leftEyePosition?: Point,
  leftEyeOpenProbability?: number,
  rightEyePosition?: Point,
  rightEyeOpenProbability?: number,
  leftCheekPosition?: Point,
  rightCheekPosition?: Point,
  leftMouthPosition?: Point,
  mouthPosition?: Point,
  rightMouthPosition?: Point,
  bottomMouthPosition?: Point,
  noseBasePosition?: Point,
  yawAngle?: number,
  rollAngle?: number,
  contours?: FaceContours,
};

type DetectionOptions = {
  mode?: $Keys<typeof FaceDetectorModule.Mode>,
  detectLandmarks?: $Keys<typeof FaceDetectorModule.Landmarks>,
  detectContours?: $Keys<typeof FaceDetectorModule.Contours>,
  runClassifications?: $Keys<typeof FaceDetectorModule.Classifications>,
};

export default class FaceDetector {
  static Constants = {
    Mode: FaceDetectorModule.Mode,
    Landmarks: FaceDetectorModule.Landmarks,
    Contours: FaceDetectorModule.Contours,
    Classifications: FaceDetectorModule.Classifications,
  };

  static detectFacesAsync(
    uri: string,
    options: ?DetectionOptions,
  ): Promise<{
    faces: Array<FaceFeature>,
    image: {
      uri: string,
      width: number,
      height: number,
      orientation: any,
    },
  }> {
    return FaceDetectorModule.detectFaces({ ...options, uri });
  }
}

export const Constants = FaceDetector.Constants;
