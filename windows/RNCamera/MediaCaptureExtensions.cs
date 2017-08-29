using Windows.Media.Capture;

namespace RNCamera
{
    static class MediaCaptureExtensions
    {
        public static void SetTorchMode(this MediaCapture mediaCapture, int torchMode)
        {
            var torchControl = mediaCapture.VideoDeviceController.TorchControl;
            if (torchControl.Supported)
            {
                torchControl.Enabled = torchMode == RCTCameraModule.CameraTorchModeOn;
            }
        }

        public static void SetFlashMode(this MediaCapture mediaCapture, int flashMode)
        {
            var flashControl = mediaCapture.VideoDeviceController.FlashControl;
            if (flashControl.Supported)
            {
                flashControl.Enabled = flashMode == RCTCameraModule.CameraFlashModeOn;
                flashControl.Auto = flashMode == RCTCameraModule.CameraFlashModeAuto;
            }
        }
    }
}
