declare module 'react-native-camera' {

    import * as React from 'react';
    import {
        ViewStyle,
    } from 'react-native';
    import {
        PropTypes,
    } from 'react';

    export interface CameraProps extends React.Props<Camera> {
        style?: ViewStyle;
        aspect?: Aspect;
        captureAudio?: boolean;
        captureMode?: number;
        captureQuality?: number;
        captureTarget?: number;
        defaultOnFocusComponent?: boolean;
        flashMode?: number;
        keepAwake?: boolean;
        onBarCodeRead?: PropTypes.func;
        barcodeScannerEnabled?: boolean;
        cropToPreview?: boolean;
        onFocusChanged?: PropTypes.func;
        onZoomChanged?: PropTypes.func;
        mirrorImage?: boolean;
        fixOrientation?: boolean;
        barCodeTypes?: PropTypes.array;
        orientation?: number;
        playSoundOnCapture?: boolean;
        torchMode?: number;
        type?: number;
        permissionDialogTitle?: PropTypes.string;
        permissionDialogMessage?: PropTypes.string;
        notAuthorizedView?: PropTypes.element;
        pendingAuthorizationView?: PropTypes.element;
    }

    export default class Camera extends React.Component<CameraProps> {
        stopCapture(): void;
        capture(CameraProps): Promise<void>;
    }
}
