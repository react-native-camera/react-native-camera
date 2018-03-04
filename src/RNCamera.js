// @flow
import React from 'react';
import PropTypes from 'prop-types';
import { mapValues } from 'lodash';
import {
  findNodeHandle,
  Platform,
  NativeModules,
  ViewPropTypes,
  requireNativeComponent,
  View,
  ActivityIndicator,
  Text,
} from 'react-native';

import type { FaceFeature } from './FaceDetector';

import { requestPermissions } from './handlePermissions';

type PictureOptions = {
  quality?: number,
};

type TrackedFaceFeature = FaceFeature & {
  faceID?: number,
};

type RecordingOptions = {
  maxDuration?: number,
  maxFileSize?: number,
  quality?: number | string,
};

type EventCallbackArgumentsType = {
  nativeEvent: Object,
};

type PropsType = ViewPropTypes & {
  zoom?: number,
  ratio?: string,
  focusDepth?: number,
  type?: number | string,
  onCameraReady?: Function,
  onBarCodeRead?: Function,
  faceDetectionMode?: number,
  faceDetectionExpectedOrientation?: number,
  objectsToDetect?: number,
  flashMode?: number | string,
  barCodeTypes?: Array<string>,
  whiteBalance?: number | string,
  faceDetectionLandmarks?: number,
  autoFocus?: string | boolean | number,
  faceDetectionClassifications?: number,
  onFacesDetected?: ({ faces: Array<TrackedFaceFeature> }) => void,
  captureAudio?: boolean,
};

const CameraManager: Object = NativeModules.RNCameraManager ||
  NativeModules.RNCameraModule || {
    stubbed: true,
    Type: {
      back: 1,
    },
    AutoFocus: {
      on: 1,
    },
    FlashMode: {
      off: 1,
    },
    WhiteBalance: {},
    BarCodeType: {},
    FaceDetection: {
      fast: 1,
      Mode: {},
      Landmarks: {
        none: 0,
      },
      Classifications: {
        none: 0,
      },
    },
  };

const EventThrottleMs = 500;

export default class Camera extends React.Component<PropsType> {
  static Constants = {
    Type: CameraManager.Type,
    FlashMode: CameraManager.FlashMode,
    AutoFocus: CameraManager.AutoFocus,
    WhiteBalance: CameraManager.WhiteBalance,
    VideoQuality: CameraManager.VideoQuality,
    BarCodeType: CameraManager.BarCodeType,
    FaceDetection: CameraManager.FaceDetection,
  };

  // Values under keys from this object will be transformed to native options
  static ConversionTables = {
    type: CameraManager.Type,
    flashMode: CameraManager.FlashMode,
    autoFocus: CameraManager.AutoFocus,
    whiteBalance: CameraManager.WhiteBalance,
    faceDetectionMode: CameraManager.FaceDetection.Mode,
    faceDetectionLandmarks: CameraManager.FaceDetection.Landmarks,
    faceDetectionClassifications: CameraManager.FaceDetection.Classifications,
  };

  static propTypes = {
    ...ViewPropTypes,
    zoom: PropTypes.number,
    ratio: PropTypes.string,
    focusDepth: PropTypes.number,
    onMountError: PropTypes.func,
    onCameraReady: PropTypes.func,
    onBarCodeRead: PropTypes.func,
    onFacesDetected: PropTypes.func,
    faceDetectionMode: PropTypes.number,
    faceDetectionExpectedOrientation: PropTypes.number,
    objectsToDetect: PropTypes.number,
    faceDetectionLandmarks: PropTypes.number,
    faceDetectionClassifications: PropTypes.number,
    barCodeTypes: PropTypes.arrayOf(PropTypes.string),
    type: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    flashMode: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    whiteBalance: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    autoFocus: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number,
      PropTypes.bool,
    ]),
    permissionDialogTitle: PropTypes.string,
    permissionDialogMessage: PropTypes.string,
    notAuthorizedView: PropTypes.element,
    pendingAuthorizationView: PropTypes.element,
    captureAudio: PropTypes.bool,
  };

  static defaultProps: Object = {
    zoom: 0,
    ratio: '4:3',
    focusDepth: 0,
    type: CameraManager.Type.back,
    autoFocus: CameraManager.AutoFocus.on,
    flashMode: CameraManager.FlashMode.off,
    whiteBalance: CameraManager.WhiteBalance.auto,
    faceDetectionExpectedOrientation: -1,
    objectsToDetect: -1,
    faceDetectionMode: CameraManager.FaceDetection.fast,
    barCodeTypes: Object.values(CameraManager.BarCodeType),
    faceDetectionLandmarks: CameraManager.FaceDetection.Landmarks.none,
    faceDetectionClassifications:
      CameraManager.FaceDetection.Classifications.none,
    permissionDialogTitle: '',
    permissionDialogMessage: '',
    notAuthorizedView: (
      <View
        style={{
          flex: 1,
          alignItems: 'center',
          justifyContent: 'center',
        }}>
        <Text
          style={{
            textAlign: 'center',
            fontSize: 16,
          }}>
          Camera not authorized
        </Text>
      </View>
    ),
    pendingAuthorizationView: (
      <View
        style={{
          flex: 1,
          alignItems: 'center',
          justifyContent: 'center',
        }}>
        <ActivityIndicator size="small" />
      </View>
    ),
    captureAudio: false,
  };

  _cameraRef: ?Object;
  _cameraHandle: ?number;
  _lastEvents: { [string]: string };
  _lastEventsTimes: { [string]: Date };

  constructor(props: PropsType) {
    super(props);
    this._lastEvents = {};
    this._lastEventsTimes = {};
    this.state = {
      isAuthorized: false,
      isAuthorizationChecked: false,
    };
  }

  async takePictureAsync(options?: PictureOptions) {
    if (!options) {
      options = {};
    }
    if (!options.quality) {
      options.quality = 1;
    }
    return await CameraManager.takePicture(options, this._cameraHandle);
  }

  async getSupportedRatiosAsync() {
    if (Platform.OS === 'android') {
      return await CameraManager.getSupportedRatios(this._cameraHandle);
    } else {
      throw new Error('Ratio is not supported on iOS');
    }
  }

  async recordAsync(options?: RecordingOptions) {
    if (!options || typeof options !== 'object') {
      options = {};
    } else if (typeof options.quality === 'string') {
      options.quality = Camera.Constants.VideoQuality[options.quality];
    }
    return await CameraManager.record(options, this._cameraHandle);
  }

  stopRecording() {
    CameraManager.stopRecording(this._cameraHandle);
  }

  _onMountError = () => {
    if (this.props.onMountError) {
      this.props.onMountError();
    }
  };

  _onCameraReady = () => {
    if (this.props.onCameraReady) {
      this.props.onCameraReady();
    }
  };

  _onObjectDetected = (callback: ?Function) => ({
    nativeEvent,
  }: EventCallbackArgumentsType) => {
    const { type } = nativeEvent;

    if (
      this._lastEvents[type] &&
      this._lastEventsTimes[type] &&
      JSON.stringify(nativeEvent) === this._lastEvents[type] &&
      new Date() - this._lastEventsTimes[type] < EventThrottleMs
    ) {
      return;
    }

    if (callback) {
      callback(nativeEvent);
      this._lastEventsTimes[type] = new Date();
      this._lastEvents[type] = JSON.stringify(nativeEvent);
    }
  };

  _setReference = (ref: ?Object) => {
    if (ref) {
      this._cameraRef = ref;
      this._cameraHandle = findNodeHandle(ref);
    } else {
      this._cameraRef = null;
      this._cameraHandle = null;
    }
  };

  async componentWillMount() {
    const hasVideoAndAudio = this.props.captureAudio;
    const isAuthorized = await requestPermissions(
      hasVideoAndAudio,
      CameraManager,
      this.props.permissionDialogTitle,
      this.props.permissionDialogMessage
    );
    this.setState({ isAuthorized, isAuthorizationChecked: true });
  }

  render() {
    const nativeProps = this._convertNativeProps(this.props);

    if (this.state.isAuthorized) {
      return (
        <RNCamera
          {...nativeProps}
          ref={this._setReference}
          onMountError={this._onMountError}
          onCameraReady={this._onCameraReady}
          onBarCodeRead={this._onObjectDetected(this.props.onBarCodeRead)}
          onFacesDetected={this._onObjectDetected(this.props.onFacesDetected)}
        />
      );
    } else if (!this.state.isAuthorizationChecked) {
      return this.props.pendingAuthorizationView;
    } else {
      return this.props.notAuthorizedView;
    }
  }

  _convertNativeProps(props: PropsType) {
    const newProps = mapValues(props, this._convertProp);

    if (props.onBarCodeRead) {
      newProps.barCodeScannerEnabled = true;
    }

    if (props.onFacesDetected) {
      newProps.faceDetectorEnabled = true;
    }

    if (Platform.OS === 'ios') {
      delete newProps.ratio;
    }

    return newProps;
  }

  _convertProp(value: *, key: string): * {
    if (typeof value === 'string' && Camera.ConversionTables[key]) {
      return Camera.ConversionTables[key][value];
    }

    return value;
  }
}

export const Constants = Camera.Constants;

const RNCamera = requireNativeComponent('RNCamera', Camera, {
  nativeOnly: {
    accessibilityComponentType: true,
    accessibilityLabel: true,
    accessibilityLiveRegion: true,
    barCodeScannerEnabled: true,
    faceDetectorEnabled: true,
    importantForAccessibility: true,
    onBarCodeRead: true,
    onCameraReady: true,
    onFaceDetected: true,
    onLayout: true,
    onMountError: true,
    renderToHardwareTextureAndroid: true,
    testID: true,
  },
});
