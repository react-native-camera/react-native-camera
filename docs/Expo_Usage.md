# [Expo](https://expo.io/) Usage

RNCamera of react-native-camera is heavily based on Expo camera module. Thanks @aalices and Expo for the great work.

So you don't need to use **react-native-camera** if you have the following config:

- If you are using [Expo](https://expo.io)
- If you are using [create-react-native-app](https://github.com/react-community/create-react-native-app)
- If you eject a [create-react-native-app](https://github.com/react-community/create-react-native-app) app and are using [ExpoKit](https://docs.expo.io/versions/latest/expokit/expokit)

## How to migrate from Expo to react-native-camera

If you decide to eject without using ExpoKit, you can follow react-native-camera installation instructions and just change the usage of your Camera component to RNCamera:

```diff
- import { Camera } from 'expo';
+ import { RNCamera } from 'react-native-camera';

- <Camera
+ <RNCamera
```

## How to migrate from react-native-camera to Expo Camera Module

```diff
- import { RNCamera } from 'react-native-camera';
+ import { Camera } from 'expo';

- <RNCamera
+ <Camera
```
