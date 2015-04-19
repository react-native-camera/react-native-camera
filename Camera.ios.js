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

  getInitialState: function() {
    return {
      isAuthorized: false,
      aspect: this.props.aspect || 'Fill',
      type: this.props.type || 'Back',
      orientation: this.props.orientation || 'Portrait'
    };
  },

  componentWillMount: function() {
    NativeModules.CameraManager.checkDeviceAuthorizationStatus((function(err, isAuthorized) {
      this.state.isAuthorized = isAuthorized;
      this.setState(this.state);
    }).bind(this));
    this.cameraBarCodeReadListener = DeviceEventEmitter.addListener('CameraBarCodeRead', this._onBarCodeRead);
  },

  componentWillUnmount: function() {
    this.cameraBarCodeReadListener.remove();
  },

  render: function() {
    var style = flattenStyle([styles.base, this.props.style]);

    aspect = NativeModules.CameraManager.aspects[this.state.aspect];
    type = NativeModules.CameraManager.cameras[this.state.type];
    orientation = NativeModules.CameraManager.orientations[this.state.orientation];

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

  switch: function() {
    this.state.type = this.state.type == 'Back' ? 'Front' : 'Back';
    this.setState(this.state);
  },

  takePicture: function(cb) {
    NativeModules.CameraManager.takePicture(cb);
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

module.exports = Camera;
