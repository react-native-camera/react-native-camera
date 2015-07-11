var React = require('React');
var DeviceEventEmitter = require('RCTDeviceEventEmitter');
var NativeModules = require('NativeModules');
var ReactNativeViewAttributes = require('ReactNativeViewAttributes');
var StyleSheet = require('StyleSheet');
var createReactNativeComponentClass = require('createReactNativeComponentClass');
var PropTypes = require('ReactPropTypes');
var StyleSheetPropType = require('StyleSheetPropType');
var NativeMethodsMixin = require('NativeMethodsMixin');
var flattenStyle = require('flattenStyle');
var merge = require('merge');

var constants = {
  Aspect: NativeModules.CameraManager.Aspect,
  BarCodeType: NativeModules.CameraManager.BarCodeType,
  Type: NativeModules.CameraManager.Type,
  CaptureMode: NativeModules.CameraManager.CaptureMode,
  CaptureTarget: NativeModules.CameraManager.CaptureTarget,
  Orientation: NativeModules.CameraManager.Orientation,
  FlashMode: NativeModules.CameraManager.FlashMode,
  TorchMode: NativeModules.CameraManager.TorchMode,
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
    ])
  },

  mixins: [NativeMethodsMixin],

  viewConfig: {
    uiViewClassName: 'UIView',
    validAttributes: ReactNativeViewAttributes.UIView
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
    NativeModules.CameraManager.checkDeviceAuthorizationStatus((function(err, isAuthorized) {
      this.state.isAuthorized = isAuthorized;
      this.setState(this.state);
    }).bind(this));
    this.cameraBarCodeReadListener = DeviceEventEmitter.addListener('CameraBarCodeRead', this._onBarCodeRead);
  },

  componentWillUnmount() {
    this.cameraBarCodeReadListener.remove();
    
    if (this.state.isRecording) {
      this.stopRecording();
    }
  },

  render() {
    var style = flattenStyle([styles.base, this.props.style]);

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

    var nativeProps = merge(this.props, {
      style,
      aspect: aspect,
      type: type,
      orientation: orientation,
      flashMode: flashMode,
      torchMode: torchMode
    });

    return <RCTCamera {... nativeProps} />
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
      target: this.props.captureTarget
    }, options);

    if (typeof options.mode === 'string') {
      options.mode = constants.CaptureMode[options.mode];
    }
    
    if (options.mode === constants.CaptureMode.video) {
      options.totalSeconds = (options.totalSeconds > -1 ? options.totalSeconds : -1);
      options.preferredTimeScale = options.preferredTimeScale || 30;
      this.setState({ isRecording: true });
    }

    if (typeof options.target === 'string') {
      options.target = constants.CaptureTarget[options.target];
    }

    NativeModules.CameraManager.capture(options, cb);
  },

  stopCapture() {
    if (this.state.isRecording) {
      NativeModules.CameraManager.stopCapture();
      this.setState({ isRecording: false });
    }
  }

});

var RCTCamera = createReactNativeComponentClass({
  validAttributes: merge(ReactNativeViewAttributes.UIView, {
    aspect: true,
    type: true,
    orientation: true,
    flashMode: true,
    torchMode: true
  }),
  uiViewClassName: 'RCTCamera',
});

var styles = StyleSheet.create({
  base: { },
});

Camera.constants = constants;
module.exports = Camera;
