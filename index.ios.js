import React, {
  Component,
  NativeAppEventEmitter,
  NativeModules,
  PropTypes,
  StyleSheet,
  requireNativeComponent
}

const CameraManager = NativeModules.CameraManager;
const CAMERA_REF = 'camera';

function convertStringProps(props) {
  const newProps = { ...props };
  if (typeof props.aspect === 'string') {
    newProps.aspect = constants.Aspect[aspect];
  }

  if (typeof props.flashMode === 'string') {
    newProps.flashMode = constants.FlashMode[flashMode];
  }

  if (typeof props.orientation === 'string') {
    newProps.orientation = constants.Orientation[orientation];
  }

  if (typeof props.torchMode === 'string') {
    newProps.torchMode = constants.TorchMode[torchMode];
  }

  if (typeof props.type === 'string') {
    newProps.type = constants.Type[type];
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
    type: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number
    ]),
    orientation: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number
    ]),
    flashMode: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number
    ]),
    torchMode: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number
    ]),
    onBarCodeRead: PropTypes.func,
    onFocusChanged: PropTypes.func,
    onZoomChanged: PropTypes.func
  };
  
  static defaultProps = {
    aspect: constants.Aspect.fill,
    type: constants.Type.back,
    orientation: constants.Orientation.auto,
    captureAudio: true,
    captureMode: constants.CaptureMode.still,
    captureTarget: constants.CaptureTarget.cameraRoll,
    flashMode: constants.FlashMode.off,
    torchMode: constants.TorchMode.off,
    onBarCodeRead: () => {},
    onFocusChanged: () => {},
    onZoomChanged: () => {}
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
    }
  }

  async componentWillMount() {
    const isAuthorized = await CameraManager.checkDeviceAuthorizationStatus();
    this.setState({ isAuthorized });

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
      ...options
    };

    if (options.mode === constants.CaptureMode.video) {
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
});

const RCTCamera = requireNativeComponent('RCTCamera', Camera);

const styles = StyleSheet.create({
  base: {},
});
