import React, { Component } from 'react';
import PropTypes from 'prop-types';
import {
  DeviceEventEmitter, // android
  NativeAppEventEmitter, // ios
  NativeModules,
  Platform,
  StyleSheet,
  requireNativeComponent,
  View,
  ViewPropTypes
} from 'react-native';

const CameraManager = NativeModules.CameraManager || NativeModules.CameraModule;
const CAMERA_REF = 'camera';

function convertNativeProps(props) {
  const newProps = { ...props };
  if (typeof props.aspect === 'string') {
    newProps.aspect = Camera.constants.Aspect[props.aspect];
  }

  if (typeof props.flashMode === 'string') {
    newProps.flashMode = Camera.constants.FlashMode[props.flashMode];
  }

  if (typeof props.orientation === 'string') {
    newProps.orientation = Camera.constants.Orientation[props.orientation];
  }

  if (typeof props.torchMode === 'string') {
    newProps.torchMode = Camera.constants.TorchMode[props.torchMode];
  }

  if (typeof props.type === 'string') {
    newProps.type = Camera.constants.Type[props.type];
  }

  if (typeof props.captureQuality === 'string') {
    newProps.captureQuality = Camera.constants.CaptureQuality[props.captureQuality];
  }

  if (typeof props.captureMode === 'string') {
    newProps.captureMode = Camera.constants.CaptureMode[props.captureMode];
  }

  if (typeof props.captureTarget === 'string') {
    newProps.captureTarget = Camera.constants.CaptureTarget[props.captureTarget];
  }

  // do not register barCodeTypes if no barcode listener
  if (typeof props.onBarCodeRead !== 'function') {
    newProps.barCodeTypes = [];
  }

  newProps.barcodeScannerEnabled = typeof props.onBarCodeRead === 'function'

  return newProps;
}

export default class Camera extends Component {

  static constants = {
    Aspect: CameraManager.Aspect,
    BarCodeType: CameraManager.BarCodeType,
    Type: CameraManager.Type,
    CaptureMode: CameraManager.CaptureMode,
    CaptureTarget: CameraManager.CaptureTarget,
    CaptureQuality: CameraManager.CaptureQuality,
    Orientation: CameraManager.Orientation,
    FlashMode: CameraManager.FlashMode,
    TorchMode: CameraManager.TorchMode
  };

  static propTypes = {
    ...ViewPropTypes,
    aspect: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number
    ]),
    captureAudio: PropTypes.bool,
    captureMode: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number
    ]),
    captureQuality: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number
    ]),
    captureTarget: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number
    ]),
    defaultOnFocusComponent: PropTypes.bool,
    flashMode: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number
    ]),
    keepAwake: PropTypes.bool,
    onBarCodeRead: PropTypes.func,
    barcodeScannerEnabled: PropTypes.bool,
    onFocusChanged: PropTypes.func,
    onZoomChanged: PropTypes.func,
    mirrorImage: PropTypes.bool,
    fixOrientation: PropTypes.bool,
    barCodeTypes: PropTypes.array,
    orientation: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number
    ]),
    playSoundOnCapture: PropTypes.bool,
    torchMode: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number
    ]),
    type: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number
    ])
  };

  static defaultProps = {
    aspect: CameraManager.Aspect.fill,
    type: CameraManager.Type.back,
    orientation: CameraManager.Orientation.auto,
    fixOrientation: false,
    captureAudio: false,
    captureMode: CameraManager.CaptureMode.still,
    captureTarget: CameraManager.CaptureTarget.cameraRoll,
    captureQuality: CameraManager.CaptureQuality.high,
    defaultOnFocusComponent: true,
    flashMode: CameraManager.FlashMode.off,
    playSoundOnCapture: true,
    torchMode: CameraManager.TorchMode.off,
    mirrorImage: false,
    barCodeTypes: Object.values(CameraManager.BarCodeType),
  };

  static checkDeviceAuthorizationStatus = CameraManager.checkDeviceAuthorizationStatus;
  static checkVideoAuthorizationStatus = CameraManager.checkVideoAuthorizationStatus;
  static checkAudioAuthorizationStatus = CameraManager.checkAudioAuthorizationStatus;

  setNativeProps(props) {
    this.refs[CAMERA_REF].setNativeProps(props);
  }

  constructor() {
    super();
    this.state = {
      isAuthorized: false,
      isRecording: false
    };
  }

  async componentWillMount() {
    this._addOnBarCodeReadListener()

    let { captureMode } = convertNativeProps({ captureMode: this.props.captureMode })
    let hasVideoAndAudio = this.props.captureAudio && captureMode === Camera.constants.CaptureMode.video
    let check = hasVideoAndAudio ? Camera.checkDeviceAuthorizationStatus : Camera.checkVideoAuthorizationStatus;

    if (check) {
      const isAuthorized = await check();
      this.setState({ isAuthorized });
    }
  }

  componentWillUnmount() {
    this._removeOnBarCodeReadListener()

    if (this.state.isRecording) {
      this.stopCapture();
    }
  }

  componentWillReceiveProps(newProps) {
    const { onBarCodeRead } = this.props
    if (onBarCodeRead !== newProps.onBarCodeRead) {
      this._addOnBarCodeReadListener(newProps)
    }
  }

  _addOnBarCodeReadListener(props) {
    const { onBarCodeRead } = props || this.props
    this._removeOnBarCodeReadListener()
    if (onBarCodeRead) {
      this.cameraBarCodeReadListener = Platform.select({
        ios: NativeAppEventEmitter.addListener('CameraBarCodeRead', this._onBarCodeRead),
        android: DeviceEventEmitter.addListener('CameraBarCodeReadAndroid',  this._onBarCodeRead)
      })
    }
  }
  _removeOnBarCodeReadListener() {
    const listener = this.cameraBarCodeReadListener
    if (listener) {
      listener.remove()
    }
  }

  render() {
    const style = [styles.base, this.props.style];
    const nativeProps = convertNativeProps(this.props);

    return <RCTCamera ref={CAMERA_REF} {...nativeProps} />;
  }

  _onBarCodeRead = (data) => {
    if (this.props.onBarCodeRead) {
      this.props.onBarCodeRead(data)
    }
  };

  capture(options) {
    const props = convertNativeProps(this.props);
    options = {
      audio: props.captureAudio,
      barCodeTypes: props.barCodeTypes,
      mode: props.captureMode,
      playSoundOnCapture: props.playSoundOnCapture,
      target: props.captureTarget,
      quality: props.captureQuality,
      type: props.type,
      title: '',
      description: '',
      mirrorImage: props.mirrorImage,
      fixOrientation: props.fixOrientation,
      ...options
    };

    if (options.mode === Camera.constants.CaptureMode.video) {
      options.totalSeconds = (options.totalSeconds > -1 ? options.totalSeconds : -1);
      options.preferredTimeScale = options.preferredTimeScale || 30;
      this.setState({ isRecording: true });
    }

    return CameraManager.capture(options);
  }

  stopCapture() {
    if (this.state.isRecording) {
      this.setState({ isRecording: false });
      return CameraManager.stopCapture();
    }
    return Promise.resolve("Not Recording.");
  }

  getFOV() {
    return CameraManager.getFOV();
  }

  hasFlash() {
    if (Platform.OS === 'android') {
      const props = convertNativeProps(this.props);
      return CameraManager.hasFlash({
        type: props.type
      });
    }
    return CameraManager.hasFlash();
  }
}

export const constants = Camera.constants;

const RCTCamera = requireNativeComponent('RCTCamera', Camera, {nativeOnly: {
  testID: true,
  renderToHardwareTextureAndroid: true,
  accessibilityLabel: true,
  importantForAccessibility: true,
  accessibilityLiveRegion: true,
  accessibilityComponentType: true,
  onLayout: true
}});

const styles = StyleSheet.create({
  base: {},
});
