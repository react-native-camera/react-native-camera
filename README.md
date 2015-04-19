# react-native-camera

A camera viewport for React Native. This module is currently in the very early stages of development.

## Known Issues
Below is a list of known issues. Pull requests are welcome for any of these issues!

- [Camera module may cause app to crash in simulator](https://github.com/lwansbrough/react-native-camera/issues/8)

## Getting started

1. `npm install react-native-camera@latest --save`
2. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
3. Go to `node_modules` ➜ `react-native-camera` and add `RCTCamera.xcodeproj`
4. In XCode, in the project navigator, select your project. Add `libRCTCamera.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
5. Click `RCTCamera.xcodeproj` in the project navigator and go the `Build Settings` tab. Make sure 'All' is toggled on (instead of 'Basic'). Look for `Header Search Paths` and make sure it contains both `$(SRCROOT)/../react-native/React` and `$(SRCROOT)/../../React` - mark both as `recursive`.
5. Run your project (`Cmd+R`)

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
  TouchableHighlight
} = React;
var Camera = require('react-native-camera');

var cameraApp = React.createClass({
  render: function() {
    return (
      <View>
        <TouchableHighlight onPress={this._switchCamera}>
          <View>
            <Camera
              ref="cam"
              aspect="Stretch"
              type="Front"
              orientation="PortraitUpsideDown"
              style={{height: 200, width: 200}}
            />
          </View>
        </TouchableHighlight>
      </View>
    );
  },
  _switchCamera: function() {
    this.refs.cam.switch();
  }
});

AppRegistry.registerComponent('cameraApp', () => cameraApp);
```

## Properties

#### `aspect`

Values: `Fit`, `Fill` (default), `Stretch`

The `aspect` property allows you to define how your viewfinder renders the camera's view. For instance, if you have a square viewfinder and you want to fill the it entirely, you have two options: `Fill`, where the aspect ratio of the camera's view is preserved by cropping the view or `Stretch`, where the aspect ratio is skewed in order to fit the entire image inside the viewfinder. The other option is `Fit`, which ensures the camera's entire view fits inside your viewfinder without altering the aspect ratio.

#### `type`

Values: `Front`, `Back` (default)

Use the `type` property to specify which camera to use.


#### `orientation`

Values: `LandscapeLeft`, `LandscapeRight`, `Portrait` (default), `PortraitUpsideDown`

The `orientation` property allows you to specify the current orientation of the phone to ensure the viewfinder is "the right way up."

TODO: Add support for an `Auto` value to automatically adjust for orientation changes.

#### `onBarCodeRead`

Will call the specified method when a barcode is detected in the camera's view.

Event contains `data` (the data in the barcode) and `bounds` (the rectangle which outlines the barcode.)

## Component methods

You can access component methods by adding a `ref` (ie. `ref="camera"`) prop to your `<Camera>` element, then you can use `this.refs.camera.switch()`, etc. inside your component.

#### `switch()`

The `switch()` method toggles between the `Front` and `Back` cameras.


#### `takePicture(callback)`

Basic implementation of image capture. This method is subject to change, but currently works by accepting a callback like `function(err, base64EncodedJpeg) { ... }`.

## Subviews
This component supports subviews, so if you wish to use the camera view as a background or if you want to layout buttons/images/etc. inside the camera then you can do that.

## Todo
These are some features I think would be important/beneficial to have included with this module. Pull requests welcome!

- [ ] Video support
- [ ] Flash mode setting
- [ ] Automatic orientation adjustment
- [ ] Tap to focus
- [ ] Optional facial recognition (w/ ability to style box around face)

------------

Thanks to Brent Vatne (@brentvatne) for the `react-native-video` module which provided me with a great example of how to set up this module.
