import { NativeModules, PermissionsAndroid, Platform } from 'react-native';

const request = async (permission, rationale, denyOnDismiss) => {
  const {
    PermissionsAndroid: { shouldShowRequestPermissionRationale, requestPermission },
    DialogManagerAndroid: { dismissed, buttonClicked, buttonNegative, showAlert },
  } = NativeModules;

  if (rationale) {
    const shouldShowRationale = await shouldShowRequestPermissionRationale(permission);

    if (shouldShowRationale) {
      return new Promise((resolve, reject) => {
        showAlert(
          rationale,
          () => reject(new Error('Error showing rationale')),
          (action, buttonKey) =>
            resolve(
              (action === dismissed && denyOnDismiss) ||
              (action === buttonClicked && buttonKey === buttonNegative)
                ? PermissionsAndroid.RESULTS.DENIED
                : requestPermission(permission),
            ),
        );
      });
    }
  }

  return requestPermission(permission);
};

export const requestPermissions = async (
  hasVideoAndAudio,
  CameraManager,
  permissionDialogTitle,
  permissionDialogMessage,
  permissionButtonPositive,
  permissionButtonNegative,
) => {
  if (Platform.OS === 'ios') {
    const check = hasVideoAndAudio
      ? CameraManager.checkDeviceAuthorizationStatus
      : CameraManager.checkVideoAuthorizationStatus;

    if (check) return await check();
  } else if (Platform.OS === 'android') {
    const rationale =
      permissionDialogTitle || permissionDialogMessage
        ? {
            title: permissionDialogTitle,
            message: permissionDialogMessage,
            buttonPositive: permissionButtonPositive,
            buttonNegative: permissionButtonNegative,
          }
        : undefined;
    const denyOnDismiss = !!permissionButtonPositive; // otherwise no possibility to accept rationale
    const granted = await request(PermissionsAndroid.PERMISSIONS.CAMERA, rationale, denyOnDismiss);
    if (!hasVideoAndAudio)
      return granted === PermissionsAndroid.RESULTS.GRANTED || granted === true;
    const grantedAudio = await request(
      PermissionsAndroid.PERMISSIONS.RECORD_AUDIO,
      rationale,
      denyOnDismiss,
    );
    return (
      (granted === PermissionsAndroid.RESULTS.GRANTED || granted === true) &&
      (grantedAudio === PermissionsAndroid.RESULTS.GRANTED || grantedAudio === true)
    );
  }

  return true;
};
