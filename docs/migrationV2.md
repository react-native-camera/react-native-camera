# Migrating from version 1.x to 2.x

Version 2.x of react-native-camera moves to using Firebase MLKit for advanced features such as text/face recognition. Users can now opt into useing MLKit in their app by choosing a certain flavor of the library (Android) or selecting a desired podspec (iOS). This allows users who do not need Firebase MLKit-based features to not be forced to set up a Firebase project.

## Required steps

### a. No advanced features/ opt into using deprecated Google Mobile Vision

#### Android

Please insert the following line in `android/app/build.gradle` inside defaultConfig block:

```gradle
android {
  ...
  defaultConfig {
    ...
    missingDimensionStrategy 'react-native-camera', 'general' <-- insert this line
  }
}
```

#### iOS

No additional steps required

### b. Text/Face/Barcode detection using Firebase MLKit

#### Android

1. Please insert the following line in `android/app/build.gradle`, inside defaultConfig block:

```gradle
android {
  ...
  defaultConfig {
    ...
    missingDimensionStrategy 'react-native-camera', 'mlkit' <-- insert this line
  }
}
```

2. The use of Firebase MLKit requires seting up Firebase project for your app. If you have not already added Firebase to your app, please follow the steps described in [getting started guide](https://firebase.google.com/docs/android/setup).
   In short, you would need to

- Register your app in Firebase console.
- Download google-services.json and place it in `android/app/`
- add the folowing to project level build.gradle:

```gradle
    buildscript {
      dependencies {
        // Add this line
        classpath 'com.google.gms:google-services:4.0.1' <-- you might want to use different version
      }
    }
```

- add to the bottom of `android/app/build.gradle` file the following:

```gradle
  apply plugin: 'com.google.gms.google-services'
```

3. Configure your app to automatically download the ML model to the device after your app is installed from the Play Store. If you do not enable install-time model downloads, the model will be downloaded the first time you run the on-device detector. Requests you make before the download has completed will produce no results.

```xml
<application ...>
...
  <meta-data
      android:name="com.google.firebase.ml.vision.DEPENDENCIES"
      android:value="ocr" />
  <!-- To use multiple models, list all needed models: android:value="ocr, face, barcode" -->
</application>
```

#### iOS

1. The use of Firebase MLKit requires setting up Firebase project for your app.
   If you have not already added Firebase to your app, please follow the steps described in [getting started guide](https://firebase.google.com/docs/ios/setup).
   In short, you would need to

- Register your app in Firebase console.
- Download `GoogleService-Info.plist` and add it to your project
- Add `pod 'Firebase/Core'` to your Podfile
- In your `AppDelegate.m` file add the following lines:

```objective-c
#import <Firebase.h> // <--- add this
...

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
  [FIRApp configure]; // <--- add this
  ...
}
```

2. Check that you have selected a correct podspec in your Podfile, e.g. if you want to use Text Recognition, your Podfile should contain the following:

```
pod 'react-native-camera', path: '../node_modules/react-native-camera', subspecs: [
  'TextDetector'
]
```
