---
id: installation
title: Installation
---

This document is split into two main sections:

1. Required installation steps for basic usage of `react-native-camera`
2. Additional installation steps for usage of Face Detection/Text Recognition/BarCode with [MLKit](https://developers.google.com/ml-kit)

# Required installation steps

_These steps assume installation for iOS/Android. To install it with Windows, see [Windows](#windows) below_

## Mostly automatic install with autolinking (RN > 0.60)

1. `npm install react-native-camera --save`
2. Run `cd ios && pod install && cd ..`

## Mostly automatic install with react-native link (RN < 0.60)

1. `npm install react-native-camera --save`
2. `react-native link react-native-camera`

## Manual install - iOS (not recommended)

1. `npm install react-native-camera --save`
2. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
3. Go to `node_modules` ➜ `react-native-camera` and add `RNCamera.xcodeproj`
4. Expand the `RNCamera.xcodeproj` ➜ `Products` folder
5. In XCode, in the project navigator, select your project. Add `libRNCamera.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
6. Click `RNCamera.xcodeproj` in the project navigator and go the `Build Settings` tab. Make sure 'All' is toggled on (instead of 'Basic'). In the `Search Paths` section, look for `Header Search Paths` and make sure it contains both `$(SRCROOT)/../../react-native/React` and `$(SRCROOT)/../../../React` - mark both as `recursive`.

## Manual install - Android (not recommended)

1. `npm install react-native-camera --save`
2. Open up `android/app/src/main/java/[...]/MainApplication.java`

- Add `import org.reactnative.camera.RNCameraPackage;` to the imports at the top of the file
- Add `new RNCameraPackage()` to the list returned by the `getPackages()` method. Add a comma to the previous item if there's already something there.

3. Append the following lines to `android/settings.gradle`:

```gradle
include ':react-native-camera'
project(':react-native-camera').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-camera/android')
```

4. Insert the following lines in `android/app/build.gradle` inside the dependencies block:

```gradle
implementation project(':react-native-camera')
```

## iOS - other required steps

Add permissions with usage descriptions to your app `Info.plist`:

```xml
<!-- Required with iOS 10 and higher -->
<key>NSCameraUsageDescription</key>
<string>Your message to user when the camera is accessed for the first time</string>

<!-- Required with iOS 11 and higher: include this only if you are planning to use the camera roll -->
<key>NSPhotoLibraryAddUsageDescription</key>
<string>Your message to user when the photo library is accessed for the first time</string>

<!-- Include this only if you are planning to use the camera roll -->
<key>NSPhotoLibraryUsageDescription</key>
<string>Your message to user when the photo library is accessed for the first time</string>

<!-- Include this only if you are planning to use the microphone for video recording -->
<key>NSMicrophoneUsageDescription</key>
<string>Your message to user when the microphone is accessed for the first time</string>
```

<details>
  <summary>Additional information in case of problems</summary>

You might need to adjust your Podfile following the example below:

```ruby
target 'yourTargetName' do
  # See http://facebook.github.io/react-native/docs/integration-with-existing-apps.html#configuring-cocoapods-dependencies
  pod 'React', :path => '../node_modules/react-native', :subspecs => [
    'Core',
    'CxxBridge', # Include this for RN >= 0.47
    'DevSupport', # Include this to enable In-App Devmenu if RN >= 0.43
    'RCTText',
    'RCTNetwork',
    'RCTWebSocket', # Needed for debugging
    'RCTAnimation', # Needed for FlatList and animations running on native UI thread
    # Add any other subspecs you want to use in your project
  ]

  # Explicitly include Yoga if you are using RN >= 0.42.0
  pod 'yoga', :path => '../node_modules/react-native/ReactCommon/yoga'

  # Third party deps podspec link
  pod 'react-native-camera', path: '../node_modules/react-native-camera'

end

post_install do |installer|
  installer.pods_project.targets.each do |target|
    if target.name == "React"
      target.remove_from_project
    end
  end
end
```

</details>

## Android - other required steps

Add permissions to your app `android/app/src/main/AndroidManifest.xml` file:

```xml
<!-- Required -->
<uses-permission android:name="android.permission.CAMERA" />

<!-- Include this only if you are planning to use the camera roll -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

<!-- Include this only if you are planning to use the microphone for video recording -->
<uses-permission android:name="android.permission.RECORD_AUDIO"/>
```

Insert the following lines in `android/app/build.gradle`:

```gradle
android {
  ...
  defaultConfig {
    ...
    missingDimensionStrategy 'react-native-camera', 'general' // <--- insert this line
  }
}
```

<details>
  <summary>Additional information in case of problems</summary>

1. Make sure you use `JDK >= 1.7` and your `buildToolsVersion >= 25.0.2`

2. Make sure you have jitpack added in `android/build.gradle`

```gradle
allprojects {
    repositories {
        maven { url "https://www.jitpack.io" }
        maven { url "https://maven.google.com" }
    }
}
```

</details>

# Additional installation steps

Follow these optional steps if you want to use Face Detection/Text Recognition/BarCode with [MLKit](https://developers.google.com/ml-kit).

## iOS

If you want any of these optional features, you will need to use CocoaPods.

> MLKit for iOS runs on arm64/x86_64 devices only; armv7/x86 is not supported

### Modifying Podfile

Add dependency towards `react-native-camera` in your `Podfile` with `subspecs` using one of the following:

- For Face Detection:

```ruby
pod 'react-native-camera', path: '../node_modules/react-native-camera', subspecs: [
  'FaceDetectorMLKit'
]
```

- For Text Recognition:

```ruby
pod 'react-native-camera', path: '../node_modules/react-native-camera', subspecs: [
  'TextDetector'
]
```

- For BarCode Recognition:

```ruby
pod 'react-native-camera', path: '../node_modules/react-native-camera', subspecs: [
  'BarcodeDetectorMLKit'
]
```

- For all possible detections:

```ruby
pod 'react-native-camera', path: '../node_modules/react-native-camera', subspecs: [
  'TextDetector',
  'FaceDetectorMLKit',
  'BarcodeDetectorMLKit'
]
```

Then run `cd ios && pod install && cd ..`


<details>
  <summary>Additional information in case of problems</summary>

- If you see build errors like `ld: symbol(s) not found for architecture armv7` you will need to exclude armv7 arch in your Xcode (Xcode -> Build Setting -> Excluded Architectures -> Add 'armv7' for 'Any iOS SDK' ).
  </details>

## Android

### Modifying build.gradle

Modify the following lines in `android/app/build.gradle`:

```gradle
android {
  ...
  defaultConfig {
    ...
    missingDimensionStrategy 'react-native-camera', 'mlkit' // <--- replace general with mlkit
  }
}
```

### Setting up MLKit

**If you don't use any other Firebase component in your project**

1. Add the folowing to project level `build.gradle`:

```gradle
buildscript {
  dependencies {
  // Add this line
  classpath 'com.google.android.gms:strict-version-matcher-plugin:1.2.1' // <--- you might want to use different version
  }
}
```

2. add to the bottom of `android/app/build.gradle` file

```gradle
apply plugin: 'com.google.gms.google-services'  // Google Services plugin
```

**If you have Firebase integrated already**

1. Add the folowing to project level `build.gradle`:

```gradle
buildscript {
  dependencies {
  // Add this line
  classpath 'com.google.gms:google-services:4.3.3'  // Google Services plugin(you might want to use different version)
  }
}
```

2. add to the bottom of `android/app/build.gradle` file

```gradle
apply plugin: 'com.google.android.gms.strict-version-matcher-plugin'
```

<details>
  <summary>Additional information in case of problems</summary>
  The current Android library defaults to the below values for the Google SDK and Libraries,

```gradle
def DEFAULT_COMPILE_SDK_VERSION             = 29
def DEFAULT_BUILD_TOOLS_VERSION             = "29.0.2"
def DEFAULT_TARGET_SDK_VERSION              = 29
def DEFAULT_SUPPORT_LIBRARY_VERSION         = "27.1.0"
```

You can override this settings by adding a Project-wide gradle configuration properties for
use by all modules in your ReactNative project by adding the below to `android/build.gradle`
file,

```gradle
buildscript {...}

allprojects {...}

/**
* Project-wide gradle configuration properties for use by all modules
*/
ext {
    compileSdkVersion           = 29
    targetSdkVersion            = 29
    buildToolsVersion           = "29.0.2"
    supportLibVersion           = "27.1.0"
}
```

The above settings in the ReactNative project over-rides the values present in the `react-native-camera`
module. For your reference please check [android/build.gradle](android/build.gradle) file of the module.

</details>

# Windows

## Mostly automatic install with autolinking (RNW >= 0.63)

1. `npm install react-native-camera --save`
2. See [Additional steps - Windows](#additional-steps-windows) below

## Manual install - Windows (RNW 0.62)

1. `npm install react-native-camera --save`
2. Link the library as described below:
   1. Add the _ReactNativeCameraCPP_ project to your solution (eg. `windows\yourapp.sln`)
      1. Open your solution in Visual Studio 2019
      2. Right-click Solution icon in Solution Explorer > Add > Existing Project...
      3. Select `node_modules\react-native-camera\windows\ReactNativeCameraCPP\ReactNativeCameraCPP.vcxproj`
   2. Add a reference to _ReactNativeCameraCPP_ to your main application project (eg. `windows\yourapp\yourapp.vcxproj`)
      1. Open your solution in Visual Studio 2019
      2. Right-click main application project > Add > Reference...
      3. Check _ReactNativeCameraCPP_ from Solution Projects
3. Modify files below to add the package providers to your main application project
   1. `pch.h`
      1. Add `#include "winrt/ReactNativeCameraCPP.h"`
   2. `App.cpp`
      1. Add `PackageProviders().Append(winrt::ReactNativeCameraCPP::ReactPackageProvider());` before `InitializeComponent();`
4. See [Additional steps - Windows](#additional-steps-windows) below

## Manual install - Windows (RNW 0.61)

Follow [Manual install - Windows (RNW 0.62)](#manual-install-windows-rnw-062) above, but for step 2 substitute _ReactNativeCameraCPP61_ for _ReactNativeCameraCPP_.

## Additional steps - Windows

You need to declare that your app wants to access the camera:

1. Add the capabilities (permissions) for the webcam and microphone as described here: [Add capability declarations to the app manifest](https://docs.microsoft.com/en-us/windows/uwp/audio-video-camera/simple-camera-preview-access#add-capability-declarations-to-the-app-manifest)
2. If you plan on capturing images to the Pictures Library, or videos to the Videos Library, be sure to enable those capabilities too

Follow the [Q & A](QA.md) section if you are having compilation issues.
