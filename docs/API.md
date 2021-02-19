---
id: api
title: Work in progress
---

## Props Index

[**wip**]

- [`zoom`](API.md#zoom)
- [`maxZoom`](API.md#maxzoom)
- [`type`](API.md#type)
- [`cameraId`](API.md#cameraid)
- [`flashMode`](API.md#flashmode)
- [`exposure`](API.md#exposure)
- [`whiteBalance`](API.md#whiteBalance)
- [`autoFocus`](API.md#autoFocus)
- [`ratio`](API.md#ratio)
- [`pictureSize`](API.md#pictureSize)
- [`focusDepth`](API.md#focusDepth)
- [`onMountError`](API.md#onMountError)
- [`onCameraReady`](API.md#onCameraReady)

## Methods Index

- [`takePictureAsync`](API.md#takepictureasync)
- [`recordAsync`](API.md#recordasync)
- [`refreshAuthorizationStatus`](API.md#refreshauthorizationstatus)
- [`stopRecording`](API.md#stoprecording)
- [`pausePreview`](API.md#pausepreview)
- [`resumePreview`](API.md#resumepreview)
- [`getAvailablePictureSizes`](API.md#getavailablepicturesizes)
- [`getSupportedRatiosAsync`](API.md#getsupportedratiosasync-android-only)
- [`isRecording`](API.md#isrecording-ios-only)
- [`getSupportedPreviewFpsRange`](API.md#getsupportedpreviewfpsrange-android-only)

## Props

---

### `zoom`

This property specifies the zoom value of the camera. Ranges from 0 to 1. Default to 0.

| Type   | Default Value |
| ------ | ------------- |
| number | 0             |

---

### `maxZoom`

The maximum zoom value of the camera. Defaults to 0.

| Type   | Default Value |
| ------ | ------------- |
| number | 0             |

---

### `type`

This property defines which camera on the phone the component is using.
Possible values:

- `front`
- `back`

| Type   | Default Value |
| ------ | ------------- |
| number | 'back'        |

---

### `cameraId`

For selecting from multiple cameras on Android devices. See [2492](https://github.com/react-native-community/react-native-camera/pull/2492) for more info. Can be retrieved with `getCameraIds()`

| Type   | Default Value | Platform |
| ------ | ------------- | -------- |
| String | `''`          | Android, iOS  |

---

### `flashMode`

Determines the state of the camera flash. Has the following possible states.

```off: '1',
on: 'auto',
auto: 'torch',
torch: 'off'
```

| Type   | Default Value |
| ------ | ------------- |
| object | `{ off: 1 }`  |

### `ratio`

A string representing the camera ratio in the format 'height:width'. Default is `"4:3"`.

Use `getSupportedRatiosAsync` method to get ratio strings supported by your camera on Android.

| Type   | Default Value |
| ------ | ------------- |
| string | `4:3`         |

### `pictureSize`

This prop has a different behaviour for Android and iOS and should rarely be set.

For Android, this prop attempts to control the camera sensor capture resolution, similar to how `ratio` behaves. This is useful for cases where a low resolution image is required, and makes further resizing less intensive on the device's memory. The list of possible values can be requested with `getAvailablePictureSizes`, and the value should be set in the format of `<width>x<height>`. Internally, the native code will attempt to get the best suited resolution for the given `pictureSize` value if the provided value is invalid, and will default to the highest resolution available.

For iOS, this prop controls the internal camera preset value and should rarely be changed. However, this value can be set to setup the sensor to match the video recording's quality in order to prevent flickering. The list of valid values can be gathered from https://developer.apple.com/documentation/avfoundation/avcapturesessionpreset and can also be requested with `getAvailablePictureSizes`.

| Type   | Default Value |
| ------ | ------------- |
| string | `None`        |

## Methods

## takePictureAsync()

Returns a promise with TakePictureResponse.

### Method type

```ts
takePictureAsync(options?: TakePictureOptions): Promise<TakePictureResponse>;
```

```ts
interface TakePictureOptions {
  quality?: number;
  orientation?: keyof Orientation | OrientationNumber;
  base64?: boolean;
  exif?: boolean;
  width?: number;
  mirrorImage?: boolean;
  doNotSave?: boolean;
  pauseAfterCapture?: boolean;
  writeExif?: boolean | { [name: string]: any };

  /** Android only */
  fixOrientation?: boolean;

  /** iOS only */
  forceUpOrientation?: boolean;
  imageType?: ImageType;
}

interface TakePictureResponse {
  width: number;
  height: number;
  uri: string;
  base64?: string;
  exif?: { [name: string]: any };
  pictureOrientation: number;
  deviceOrientation: number;
}
```

### Usage example

```js
takePicture = async () => {
  if (this.camera) {
    const data = await this.camera.takePictureAsync();
    console.warn('takePictureResponse ', data);
  }
};
```

---

## recordAsync()

Returns a promise with RecordResponse.

### Method type

```ts
recordAsync(options?: RecordOptions): Promise<RecordResponse>;
```

```ts
interface RecordOptions {
  quality?: keyof VideoQuality;
  orientation?: keyof Orientation | OrientationNumber;
  maxDuration?: number;
  maxFileSize?: number;
  mute?: boolean;
  mirrorVideo?: boolean;
  path?: string;
  videoBitrate?: number;
  fps?: number;

  /** iOS only */
  codec?: keyof VideoCodec | VideoCodec[keyof VideoCodec];
}

interface RecordResponse {
  /** Path to the video saved on your app's cache directory. */
  uri: string;
  videoOrientation: number;
  deviceOrientation: number;
  isRecordingInterrupted: boolean;
  /** iOS only */
  codec: VideoCodec[keyof VideoCodec];
}
```

### Usage example

```js
takeVideo = async () => {
  if (this.camera) {
    try {
      const promise = this.camera.recordAsync(this.state.recordOptions);

      if (promise) {
        this.setState({ isRecording: true });
        const data = await promise;
        this.setState({ isRecording: false });
        console.warn('takeVideo', data);
      }
    } catch (e) {
      console.error(e);
    }
  }
};
```

---

## refreshAuthorizationStatus()

Allows to make RNCamera check Permissions again and set status accordingly.
Making it possible to refresh status of RNCamera after user initially rejected the permissions.

### Method type

```ts
refreshAuthorizationStatus(): Promise<void>;
```

### Usage example

```js
/* -> {

} */
```

---

## stopRecording()

Should be called after recordAsync() to make the promise be fulfilled and get the video uri.

### Method type

```ts
stopRecording(): void;
```

### Usage example

```js
stopRecording(): void;
/* -> {

} */
```

---

## pausePreview()

Pauses the preview. The preview can be resumed again by using resumePreview().

### Method type

```ts
pausePreview(): void;
```

### Usage example

```js
/* -> {

} */
```

---

## resumePreview()

Resumes the preview after pausePreview() has been called.

### Method type

```ts
resumePreview(): void;
```

### Usage example

```js
/* -> {

} */
```

---

## getAvailablePictureSizes()

Returns a promise with getAvailablePictureSizes.

### Method type

```ts
getAvailablePictureSizes(): Promise<string[]>;

```

### Usage example

```js
/* -> {

} */
```

---

## getSupportedRatiosAsync() - Android only

Android only. Returns a promise. The promise will be fulfilled with an object with an array containing strings with all camera aspect ratios supported by the device.

### Method type

```ts
getSupportedRatiosAsync(): Promise<string[]>;
```

### Usage example

```js
/* -> {

} */
```

---

## isRecording() - iOS only

iOS only. Returns a promise. The promise will be fulfilled with a boolean indicating if currently recording is started or stopped.

### Method type

```ts
isRecording(): Promise<boolean>;

```

### Usage example

```js
const isRecording = await isRecording();
/* -> {
  isRecording = true
} */
```

- [`getSupportedPreviewFpsRange`](API.md#getSupportedPreviewFpsRange`)

## getSupportedPreviewFpsRange - Android only

Android only. Returns a promise. The promise will be fulfilled with a json object including the fps ranges available for those devices ([android docs](<https://developer.android.com/reference/android/hardware/Camera.Parameters#getSupportedPreviewFpsRange()>))

### Method type

```ts
getSupportedPreviewFpsRange(): Promise<[{MINIMUM_FPS: string, MAXIMUM_FPS: string}]>;

```

### Usage example

```js
const previewRange = await this.camera.getSupportedPreviewFpsRange();
/* -> [
  {
    MINIMUM_FPS: "15000",
    MAXIMUM_FPS: "15000"
  },
  {
    MINIMUM_FPS: "20000",
    MAXIMUM_FPS: "20000"
  }
] */
```

---
