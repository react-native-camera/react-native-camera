# RCTCamera

As of 1.0.0 release, RCTCamera is deprecated. Please use RNCamera for the latest fixes and improvements.

## Usage

All you need is to `import` `Camera` from the `react-native-camera` module and then use the
`<Camera/>` tag.

```javascript
'use strict';
import React, { Component } from 'react';
import { AppRegistry, Dimensions, StyleSheet, Text, TouchableHighlight, View } from 'react-native';
import Camera from 'react-native-camera';

class BadInstagramCloneApp extends Component {
  _requestPermissions = async () => {
    if (Platform.OS === 'android') {
      const result = await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.CAMERA)
      return result === PermissionsAndroid.RESULTS.GRANTED || result === true
    }
    return true
  }

  _takePicture = async () => {
    if (this.camera) {
      const options = { quality: 0.5, base64: true }
      const data = await this.camera.takePictureAsync(options)
      console.log(data.uri)
    }
  }

  _onBarCodeRead = (e) => {
    console.log(`Barcode Found! Type: ${e.type}\nData: ${e.data}`)
  }

  componentDidMount = () => {
    ({ _, status }) => {
      if (status !== 'PERMISSION_GRANTED') {
        this._requestPermissions()
      }
    }
  }

  render() {
    return (
      <View style={styles.container}>
        <Camera
          ref={(ref) => {
            this.camera = ref;
          }}
          onBarCodeRead={(e) => this._onBarCodeRead(e)}
          style={styles.preview}>
        <Text style={styles.capture} onPress={() => this._takePicture()}>[CAPTURE]</Text>
      </Camera>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    flexDirection: 'column',
    backgroundColor: 'black'
  },
  preview: {
    flex: 1,
    justifyContent: 'flex-end',
    alignItems: 'center'
  },
  capture: {
    flex: 0,
    backgroundColor: '#fff',
    borderRadius: 5,
    padding: 15,
    paddingHorizontal: 20,
    alignSelf: 'center',
    margin: 20
  }
});

AppRegistry.registerComponent('BadInstagramCloneApp', () => BadInstagramCloneApp);
```

## Properties

#### `aspect`

Values: `Camera.constants.Aspect.fit` or `"fit"`, `Camera.constants.Aspect.fill` or `"fill"` (default), `Camera.constants.Aspect.stretch` or `"stretch"`

The `aspect` property allows you to define how your viewfinder renders the camera's view. For instance, if you have a square viewfinder and you want to fill it entirely, you have two options: `"fill"`, where the aspect ratio of the camera's view is preserved by cropping the view or `"stretch"`, where the aspect ratio is skewed in order to fit the entire image inside the viewfinder. The other option is `"fit"`, which ensures the camera's entire view fits inside your viewfinder without altering the aspect ratio.

#### `cropToPreview`

Values: `true` or `false` (default)

Will crop the captured image to match the content that is displayed in the preview view. Works on both `Android` and `iOS`. Will be ignored if `captureMode` is other then `Camera.constants.CaptureMode.still`.

#### `iOS` `captureAudio`

Values: `true` (Boolean), `false` (default)

_Applies to video capture mode only._ Specifies whether or not audio should be captured with the video.

#### `captureMode`

Values: `Camera.constants.CaptureMode.still` (default), `Camera.constants.CaptureMode.video`

The type of capture that will be performed by the camera - either a still image or video.

#### `captureTarget`

Values: `Camera.constants.CaptureTarget.cameraRoll` (default), `Camera.constants.CaptureTarget.disk`, `Camera.constants.CaptureTarget.temp`, ~~`Camera.constants.CaptureTarget.memory`~~ (deprecated),

This property allows you to specify the target output of the captured image data. The disk output has been shown to improve capture response time, so that is the recommended value. When using the deprecated memory output, the image binary is sent back as a base64-encoded string.

#### `captureQuality`

Values: `Camera.constants.CaptureQuality.high` or `"high"` (default), `Camera.constants.CaptureQuality.medium` or `"medium"`, `Camera.constants.CaptureQuality.low` or `"low"`, `Camera.constants.CaptureQuality.photo` or `"photo"`, `Camera.constants.CaptureQuality["1080p"]` or `"1080p"`, `Camera.constants.CaptureQuality["720p"]` or `"720p"`, `Camera.constants.CaptureQuality["480p"]` or `"480p"`.

This property allows you to specify the quality output of the captured image or video. By default the quality is set to high.

When choosing more-specific quality settings (1080p, 720p, 480p), note that each platform and device supports different valid picture/video sizes, and actual resolution within each of these quality settings might differ. There should not be too much variance (if any) for iOS; 1080p should give 1920x1080, 720p should give 1280x720, and 480p should give 640x480 (note that iOS 480p therefore is NOT the typical 16:9 HD aspect ratio, and the typically-HD camera preview screen may differ greatly in aspect from what you actually record!!). For Android, expect more variance: on most Androids, 1080p _should_ give 1920x1080 and 720p _should_ give 1280x720; however, 480p will at "best" be 853x480 (16:9 HD aspect ratio), but falls back/down to 800x480, 720x480, or "worse", depending on what is closest-but-less-than 853x480 and available on the actual device. If your application requires knowledge of the precise resolution of the output image/video, you might consider manually determine the actual resolution itself after capture has completed (particularly for 480p on Android).

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
- `ean13` (`iOS` converts `upca` barcodes to `ean13` by adding a leading 0)
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
Example: `<Camera barCodeTypes={[Camera.constants.BarCodeType.qr]} />`

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

iOS: Called when a touch focus gesture has been made.
By default, `onFocusChanged` is not defined and tap-to-focus is disabled.

Android: This callback is not yet implemented. However, Android will
automatically do tap-to-focus if the device supports auto-focus; there is
currently no way to manage this from javascript.

To get autofocus/tap to focus functionalities working correctly in android
make sure that the proper permissions are set in your `AndroidManifest.xml`:

```java
<uses-feature android:name="android.hardware.camera" />
<uses-feature android:name="android.hardware.camera.autofocus" />
```

#### `iOS` `defaultOnFocusComponent`

Values:
`true` (default)
`false`

If `defaultOnFocusComponent` set to false, default internal implementation of visual feedback for tap-to-focus gesture will be disabled.

#### `iOS` `onZoomChanged: Event { nativeEvent: { velocity, zoomFactor } }`

iOS: Called when focus has changed.
By default, `onZoomChanged` is not defined and pinch-to-zoom is disabled.

Android: This callback is not yet implemented. However, Android will
automatically handle pinch-to-zoom; there is currently no way to manage this
from javascript.

#### `iOS` `keepAwake`

If set to `true`, the device will not sleep while the camera preview is visible. This mimics the behavior of the default camera app, which keeps the device awake while open.

#### `Android` `clearWindowBackground`

Values:
`true`
`false` (default)

If you encounter performance issue while using a window background drawable (typically defined in theme to emulate splashscreen behavior), set this to true to automatically clear window background once camera is started.

#### `Android` `permissionDialogTitle`

Starting on android M individual permissions must be granted for certain services, the camera is one of them, you can use this to change the title of the dialog prompt requesting permissions.

#### `Android` `permissionDialogMessage`

Starting on android M individual permissions must be granted for certain services, the camera is one of them, you can use this to change the content of the dialog prompt requesting permissions.

#### `notAuthorizedView`

By default a `Camera not authorized` message will be displayed when access to the camera has been denied, if set displays the passed react element instead of the default one.

#### `pendingAuthorizationView`

By default a <ActivityIndicator> will be displayed while the component is waiting for the user to grant/deny access to the camera, if set displays the passed react element instead of the default one.

#### `pendingAuthorizationView`

#### `mirrorImage`

If set to `true`, the image returned will be mirrored.

#### `mirrorVideo`

If set to `true`, the video returned will be mirrored.

#### `fixOrientation` (_deprecated_)

If set to `true`, the image returned will be rotated to the _right way up_. WARNING: It uses a significant amount of memory and my cause your application to crash if the device cannot provide enough RAM to perform the rotation.

(_If you find that you need to use this option because your images are incorrectly oriented by default,
could please submit a PR and include the make model of the device. We believe that it's not
required functionality any more and would like to remove it._)

## Component instance methods

You can access component methods by adding a `ref` (ie. `ref="camera"`) prop to your `<Camera>` element, then you can use `this.refs.camera.capture(cb)`, etc. inside your component.

#### `capture([options]): Promise`

Captures data from the camera. What is captured is based on the `captureMode` and `captureTarget` props. `captureMode` tells the camera whether you want a still image or video. `captureTarget` allows you to specify how you want the data to be captured and sent back to you. See `captureTarget` under Properties to see the available values.

Supported options:

- `audio` (See `captureAudio` under Properties)
- `mode` (See `captureMode` under Properties)
- `target` (See `captureTarget` under Properties)
- `metadata` This is metadata to be added to the captured image.
- `metadata.location` This is the object returned from `navigator.geolocation.getCurrentPosition()` (React Native's geolocation polyfill). It will add GPS metadata to the image.
- `rotation` This will rotate the image by the number of degrees specified.
- `jpegQuality` (integer between 1 and 100) This property is used to compress the output jpeg file with 100% meaning no jpeg compression will be applied.
- `totalSeconds` This will limit video length by number of seconds specified. Only works in video capture mode.

The promise will be fulfilled with an object with some of the following properties:

- `data`: Returns a base64-encoded string with the capture data (only returned with the deprecated `Camera.constants.CaptureTarget.memory`)
- `path`: Returns the path of the captured image or video file on disk
- `width`: (not yet implemented for Android video) returns the image or video file's frame width (taking image orientation into account)
- `height`: (not yet implemented for Android video) returns the image or video file's frame height (taking image orientation into account)
- `duration`: (currently iOS video only) video file duration
- `size`: (currently iOS video only) video file size (in bytes)

#### `iOS` `getFOV(): Promise`

Returns the camera's current field of view.

#### `hasFlash(): Promise`

Returns whether or not the camera has flash capabilities.

#### `stopCapture()`

Ends the current capture session for video captures. Only applies when the current `captureMode` is `video`.

#### `stopPreview()`

Stops the camera preview from running, and natively will make the current capture session pause.

#### `startPreview()`

Starts the camera preview again if previously stopped.

## Component static methods

#### `iOS` `Camera.checkDeviceAuthorizationStatus(): Promise`

Exposes the native API for checking if the device has authorized access to the camera (camera and microphone permissions). Can be used to call before loading the Camera component to ensure proper UX. The promise will be fulfilled with `true` or `false` depending on whether the device is authorized. Note, [as of iOS 10](https://developer.apple.com/library/content/documentation/AudioVideo/Conceptual/PhotoCaptureGuide/#//apple_ref/doc/uid/TP40017511-CH1-DontLinkElementID_3), you will need to add `NSCameraUsageDescription` and `NSMicrophoneUsageDescription` to your XCode project's Info.plist file or you might experience a crash.

#### `iOS` `Camera.checkVideoAuthorizationStatus(): Promise`

The same as `Camera.checkDeviceAuthorizationStatus()` but only checks the camera permission. Note, as of iOS 10, you will need to add `NSCameraUsageDescription` to your XCode project's Info.plist file or you might experience a crash.

#### `iOS` `Camera.checkAudioAuthorizationStatus(): Promise`

The same as `Camera.checkDeviceAuthorizationStatus()` but only checks the microphone permission. Note, as of iOS 10, you will need to add `NSMicrophoneUsageDescription` to your XCode project's Info.plist file or you might experience a crash.

## Subviews

This component supports subviews, so if you wish to use the camera view as a background or if you want to layout buttons/images/etc. inside the camera then you can do that.

## Example

To see more of the `RCTCamera` in action, you can check out the source in [Example](https://github.com/lwansbrough/react-native-camera/tree/master/Example) folder.

## Open Collective

We are just beginning a funding campaign for react-native-camera. Contributions are greatly appreciated. When we gain more than \$250 we will begin distributing funds to core maintainers in a fully transparent manner. Feedback for this process is welcomed, we will continue to evolve the strategy as we grow and learn more.

### Backers

Support us with a monthly donation and help us continue our activities. [[Become a backer](https://opencollective.com/react-native-camera#backer)]

<a href="https://opencollective.com/react-native-camera/backer/0/website" target="_blank"><img src="https://opencollective.com/react-native-camera/backer/0/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/backer/1/website" target="_blank"><img src="https://opencollective.com/react-native-camera/backer/1/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/backer/2/website" target="_blank"><img src="https://opencollective.com/react-native-camera/backer/2/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/backer/3/website" target="_blank"><img src="https://opencollective.com/react-native-camera/backer/3/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/backer/4/website" target="_blank"><img src="https://opencollective.com/react-native-camera/backer/4/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/backer/5/website" target="_blank"><img src="https://opencollective.com/react-native-camera/backer/5/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/backer/6/website" target="_blank"><img src="https://opencollective.com/react-native-camera/backer/6/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/backer/7/website" target="_blank"><img src="https://opencollective.com/react-native-camera/backer/7/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/backer/8/website" target="_blank"><img src="https://opencollective.com/react-native-camera/backer/8/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/backer/9/website" target="_blank"><img src="https://opencollective.com/react-native-camera/backer/9/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/backer/10/website" target="_blank"><img src="https://opencollective.com/react-native-camera/backer/10/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/backer/11/website" target="_blank"><img src="https://opencollective.com/react-native-camera/backer/11/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/backer/12/website" target="_blank"><img src="https://opencollective.com/react-native-camera/backer/12/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/backer/13/website" target="_blank"><img src="https://opencollective.com/react-native-camera/backer/13/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/backer/14/website" target="_blank"><img src="https://opencollective.com/react-native-camera/backer/14/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/backer/15/website" target="_blank"><img src="https://opencollective.com/react-native-camera/backer/15/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/backer/16/website" target="_blank"><img src="https://opencollective.com/react-native-camera/backer/16/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/backer/17/website" target="_blank"><img src="https://opencollective.com/react-native-camera/backer/17/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/backer/18/website" target="_blank"><img src="https://opencollective.com/react-native-camera/backer/18/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/backer/19/website" target="_blank"><img src="https://opencollective.com/react-native-camera/backer/19/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/backer/20/website" target="_blank"><img src="https://opencollective.com/react-native-camera/backer/20/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/backer/21/website" target="_blank"><img src="https://opencollective.com/react-native-camera/backer/21/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/backer/22/website" target="_blank"><img src="https://opencollective.com/react-native-camera/backer/22/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/backer/23/website" target="_blank"><img src="https://opencollective.com/react-native-camera/backer/23/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/backer/24/website" target="_blank"><img src="https://opencollective.com/react-native-camera/backer/24/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/backer/25/website" target="_blank"><img src="https://opencollective.com/react-native-camera/backer/25/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/backer/26/website" target="_blank"><img src="https://opencollective.com/react-native-camera/backer/26/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/backer/27/website" target="_blank"><img src="https://opencollective.com/react-native-camera/backer/27/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/backer/28/website" target="_blank"><img src="https://opencollective.com/react-native-camera/backer/28/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/backer/29/website" target="_blank"><img src="https://opencollective.com/react-native-camera/backer/29/avatar.svg"></a>

### Sponsors

Become a sponsor and get your logo on our README on Github with a link to your site. [[Become a sponsor](https://opencollective.com/react-native-camera#sponsor)]

<a href="https://opencollective.com/react-native-camera/sponsor/0/website" target="_blank"><img src="https://opencollective.com/react-native-camera/sponsor/0/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/sponsor/1/website" target="_blank"><img src="https://opencollective.com/react-native-camera/sponsor/1/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/sponsor/2/website" target="_blank"><img src="https://opencollective.com/react-native-camera/sponsor/2/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/sponsor/3/website" target="_blank"><img src="https://opencollective.com/react-native-camera/sponsor/3/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/sponsor/4/website" target="_blank"><img src="https://opencollective.com/react-native-camera/sponsor/4/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/sponsor/5/website" target="_blank"><img src="https://opencollective.com/react-native-camera/sponsor/5/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/sponsor/6/website" target="_blank"><img src="https://opencollective.com/react-native-camera/sponsor/6/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/sponsor/7/website" target="_blank"><img src="https://opencollective.com/react-native-camera/sponsor/7/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/sponsor/8/website" target="_blank"><img src="https://opencollective.com/react-native-camera/sponsor/8/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/sponsor/9/website" target="_blank"><img src="https://opencollective.com/react-native-camera/sponsor/9/avatar.svg"></a>

---

Thanks to Brent Vatne (@brentvatne) for the `react-native-video` module which provided me with a great example of how to set up this module.
