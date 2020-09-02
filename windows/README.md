# React Native Camera (Windows)

React Native Camera is currently maintained for React Native Windows (RNW) >= 0.61.

There are two implementations of `react-native-camera` in this folder:

1. _ReactNativeCameraCPP_ is the currently maintained implementation:
   1. Use _ReactNativeCameraCPP_ for RNW >= 0.62.
   2. Use _ReactNativeCameraCPP61_ for RNW 0.61.
2. _RNCamera_ is a legacy implementation for `react-native-windows@0.59.0-legacy.2`. It is no longer maintained.

## Why all the different versions?

RNW was originally implemented in C#, where development stopped at version 0.59. _RNCamera_ is the version of React Native Camera written in C# against that (now "legacy") RNW.

RNW was then rebuilt from scratch in C++, and version 0.61 was the first release to support native community modules. _ReactNativeCameraCPP_ is the version of React Native Camera written in C++ against the current RNW.

RNW 0.62 brought a variety of build improvements, but now requires both Visual Studio 2019 and a newer Windows SDK. So while the native module APIs are 99% forward-compatible, it's currently necessary to maintain a separate `ReactNativeCameraCPP61.vcxproj` project for RNW 0.61 users. The `ReactNativeCameraCPP.vcxproj` project targets RNW >= 0.62 users.

# Local Development Setup (RNW >= 0.61)

In order to work on _ReactNativeCameraCPP_, you'll need to install the [Windows Development Dependencies](https://microsoft.github.io/react-native-windows/docs/rnw-dependencies).

In addition, `react-native-camera` targets React Native 0.59 and doesn't include React Native Windows as a dependency. So in order to build _ReactNativeCameraCPP_ locally you'll need to temporarily upgrade the development dependencies:

## RNW >= 0.63

```
yarn upgrade react-native@^0.63
yarn add react-native-windows@^0.63 --dev
```

Now you should be able to open `ReactNativeCameraCPP.sln` in Visual Studio and build the project.

## RNW 0.62

```
yarn upgrade react-native@^0.62
yarn add react-native-windows@^0.62 --dev
```

Now you should be able to open `ReactNativeCameraCPP62.sln` in Visual Studio and build the project.

## RNW 0.61

```
yarn upgrade react-native@^0.61
yarn add react-native-windows@^0.61 --dev
```

Now you should be able to open `ReactNativeCameraCPP61.sln` in Visual Studio and build the project.
