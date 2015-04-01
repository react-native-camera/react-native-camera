# react-native-camera

A camera viewport for React Native. This module is currently in the very early stages of development and does not support image capture at this time, though it is coming.

## Getting started

1. Fetch from NPM: `npm install react-native-camera --save`
2. In XCode, right click `Libraries` and `Add Files to ______`
3. Add `libRCTCamera.a` to `Build Phases -> Link Binary With Libraries`

## Usage

All you need is to `require` the `react-native-camera` module and then use the
`<Camera/>` tag.

```
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

### Props

#### `aspect`

Values: `Fit`, `Fill` (default), `Stretch`

The `aspect` prop allows you to define how your viewfinder renders the camera's view. For instance, if you have a square viewfinder and you want to fill the it entirely, you have two options: `Fill`, where the aspect ratio of the camera's view is preserved by cropping the view or `Stretch`, where the aspect ratio is skewed in order to fit the entire image inside the viewfinder. The other option is `Fit`, which ensures the camera's entire view fits inside your viewfinder without altering the aspect ratio.

#### `orientation`

Values: `LandscapeLeft`, `LandscapeRight`, `Portrait` (default), `PortraitUpsideDown`

The `orientation` prop allows you to specify the current orientation of the phone to ensure the viewfinder is "the right way up."

TODO: Add support for an `Auto` value to automatically adjust for orientation changes.

------------

Thanks to Brent Vatne (@brentvatne) for the `react-native-video` module which provided me with a great example of how to set up this module.
