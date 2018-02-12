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
