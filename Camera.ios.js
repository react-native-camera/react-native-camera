var React = require('React');
var DeviceEventEmitter = require('RCTDeviceEventEmitter');
var NativeModules = require('NativeModules');
var ReactIOSViewAttributes = require('ReactIOSViewAttributes');
var StyleSheet = require('StyleSheet');
var createReactIOSNativeComponentClass = require('createReactIOSNativeComponentClass');
var PropTypes = require('ReactPropTypes');
var StyleSheetPropType = require('StyleSheetPropType');
var NativeMethodsMixin = require('NativeMethodsMixin');
var flattenStyle = require('flattenStyle');
var merge = require('merge');

var constants = {
  Aspect: NativeModules.CameraManager.Aspect,
  Camera: NativeModules.CameraManager.Camera,
  CaptureMode: NativeModules.CameraManager.CaptureMode,
  CaptureTarget: NativeModules.CameraManager.CaptureTarget,
  Orientation: NativeModules.CameraManager.Orientation
};

var Camera = React.createClass({
  propTypes: {
    aspect: PropTypes.string,
    type: PropTypes.string,
    orientation: PropTypes.string,
  },

  mixins: [NativeMethodsMixin],

  viewConfig: {
    uiViewClassName: 'UIView',
    validAttributes: ReactIOSViewAttributes.UIView
  },

  getDefaultProps() {
    return {
      aspect: constants.Aspect.fill,
      type: constants.Camera.back,
      orientation: constants.Orientation.portrait,
      captureMode: constants.CaptureMode.still,
      captureTarget: constants.CaptureTarget.memory
    };
  },

  getInitialState() {
    return {
      isAuthorized: false
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
  },

  render() {
    var style = flattenStyle([styles.base, this.props.style]);

    var aspect = this.props.aspect,
        type = this.props.type,
        orientation = this.props.orientation;

    switch (aspect.toLowerCase()) {
      case 'fill':
        aspect = constants.Aspect.fill;
        break;
      case 'fit':
        aspect = constants.Aspect.fit;
        break;
      case 'stretch':
        aspect = constants.Aspect.stretch;
        break;
      default:
        break;
    }

    if (typeof orientation !== 'number') {
      switch (orientation.toLowerCase()) {
        case 'landscapeleft':
          orientation = constants.Orientation.landscapeLeft;
          break;
        case 'landscaperight':
          orientation = constants.Orientation.landscapeRight;
          break;
        case 'portrait':
          orientation = constants.Orientation.portrait;
          break;
        case 'portraitUpsideDown':
          orientation = constants.Orientation.portraitUpsideDown;
          break;
      }
    }

    if (typeof type !== 'number') {
      type = constants.Camera[type];
    }

    var nativeProps = merge(this.props, {
      style,
      aspect: aspect,
      type: type,
      orientation: orientation
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
      mode: this.props.captureMode,
      target: this.props.captureTarget
    }, options);

    NativeModules.CameraManager.capture(options, cb);
  }

});

var RCTCamera = createReactIOSNativeComponentClass({
  validAttributes: merge(ReactIOSViewAttributes.UIView, {
    aspect: true,
    type: true,
    orientation: true
  }),
  uiViewClassName: 'RCTCamera',
});

var styles = StyleSheet.create({
  base: { },
});

Camera.constants = constants;
module.exports = Camera;
