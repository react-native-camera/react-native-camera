# React Native Camera [![Backers on Open Collective](https://opencollective.com/react-native-camera/backers/badge.svg)](#backers) [![Sponsors on Open Collective](https://opencollective.com/react-native-camera/sponsors/badge.svg)](#sponsors) [![npm version](https://badge.fury.io/js/react-native-camera.svg)](http://badge.fury.io/js/react-native-camera) [![Gitter](https://badges.gitter.im/lwansbrough/react-native-camera.svg)](https://gitter.im/lwansbrough/react-native-camera)

The comprehensive camera module for React Native. Including photographs, videos, and barcode scanning!

### Experimental
RNCamera (Android/Ios) and FaceDetector(Android only) module  based on Expo camera module (https://docs.expo.io/versions/latest/sdk/camera.html)

You can test and use this from `master` like this:

`import { RNCamera, FaceDetector } from 'react-native-camera';`

#### How to use master branch?
Inside your package.json, use this
`"react-native-camera": "git+https://git@github.com/react-native-community/react-native-camera"`
instead of `"react-native-camera": "^0.12.0"`.

#### Package change

We are getting close to the 1.0 release and changed the package name's from RCTCamera to RNCamera, if you are using master branch, your app will not compile right away.

Steps to adapt:

### iOS

Open your app's XCode project. Expand the Libraries folder in the project navigation and right click and delete the RCTCamera.xcodeproj.

On your project's target, on `Build Phases`, click on libRCTCamera.a and delete (press the - button below).

You can follow the instalation steps for RNCamera on the readme to link the new RNCamera project to your app's XCode project.

You can do it via `react-native link` command or by the manual steps.

Before building and running again, do a complete clean on your project.


### Android

1. On the MainApplication of your Android project change the import of RCTCameraPackage line to:
```
import org.reactnative.camera.RNCameraPackage;
```

2. Inside the getPackages() methods change `new RCTCameraPackage()` to `new RNCameraPackage()`.

### Contributing
- Pull Requests are welcome, if you open a pull request we will do our best to get to it in a timely manner
- Pull Request Reviews and even more welcome! we need help testing, reviewing, and updating open PRs
- If you are interested in contributing more actively, please contact me (same username on Twitter, Facebook, etc.) Thanks!
- We are now on [Open Collective](https://opencollective.com/react-native-camera#sponsor)! Contributions are appreciated and will be used to fund core contributors. [more details](#open-collective)
- If you want to help us coding, join Expo slack https://slack.expo.io/, so we can chat over there. (#react-native-camera)

#### Breaking Changes
##### android build tools has been bumped to 25.0.2, please update (can be done via android cli or AndroidStudio)
##### react-native header imports have changed in v0.40, and that means breaking changes for all! [Reference PR & Discussion](https://github.com/lwansbrough/react-native-camera/pull/544).
- if on react-native < 0.40: `npm i react-native-camera@0.4`
- if on react-native >= 0.40 `npm i react-native-camera@0.6`

##### Permissions
To use the camera on Android you must ask for camera permission:
```java
  <uses-permission android:name="android.permission.CAMERA" />
```
To enable `video recording` feature you have to add the following code to the `AndroidManifest.xml`:
```java
  <uses-permission android:name="android.permission.RECORD_AUDIO"/>
  <uses-permission android:name="android.permission.RECORD_VIDEO"/>
  <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
  <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

![5j2jduk](https://cloud.githubusercontent.com/assets/2302315/22190752/6bc6ccd0-e0da-11e6-8e2f-6f22a3567a57.gif)

## Getting started

### Requirements
1. JDK >= 1.7 (if you run on 1.6 you will get an error on "_cameras = new HashMap<>();")
2. With iOS 10 and higher you need to add the "Privacy - Camera Usage Description" key to the Info.plist of your project. This should be found in 'your_project/ios/your_project/Info.plist'. Add the following code:
```
<key>NSCameraUsageDescription</key>
<string>Your message to user when the camera is accessed for the first time</string>

<!-- Include this only if you are planning to use the camera roll -->
<key>NSPhotoLibraryUsageDescription</key>
<string>Your message to user when the photo library is accessed for the first time</string>

<!-- Include this only if you are planning to use the microphone for video recording -->
<key>NSMicrophoneUsageDescription</key>
<string>Your message to user when the microphone is accessed for the first time</string>
```
3. On Android, you require `buildToolsVersion` of `25.0.2+`. _This should easily and automatically be downloaded by Android Studio's SDK Manager._

4. On iOS 11 and later you need to add `NSPhotoLibraryAddUsageDescription` key to the Info.plist. This key lets you describe the reason your app seeks write-only access to the user’s photo library. Info.plist can be found in 'your_project/ios/your_project/Info.plist'. Add the following code:
```
<!-- Include this only if you are planning to use the camera roll -->
<key>NSPhotoLibraryAddUsageDescription</key>
<string>Your message to user when the photo library is accessed for the first time</string>
```

### Mostly automatic install with react-native
1. `npm install react-native-camera --save`
3. `react-native link react-native-camera`

### Mostly automatic install with CocoaPods
1. `npm install react-native-camera --save`
2. Add the plugin dependency to your Podfile, pointing at the path where NPM installed it:
```obj-c
pod 'react-native-camera', path: '../node_modules/react-native-camera'
```
3. Run `pod install`

### Manual install
#### iOS
1. `npm install react-native-camera --save`
2. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
3. Go to `node_modules` ➜ `react-native-camera` and add `RCTCamera.xcodeproj`
4. In XCode, in the project navigator, select your project. Add `libRCTCamera.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
5. Click `RCTCamera.xcodeproj` in the project navigator and go the `Build Settings` tab. Make sure 'All' is toggled on (instead of 'Basic'). In the `Search Paths` section, look for `Header Search Paths` and make sure it contains both `$(SRCROOT)/../../react-native/React` and `$(SRCROOT)/../../../React` - mark both as `recursive`.
5. Run your project (`Cmd+R`)


### Post install steps
#### iOS

Face Detecion (available on RNCamera on master branch) is optional. If you want it, you are going to need to install GMV, as mentioned in the next section.

If you do not need it, open your app xcode project, on the Project Navigator, expand the RCTCamera project, right click on the FaceDetector folder and delete it (move to trash, if you want). If you keep that folder and do not follow the GMV installation setps, your project will not compile.

#### Installing GMV on iOS (master branch only)
GMV (Google Mobile Vision) is used for Face detection by the iOS RNCamera. You have to link the google frameworks to your project to successfully compile the RCTCamera project.

1. Download:
Google Symbol Utilities: https://www.gstatic.com/cpdc/dbffca986f6337f8-GoogleSymbolUtilities-1.1.1.tar.gz
Google Utilities: https://dl.google.com/dl/cpdc/978f81964b50a7c0/GoogleUtilities-1.3.2.tar.gz
Google Mobile Vision: https://dl.google.com/dl/cpdc/df83c97cbca53eaf/GoogleMobileVision-1.1.0.tar.gz
Google network Utilities: https://dl.google.com/dl/cpdc/54fd7b7ef8fd3edc/GoogleNetworkingUtilities-1.2.2.tar.gz
Google Interchange Utilities: https://dl.google.com/dl/cpdc/1a7f7ba905b2c029/GoogleInterchangeUtilities-1.2.2.tar.gz

2. Extract everything to one folder. Delete "BarcodeDetector" and "copy" folders from Google Mobile Vision.

3. Open XCode, right click on your project and choose "New Group". Rename the new folder to "Frameworks". Right click on "Frameworks" and select "add files to 'YOUR_PROJECT'". Select all content from the folder of step 2, click on Options. Select "Copy items if needed", leave "Create groups" selected and choose all your targets on the "Add to targets" section. Then, click on "Add".

4. On your target -> Build Phases -> Link Binary with Libraries -> add AddressBook.framework
5. On your target -> Build Settings -> Other Linker Flags -> add -lz, -ObjC and -lc++
6. To force indexing and prevent erros, restart xcode and reopen your project again before compiling.

#### Android
1. `npm install react-native-camera --save`
2. Open up `android/app/src/main/java/[...]/MainApplication.java
  - Add `import com.lwansbrough.RCTCamera.RCTCameraPackage;` to the imports at the top of the file
  - Add `new RCTCameraPackage()` to the list returned by the `getPackages()` method. Add a comma to the previous item if there's already something there.

3. Append the following lines to `android/settings.gradle`:

	```gradle
	include ':react-native-camera'
	project(':react-native-camera').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-camera/android')
	```

4. Insert the following lines inside the dependencies block in `android/app/build.gradle`:

	```gradle
    compile project(':react-native-camera')
	```
5. Declare the permissions in your Android Manifest (required for `video recording` feature)

  ```java
  <uses-permission android:name="android.permission.RECORD_AUDIO"/>
  <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
  <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
  ```

6. Add jitpack to android/build.gradle
```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
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
	  onBarCodeRead={this.onBarCodeRead.bind(this)}
          style={styles.preview}
          aspect={Camera.constants.Aspect.fill}>
          <Text style={styles.capture} onPress={this.takePicture.bind(this)}>[CAPTURE]</Text>
        </Camera>
      </View>
    );
  }

  onBarCodeRead(e) {
    console.log(
        "Barcode Found!",
        "Type: " + e.type + "\nData: " + e.data
    );
  }

  takePicture() {
    const options = {};
    //options.location = ...
    this.camera.capture({metadata: options})
      .then((data) => console.log(data))
      .catch(err => console.error(err));
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    flexDirection: 'row',
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

The `aspect` property allows you to define how your viewfinder renders the camera's view. For instance, if you have a square viewfinder and you want to fill it entirely, you have two options: `"fill"`, where the aspect ratio of the camera's view is preserved by cropping the view or `"stretch"`, where the aspect ratio is skewed in order to fit the entire image inside the viewfinder. The other option is `"fit"`, which ensures the camera's entire view fits inside your viewfinder without altering the aspect ratio.

#### `cropToPreview`

Values: `true` or `false` (default)

Will crop the captured image to match the content that is displayed in the preview view. Works on both `Android` and `iOS`. Will be ignored if `captureMode` is other then `Camera.constants.CaptureMode.still`.

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

#### `fixOrientation` (_deprecated_)

If set to `true`, the image returned will be rotated to the _right way up_.  WARNING: It uses a significant amount of memory and my cause your application to crash if the device cannot provide enough RAM to perform the rotation.

(_If you find that you need to use this option because your images are incorrectly oriented by default,
could please submit a PR and include the make model of the device.  We believe that it's not
required functionality any more and would like to remove it._)

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

To see more of the `react-native-camera` in action, you can check out the source in [Example](https://github.com/lwansbrough/react-native-camera/tree/master/Example) folder.

## Q & A

#### meta-data android 26
```
  AndroidManifest.xml:25:13-35 Error:
       Attribute meta-data#android.support.VERSION@value value=(26.0.2) from [com.android.support:exifinterface:26.0.2] Android
  Manifest.xml:25:13-35
    is also present at [com.android.support:support-v4:26.0.1] AndroidManifest.xml:28:13-35 value=(26.0.1).
          Suggestion: add 'tools:replace="android:value"' to <meta-data> element at AndroidManifest.xml:23:9-25:38 to override.
```

Add this to your AndroidManifest.xml:

- [ ]           xmlns:tools="http://schemas.android.com/tools"

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
          xmlns:tools="http://schemas.android.com/tools"
```
- [ ] tools:node="replace"
```xml
<application
      android:name=".MainApplication"
      android:allowBackup="true"
      android:label="@string/app_name"
      android:icon="@mipmap/ic_launcher"
      android:theme="@style/AppTheme"
      tools:node="replace"
    >
```

#### When I try to build my project, I get following error:
```
Execution failed for task ':app:processDebugManifest'.
> Manifest merger failed : Attribute meta-data#android.support.VERSION@value value=(26.0.2) from [com.android.support:exifinterface:26.0.2] AndroidManifest.xml:25:13-35
        is also present at [com.android.support:support-v4:26.0.1] AndroidManifest.xml:28:13-35 value=(26.0.1).
        Suggestion: add 'tools:replace="android:value"' to <meta-data> element at AndroidManifest.xml:23:9-25:38 to override.
```
#### As the error message hints `com.android.support:exifinterface:26.0.2` is already found in `com.android.support:support-v4:26.0.1`
To fix this issue, modify your project's `android/app/build.gradle` as follows:
```Gradle
dependencies {
    compile (project(':react-native-camera')) {
        exclude group: "com.android.support"

        // uncomment this if also com.google.android.gms:play-services-vision versions are conflicting
        // this can happen if you use react-native-firebase
        // exclude group: "com.google.android.gms"
    }

    compile ('com.android.support:exifinterface:26.0.1') {
        force = true;
    }

    // uncomment this if you uncommented the previous line
    // compile ('com.google.android.gms:play-services-vision:11.6.0') {
    //    force = true;
    // }
}
```

## Open Collective
We are just beginning a funding campaign for react-native-camera. Contributions are greatly appreciated. When we gain more than $250 we will begin distributing funds to core maintainers in a fully transparent manner. Feedback for this process is welcomed, we will continue to evolve the strategy as we grow and learn more.

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


------------

Thanks to Brent Vatne (@brentvatne) for the `react-native-video` module which provided me with a great example of how to set up this module.
