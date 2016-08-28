#### `Run example`

From project root run through cli:
- `cd Example/`
- `npm install`

For Android:
- `adb reverse tcp:8081 tcp:8081` or in Dev Settings input COMPUTER_IP:8081 for debug server
- `react-native run-android`

For iOS build:
- Open Example.xcodeproj with XCode
- Change IP for jsCodeLocation in AppDelegate.m file
- Run from XCode
