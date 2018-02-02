# React Native Camera [![Backers on Open Collective](https://opencollective.com/react-native-camera/backers/badge.svg)](#backers) [![Sponsors on Open Collective](https://opencollective.com/react-native-camera/sponsors/badge.svg)](#sponsors) [![npm version](https://badge.fury.io/js/react-native-camera.svg)](http://badge.fury.io/js/react-native-camera) [![Gitter](https://badges.gitter.im/lwansbrough/react-native-camera.svg)](https://gitter.im/lwansbrough/react-native-camera)

The comprehensive camera module for React Native. Including photographs, videos, face detection and barcode scanning!

`import { RNCamera, FaceDetector } from 'react-native-camera';`

#### How to use master branch?
Inside your package.json, use this
`"react-native-camera": "git+https://git@github.com/react-native-community/react-native-camera"`
instead of `"react-native-camera": "^0.13.0"`.

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

## Migrating from RCTCamera to RNCamera

See this [doc](https://github.com/react-native-community/react-native-camera/blob/master/docs/migration.md).

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
3. Go to `node_modules` ➜ `react-native-camera` and add `RNCamera.xcodeproj`
4. In XCode, in the project navigator, select your project. Add `libRNCamera.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
5. Click `RNCamera.xcodeproj` in the project navigator and go the `Build Settings` tab. Make sure 'All' is toggled on (instead of 'Basic'). In the `Search Paths` section, look for `Header Search Paths` and make sure it contains both `$(SRCROOT)/../../react-native/React` and `$(SRCROOT)/../../../React` - mark both as `recursive`.

### Face Detection Steps

Face Detecion is optional on iOS. If you want it, you are going to need to install Google Mobile Vision frameworks in your project, as mentioned in the next section.

##### No Face Detection steps

If you do not need it and do not wnat to install the GMV frameworks, open your app xcode project, on the Project Navigator, expand the RNCamera project, right click on the FaceDetector folder and delete it (move to trash, if you want). If you keep that folder and do not follow the GMV installation setps, your project will not compile.

If you want to make this automatic, you can add a postinstall script to your app `package.json`. Inside the `postinstall_project` there is a xcode project ready with the folder removed (we opened xcode, removed the folder from the project and copied the resulting project file). The post install script is:
```
#!/bin/bash
rm -rf node_modules/react-native-camera/ios/FaceDetector
cp node_modules/react-native-camera/postinstall_project/projectWithoutFaceDetection.pbxproj node_modules/react-native-camera/ios/RCTCamera.xcodeproj/project.pbxproj
```

And add something like this to the `scripts` section in your `package.json`:

```
"postinstall": "./scripts/post.sh",
```

##### Installing GMV frameworks
GMV (Google Mobile Vision) is used for Face detection by the iOS RNCamera. You have to link the google frameworks to your project to successfully compile the RNCamera project.

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
  - Add `import org.reactnative.RNCameraPackage;` to the imports at the top of the file
  - Add `new RNCameraPackage()` to the list returned by the `getPackages()` method. Add a comma to the previous item if there's already something there.

3. Append the following lines to `android/settings.gradle`:

	```gradle
	include ':react-native-camera'
	project(':react-native-camera').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-camera/android')
	```

4. Insert the following lines inside the dependencies block in `android/app/build.gradle`:

	```gradle
        compile (project(':react-native-camera')) {
        exclude group: "com.google.android.gms"
    }
        compile ("com.google.android.gms:play-services-vision:10.2.0") {
        force = true;
    }
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

Follow the Q & A section if you are having compilation issues.

## Usage

### RNCamera

Take a look into this [documentation](https://github.com/react-native-community/react-native-camera/blob/master/docs/RNCamera.md).

### RCTCamera

Since `1.0.0`, RCTCamera is deprecated, but if you want to use it, you can see its [documentation](https://github.com/react-native-community/react-native-camera/blob/master/docs/RCTCamera.md).

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
