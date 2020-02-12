// @flow
import RNCamera, {
    type Status as _CameraStatus,
    Constants as RNCConstants,
    ConversionTables,
  } from './RNCamera';  
import FaceDetector from './FaceDetector';

export type CameraStatus = _CameraStatus;
export { RNCamera, FaceDetector, RNCConstants, ConversionTables };
