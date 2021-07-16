---
id: migrationv4
title: Migrating from version 2.x/3.x to 4.x
sidebar_label: Migrating from version 2.x/3.x to 4.x
---

Version 3.x of react-native-camera replaces `MLKit for Firebase`(which has been deprecated) with `Google MLKit`, allowing you to use advanced features such as text/face recognition without Firebase. You can learn more at [MLKit](https://developers.google.com/ml-kit/migration)

## Required steps

> Skip this if you need Firebase in your project

### Remove Firebase project

#### Android

1. Remove the Firebase configuration file by deleting the google-services.json config file at `android/app/`.

2. Replace the Google Services Gradle plugin classpath in `android/app/build.gradle` with the one for the Strict Version Matcher plugin:

```diff
    buildscript {
      dependencies {
        // Add this line
-       classpath 'com.google.gms:google-services:4.0.1' <-- you might want to use different version
+       classpath 'com.google.android.gms:strict-version-matcher-plugin:1.2.1'
      }
    }
```

3. Replace the Google Services Gradle plugin in `android/build.gradle` with the Strict Version Matcher plugin:

```diff
- apply plugin: 'com.google.gms.google-services'
+ apply plugin: 'com.google.android.gms.strict-version-matcher-plugin'
```

4. Remove meta fron `android/app/Manifest.xml`(MLKit use bundled models)

```diff
<application ...>
...
- <meta-data
-     android:name="com.google.firebase.ml.vision.DEPENDENCIES"
-     android:value="ocr" />
- <!-- To use multiple models, list all needed models: android:value="ocr, face, barcode" -->
</application>
```

#### iOS

1. Remove `GoogleService-Info.plist` from your project

2. Remove `pod 'Firebase/Core'` from your Podfile

1\3. Remove Firebase code from `AppDelegate.m`

```diff
-#import <Firebase.h> // <--- add this
...

\- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
- [FIRApp configure]; // <--- add this
  ...
}
```
