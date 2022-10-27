// Type definitions for react-native-camera 1.0
// Definitions by: Felipe Constantino <https://github.com/fconstant>
//                 Trent Jones <https://github.com/FizzBuzz791>
//                 Brent Kelly <https://github.com/mrbrentkelly>
// If you modify this file, put your GitHub info here as well (for easy contacting purposes)

/*
 * Author notes:
 * I've tried to find a easy tool to convert from Flow to Typescript definition files (.d.ts).
 * So we woudn't have to do it manually... Sadly, I haven't found it.
 *
 * If you are seeing this from the future, please, send us your cutting-edge technology :) (if it exists)
 */
import { Component, ReactNode } from 'react';
import { NativeMethods, ViewProperties, findNodeHandle } from 'react-native';

type Orientation = Readonly<{
  auto: any;
  landscapeLeft: any;
  landscapeRight: any;
  portrait: any;
  portraitUpsideDown: any;
}>;
type OrientationNumber = 1 | 2 | 3 | 4;
type AutoFocus = Readonly<{ on: any; off: any }>;
type VideoStabilization = Readonly<{ off: any; standard: any; cinematic: any; auto: any }>;
type FlashMode = Readonly<{ on: any; off: any; torch: any; auto: any }>;
type CameraType = Readonly<{ front: any; back: any }>;
type WhiteBalance = Readonly<{
  sunny: any;
  cloudy: any;
  shadow: any;
  incandescent: any;
  fluorescent: any;
  auto: any;
}>;
type CustomWhiteBalance = {
  temperature: number;
  tint: number;
  redGainOffset?: number;
  greenGainOffset?: number;
  blueGainOffset?: number;
};
type BarCodeType = Readonly<{
  aztec: any;
  code128: any;
  code39: any;
  code39mod43: any;
  code93: any;
  ean13: any;
  ean8: any;
  pdf417: any;
  qr: any;
  upc_e: any;
  interleaved2of5: any;
  itf14: any;
  datamatrix: any;
}>;
type VideoQuality = Readonly<{
  '2160p': any;
  '1080p': any;
  '720p': any;
  '480p': any;
  '4:3': any;
  /** iOS Only. Android not supported. */
  '288p': any;
}>;
type VideoCodec = Readonly<{
  H264: symbol;
  JPEG: symbol;
  HVEC: symbol;
  AppleProRes422: symbol;
  AppleProRes4444: symbol;
}>;
type ImageType = Readonly<{
  'jpeg': any;
  'png': any;
}>;

type FaceDetectionClassifications = Readonly<{ all: any; none: any }>;
type FaceDetectionLandmarks = Readonly<{ all: any; none: any }>;
type FaceDetectionMode = Readonly<{ fast: any; accurate: any }>;
type GoogleVisionBarcodeType = Readonly<{
  CODE_128: any;
  CODE_39: any;
  CODABAR: any;
  DATA_MATRIX: any;
  EAN_13: any;
  EAN_8: any;
  ITF: any;
  QR_CODE: any;
  UPC_A: any;
  UPC_E: any;
  PDF417: any;
  AZTEC: any;
  ALL: any;
}>;
type GoogleVisionBarcodeMode = Readonly<{ NORMAL: any; ALTERNATE: any; INVERTED: any }>;

// FaCC (Function as Child Components)
type Self<T> = { [P in keyof T]: P };
type CameraStatus = Readonly<Self<{ READY: any; PENDING_AUTHORIZATION: any; NOT_AUTHORIZED: any }>>;
type RecordAudioPermissionStatus = Readonly<
  Self<{
    AUTHORIZED: 'AUTHORIZED';
    PENDING_AUTHORIZATION: 'PENDING_AUTHORIZATION';
    NOT_AUTHORIZED: 'NOT_AUTHORIZED';
  }>
>;
type FaCC = (
  params: {
    camera: RNCamera;
    status: keyof CameraStatus;
    recordAudioPermissionStatus: keyof RecordAudioPermissionStatus;
  },
) => JSX.Element;

export interface Constants {
  CameraStatus: CameraStatus;
  AutoFocus: AutoFocus;
  FlashMode: FlashMode;
  VideoCodec: VideoCodec;
  Type: CameraType;
  WhiteBalance: WhiteBalance;
  VideoQuality: VideoQuality;
  ImageType: ImageType;
  BarCodeType: BarCodeType;
  FaceDetection: {
    Classifications: FaceDetectionClassifications;
    Landmarks: FaceDetectionLandmarks;
    Mode: FaceDetectionMode;
  };
  GoogleVisionBarcodeDetection: {
    BarcodeType: GoogleVisionBarcodeType;
    BarcodeMode: GoogleVisionBarcodeMode;
  };
  Orientation: {
    auto: 'auto';
    landscapeLeft: 'landscapeLeft';
    landscapeRight: 'landscapeRight';
    portrait: 'portrait';
    portraitUpsideDown: 'portraitUpsideDown';
  };
  VideoStabilization: VideoStabilization;
}

export interface BarCodeReadEvent {
  data: string;
  rawData?: string;
  type: keyof BarCodeType;
  /**
   * @description For Android use `{ width: number, height: number, origin: Array<Point<string>> }`
   * @description For iOS use `{ origin: Point<string>, size: Size<string> }`
   */
  bounds:
    | { width: number; height: number; origin: Array<Point<string>> }
    | { origin: Point<string>; size: Size<string> };
  /**
   * Raw image bytes in JPEG format (quality 100) as Base64-encoded string, only provided if `detectedImageInEvent=true`.
   */
  image: string;
}

export interface GoogleVisionBarcodesDetectedEvent {
  type: string;
  barcodes: Barcode[];
  target: number;
  /**
   * Raw image bytes in JPEG format (quality 100) as Base64-encoded string, only provided if `detectedImageInEvent=true`.
   */
  image?: string;
}

export interface RNCameraProps {
  children?: ReactNode | FaCC;
  cameraId?: string;

  autoFocus?: keyof AutoFocus;
  autoFocusPointOfInterest?: Point;
  pictureSize?: string;

  /* iOS only */
  onSubjectAreaChanged?: (event: { nativeEvent: { prevPoint: { x: number; y: number } } }) => void;
  type?: keyof CameraType;
  flashMode?: keyof FlashMode;
  notAuthorizedView?: JSX.Element;
  pendingAuthorizationView?: JSX.Element;
  useCamera2Api?: boolean;
  exposure?: number;
  whiteBalance?: keyof WhiteBalance | CustomWhiteBalance;
  captureAudio?: boolean;

  onCameraReady?(): void;
  onStatusChange?(event: {
    cameraStatus: keyof CameraStatus;
    recordAudioPermissionStatus: keyof RecordAudioPermissionStatus;
  }): void;
  onMountError?(error: { message: string }): void;

  onPictureTaken?(): void;
  onRecordingStart?(event: {
    nativeEvent: {
      uri: string;
      videoOrientation: number;
      deviceOrientation: number;
    };
  }): void;
  onRecordingEnd?(): void;

  /** iOS only */
  onAudioInterrupted?(): void;
  onAudioConnected?(): void;
  onTap?(origin: Point): void;
  onDoubleTap?(origin: Point): void;
  /** Use native pinch to zoom implementation*/
  useNativeZoom?: boolean;
  /** Value: float from 0 to 1.0 */
  zoom?: number;
  /** iOS only. float from 0 to any. Locks the max zoom value to the provided value
    A value <= 1 will use the camera's max zoom, while a value > 1
    will use that value as the max available zoom
  **/
  maxZoom?: number;
  /** Value: float from 0 to 1.0 */
  focusDepth?: number;

  // -- BARCODE PROPS
  detectedImageInEvent?: boolean;
  barCodeTypes?: Array<keyof BarCodeType>;
  googleVisionBarcodeType?: Constants['GoogleVisionBarcodeDetection']['BarcodeType'];
  googleVisionBarcodeMode?: Constants['GoogleVisionBarcodeDetection']['BarcodeMode'];
  onBarCodeRead?(event: BarCodeReadEvent): void;
  onGoogleVisionBarcodesDetected?(event: GoogleVisionBarcodesDetectedEvent): void;

  // limiting scan area
  rectOfInterest?: RectOfInterest;

  // -- FACE DETECTION PROPS

  onFacesDetected?(response: { faces: Face[] }): void;
  onFaceDetectionError?(response: { isOperational: boolean }): void;
  faceDetectionMode?: keyof FaceDetectionMode;
  faceDetectionLandmarks?: keyof FaceDetectionLandmarks;
  faceDetectionClassifications?: keyof FaceDetectionClassifications;
  trackingEnabled?: boolean;

  onTextRecognized?(response: { textBlocks: TrackedTextFeature[] }): void;
  // -- ANDROID ONLY PROPS
  /** Android only */
  ratio?: string;
  /** Android only - Deprecated */
  permissionDialogTitle?: string;
  /** Android only - Deprecated */
  permissionDialogMessage?: string;
  /** Android only */
  playSoundOnCapture?: boolean;
  /** Android only */
  playSoundOnRecord?: boolean;

  androidCameraPermissionOptions?: {
    title: string;
    message: string;
    buttonPositive?: string;
    buttonNegative?: string;
    buttonNeutral?: string;
  } | null;

  androidRecordAudioPermissionOptions?: {
    title: string;
    message: string;
    buttonPositive?: string;
    buttonNegative?: string;
    buttonNeutral?: string;
  } | null;

  // limiting scan area, must provide cameraViewDimensions for Android
  cameraViewDimensions?: Object;

  // -- IOS ONLY PROPS
  videoStabilizationMode?: keyof VideoStabilization;
  defaultVideoQuality?: keyof VideoQuality;
  /* if true, audio session will not be released on component unmount */
  keepAudioSession?: boolean;
}

interface Point<T = number> {
  x: T;
  y: T;
}

interface Size<T = number> {
  width: T;
  height: T;
}

interface RectOfInterest extends Point,Size{}

export interface Barcode {
  bounds: {
    size: Size;
    origin: Point;
  };
  data: string;
  dataRaw: string;
  type: BarcodeType;
  format?: string;
  addresses?: {
    addressesType?: 'UNKNOWN' | 'Work' | 'Home';
    addressLines?: string[];
  }[];
  emails?: Email[];
  phones?: Phone[];
  urls?: string[];
  name?: {
    firstName?: string;
    lastName?: string;
    middleName?: string;
    prefix?: string;
    pronounciation?: string;
    suffix?: string;
    formattedName?: string;
  };
  phone?: Phone;
  organization?: string;
  latitude?: number;
  longitude?: number;
  ssid?: string;
  password?: string;
  encryptionType?: string;
  title?: string;
  url?: string;
  firstName?: string;
  middleName?: string;
  lastName?: string;
  gender?: string;
  addressCity?: string;
  addressState?: string;
  addressStreet?: string;
  addressZip?: string;
  birthDate?: string;
  documentType?: string;
  licenseNumber?: string;
  expiryDate?: string;
  issuingDate?: string;
  issuingCountry?: string;
  eventDescription?: string;
  location?: string;
  organizer?: string;
  status?: string;
  summary?: string;
  start?: string;
  end?: string;
  email?: Email;
  phoneNumber?: string;
  message?: string;
}

export type BarcodeType =
  | 'EMAIL'
  | 'PHONE'
  | 'CALENDAR_EVENT'
  | 'DRIVER_LICENSE'
  | 'GEO'
  | 'SMS'
  | 'CONTACT_INFO'
  | 'WIFI'
  | 'TEXT'
  | 'ISBN'
  | 'PRODUCT'
  | 'URL';

export interface Email {
  address?: string;
  body?: string;
  subject?: string;
  emailType?: 'UNKNOWN' | 'Work' | 'Home';
}

export interface Phone {
  number?: string;
  phoneType?: 'UNKNOWN' | 'Work' | 'Home' | 'Fax' | 'Mobile';
}

export interface Face {
  faceID?: number;
  bounds: {
    size: Size;
    origin: Point;
  };
  smilingProbability?: number;
  leftEarPosition?: Point;
  rightEarPosition?: Point;
  leftEyePosition?: Point;
  leftEyeOpenProbability?: number;
  rightEyePosition?: Point;
  rightEyeOpenProbability?: number;
  leftCheekPosition?: Point;
  rightCheekPosition?: Point;
  leftMouthPosition?: Point;
  mouthPosition?: Point;
  rightMouthPosition?: Point;
  bottomMouthPosition?: Point;
  noseBasePosition?: Point;
  yawAngle?: number;
  rollAngle?: number;
}

export interface TrackedTextFeatureRecursive {
  type: 'block' | 'line' | 'element';
  bounds: {
    size: Size;
    origin: Point;
  };
  value: string;
  components?: TrackedTextFeatureRecursive[];
}

export interface TrackedTextFeature extends TrackedTextFeatureRecursive {
  components: TrackedTextFeatureRecursive[];
}

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
  imageType?: keyof ImageType;
  path?: string;
}

export interface TakePictureResponse {
  width: number;
  height: number;
  uri: string;
  base64?: string;
  exif?: { [name: string]: any };
  pictureOrientation: number;
  deviceOrientation: number;
}

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
  fps?: number;
}

export interface RecordResponse {
  /** Path to the video saved on your app's cache directory. */
  uri: string;
  videoOrientation: number;
  deviceOrientation: number;
  isRecordingInterrupted: boolean;
  /** iOS only */
  codec: VideoCodec[keyof VideoCodec];
}

export interface HardwareCamera {
  /** (iOS only) e.g: 'AVCaptureDeviceTypeBuiltInWideAngleCamera', 'AVCaptureDeviceTypeBuiltInUltraWideCamera' */
  deviceType?: string;
  id: string;
  type: number;
}

export function hasTorch(): Promise<boolean>;

export class RNCamera extends Component<RNCameraProps & ViewProperties> {
  static Constants: Constants;

  _cameraRef: null | NativeMethods;
  _cameraHandle: ReturnType<typeof findNodeHandle>;

  takePictureAsync(options?: TakePictureOptions): Promise<TakePictureResponse>;
  recordAsync(options?: RecordOptions): Promise<RecordResponse>;
  refreshAuthorizationStatus(): Promise<void>;
  stopRecording(): void;
  pausePreview(): void;
  resumePreview(): void;
  getAvailablePictureSizes(): Promise<string[]>;
  getCameraIdsAsync(): Promise<HardwareCamera[]>;

  /** Android only */
  getSupportedRatiosAsync(): Promise<string[]>;
  getSupportedPreviewFpsRange: Promise<string[]>;
  static checkIfVideoIsValid: Promise<boolean>;

  /** iOS only */
  isRecording(): Promise<boolean>;
}

interface DetectionOptions {
  mode?: keyof FaceDetectionMode;
  detectLandmarks?: keyof FaceDetectionLandmarks;
  runClassifications?: keyof FaceDetectionClassifications;
}

export class FaceDetector {
  private constructor();
  static Constants: Constants['FaceDetection'];
  static detectFacesAsync(uri: string, options?: DetectionOptions): Promise<Face[]>;
}

// -- DEPRECATED CONTENT BELOW

/**
 * @deprecated As of 1.0.0 release, RCTCamera is deprecated. Please use RNCamera for the latest fixes and improvements.
 */
export default class RCTCamera extends Component<any> {
  static constants: any;
}
