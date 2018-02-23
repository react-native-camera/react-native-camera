// Type definitions for react-native-camera 1.0
// Definitions by Felipe Constantino <https://github.com/fconstant>
// If you modify this file, put your GitHub info here as well (for easy contacting purposes)

/*
 * Author notes:
 * I've tried to find a easy tool to convert from Flow to Typescript definition files (.d.ts).
 * So we woudn't have to do it manually... Sadly, I haven't found it.
 * 
 * If you are seeing this from the future, please, send us your cutting-edge technology :) (if it exists)
 */
import { Component } from 'react';
import { ViewProperties } from "react-native";

type AutoFocus = { on, off };
type FlashMode = { on, off, torch, auto }
type CameraType = { front, back }
type WhiteBalance = { sunny, cloudy, shadow, incandescent, fluorescent, auto }
type BarCodeType = { aztec, code128, code39, code39mod43, code93, ean13, ean8, pdf417, qr, upce, interleaved2of5, itf14, datamatrix }
type VideoQuality = { '2160p', '1080p', '720p', '480p', '4:3' }

type FaceDetectionClassifications = { all, none }
type FaceDetectionLandmarks = { all, none }
type FaceDetectionMode = { fast, accurate }

export interface Constants {
    AutoFocus: AutoFocus;
    FlashMode: FlashMode;
    Type: CameraType;
    WhiteBalance: WhiteBalance;
    VideoQuality: VideoQuality;
    BarCodeType: BarCodeType;
    FaceDetection: {
        Classifications: FaceDetectionClassifications;
        Landmarks: FaceDetectionLandmarks;
        Mode: FaceDetectionMode;
    }
}

export interface RNCameraProps extends ViewProperties {
    autoFocus?: keyof AutoFocus;
    type?: keyof CameraType;
    flashMode?: keyof FlashMode;
    notAuthorizedView?: JSX.Element;
    pendingAuthorizationView?: JSX.Element;

    onCameraReady?(): void;
    onMountError?(): void;

    /** Value: float from 0 to 1.0 */
    zoom?: number;
    /** Value: float from 0 to 1.0 */
    focusDepth?: number;

    // -- BARCODE PROPS
    barCodeTypes?: Array<keyof BarCodeType>;
    onBarCodeRead?(data: string, type: keyof BarCodeType): void;

    // -- FACE DETECTION PROPS

    onFacesDetected?(response: { faces: Face[] }): void;
    onFaceDetectionError?(response: { isOperational: boolean }): void;
    faceDetectionMode?: keyof FaceDetectionMode;
    faceDetectionLandmarks?: keyof FaceDetectionLandmarks;
    faceDetectionClassifications?: keyof FaceDetectionClassifications;

    // -- ANDROID ONLY PROPS

    /** Android only */
    ratio?: number;
    /** Android only */
    permissionDialogTitle?: string;
    /** Android only */
    permissionDialogMessage?: string;

    // -- IOS ONLY PROPS
    
    /** iOS Only */
    captureAudio?: boolean;

}

interface Point {
    x: number,
    y: number
}

interface Face {
    faceID?: number,
    bounds: {
        size: {
            width: number;
            height: number;
        };
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

interface TakePictureOptions {
    quality?: number;
    base64?: boolean;
    exif?: boolean;
}

interface TakePictureResponse {
    width: number;
    height: number;
    uri: string;
    base64?: string;
    exif?: { [name: string]: any };
}


interface RecordOptions {
    quality?: keyof VideoQuality;
    maxDuration?: number;
    maxFileSize?: number;
    mute?: boolean;
}

interface RecordResponse {
    /** Path to the video saved on your app's cache directory. */
    uri: string;
}

export class RNCamera extends Component<RNCameraProps> {
    static Constants: Constants;

    takePictureAsync(options?: TakePictureOptions): Promise<TakePictureResponse>;
    recordAsync(options?: RecordOptions): Promise<RecordResponse>;
    stopRecording(): void;

    /** Android only */
    getSupportedRatiosAsync(): Promise<string[]>;
}

// -- DEPRECATED CONTENT BELOW

/**
 * @deprecated As of 1.0.0 release, RCTCamera is deprecated. Please use RNCamera for the latest fixes and improvements.
 */
export default class RCTCamera extends Component<any> {
    static constants: any;
}