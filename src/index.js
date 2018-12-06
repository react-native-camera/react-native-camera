// @flow

import Camera from './Camera';
import RNCamera, { type Status as CameraStatus } from './RNCamera';
import FaceDetector from './FaceDetector';

export type CameraStatus = CameraStatus;
export { RNCamera, FaceDetector };

export default Camera;
