# Looking for contributors
Hey there, I'm looking for active contributors to help move the development of this branch forward in a stable and timely fashion. I haven't had a need for this module in quite some time and so my person time is not being allocated to it. If you are interested in contributing more actively, please contact me (same username on Twitter, Facebook, etc.) Thanks!

# react-native-camera [![npm version](https://badge.fury.io/js/react-native-camera.svg)](http://badge.fury.io/js/react-native-camera) [![Gitter](https://badges.gitter.im/lwansbrough/react-native-camera.svg)](https://gitter.im/lwansbrough/react-native-camera)

A camera module for React Native.

#### Breaking Changes
react-native header imports have changed in v0.40, and that means breaking changes for all! [Reference PR & Discussion](https://github.com/lwansbrough/react-native-camera/pull/544).
- if on react-native < 0.40: `npm i react-native-camera@0.4`
- if on react-native >= 0.40 `npm i react-native-camera@0.5`

![5j2jduk](https://cloud.githubusercontent.com/assets/2302315/22190752/6bc6ccd0-e0da-11e6-8e2f-6f22a3567a57.gif)

## Getting started

### Requirements
1. JDK >= 1.7 (if you run on 1.6 you will get an error on "_cameras = new HashMap<>();")
2. With iOS 10 and higher you need to add the "Privacy - Camera Usage Description" key to the info.plist of your project. This should be found in 'your_project/ios/your_project/Info.plist'.  Add the following code:
```
<key>NSCameraUsageDescription</key>
<string>Your message to user when the camera is accessed for the first time</string>

<!-- Include this only if you are planning to use the camera roll -->
<key>NSPhotoLibraryUsageDescription</key>
<string>Your message to user when the photo library is accessed for the first time</string>

<!-- Include this only if you are planning to use the microphone for video recording -->
<key>NSMicrophoneUsageDescription</key>
<string>Your message to user when the microsphone is accessed for the first time</string>
```

### Mostly automatic install with react-native
1. `npm install react-native-camera@https://github.com/lwansbrough/react-native-camera.git --save`
3. `react-native link react-native-camera`

### Mostly automatic install with CocoaPods
1. `npm install react-native-camera@https://github.com/lwansbrough/react-native-camera.git --save`
2. Add the plugin dependency to your Podfile, pointing at the path where NPM installed it:
```
pod 'react-native-camera', path: '../node_modules/react-native-camera'
```
3. Run `pod install`

### Manual install
#### iOS
1. `npm install react-native-camera@https://github.com/lwansbrough/react-native-camera.git --save`
2. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
3. Go to `node_modules` ➜ `react-native-camera` and add `RCTCamera.xcodeproj`
4. In XCode, in the project navigator, select your project. Add `libRCTCamera.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
5. Click `RCTCamera.xcodeproj` in the project navigator and go the `Build Settings` tab. Make sure 'All' is toggled on (instead of 'Basic'). In the `Search Paths` section, look for `Header Search Paths` and make sure it contains both `$(SRCROOT)/../../react-native/React` and `$(SRCROOT)/../../../React` - mark both as `recursive`.
5. Run your project (`Cmd+R`)


#### Android
1. `npm install react-native-camera@https://github.com/lwansbrough/react-native-camera.git --save`
2. Open up `android/app/src/main/java/[...]/MainApplication.java
  - Add `import com.lwansbrough.RCTCamera.RCTCameraPackage;` to the imports at the top of the file
  - Add `new RCTCameraPackage()` to the list returned by the `getPackages()` method. Add a comma to the previous item if there's already something there.

3. Append the following lines to `android/settings.gradle`:

	```
	include ':react-native-camera'
	project(':react-native-camera').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-camera/android')
	```

4. Insert the following lines inside the dependencies block in `android/app/build.gradle`:

	```
    compile project(':react-native-camera')
	```
5. Declare the permissions in your Android Manifest

  ```
  <uses-permission android:name="android.permission.CAMERA" />
  <uses-feature android:name="android.hardware.camera" />
  ```

## Usage

All you need is to `require` the `react-native-camera` module and then use the
`<Camera/>` tag.

```javascript
'use strict';
import React, { Component } from 'react';
import {
  AppRegistry,
  Dimensions,
  StyleSheet,
  Text,
  TouchableHighlight,
  View
} from 'react-native';
import Camera from 'react-native-camera';

class BadInstagramCloneApp extends Component {
  render() {
    return (
      <View style={styles.container}>
        <Camera
          ref={(cam) => {
            this.camera = cam;
          }}
          style={styles.preview}
          aspect={Camera.constants.Aspect.fill}>
          <Text style={styles.capture} onPress={this.takePicture.bind(this)}>[CAPTURE]</Text>
        </Camera>
      </View>
    );
  }

  takePicture() {
    this.camera.capture()
      .then((data) => console.log(data))
      .catch(err => console.error(err));
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1
  },
  preview: {
    flex: 1,
    justifyContent: 'flex-end',
    alignItems: 'center',
    height: Dimensions.get('window').height,
    width: Dimensions.get('window').width
  },
  capture: {
    flex: 0,
    backgroundColor: '#fff',
    borderRadius: 5,
    color: '#000',
    padding: 10,
    margin: 40
  }
});

AppRegistry.registerComponent('BadInstagramCloneApp', () => BadInstagramCloneApp);
```

## Properties

#### `aspect`

Values: `Camera.constants.Aspect.fit` or `"fit"`, `Camera.constants.Aspect.fill` or `"fill"` (default), `Camera.constants.Aspect.stretch` or `"stretch"`

The `aspect` property allows you to define how your viewfinder renders the camera's view. For instance, if you have a square viewfinder and you want to fill the it entirely, you have two options: `"fill"`, where the aspect ratio of the camera's view is preserved by cropping the view or `"stretch"`, where the aspect ratio is skewed in order to fit the entire image inside the viewfinder. The other option is `"fit"`, which ensures the camera's entire view fits inside your viewfinder without altering the aspect ratio.

#### `iOS` `captureAudio`

Values: `true` (Boolean), `false` (default)

*Applies to video capture mode only.* Specifies whether or not audio should be captured with the video.


#### `captureMode`

Values: `Camera.constants.CaptureMode.still` (default), `Camera.constants.CaptureMode.video`

The type of capture that will be performed by the camera - either a still image or video.

#### `captureTarget`

Values: `Camera.constants.CaptureTarget.cameraRoll` (default), `Camera.constants.CaptureTarget.disk`, `Camera.constants.CaptureTarget.temp`, ~~`Camera.constants.CaptureTarget.memory`~~ (deprecated),

This property allows you to specify the target output of the captured image data. The disk output has been shown to improve capture response time, so that is the recommended value. When using the deprecated memory output, the image binary is sent back as a base64-encoded string.

#### `captureQuality`

Values: `Camera.constants.CaptureQuality.high` or `"high"` (default), `Camera.constants.CaptureQuality.medium` or `"medium"`, `Camera.constants.CaptureQuality.low` or `"low"`, `Camera.constants.CaptureQuality.photo` or `"photo"`, `Camera.constants.CaptureQuality["1080p"]` or `"1080p"`, `Camera.constants.CaptureQuality["720p"]` or `"720p"`, `Camera.constants.CaptureQuality["480p"]` or `"480p"`.

This property allows you to specify the quality output of the captured image or video. By default the quality is set to high.

When choosing more-specific quality settings (1080p, 720p, 480p), note that each platform and device supports different valid picture/video sizes, and actual resolution within each of these quality settings might differ. There should not be too much variance (if any) for iOS; 1080p should give 1920x1080, 720p should give 1280x720, and 480p should give 640x480 (note that iOS 480p therefore is NOT the typical 16:9 HD aspect ratio, and the typically-HD camera preview screen may differ greatly in aspect from what you actually record!!). For Android, expect more variance: on most Androids, 1080p *should* give 1920x1080 and 720p *should* give 1280x720; however, 480p will at "best" be 853x480 (16:9 HD aspect ratio), but falls back/down to 800x480, 720x480, or "worse", depending on what is closest-but-less-than 853x480 and available on the actual device. If your application requires knowledge of the precise resolution of the output image/video, you might consider manually determine the actual resolution itself after capture has completed (particularly for 480p on Android).

Android also supports `Camera.constants.CaptureQuality.preview` or `"preview"` which matches the output image to the same one used in the preview

#### `type`

Values: `Camera.constants.Type.front` or `"front"`, `Camera.constants.Type.back` or `"back"` (default)

Use the `type` property to specify which camera to use.


#### `orientation`

Values:
`Camera.constants.Orientation.auto` or `"auto"` (default),
`Camera.constants.Orientation.landscapeLeft` or `"landscapeLeft"`, `Camera.constants.Orientation.landscapeRight` or `"landscapeRight"`, `Camera.constants.Orientation.portrait` or `"portrait"`, `Camera.constants.Orientation.portraitUpsideDown` or `"portraitUpsideDown"`

The `orientation` property allows you to specify the current orientation of the phone to ensure the viewfinder is "the right way up."

#### `Android` `playSoundOnCapture`

Values: `true` (default) or `false`

This property allows you to specify whether a shutter sound is played on capture. It is currently android only, pending [a reasonable mute implementation](http://stackoverflow.com/questions/4401232/avfoundation-how-to-turn-off-the-shutter-sound-when-capturestillimageasynchrono) in iOS.

#### `onBarCodeRead`

Will call the specified method when a barcode is detected in the camera's view.

Event contains `data` (the data in the barcode) and `bounds` (the rectangle which outlines the barcode.)

The following barcode types can be recognised:

- `aztec`
- `code128`
- `code39`
- `code39mod43`
- `code93`
- `ean13`
- `ean8`
- `pdf417`
- `qr`
- `upce`
- `interleaved2of5` (when available)
- `itf14` (when available)
- `datamatrix` (when available)

The barcode type is provided in the `data` object.

#### `barCodeTypes`

An array of barcode types to search for. Defaults to all types listed above. No effect if `onBarCodeRead` is undefined.

#### `flashMode`

Values:
`Camera.constants.FlashMode.on`,
`Camera.constants.FlashMode.off`,
`Camera.constants.FlashMode.auto`

Use the `flashMode` property to specify the camera flash mode.

#### `torchMode`

Values:
`Camera.constants.TorchMode.on`,
`Camera.constants.TorchMode.off`,
`Camera.constants.TorchMode.auto`

Use the `torchMode` property to specify the camera torch mode.

#### `onFocusChanged: Event { nativeEvent: { touchPoint: { x, y } }`

Called when a touch focus gesture has been made.
By default, `onFocusChanged` is not defined and tap-to-focus is disabled.

#### `defaultOnFocusComponent`

Values:
`true` (default)
`false`

If `defaultOnFocusComponent` set to false, default internal implementation of visual feedback for tap-to-focus gesture will be disabled.

#### `onZoomChanged: Event { nativeEvent: { velocity, zoomFactor } }`

Called when focus has changed.
By default, `onZoomChanged` is not defined and pinch-to-zoom is disabled.

#### `iOS` `keepAwake`

If set to `true`, the device will not sleep while the camera preview is visible. This mimics the behavior of the default camera app, which keeps the device awake while open.

#### `mirrorImage`

If set to `true`, the image returned will be mirrored.

## Component instance methods

You can access component methods by adding a `ref` (ie. `ref="camera"`) prop to your `<Camera>` element, then you can use `this.refs.camera.capture(cb)`, etc. inside your component.

#### `capture([options]): Promise`

Captures data from the camera. What is captured is based on the `captureMode` and `captureTarget` props. `captureMode` tells the camera whether you want a still image or video. `captureTarget` allows you to specify how you want the data to be captured and sent back to you. See `captureTarget` under Properties to see the available values.

Supported options:

 - `audio` (See `captureAudio` under Properties)
 - `mode` (See  `captureMode` under Properties)
 - `target` (See `captureTarget` under Properties)
 - `metadata` This is metadata to be added to the captured image.
   - `location` This is the object returned from `navigator.geolocation.getCurrentPosition()` (React Native's geolocation polyfill). It will add GPS metadata to the image.
 - `rotation` This will rotate the image by the number of degrees specified.

The promise will be fulfilled with an object with some of the following properties:

 - `data`: Returns a base64-encoded string with the capture data (only returned with the deprecated `Camera.constants.CaptureTarget.memory`)
 - `path`: Returns the path of the captured image or video file on disk
 - `width`: (currently iOS video only) returns the video file's frame width
 - `height`: (currently iOS video only) returns the video file's frame height
 - `duration`: (currently iOS video only) video file duration
 - `size`: (currently iOS video only) video file size (in bytes)

#### `iOS` `getFOV(): Promise`

Returns the camera's current field of view.

#### `hasFlash(): Promise`

Returns whether or not the camera has flash capabilities.

#### `stopCapture()`

Ends the current capture session for video captures. Only applies when the current `captureMode` is `video`.

## Component static methods

#### `iOS` `Camera.checkDeviceAuthorizationStatus(): Promise`

Exposes the native API for checking if the device has authorized access to the camera (camera and microphone permissions). Can be used to call before loading the Camera component to ensure proper UX. The promise will be fulfilled with `true` or `false` depending on whether the device is authorized.

#### `iOS` `Camera.checkVideoAuthorizationStatus(): Promise`

The same as `Camera.checkDeviceAuthorizationStatus()` but only checks the camera permission.

#### `iOS` `Camera.checkAudioAuthorizationStatus(): Promise`

The same as `Camera.checkDeviceAuthorizationStatus()` but only checks the microphone permission.

## Subviews
This component supports subviews, so if you wish to use the camera view as a background or if you want to layout buttons/images/etc. inside the camera then you can do that.

## Example

To see more of the `react-native-camera` in action, you can check out the source in [Example](https://github.com/lwansbrough/react-native-camera/tree/master/Example) folder.

------------

Thanks to Brent Vatne (@brentvatne) for the `react-native-video` module which provided me with a great example of how to set up this module.
