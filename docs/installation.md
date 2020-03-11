---
id: installation
title: Installation
---

This document is split into two main sections:
1. Required installation steps for basic usage of `react-native-camera`
2. Additional installation steps for usage of Face Detection/Text Recognition/BarCode with [MLKit](https://developers.google.com/ml-kit)

# Required installation steps

_These steps assume installation for iOS/Android. To install it with Windows, see manual install [below](#windows)_

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
You will need to set-up Firebase project for your app (detailed steps below).

_Note:_ Installing [react-native-firebase](https://github.com/invertase/react-native-firebase) package is NOT necessary.

## iOS

If you want any of these optional features, you will need to use CocoaPods.

### Modifying Podfile

Add dependency towards `react-native-camera` in your `Podfile` with `subspecs` using one of the following:

* For Face Detection:

```ruby
pod 'react-native-camera', path: '../node_modules/react-native-camera', subspecs: [
  'FaceDetectorMLKit'
]
```

* For Text Recognition:

```ruby
pod 'react-native-camera', path: '../node_modules/react-native-camera', subspecs: [
  'TextDetector'
]
```

* For BarCode Recognition:

```ruby
pod 'react-native-camera', path: '../node_modules/react-native-camera', subspecs: [
  'BarcodeDetectorMLKit'
]
```

* For all possible detections:

```ruby
pod 'react-native-camera', path: '../node_modules/react-native-camera', subspecs: [
  'TextDetector',
  'FaceDetectorMLKit',
  'BarcodeDetectorMLKit'
]
```

Then run `cd ios && pod install && cd ..`

### Setting up Firebase

Text/Face recognition for iOS uses Firebase MLKit which requires setting up Firebase project for your app.
If you have not already added Firebase to your app, please follow the steps described in [getting started guide](https://firebase.google.com/docs/ios/setup).
In short, you would need to

1. Register your app in Firebase console.
2. Download `GoogleService-Info.plist` and add it to your project
3. Add `pod 'Firebase/Core'` to your podfile
4. In your `AppDelegate.m` file add the following lines:

```objective-c
#import <Firebase.h> // <--- add this
...

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
  [FIRApp configure]; // <--- add this
  ...
}
```

<details>
  <summary>Additional information in case of problems</summary>

  - If you have issues with duplicate symbols you will need to enable dead code stripping option in your Xcode (Target > Build Settings > search for "Dead code stripping") [see here](https://github.com/firebase/quickstart-ios/issues/487#issuecomment-415313053).
  - If you are using `pod Firebase/Core` with a version set below 5.13 you might want to add `pod 'GoogleAppMeasurement', '~> 5.3.0'` to your podfile
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

### Setting up Firebase

Using Firebase MLKit requires seting up Firebase project for your app. If you have not already added Firebase to your app, please follow the steps described in [getting started guide](https://firebase.google.com/docs/android/setup).
In short, you would need to

1. Register your app in Firebase console.
2. Download google-services.json and place it in `android/app/`
3. Add the folowing to project level `build.gradle`:

```gradle
buildscript {
  dependencies {
  // Add this line
  classpath 'com.google.gms:google-services:4.0.1' // <--- you might want to use different version
  }
}
```

4. add to the bottom of `android/app/build.gradle` file

```gradle
apply plugin: 'com.google.gms.google-services'
```

5. Configure your app to automatically download the ML model to the device after your app is installed from the Play Store. If you do not enable install-time model downloads, the model will be downloaded the first time you run the on-device detector. Requests you make before the download has completed will produce no results.

```xml
<application ...>
...
  <meta-data
      android:name="com.google.firebase.ml.vision.DEPENDENCIES"
      android:value="ocr, face" /> <!-- choose models that you will use -->
</application>
```

<details>
  <summary>Additional information in case of problems</summary>
  The current Android library defaults to the below values for the Google SDK and Libraries,

  ```gradle
  def DEFAULT_COMPILE_SDK_VERSION             = 26
  def DEFAULT_BUILD_TOOLS_VERSION             = "26.0.2"
  def DEFAULT_TARGET_SDK_VERSION              = 26
  def DEFAULT_GOOGLE_PLAY_SERVICES_VERSION    = "12.0.1"
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
      compileSdkVersion           = 26
      targetSdkVersion            = 26
      buildToolsVersion           = "26.0.2"
      googlePlayServicesVersion   = "12.0.1"
      googlePlayServicesVisionVersion = "15.0.2"
      supportLibVersion           = "27.1.0"
  }
  ```

  The above settings in the ReactNative project over-rides the values present in the `react-native-camera`
  module. For your reference below is the `android/build.gradle` file of the module.

  ```gradle
  def safeExtGet(prop, fallback) {
      rootProject.ext.has(prop) ? rootProject.ext.get(prop) : fallback
  }

  buildscript {
    repositories {
      google()
      maven {
        url 'https://maven.google.com'
      }
      jcenter()
    }

    dependencies {
      classpath 'com.android.tools.build:gradle:3.3.1'
    }
  }

  apply plugin: 'com.android.library'

  android {
    compileSdkVersion safeExtGet('compileSdkVersion', 28)
    buildToolsVersion safeExtGet('buildToolsVersion', '28.0.3')

    defaultConfig {
      minSdkVersion safeExtGet('minSdkVersion', 16)
      targetSdkVersion safeExtGet('targetSdkVersion', 28)
    }

    flavorDimensions "react-native-camera"

    productFlavors {
      general {
        dimension "react-native-camera"
      }
      mlkit {
        dimension "react-native-camera"
      }
    }

    sourceSets {
      main {
        java.srcDirs = ['src/main/java']
      }
      general {
        java.srcDirs = ['src/general/java']
      }
      mlkit {
        java.srcDirs = ['src/mlkit/java']
      }
    }

    lintOptions {
      abortOnError false
      warning 'InvalidPackage'
    }
  }

  repositories {
    google()
    jcenter()
    maven {
    url 'https://maven.google.com'
    }
    maven { url "https://jitpack.io" }
    maven {
      // All of React Native (JS, Obj-C sources, Android binaries) is installed from npm
      url "$rootDir/../node_modules/react-native/android"
    }
  }

  dependencies {
    def googlePlayServicesVisionVersion = safeExtGet('googlePlayServicesVisionVersion', safeExtGet('googlePlayServicesVersion', '17.0.2'))

    implementation 'com.facebook.react:react-native:+'
    implementation "com.google.zxing:core:3.3.3"
    implementation "com.drewnoakes:metadata-extractor:2.11.0"
    generalImplementation "com.google.android.gms:play-services-vision:$googlePlayServicesVisionVersion"
    implementation "com.android.support:exifinterface:${safeExtGet('supportLibVersion', '28.0.0')}"
    implementation "com.android.support:support-annotations:${safeExtGet('supportLibVersion', '28.0.0')}"
    implementation "com.android.support:support-v4:${safeExtGet('supportLibVersion', '28.0.0')}"
    mlkitImplementation "com.google.firebase:firebase-ml-vision:${safeExtGet('firebase-ml-vision', '19.0.3')}"
    mlkitImplementation "com.google.firebase:firebase-ml-vision-face-model:${safeExtGet('firebase-ml-vision-face-model', '17.0.2')}"
  }
  ```

  If you are using a version of `googlePlayServicesVersion` that does not have `play-services-vision`, you can specify a different version of `play-services-vision` by adding `googlePlayServicesVisionVersion` to the project-wide properties

  ```gradle
  ext {
      compileSdkVersion           = 26
      targetSdkVersion            = 26
      buildToolsVersion           = "26.0.2"
      googlePlayServicesVersion   = "16.0.1"
      googlePlayServicesVisionVersion = "15.0.2"
      supportLibVersion           = "27.1.0"
  }
  ```
</details>

# Windows

## Windows RNW C++/WinRT details
1. `yarn install react-native-camera`
2. Link the library as described below:
   windows/myapp.sln
     Add the ReactNativeCamera project to your solution.
     Open the solution in Visual Studio 2019
     Right-click Solution icon in Solution Explorer > Add > Existing Project Select node_modules\react-native-camera\windows\ReactNativeCameraCPP\ReactNativeCameraCPP.vcxproj
   windows/myapp/myapp.vcxproj
     Add a reference to ReactNativeCameraCPP to your main application project. From Visual Studio 2019:
     Right-click main application project > Add > Reference... Check ReactNativeCameraCPP from Solution Projects.
3. Modify files below to add the Camera package providers to your main application project
   pch.h
     Add #include "winrt/ReactNativeCameraCPP.h".
  app.cpp
     Add PackageProviders().Append(winrt::ReactNativeCameraCPP::ReactPackageProvider()); before InitializeComponent();
3. Add the capabilities (permissions) for the webcam and microphone as described here: [docs.microsoft / audio-video-camera](https://docs.microsoft.com/en-us/windows/uwp/audio-video-camera/simple-camera-preview-access#add-capability-declarations-to-the-app-manifest)

Follow the [Q & A](QA.md) section if you are having compilation issues.
