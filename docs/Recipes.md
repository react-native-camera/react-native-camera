# Recipes

The idea is to provide a list of useful snippets, links and resources to be used together with react-native-camera

## Stop Face Detection/Barcode Detection/Text Recognition

You can use a state variable and use it to determine if you want to turn on or off face detection/barcode detection/text recognition.

Example:
```
const { shouldFaceDetect } = this.state;
<RNCamera
  ...
  onFaceDetected={shouldFaceDetect ? this.handleFaceDetection : null}
</RNCamera>
```

Passing `null` to `onFaceDetected`, `onGoogleVisionBarcodesDetected`, `onTextRecognized`, `onBarCodeRead` automatically turns off the correspondent detector.

### Events continue if screen is mounted but not on top of stack

Lets say you use Face Detection, you take a picture and then takes the user to another screen to see that picture. Meanwhile, RNCamera is still mounted on the previous screen. `onFaceDetected` will still be called if you do not prevent it. For example (using [`react-navigation`](https://github.com/react-navigation/react-navigation):

```
const takePictureAndShow = () => {
  const { uri } = await this.camera.takePictureAsync();
  this.props.navigation.navigate(RouteNames.SEE_PHOTO, {
    photoUri: uri,
  };
}
```

When you go to the route SEE_PHOTO, `onFaceDetected` will still be called after the user is seeing that screen and not RNCamera. With `react-naviagtion`, what you could do is:

```
const { navigation } = this.props;
<RNCamera
  ...
  onFaceDetected={navigation.isFocused() ? this.handleFaceDetected : null}
</RNCamera>
```

## Sending the image to a server

A good way is to get the base64 string representation of your image. You can get it from RNCamera by passing the `base64: true` option lto `takePictureAsync` like:
```
if (this.camera) {
  const data = await this.camera.takePictureAsync({ base64: true });
  console.log('base64: ', data.base64);
  this.props.action.sendImageToServer(data.base64);
}
```

Getting the base64 string may take a while, so an alternative, to not make the user wait too much for its photo is to get the returned uri and use another library to get the base64 string while you are already displaying the taken picture to the user.

One option for this is [`react-native-fs`](https://github.com/itinance/react-native-fs), here is how we use it.

```
export async function getBase64(imageUri: string) {
  const filepath = imageUri.split('//')[1];
  const imageUriBase64 = await RNFS.readFile(filepath, 'base64');
  return `data:image/jpeg;base64,${imageUriBase64}`;
}

const takePicture = () => {
  const { uri } = await this.camera.takePictureAsync();
  this.setState({ photoUri: uri });//show image to user now
  const base64 = await getBase64(uri);
  console.log('base64: ', base64);
  this.props.actions.sendImageToServer(base64);
}
```

## How to get a video thumbnail?

Use this package https://github.com/phuochau/react-native-thumbnail
