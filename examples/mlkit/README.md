# React Native Camera MLKit Example

An example project demonstrating the use of MLKit-based Text and Face Recognition features of react-native-camera.

### Features

Features of Basic Example + Face and Text Recognition.

Face Recognition: draws polygons around faces and red circles on top of face landmarks (ears, mouth, nose, etc.).

Text Recognition: draws polygons around text blocks and recognized within them.

### Setup

1. Run `yarn install`.

2. Create Firebase project, generate `google-services.json` and place it into `./android/app` folder, generate `GoogleService-Info.plist` and place it into `./ios/mlkit` folder.

3. Build project (you will likely need to manage signing if you are building for ios device)

### Contributing

- Pull Requests are welcome, if you open a pull request we will do our best to get to it in a timely manner
- Pull Request Reviews and even more welcome! we need help testing, reviewing, and updating open PRs
- If you are interested in contributing more actively, please contact me (same username on Twitter, Facebook, etc.) Thanks!
- If you want to help us coding, join Expo slack https://slack.expo.io/, so we can chat over there. (#react-native-camera)

### FAQ

## Why is `react-native-camera` not listed as a dependency in `package.json`?

`react-native` uses `metro` for dependency resolution. In order to not recursively install this example into the `node_modules` of this example we use `rn-cli.config.js` to resolve `react-native-camera`. This also allows a quicker iteration when developing (without having to `yarn install` after every single change in `react-native-camera`).
