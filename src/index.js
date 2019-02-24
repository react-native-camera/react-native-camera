// @flow

import Camera from './Camera';
import RNCamera, { type Status as _CameraStatus } from './RNCamera';
import FaceDetector from './FaceDetector';
import BarcodeFinderMask from './subviews/BarcodeFinderMask';

export type CameraStatus = _CameraStatus;
export { RNCamera, FaceDetector, BarcodeFinderMask };

export default Camera;
