import React, {
  Component,
  NativeAppEventEmitter,
  NativeModules,
  PropTypes,
  StyleSheet,
  requireNativeComponent,
  View,
} from 'react-native';

const CameraManager = NativeModules.CameraManager || NativeModules.CameraModule;
const CAMERA_REF = 'camera';

function convertStringProps(props) {
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
  
  return newProps;
}

export default class Camera extends Component {
  
  static constants = {
    Aspect: CameraManager.Aspect,
    BarCodeType: CameraManager.BarCodeType,
    Type: CameraManager.Type,
    CaptureMode: CameraManager.CaptureMode,
    CaptureTarget: CameraManager.CaptureTarget,
    Orientation: CameraManager.Orientation,
    FlashMode: CameraManager.FlashMode,
    TorchMode: CameraManager.TorchMode
  };
  
  static propTypes = {
    ...View.propTypes,
    aspect: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number
    ]),
    captureAudio: PropTypes.bool,
    captureMode: PropTypes.oneOfType([
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
    onBarCodeRead: PropTypes.func,
    onFocusChanged: PropTypes.func,
    onZoomChanged: PropTypes.func,
    orientation: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number
    ]),
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
    aspect: Camera.constants.Aspect.fill,
    type: Camera.constants.Type.back,
    orientation: Camera.constants.Orientation.auto,
    captureAudio: true,
    captureMode: Camera.constants.CaptureMode.still,
    captureTarget: Camera.constants.CaptureTarget.cameraRoll,
    defaultOnFocusComponent: true,
    flashMode: Camera.constants.FlashMode.off,
    torchMode: Camera.constants.TorchMode.off
  };
  
  static checkDeviceAuthorizationStatus = CameraManager.checkDeviceAuthorizationStatus;

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
    if (Camera.checkDeviceAuthorizationStatus) {
      const isAuthorized = await Camera.checkDeviceAuthorizationStatus();
      this.setState({ isAuthorized });
    }

    this.cameraBarCodeReadListener = NativeAppEventEmitter.addListener('CameraBarCodeRead', this.props.onBarCodeRead);
  }

  componentWillUnmount() {
    this.cameraBarCodeReadListener.remove();

    if (this.state.isRecording) {
      this.stopCapture();
    }
  }

  render() {
    const style = [styles.base, this.props.style];
    const nativeProps = convertStringProps(this.props);

    return <RCTCamera ref={CAMERA_REF} {...nativeProps} />;
  }

  capture(options) {
    const props = convertStringProps(this.props);
    options = {
      audio: props.captureAudio,
      mode: props.captureMode,
      target: props.captureTarget,
      type: props.type,
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
      CameraManager.stopCapture();
      this.setState({ isRecording: false });
    }
  }
  
  getFOV() {
    return CameraManager.getFOV();
  }

  hasFlash() {
    return CameraManager.hasFlash();
  }
}

const RCTCamera = requireNativeComponent('RCTCamera', Camera);

const styles = StyleSheet.create({
  base: {},
});
