# AndroidX Migration

AndroidX is the replacement for Google Support Libraries. It is the open-source project that the Android team uses to
develop, test, package, version and release libraries within Jetpack. Moving forward, all new Android development
will be in AndroidX, the Support Libraries are depreciated.

> AndroidX is a major improvement to the original Android Support Library. Like the Support Library, AndroidX ships separately from the Android OS and provides backwards-compatibility across Android releases. AndroidX fully replaces the Support Library by providing feature parity and new libraries.

See: https://developer.android.com/jetpack/androidx/

## Migration via Android studio

You can follow the instructions [here](https://developer.android.com/jetpack/androidx/migrate) to migrate you existing
project.

## Manual

#### Update the android section `android/app/build.gradle` to

```
android {
    compileSdkVersion 28
    buildToolsVersion "28.0.3"

    defaultConfig {
        ...
        minSdkVersion 16
        targetSdkVersion 28
    }
...
```

If you have any `dependencies {` that are using the old Google Support Libraries, you'll need to update them
to use the androidx version. Can check the full migration list [here](https://developer.android.com/jetpack/androidx/migrate).

#### Update `android/app/src/main/AndroidManifest.xml`

remove

```xml
<uses-sdk android:minSdkVersion="16" android:targetSdkVersion="26" />
```

#### Update `android/build.gradle`

```
buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.2.1'

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        mavenLocal()
        google()
        jcenter()
        maven { url "https://jitpack.io" }
        maven {
            // All of React Native (JS, Obj-C sources, Android binaries) is installed from npm
            url "$rootDir/../node_modules/react-native/android"
        }
    }
}

ext {
    compileSdkVersion = 28
    buildToolsVersion = '28.0.3'
}

subprojects { subproject ->
    afterEvaluate{
        if((subproject.plugins.hasPlugin('android') || subproject.plugins.hasPlugin('android-library'))) {
            android {
                compileSdkVersion rootProject.ext.compileSdkVersion
                buildToolsVersion rootProject.ext.buildToolsVersion
            }
        }
    }
}
```

#### Update `android/gradle.properties`

add

```
android.enableJetifier = true;
android.useAndroidX = true;
```

#### Update `android/gradle/wrapper/gradle-wrapper.properties`

make sure your gradle `distributionUrl=https\://services.gradle.org/distributions/gradle-4.6-all.zip`
