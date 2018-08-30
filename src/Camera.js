// @flow
import React, { Component } from 'react';
import PropTypes from 'prop-types';
import {
  DeviceEventEmitter, // android
  NativeAppEventEmitter, // ios
  NativeModules,
  Platform,
  StyleSheet,
  findNodeHandle,
  requireNativeComponent,
  ViewPropTypes,
  ActivityIndicator,
  View,
  Text,
  UIManager,
} from 'react-native';

import { requestPermissions } from './handlePermissions';

const styles = StyleSheet.create({
  base: {},
  authorizationContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  notAuthorizedText: {
    textAlign: 'center',
    fontSize: 16,
  },
});

const CameraManager = NativeModules.CameraManager || NativeModules.CameraModule;

function convertNativeProps(props) {
  const newProps = { ...props };
  if (typeof props.aspect === 'string') {
    newProps.aspect = Camera.constants.Aspect[props.aspect];
  }

  if (typeof props.flashMode === 'string') {
    newProps.flashMode = Camera.constants.FlashMode[props.flashMode];
  }

  if (typeof props.zoom === 'string' || typeof props.zoom === 'number') {
    if (props.zoom >= 0 && props.zoom <= 100) {
      newProps.zoom = parseInt(props.zoom);
    }
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

  newProps.barcodeScannerEnabled = typeof props.onBarCodeRead === 'function';

  return newProps;
}

var Camera, constants;

if (CameraManager) {
  
Camera = class Camera extends Component {
  static constants = {
    Aspect: CameraManager.Aspect,
    BarCodeType: CameraManager.BarCodeType,
    Type: CameraManager.Type,
    CaptureMode: CameraManager.CaptureMode,
    CaptureTarget: CameraManager.CaptureTarget,
    CaptureQuality: CameraManager.CaptureQuality,
    Orientation: CameraManager.Orientation,
    FlashMode: CameraManager.FlashMode,
    Zoom: CameraManager.Zoom,
    TorchMode: CameraManager.TorchMode,
  };

  static propTypes = {
    ...ViewPropTypes,
    aspect: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    captureAudio: PropTypes.bool,
    captureMode: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    captureQuality: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    captureTarget: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    defaultOnFocusComponent: PropTypes.bool,
    flashMode: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    zoom: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    keepAwake: PropTypes.bool,
    onBarCodeRead: PropTypes.func,
    barcodeScannerEnabled: PropTypes.bool,
    cropToPreview: PropTypes.bool,
    clearWindowBackground: PropTypes.bool,
    onFocusChanged: PropTypes.func,
    onZoomChanged: PropTypes.func,
    mirrorImage: PropTypes.bool,
    mirrorVideo: PropTypes.bool,
    fixOrientation: PropTypes.bool,
    barCodeTypes: PropTypes.array,
    orientation: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    playSoundOnCapture: PropTypes.bool,
    torchMode: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    type: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    permissionDialogTitle: PropTypes.string,
    permissionDialogMessage: PropTypes.string,
    notAuthorizedView: PropTypes.element,
    pendingAuthorizationView: PropTypes.element,
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
    zoom: 0,
    playSoundOnCapture: true,
    torchMode: CameraManager.TorchMode.off,
    mirrorImage: false,
    mirrorVideo: false,
    cropToPreview: false,
    clearWindowBackground: false,
    barCodeTypes: Object.values(CameraManager.BarCodeType),
    permissionDialogTitle: '',
    permissionDialogMessage: '',
    notAuthorizedView: (
      <View style={styles.authorizationContainer}>
        <Text style={styles.notAuthorizedText}>Camera not authorized</Text>
      </View>
    ),
    pendingAuthorizationView: (
      <View style={styles.authorizationContainer}>
        <ActivityIndicator size="small" />
      </View>
    ),
  };

  static checkDeviceAuthorizationStatus = CameraManager.checkDeviceAuthorizationStatus;
  static checkVideoAuthorizationStatus = CameraManager.checkVideoAuthorizationStatus;
  static checkAudioAuthorizationStatus = CameraManager.checkAudioAuthorizationStatus;

  setNativeProps(props) {
    // eslint-disable-next-line
    this._cameraRef.setNativeProps(props);
  }

  constructor() {
    super();
    this.state = {
      isAuthorized: false,
      isAuthorizationChecked: false,
      isRecording: false,
    };
    this._cameraRef = null;
    this._cameraHandle = null;
  }

  // eslint-disable-next-line
  async componentWillMount() {
    this._addOnBarCodeReadListener();
    this._addOnFocusChanged();
    this._addOnZoomChanged();

    let { captureMode } = convertNativeProps({ captureMode: this.props.captureMode });
    let hasVideoAndAudio =
      this.props.captureAudio && captureMode === Camera.constants.CaptureMode.video;

    const isAuthorized = await requestPermissions(
      hasVideoAndAudio,
      Camera,
      this.props.permissionDialogTitle,
      this.props.permissionDialogMessage,
    );
    this.setState({ isAuthorized, isAuthorizationChecked: true });
  }

  componentWillUnmount() {
    this._removeOnBarCodeReadListener();
    this._removeOnFocusChanged();
    this._removeOnZoomChanged();
    if (this.state.isRecording) {
      this.stopCapture();
    }
  }

  // eslint-disable-next-line
  componentWillReceiveProps(newProps) {
    const { onBarCodeRead, onFocusChanged, onZoomChanged } = this.props;
    if (onBarCodeRead !== newProps.onBarCodeRead) {
      this._addOnBarCodeReadListener(newProps);
    }
    if (onFocusChanged !== !newProps.onFocusChanged) {
      this._addOnFocusChanged(newProps);
    }
    if (onZoomChanged !== !newProps.onZoomChanged) {
      this._addOnZoomChanged(newProps);
    }
  }

  _addOnBarCodeReadListener(props) {
    const { onBarCodeRead } = props || this.props;
    this._removeOnBarCodeReadListener();
    if (onBarCodeRead) {
      this.cameraBarCodeReadListener = Platform.select({
        ios: NativeAppEventEmitter.addListener('CameraBarCodeRead', this._onBarCodeRead),
        android: DeviceEventEmitter.addListener('CameraBarCodeReadAndroid', this._onBarCodeRead),
      });
    }
  }
  _addOnFocusChanged(props) {
    if (Platform.OS === 'ios') {
      const { onFocusChanged } = props || this.props;
      this.focusListener = NativeAppEventEmitter.addListener('focusChanged', onFocusChanged);
    }
  }

  _addOnZoomChanged(props) {
    if (Platform.OS === 'ios') {
      const { onZoomChanged } = props || this.props;
      this.zoomListener = NativeAppEventEmitter.addListener('zoomChanged', onZoomChanged);
    }
  }
  _removeOnBarCodeReadListener() {
    const listener = this.cameraBarCodeReadListener;
    if (listener) {
      listener.remove();
    }
  }
  _removeOnFocusChanged() {
    const listener = this.focusListener;
    if (listener) {
      listener.remove();
    }
  }
  _removeOnZoomChanged() {
    const listener = this.zoomListener;
    if (listener) {
      listener.remove();
    }
  }

  _setReference = ref => {
    if (ref) {
      this._cameraRef = ref;
      this._cameraHandle = findNodeHandle(ref);
    } else {
      this._cameraRef = null;
      this._cameraHandle = null;
    }
  };

  render() {
    // TODO - style is not used, figure it out why
    // eslint-disable-next-line
    const style = [styles.base, this.props.style];
    const nativeProps = convertNativeProps(this.props);

    if (this.state.isAuthorized) {
      return <RCTCamera ref={this._setReference} {...nativeProps} />;
    } else if (!this.state.isAuthorizationChecked) {
      return this.props.pendingAuthorizationView;
    } else {
      return this.props.notAuthorizedView;
    }
  }

  _onBarCodeRead = data => {
    if (this.props.onBarCodeRead) {
      this.props.onBarCodeRead(data);
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
      mirrorVideo: props.mirrorVideo,
      fixOrientation: props.fixOrientation,
      cropToPreview: props.cropToPreview,
      ...options,
    };

    if (options.mode === Camera.constants.CaptureMode.video) {
      options.totalSeconds = options.totalSeconds > -1 ? options.totalSeconds : -1;
      options.preferredTimeScale = options.preferredTimeScale || 30;
      options.cropToPreview = false;
      this.setState({ isRecording: true });
    }

    return CameraManager.capture(options);
  }

  startPreview() {
    if (Platform.OS === 'android') {
      UIManager.dispatchViewManagerCommand(
        this._cameraHandle,
        UIManager.RCTCamera.Commands.startPreview,
        [],
      );
    } else {
      CameraManager.startPreview();
    }
  }

  stopPreview() {
    if (Platform.OS === 'android') {
      UIManager.dispatchViewManagerCommand(
        this._cameraHandle,
        UIManager.RCTCamera.Commands.stopPreview,
        [],
      );
    } else {
      CameraManager.stopPreview();
    }
  }

  stopCapture() {
    if (this.state.isRecording) {
      this.setState({ isRecording: false });
      return CameraManager.stopCapture();
    }
    return Promise.resolve('Not Recording.');
  }

  getFOV() {
    return CameraManager.getFOV();
  }

  hasFlash() {
    if (Platform.OS === 'android') {
      const props = convertNativeProps(this.props);
      return CameraManager.hasFlash({
        type: props.type,
      });
    }
    return CameraManager.hasFlash();
  }

  setZoom(zoom) {
    if (Platform.OS === 'android') {
      const props = convertNativeProps(this.props);
      return CameraManager.setZoom(
        {
          type: props.type,
        },
        zoom,
      );
    }

    return CameraManager.setZoom(zoom);
  }
}

const RCTCamera = requireNativeComponent('RCTCamera', Camera, {
  nativeOnly: {
    testID: true,
    renderToHardwareTextureAndroid: true,
    accessibilityLabel: true,
    importantForAccessibility: true,
    accessibilityLiveRegion: true,
    accessibilityComponentType: true,
    onLayout: true,
  },
});

constants = Camera.constants;

}

export default { Camera, constants };
