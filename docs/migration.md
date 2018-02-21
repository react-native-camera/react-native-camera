# Migrating from RCTCamera to RNCamera

## Project Integration

Please follow the [RNCamera doc](https://github.com/react-native-community/react-native-camera/blob/master/docs/RNCamera.md) installation guide to install the face detection frameworks on both platforms.

### iOS

Open your app's XCode project. Expand the Libraries folder in the project navigation and right click and delete the RCTCamera.xcodeproj.

On your project's target, on `Build Phases`, click on libRCTCamera.a and delete (press the - button below).

You can follow the installation steps for RNCamera on the readme to link the new RNCamera project to your app's XCode project.

You can do it via `react-native link` command or by the manual steps.

Before building and running again, do a complete clean on your project.


### Android

1. On the MainApplication of your Android project change the import of RCTCameraPackage line to:
```java
import org.reactnative.camera.RNCameraPackage;
```

2. Inside the getPackages() methods change `new RCTCameraPackage()` to `new RNCameraPackage()`.

3. On `android/app/build.gradle`, change the line: `compile (project(':react-native-camera'))` to:
```gradle
compile (project(':react-native-camera')) {
  exclude group: "com.google.android.gms"
}
compile ("com.google.android.gms:play-services-vision:10.2.0") {
  force = true;
}
```

4. Add jitpack to android/build.gradle
```gradle
allprojects {
  repositories {
    maven { url "https://jitpack.io" }
  }
}
```

## Usage differences

### imports

Instead of importing `Camera`, now, you should import `{ RNCamera }` from `react-native-camera`.

### No `captureMode` prop

On RCTCamera, you would set the camera `captureMode` to `still` or `video` and you could only record or take a picture depending on the `captureMode` of your `Camera`.

On RNCamera you do not need to specify `captureMode`. The RNCamera, in any state, can record or take a picture calling the appropriate method.

### `capture` to `takePictureAsync` or `recordAsync`

Let's say you have a component with a RCTCamera taking a photo:
```jsx
import Camera from 'react-native-camera';

class TakePicture extends Component {

  takePicture = async () => {
    try {
      const data = await this.camera.capture();
      console.log('Path to image: ' + data.path);
    } catch (err) {
      // console.log('err: ', err);
    }
  };

  render() {
    return (
      <View style={styles.container}>
        <Camera
          ref={cam => {
            this.camera = cam;
          }}
          style={styles.preview}
          aspect={Camera.constants.Aspect.fill}
          captureAudio={false}
        >
          <View style={styles.captureContainer}>
            <TouchableOpacity style={styles.capture} onPress={this.takePicture}>
              <Icon style={styles.iconCamera}>camera</Icon>
              <Text>Take Photo</Text>
            </TouchableOpacity>
          </View>
        </Camera>

        <View style={styles.space} />
      </View>
    );
  }
}
```

You should change this to:
```jsx
import { RNCamera } from 'react-native-camera';

class TakePicture extends Component {

  takePicture = async () => {
    try {
      const data = await this.camera.takePictureAsync();
      console.log('Path to image: ' + data.uri);
    } catch (err) {
      // console.log('err: ', err);
    }
  };

  render() {
    return (
      <View style={styles.container}>
        <RNCamera
          ref={cam => {
            this.camera = cam;
          }}
          style={styles.preview}
        >
          <View style={styles.captureContainer}>
            <TouchableOpacity style={styles.capture} onPress={this.takePicture}>
              <Icon style={styles.iconCamera}>camera</Icon>
              <Text>Take Photo</Text>
            </TouchableOpacity>
          </View>
        </RNCamera>

        <View style={styles.space} />
      </View>
    );
  }
}
```

The same logic applies to change `capture` to `recordAsync`.

### `flashMode` and `torchMode`

In RCTCamera, there was `flashMode` and `torchMode` prop. In RNCamera, these are combined into the `flashMode` prop.

### Other differences

Take a look into the [RCTCamera doc](https://github.com/react-native-community/react-native-camera/blob/master/docs/RCTCamera.md) and the [RNCamera doc](https://github.com/react-native-community/react-native-camera/blob/master/docs/RNCamera.md) to see more differences.
