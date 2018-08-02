import { PermissionsAndroid, Platform } from 'react-native';

export const requestPermissions = async (hasVideoAndAudio, CameraManager, permissionDialogTitle, permissionDialogMessage, microphonePermissionDialogTitle, microphonePermissionDialogMessage) => {
    if (Platform.OS === 'ios') {
        let check = hasVideoAndAudio
            ? CameraManager.checkDeviceAuthorizationStatus
            : CameraManager.checkVideoAuthorizationStatus;

        if (check) {
            const isAuthorized = await check();
            return isAuthorized;
        }
    } else if (Platform.OS === 'android') {
        const cameraPermissionDialog = permissionDialogTitle || permissionDialogMessage ? {
          title: permissionDialogTitle,
          message: permissionDialogMessage,
        } : undefined;

        const grantedCamera = await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.CAMERA, cameraPermissionDialog);
        if (!hasVideoAndAudio) {
            return grantedCamera === PermissionsAndroid.RESULTS.GRANTED || grantedCamera === true;
        }

        const microphonePermissionDialog = microphonePermissionDialogTitle || microphonePermissionDialogMessage ? {
          title: microphonePermissionDialogTitle,
          message: microphonePermissionDialogMessage,
        } : undefined;

        const grantedAudio = await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.RECORD_AUDIO, microphonePermissionDialog);

        return (grantedCamera === PermissionsAndroid.RESULTS.GRANTED || grantedCamera === true)
            && (grantedAudio === PermissionsAndroid.RESULTS.GRANTED || grantedAudio === true);
    }
    return true;
}
