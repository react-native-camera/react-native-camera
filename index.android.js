var React = require('react-native');
var { View, StyleSheet, requireNativeComponent, PropTypes, NativeModules, DeviceEventEmitter } = React;

var CAMERA_REF = 'camera';

var constants = {
  Aspect: NativeModules.CameraModule.Aspect,
  BarCodeType: NativeModules.CameraModule.BarCodeType,
  Type: NativeModules.CameraModule.Type,
  CaptureMode: NativeModules.CameraModule.CaptureMode,
  CaptureTarget: NativeModules.CameraModule.CaptureTarget,
  Orientation: NativeModules.CameraModule.Orientation,
  FlashMode: NativeModules.CameraModule.FlashMode,
  TorchMode: NativeModules.CameraModule.TorchMode
};

var Camera = React.createClass({
  propTypes: {
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
    defaultOnFocusComponent: PropTypes.bool,
    onFocusChanged: PropTypes.func,
    onZoomChanged: PropTypes.func,
    ...View.propTypes
  },

  setNativeProps(props) {
    this.refs[CAMERA_REF].setNativeProps(props);
  },

  getDefaultProps() {
    return {
      aspect: constants.Aspect.fill,
      type: constants.Type.back,
      orientation: constants.Orientation.auto,
      captureAudio: true,
      captureMode: constants.CaptureMode.still,
      captureTarget: constants.CaptureTarget.cameraRoll,
      flashMode: constants.FlashMode.off,
      torchMode: constants.TorchMode.off
    };
  },

  getInitialState() {
    return {
      isAuthorized: false,
      isRecording: false
    };
  },

  componentWillMount() {
    //// TODO: handle properly Android 6 new permissions style
    this.state.isAuthorized = true;
    this.setState(this.state);
    this.cameraBarCodeReadListener = DeviceEventEmitter.addListener('CameraBarCodeRead', this._onBarCodeRead);
  },

  componentWillUnmount() {
    this.cameraBarCodeReadListener.remove();

    if (this.state.isRecording) {
      this.stopCapture();
    }
  },

  render() {
    var style = [styles.base, this.props.style];

    var aspect = this.props.aspect,
      type = this.props.type,
      orientation = this.props.orientation,
      flashMode = this.props.flashMode,
      torchMode = this.props.torchMode;

    var legacyProps = {
      aspect: {
        Fill: 'fill',
        Fit: 'fit',
        Stretch: 'stretch'
      },
      orientation: {
        LandscapeLeft: 'landscapeLeft',
        LandscapeRight: 'landscapeRight',
        Portrait: 'portrait',
        PortraitUpsideDown: 'portraitUpsideDown'
      },
      type: {
        Front: 'front',
        Back: 'back'
      }
    };

    if (typeof aspect === 'string') {
      aspect = constants.Aspect[aspect];
    }

    if (typeof flashMode === 'string') {
      flashMode = constants.FlashMode[flashMode];
    }

    if (typeof orientation === 'string') {
      orientation = constants.Orientation[orientation];
    }

    if (typeof torchMode === 'string') {
      torchMode = constants.TorchMode[torchMode];
    }

    if (typeof type === 'string') {
      type = constants.Type[type];
    }

    var nativeProps = Object.assign({}, this.props, {
      style,
      aspect: aspect,
      type: type,
      orientation: orientation,
      flashMode: flashMode,
      torchMode: torchMode
    });

    return <RCTCameraView ref={CAMERA_REF} {... nativeProps} />;
  },

  _onBarCodeRead(e) {
    this.props.onBarCodeRead && this.props.onBarCodeRead(e);
  },

  capture(options, cb) {

    if (arguments.length == 1) {
      cb = options;
      options = {};
    }

    options = Object.assign({}, {
      audio: this.props.captureAudio,
      mode: this.props.captureMode,
      target: this.props.captureTarget,
      type: this.props.type
    }, options);

    if (typeof options.mode === 'string') {
      options.mode = constants.CaptureMode[options.mode];
    }

    if (options.mode === constants.CaptureMode.video) {
      options.totalSeconds = (options.totalSeconds > -1 ? options.totalSeconds : -1);
      options.preferredTimeScale = options.preferredTimeScale || 30;
      this.setState({isRecording: true});
    }

    if (typeof options.target === 'string') {
      options.target = constants.CaptureTarget[options.target];
    }

    if (typeof options.type === 'string') {
      options.type = constants.Type[options.type];
    }

    NativeModules.CameraModule.capture(options, cb);
  },

  stopCapture() {
    if (this.state.isRecording) {
      NativeModules.CameraManager.stopCapture();
      this.setState({ isRecording: false });
    }
  }

});

var RCTCameraView = requireNativeComponent('RCTCameraView', Camera);

var styles = StyleSheet.create({
  base: {},
});

Camera.constants = constants;
module.exports = Camera;
