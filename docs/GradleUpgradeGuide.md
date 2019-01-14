# Upgrade gradle for Android projects

To integrate react-native-camera into your own react native project and make it work for Android, you need to edit the following files in the `android` folder under your project folder:

- In the `android/gradle.properties` file:

```
android.useDeprecatedNdk=true
android.enableAapt2=false
```

- In the `android/build.gradle` file:

```
buildscript {
    repositories {
        jcenter()
        maven {
            url 'https://maven.google.com'
            name 'Google'
        }
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.1.0'

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        mavenLocal()
        jcenter()
        maven {
            url 'https://maven.google.com'
            name 'Google'
        }
        maven { url "https://jitpack.io" }
        maven {
            // All of React Native (JS, Obj-C sources, Android binaries) is installed from npm
            url "$rootDir/../node_modules/react-native/android"
        }
    }
}

subprojects {
    project.configurations.all {
        resolutionStrategy.eachDependency { details ->
            if (details.requested.group == 'com.android.support'
              && !details.requested.name.contains('multidex') ) {
                details.useVersion "26.0.1"
            }
        }
    }

    afterEvaluate {
        project -> if (project.hasProperty("android")) {
            android {
                compileSdkVersion 26
                buildToolsVersion '26.0.1'
            }
        }
    }
}
```

- In the `android/app/build.gradle` file:

```
android {
    compileSdkVersion 26
    buildToolsVersion "26.0.1"

    defaultConfig {
        applicationId "appName"
        minSdkVersion 16
        targetSdkVersion 22
        versionCode 1
        versionName "1.0"
        ndk {
            abiFilters "armeabi-v7a", "x86"
        }
    }

...

dependencies {
    implementation project(':react-native-camera')
    ...
    implementation fileTree(dir: "libs", include: ["*.jar"])
    implementation 'com.android.support:appcompat-v7:26.0.1'
    implementation "com.facebook.react:react-native:+"  // From node_modules
}
```

- In the `android/gradle/gradle-wrapper.properties` file:

```
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-4.4-all.zip
```
