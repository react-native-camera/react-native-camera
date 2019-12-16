// @flow
import React, { useState, useCallback, useMemo, useRef, useEffect } from 'react';
import PropTypes from 'prop-types';
import {
  findNodeHandle,
  Platform,
  NativeModules,
  ViewPropTypes,
  requireNativeComponent,
  View,
  ActivityIndicator,
  Text,
  StyleSheet,
  PermissionsAndroid,
} from 'react-native';

import type { FaceFeature } from './FaceDetector';

const Rationale = PropTypes.shape({
  title: PropTypes.string.isRequired,
  message: PropTypes.string.isRequired,
  buttonPositive: PropTypes.string,
  buttonNegative: PropTypes.string,
  buttonNeutral: PropTypes.string,
});

const requestPermissions = async (
  captureAudio: boolean,
  CameraManager: any,
  androidCameraPermissionOptions: Rationale,
  androidRecordAudioPermissionOptions: Rationale,
): Promise<{ hasCameraPermissions: boolean, hasRecordAudioPermissions: boolean }> => {
  let hasCameraPermissions = false;
  let hasRecordAudioPermissions = false;

  if (Platform.OS === 'ios') {
    hasCameraPermissions = await CameraManager.checkVideoAuthorizationStatus();
  } else if (Platform.OS === 'android') {
    const cameraPermissionResult = await PermissionsAndroid.request(
      PermissionsAndroid.PERMISSIONS.CAMERA,
      androidCameraPermissionOptions,
    );
    if (typeof cameraPermissionResult === 'boolean') {
      hasCameraPermissions = cameraPermissionResult;
    } else {
      hasCameraPermissions = cameraPermissionResult === PermissionsAndroid.RESULTS.GRANTED;
    }
  }

  if (captureAudio) {
    if (Platform.OS === 'ios') {
      hasRecordAudioPermissions = await CameraManager.checkRecordAudioAuthorizationStatus();
    } else if (Platform.OS === 'android') {
      if (await CameraManager.checkIfRecordAudioPermissionsAreDefined()) {
        const audioPermissionResult = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.RECORD_AUDIO,
          androidRecordAudioPermissionOptions,
        );
        if (typeof audioPermissionResult === 'boolean') {
          hasRecordAudioPermissions = audioPermissionResult;
        } else {
          hasRecordAudioPermissions = audioPermissionResult === PermissionsAndroid.RESULTS.GRANTED;
        }
      } else if (__DEV__) {
        // eslint-disable-next-line no-console
        console.warn(
          `The 'captureAudio' property set on RNCamera instance but 'RECORD_AUDIO' permissions not defined in the applications 'AndroidManifest.xml'. ` +
            `If you want to record audio you will have to add '<uses-permission android:name="android.permission.RECORD_AUDIO"/>' to your 'AndroidManifest.xml'. ` +
            `Otherwise you should set the 'captureAudio' property on the component instance to 'false'.`,
        );
      }
    }
  }

  return {
    hasCameraPermissions,
    hasRecordAudioPermissions,
  };
};

const styles = StyleSheet.create({
  authorizationContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  notAuthorizedText: {
    textAlign: 'center',
    fontSize: 16,
  },
});

type Orientation = 'auto' | 'landscapeLeft' | 'landscapeRight' | 'portrait' | 'portraitUpsideDown';
type OrientationNumber = 1 | 2 | 3 | 4;

type PictureOptions = {
  quality?: number,
  orientation?: Orientation | OrientationNumber,
  base64?: boolean,
  mirrorImage?: boolean,
  exif?: boolean,
  writeExif?: boolean | { [name: string]: any },
  width?: number,
  fixOrientation?: boolean,
  forceUpOrientation?: boolean,
  pauseAfterCapture?: boolean,
};

type TrackedFaceFeature = FaceFeature & {
  faceID?: number,
};

type TrackedTextFeature = {
  type: string,
  bounds: {
    size: {
      width: number,
      height: number,
    },
    origin: {
      x: number,
      y: number,
    },
  },
  value: string,
  components: Array<TrackedTextFeature>,
};

type TrackedBarcodeFeature = {
  bounds: {
    size: {
      width: number,
      height: number,
    },
    origin: {
      x: number,
      y: number,
    },
  },
  data: string,
  dataRaw: string,
  type: BarcodeType,
  format?: string,
  addresses?: {
    addressesType?: 'UNKNOWN' | 'Work' | 'Home',
    addressLines?: string[],
  }[],
  emails?: Email[],
  phones?: Phone[],
  urls: ?(string[]),
  name?: {
    firstName?: string,
    lastName?: string,
    middleName?: string,
    prefix?: string,
    pronounciation?: string,
    suffix?: string,
    formattedName?: string,
  },
  phone?: Phone,
  organization?: string,
  latitude?: number,
  longitude?: number,
  ssid?: string,
  password?: string,
  encryptionType?: string,
  title?: string,
  url?: string,
  firstName?: string,
  middleName?: string,
  lastName?: string,
  gender?: string,
  addressCity?: string,
  addressState?: string,
  addressStreet?: string,
  addressZip?: string,
  birthDate?: string,
  documentType?: string,
  licenseNumber?: string,
  expiryDate?: string,
  issuingDate?: string,
  issuingCountry?: string,
  eventDescription?: string,
  location?: string,
  organizer?: string,
  status?: string,
  summary?: string,
  start?: string,
  end?: string,
  email?: Email,
  phoneNumber?: string,
  message?: string,
};

type BarcodeType =
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

type Email = {
  address?: string,
  body?: string,
  subject?: string,
  emailType?: 'UNKNOWN' | 'Work' | 'Home',
};

type Phone = {
  number?: string,
  phoneType?: 'UNKNOWN' | 'Work' | 'Home' | 'Fax' | 'Mobile',
};

type RecordingOptions = {
  maxDuration?: number,
  maxFileSize?: number,
  orientation?: Orientation,
  quality?: number | string,
  codec?: string,
  mute?: boolean,
  path?: string,
  videoBitrate?: number,
};

type EventCallbackArgumentsType = {
  nativeEvent: Object,
};

type Rect = {
  x: number,
  y: number,
  width: number,
  height: number,
};

type PropsType = typeof View.props & {
  zoom?: number,
  maxZoom?: number,
  ratio?: string,
  focusDepth?: number,
  type?: number | string,
  onCameraReady?: Function,
  onAudioInterrupted?: Function,
  onAudioConnected?: Function,
  onStatusChange?: Function,
  onBarCodeRead?: Function,
  onPictureTaken?: Function,
  onPictureSaved?: Function,
  onGoogleVisionBarcodesDetected?: ({ barcodes: Array<TrackedBarcodeFeature> }) => void,
  onSubjectAreaChanged?: ({ nativeEvent: { prevPoint: {| x: number, y: number |} } }) => void,
  faceDetectionMode?: number,
  trackingEnabled?: boolean,
  flashMode?: number | string,
  exposure?: number,
  barCodeTypes?: Array<string>,
  googleVisionBarcodeType?: number,
  googleVisionBarcodeMode?: number,
  whiteBalance?: number | string,
  faceDetectionLandmarks?: number,
  autoFocus?: string | boolean | number,
  autoFocusPointOfInterest?: { x: number, y: number },
  faceDetectionClassifications?: number,
  onFacesDetected?: ({ faces: Array<TrackedFaceFeature> }) => void,
  onTextRecognized?: ({ textBlocks: Array<TrackedTextFeature> }) => void,
  captureAudio?: boolean,
  keepAudioSession?: boolean,
  useCamera2Api?: boolean,
  playSoundOnCapture?: boolean,
  videoStabilizationMode?: number | string,
  pictureSize?: string,
  rectOfInterest: Rect,
};

type StateType = {
  isAuthorized: boolean,
  isAuthorizationChecked: boolean,
  recordAudioPermissionStatus: RecordAudioPermissionStatus,
};

export type Status = 'READY' | 'PENDING_AUTHORIZATION' | 'NOT_AUTHORIZED';

const CameraStatus: { [key: Status]: Status } = {
  READY: 'READY',
  PENDING_AUTHORIZATION: 'PENDING_AUTHORIZATION',
  NOT_AUTHORIZED: 'NOT_AUTHORIZED',
};

export type RecordAudioPermissionStatus = 'AUTHORIZED' | 'NOT_AUTHORIZED' | 'PENDING_AUTHORIZATION';

const RecordAudioPermissionStatusEnum: {
  [key: RecordAudioPermissionStatus]: RecordAudioPermissionStatus,
} = {
  AUTHORIZED: 'AUTHORIZED',
  PENDING_AUTHORIZATION: 'PENDING_AUTHORIZATION',
  NOT_AUTHORIZED: 'NOT_AUTHORIZED',
};

const CameraManager: Object = NativeModules.RNCameraManager ||
  NativeModules.RNCameraModule || {
    stubbed: true,
    Type: {
      back: 1,
    },
    AutoFocus: {
      on: 1,
    },
    FlashMode: {
      off: 1,
    },
    WhiteBalance: {},
    BarCodeType: {},
    FaceDetection: {
      fast: 1,
      Mode: {},
      Landmarks: {
        none: 0,
      },
      Classifications: {
        none: 0,
      },
    },
    GoogleVisionBarcodeDetection: {
      BarcodeType: 0,
      BarcodeMode: 0,
    },
  };

const EventThrottleMs = 500;

const mapValues = (input, mapper) => {
  const result = {};
  Object.entries(input).map(([key, value]) => {
    result[key] = mapper(value, key);
  });
  return result;
};

export const Constants = {
  Type: CameraManager.Type,
  FlashMode: CameraManager.FlashMode,
  AutoFocus: CameraManager.AutoFocus,
  WhiteBalance: CameraManager.WhiteBalance,
  VideoQuality: CameraManager.VideoQuality,
  VideoCodec: CameraManager.VideoCodec,
  BarCodeType: CameraManager.BarCodeType,
  GoogleVisionBarcodeDetection: CameraManager.GoogleVisionBarcodeDetection,
  FaceDetection: CameraManager.FaceDetection,
  CameraStatus,
  RecordAudioPermissionStatus: RecordAudioPermissionStatusEnum,
  VideoStabilization: CameraManager.VideoStabilization,
  Orientation: {
    auto: 'auto',
    landscapeLeft: 'landscapeLeft',
    landscapeRight: 'landscapeRight',
    portrait: 'portrait',
    portraitUpsideDown: 'portraitUpsideDown',
  },
};

export const ConversionTables = {
  type: CameraManager.Type,
  flashMode: CameraManager.FlashMode,
  exposure: CameraManager.Exposure,
  autoFocus: CameraManager.AutoFocus,
  whiteBalance: CameraManager.WhiteBalance,
  faceDetectionMode: (CameraManager.FaceDetection || {}).Mode,
  faceDetectionLandmarks: (CameraManager.FaceDetection || {}).Landmarks,
  faceDetectionClassifications: (CameraManager.FaceDetection || {}).Classifications,
  googleVisionBarcodeType: (CameraManager.GoogleVisionBarcodeDetection || {}).BarcodeType,
  videoStabilizationMode: CameraManager.VideoStabilization || {},
};

export default function Camera(props) {
  let _cameraRef: ?Object;
  let _cameraHandle: ?number;
  let _lastEvents: { [string]: string };
  let _lastEventsTimes: { [string]: Date };
  let _isMounted: boolean;

  const [isAuthorized, setIsAuthorized] = useState(false);
  const [isAuthorizationChecked, setIsAuthorizationChecked] = useState(false);
  const [recordAudioPermissionStatus, setRecordAudioPermissionStatus] = useState(
    RecordAudioPermissionStatusEnum.PENDING_AUTHORIZATION,
  );

  const convertProp = useMemo((value: *, key: string) => {
    if (typeof value === 'string' && Camera.ConversionTables[key]) {
      return Camera.ConversionTables[key][value];
    }

    return value;
  }, []);
  const convertNativeProps = useCallback(
    (nativeProps: PropsType) => () => {
      const { children, ...props } = nativeProps;
      const newProps = mapValues(props, convertProp);

      if (props.onBarCodeRead) {
        newProps.barCodeScannerEnabled = true;
      }

      if (props.onGoogleVisionBarcodesDetected) {
        newProps.googleVisionBarcodeDetectorEnabled = true;
      }

      if (props.onFacesDetected) {
        newProps.faceDetectorEnabled = true;
      }

      if (props.onTextRecognized) {
        newProps.textRecognizerEnabled = true;
      }

      if (Platform.OS === 'ios') {
        delete newProps.googleVisionBarcodeMode;
        delete newProps.ratio;
      }

      return newProps;
    },
    [],
  );
  const hasFaCC = useMemo(() => typeof props.children === 'function', [props.children]);
  const setReference = useCallback(
    (ref: ?Object) => {
      if (ref) {
        _cameraRef = ref;
        _cameraHandle = findNodeHandle(ref);
      } else {
        _cameraRef = null;
        _cameraHandle = null;
      }
    },
    [_cameraRef, _cameraHandle],
  );
  const _onMountError = useCallback(
    ({ nativeEvent }: EventCallbackArgumentsType) => {
      if (props.onMountError) {
        props.onMountError(nativeEvent);
      }
    },
    [props.onMountError],
  );
  const _onCameraReady = useCallback(() => {
    if (props.onCameraReady) {
      props.onCameraReady();
    }
  }, [props.onCameraReady]);
  const _onAudioInterrupted = useCallback(() => {
    if (props.onAudioInterrupted) {
      props.onAudioInterrupted();
    }
  }, [props.onAudioInterrupted]);
  const _onAudioConnected = useCallback(() => {
    if (props.onAudioConnected) {
      props.onAudioConnected();
    }
  }, [props.onAudioConnected]);
  const _onObjectDetected = useCallback(
    (callback: ?Function) => ({ nativeEvent }: EventCallbackArgumentsType) => {
      const { type } = nativeEvent;
      if (
        _lastEvents[type] &&
        _lastEventsTimes[type] &&
        JSON.stringify(nativeEvent) === _lastEvents[type] &&
        new Date() - _lastEventsTimes[type] < EventThrottleMs
      ) {
        return;
      }

      if (callback) {
        callback(nativeEvent);
        _lastEventsTimes[type] = new Date();
        _lastEvents[type] = JSON.stringify(nativeEvent);
      }
    },
    [],
  );
  const _onPictureSaved = useCallback(
    ({ nativeEvent }: EventCallbackArgumentsType) => {
      if (props.onPictureSaved) {
        props.onPictureSaved(nativeEvent);
      }
    },
    [props.onPictureSaved],
  );
  const _onSubjectAreaChanged = useCallback(
    e => () => {
      if (props.onSubjectAreaChanged) {
        props.onSubjectAreaChanged(e);
      }
    },
    [props.onSubjectAreaChanged],
  );
  const getStatus = useMemo(() => {
    if (isAuthorizationChecked === false) {
      return CameraStatus.PENDING_AUTHORIZATION;
    }
    return isAuthorized ? CameraStatus.READY : CameraStatus.NOT_AUTHORIZED;
  }, [isAuthorizationChecked, isAuthorized]);
  const renderChildren = useMemo(() => {
    if (hasFaCC) {
      return props.children({
        camera: this,
        status: getStatus,
        recordAudioPermissionStatus: recordAudioPermissionStatus,
      });
    }
    return props.children;
  }, []);

  const { style, ...nativeProps } = convertNativeProps(props);

  const arePermissionsGranted = async () => {
    const {
      permissionDialogTitle,
      permissionDialogMessage,
      androidCameraPermissionOptions,
      androidRecordAudioPermissionOptions,
    } = props;

    let cameraPermissions = androidCameraPermissionOptions;
    let audioPermissions = androidRecordAudioPermissionOptions;
    if (permissionDialogTitle || permissionDialogMessage) {
      // eslint-disable-next-line no-console
      console.warn(
        'permissionDialogTitle and permissionDialogMessage are deprecated. Please use androidCameraPermissionOptions instead.',
      );
      cameraPermissions = {
        ...cameraPermissions,
        title: permissionDialogTitle,
        message: permissionDialogMessage,
      };
      audioPermissions = {
        ...audioPermissions,
        title: permissionDialogTitle,
        message: permissionDialogMessage,
      };
    }

    const { hasCameraPermissions, hasRecordAudioPermissions } = await requestPermissions(
      props.captureAudio,
      CameraManager,
      cameraPermissions,
      audioPermissions,
    );

    const recordAudioPermissionStatus = hasRecordAudioPermissions
      ? RecordAudioPermissionStatusEnum.AUTHORIZED
      : RecordAudioPermissionStatusEnum.NOT_AUTHORIZED;
    return { hasCameraPermissions, recordAudioPermissionStatus };
  };

  const refreshAuthorizationStatus = async () => {
    const { hasCameraPermissions, recordAudioPermissionStatus } = await arePermissionsGranted();
    if (_isMounted === false) {
      return;
    }

    setIsAuthorized(hasCameraPermissions);
    setIsAuthorizationChecked(true);
    setRecordAudioPermissionStatus(recordAudioPermissionStatus);
  };

  const _onStatusChange = useCallback(() => {
    if (props.onStatusChange) {
      props.onStatusChange({
        cameraStatus: getStatus(),
        recordAudioPermissionStatus,
      });
    }
  }, [props.onStatusChange]);

  const stopRecording = useCallback(() => {
    CameraManager.stopRecording(_cameraHandle);
  }, [_cameraHandle]);

  const pausePreview = useCallback(() => {
    CameraManager.pausePreview(_cameraHandle);
  }, [_cameraHandle]);

  const isRecording = useCallback(() => {
    return CameraManager.isRecording(_cameraHandle);
  }, [_cameraHandle]);

  const resumePreview = useCallback(() => {
    CameraManager.resumePreview(_cameraHandle);
  }, [_cameraHandle]);

  const takePictureAsync = async (options?: PictureOptions) => {
    if (!options) {
      options = {};
    }
    if (!options.quality) {
      options.quality = 1;
    }

    if (options.orientation) {
      if (typeof options.orientation !== 'number') {
        const { orientation } = options;
        options.orientation = CameraManager.Orientation[orientation];
        if (__DEV__) {
          if (typeof options.orientation !== 'number') {
            // eslint-disable-next-line no-console
            console.warn(`Orientation '${orientation}' is invalid.`);
          }
        }
      }
    }

    if (options.pauseAfterCapture === undefined) {
      options.pauseAfterCapture = false;
    }

    if (!_cameraHandle) {
      throw 'Camera handle cannot be null';
    }

    return await CameraManager.takePicture(options, _cameraHandle);
  };

  const getSupportedRatiosAsync = async () => {
    if (Platform.OS === 'android') {
      return await CameraManager.getSupportedRatios(_cameraHandle);
    } else {
      throw new Error('Ratio is not supported on iOS');
    }
  };

  const getCameraIdsAsync = async () => {
    if (Platform.OS === 'android') {
      return await CameraManager.getCameraIds(_cameraHandle);
    } else {
      return await CameraManager.getCameraIds(); // iOS does not need a camera instance
    }
  };

  const getAvailablePictureSizes = async (): string[] => {
    //$FlowFixMe
    return await CameraManager.getAvailablePictureSizes(props.ratio, _cameraHandle);
  };

  const recordAsync = async (options?: RecordingOptions) => {
    if (!options || typeof options !== 'object') {
      options = {};
    } else if (typeof options.quality === 'string') {
      options.quality = Camera.Constants.VideoQuality[options.quality];
    }
    if (options.orientation) {
      if (typeof options.orientation !== 'number') {
        const { orientation } = options;
        options.orientation = CameraManager.Orientation[orientation];
        if (__DEV__) {
          if (typeof options.orientation !== 'number') {
            // eslint-disable-next-line no-console
            console.warn(`Orientation '${orientation}' is invalid.`);
          }
        }
      }
    }

    if (__DEV__) {
      if (options.videoBitrate && typeof options.videoBitrate !== 'number') {
        // eslint-disable-next-line no-console
        console.warn('Video Bitrate should be a positive integer');
      }
    }

    const { captureAudio } = props;

    if (
      !captureAudio ||
      recordAudioPermissionStatus !== RecordAudioPermissionStatusEnum.AUTHORIZED
    ) {
      options.mute = true;
    }

    if (__DEV__) {
      if (
        (!options.mute || captureAudio) &&
        recordAudioPermissionStatus !== RecordAudioPermissionStatusEnum.AUTHORIZED
      ) {
        // eslint-disable-next-line no-console
        console.warn('Recording with audio not possible. Permissions are missing.');
      }
    }

    return await CameraManager.record(options, _cameraHandle);
  };

  useEffect(() => {
    async function getPermissionsAndSetState() {
      const { hasCameraPermissions, recordAudioPermissionStatus } = await arePermissionsGranted();
      if (_isMounted === false) {
        return;
      }

      setIsAuthorized(hasCameraPermissions);
      setIsAuthorizationChecked(true);
      setRecordAudioPermissionStatus(recordAudioPermissionStatus);
      _onStatusChange();
    }

    getPermissionsAndSetState();

    return () => {
      _isMounted = false;
    };
  }, []);

  if (isAuthorized || hasFaCC) {
    return (
      <View style={style}>
        <RNCamera
          {...nativeProps}
          style={StyleSheet.absoluteFill}
          ref={setReference}
          onMountError={_onMountError}
          onCameraReady={_onCameraReady}
          onAudioInterrupted={_onAudioInterrupted}
          onAudioConnected={_onAudioConnected}
          onGoogleVisionBarcodesDetected={_onObjectDetected(props.onGoogleVisionBarcodesDetected)}
          onBarCodeRead={_onObjectDetected(props.onBarCodeRead)}
          onFacesDetected={_onObjectDetected(props.onFacesDetected)}
          onTextRecognized={_onObjectDetected(props.onTextRecognized)}
          onPictureSaved={_onPictureSaved}
          onSubjectAreaChanged={_onSubjectAreaChanged}
        />
        {renderChildren}
      </View>
    );
  } else if (!isAuthorizationChecked) {
    return props.pendingAuthorizationView;
  } else {
    return props.notAuthorizedView;
  }
}

Camera.propTypes = {
  ...ViewPropTypes,
  zoom: PropTypes.number,
  maxZoom: PropTypes.number,
  ratio: PropTypes.string,
  focusDepth: PropTypes.number,
  onMountError: PropTypes.func,
  onCameraReady: PropTypes.func,
  onAudioInterrupted: PropTypes.func,
  onAudioConnected: PropTypes.func,
  onStatusChange: PropTypes.func,
  onBarCodeRead: PropTypes.func,
  onPictureTaken: PropTypes.func,
  onPictureSaved: PropTypes.func,
  onGoogleVisionBarcodesDetected: PropTypes.func,
  onFacesDetected: PropTypes.func,
  onTextRecognized: PropTypes.func,
  onSubjectAreaChanged: PropTypes.func,
  trackingEnabled: PropTypes.bool,
  faceDetectionMode: PropTypes.number,
  faceDetectionLandmarks: PropTypes.number,
  faceDetectionClassifications: PropTypes.number,
  barCodeTypes: PropTypes.arrayOf(PropTypes.string),
  googleVisionBarcodeType: PropTypes.number,
  googleVisionBarcodeMode: PropTypes.number,
  type: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  cameraId: PropTypes.string,
  flashMode: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  exposure: PropTypes.number,
  whiteBalance: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  autoFocus: PropTypes.oneOfType([PropTypes.string, PropTypes.number, PropTypes.bool]),
  autoFocusPointOfInterest: PropTypes.shape({ x: PropTypes.number, y: PropTypes.number }),
  permissionDialogTitle: PropTypes.string,
  permissionDialogMessage: PropTypes.string,
  androidCameraPermissionOptions: Rationale,
  androidRecordAudioPermissionOptions: Rationale,
  notAuthorizedView: PropTypes.element,
  pendingAuthorizationView: PropTypes.element,
  captureAudio: PropTypes.bool,
  keepAudioSession: PropTypes.bool,
  useCamera2Api: PropTypes.bool,
  playSoundOnCapture: PropTypes.bool,
  videoStabilizationMode: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  pictureSize: PropTypes.string,
  mirrorVideo: PropTypes.bool,
  rectOfInterest: PropTypes.any,
  defaultVideoQuality: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
};

Camera.defaultProps = {
  zoom: 0,
  maxZoom: 0,
  ratio: '4:3',
  focusDepth: 0,
  type: CameraManager.Type.back,
  cameraId: null,
  autoFocus: CameraManager.AutoFocus.on,
  flashMode: CameraManager.FlashMode.off,
  exposure: -1,
  whiteBalance: CameraManager.WhiteBalance.auto,
  faceDetectionMode: (CameraManager.FaceDetection || {}).fast,
  barCodeTypes: Object.values(CameraManager.BarCodeType),
  googleVisionBarcodeType: ((CameraManager.GoogleVisionBarcodeDetection || {}).BarcodeType || {})
    .None,
  googleVisionBarcodeMode: ((CameraManager.GoogleVisionBarcodeDetection || {}).BarcodeMode || {})
    .NORMAL,
  faceDetectionLandmarks: ((CameraManager.FaceDetection || {}).Landmarks || {}).none,
  faceDetectionClassifications: ((CameraManager.FaceDetection || {}).Classifications || {}).none,
  permissionDialogTitle: '',
  permissionDialogMessage: '',
  androidCameraPermissionOptions: {
    title: '',
    message: '',
  },
  androidRecordAudioPermissionOptions: {
    title: '',
    message: '',
  },
  notAuthorizedView: (
    <View style={styles.authorizationContainer}>
      <Text style={styles.notAuthorizedText}>Camera not authorized</Text>
    </View>
  ),
  pendingAuthorizationView: (
    <View style={styles.authorizationContainer}>
      <ActivityIndicator size="small" />
    </View>
  ),
  captureAudio: true,
  keepAudioSession: false,
  useCamera2Api: false,
  playSoundOnCapture: false,
  pictureSize: 'None',
  videoStabilizationMode: 0,
  mirrorVideo: false,
};

const RNCamera = requireNativeComponent('RNCamera', Camera, {
  nativeOnly: {
    accessibilityComponentType: true,
    accessibilityLabel: true,
    accessibilityLiveRegion: true,
    barCodeScannerEnabled: true,
    googleVisionBarcodeDetectorEnabled: true,
    faceDetectorEnabled: true,
    textRecognizerEnabled: true,
    importantForAccessibility: true,
    onBarCodeRead: true,
    onGoogleVisionBarcodesDetected: true,
    onCameraReady: true,
    onAudioInterrupted: true,
    onAudioConnected: true,
    onPictureSaved: true,
    onFaceDetected: true,
    onLayout: true,
    onMountError: true,
    onSubjectAreaChanged: true,
    renderToHardwareTextureAndroid: true,
    testID: true,
  },
});
