---
id: api
title: Work in progress
---
## API props
[**wip**]

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

  /** Android only */
  skipProcessing?: boolean;
  fixOrientation?: boolean;
  writeExif?: boolean | { [name: string]: any };

  /** iOS only */
  forceUpOrientation?: boolean;
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

---
