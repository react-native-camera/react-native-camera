// @flow

import Camera from './Camera';
import RNCamera, { type Status as _CameraStatus } from './RNCamera';
import FaceDetector from './FaceDetector';

export type CameraStatus = _CameraStatus;
export { RNCamera, FaceDetector };

export default Camera;
