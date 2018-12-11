# React Native Camera Basic Example

An example project demonstrating the use of react-native-camera.

### Features

Shows all different attributes of the camera like front and back cameras, flash modes, WB, etc.

Demonstrates the usage of the face detection feature by drawing polygons around the detected face.

### Setup

run `yarn install`

### Contributing

* Pull Requests are welcome, if you open a pull request we will do our best to get to it in a timely manner
* Pull Request Reviews and even more welcome! we need help testing, reviewing, and updating open PRs
* If you are interested in contributing more actively, please contact me (same username on Twitter, Facebook, etc.) Thanks!
* If you want to help us coding, join Expo slack https://slack.expo.io/, so we can chat over there. (#react-native-camera)

### FAQ

## Why is `react-native-camera` not listed as a dependency in `package.json`?

`react-native` uses `metro` for dependency resolution. In order to not recursively install this example into the `node_modules` of this example we use `rn-cli.config.js` to resolve `react-native-camera`. This also allows a quicker iteration when developing (without having to `yarn install` after every single change in `react-native-camera`).

Also the Header Search Paths in `ios/RNCameraExample.xcodeproj/project.pbxproj` are changed to fit the `react-native-camera` root directory.
