import { PermissionsAndroid, Platform } from 'react-native';

export const requestPermissions = async (hasVideoAndAudio, CameraManager, permissionDialogTitle, permissionDialogMessage) => {
    if (Platform.OS === 'ios') {
        let check = hasVideoAndAudio
            ? CameraManager.checkDeviceAuthorizationStatus
            : CameraManager.checkVideoAuthorizationStatus;

        if (check) {
            const isAuthorized = await check();
            return isAuthorized;
        }
    } else if (Platform.OS === 'android') {
        const grantedCamera = await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.CAMERA, {
            title: permissionDialogTitle,
            message: permissionDialogMessage,
          });
        if (!hasVideoAndAudio) {
            return grantedCamera === PermissionsAndroid.RESULTS.GRANTED || grantedCamera === true;
        }
        const grantedAudio = await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.RECORD_AUDIO, {
            title: permissionDialogTitle,
            message: permissionDialogMessage,
        });

        return (grantedCamera === PermissionsAndroid.RESULTS.GRANTED || grantedCamera === true)
            && (grantedAudio === PermissionsAndroid.RESULTS.GRANTED || grantedAudio === true);
    }
    return true;
}
