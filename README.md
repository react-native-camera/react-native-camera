# react-native-camera

A camera viewport for React Native. This module is currently in the very early stages of development and does not support image capture at this time, though it is coming.

## Getting started

1. Fetch from NPM: `npm install react-native-camera --save`
2. In XCode, right click `Libraries` and `Add Files to ______`
3. Add `libRCTCamera.a` to `Build Phases -> Link Binary With Libraries`

## Usage

All you need is to `require` the `react-native-camera` module and then use the
`<Camera/>` tag.

```javascript
var React = require('react-native');
var {
  AppRegistry,
  StyleSheet,
  Text,
  View,
} = React;
var Camera = require('react-native-camera');

var cameraApp = React.createClass({
  render: function() {
    return (
      <View>
        <Camera style={{height: 200, width: 200}}/>
      </View>
    );
  }
});

AppRegistry.registerComponent('cameraApp', () => cameraApp);
```

------------

Thanks to Brent Vatne (@brentvatne) for the `react-native-video` module which provided me with a great example of how to set up this module.
