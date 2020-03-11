using Newtonsoft.Json.Linq;
using ReactNative.Bridge;
using ReactNative.Modules.Core;
using ReactNative.UIManager;
using ReactNative.UIManager.Annotations;
using System.Linq;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media;
using ZXing;

namespace RNCamera
{
    class RCTCameraViewManager : SimpleViewManager<CaptureElement>
    {
        private readonly ReactContext _reactContext;

        public RCTCameraViewManager(ReactContext reactContext)
        {
            _reactContext = reactContext;
        }

        public override string Name
        {
            get
            {
                return "RNCamera";
            }
        }

        public CameraForViewManager CameraManager
        {
            get
            {
                return _reactContext.GetNativeModule<RCTCameraModule>().CameraManager;
            }
        }

        [ReactProp("aspect")]
        public void SetAspect(CaptureElement view, int aspect)
        {
            switch (aspect)
            {
                case RCTCameraModule.CameraAspectFill:
                    view.Stretch = Stretch.Uniform;
                    break;
                case RCTCameraModule.CameraAspectFit:
                    view.Stretch = Stretch.UniformToFill;
                    break;
                case RCTCameraModule.CameraAspectStretch:
                    view.Stretch = Stretch.Fill;
                    break;
                default:
                    view.Stretch = Stretch.None;
                    break;
            }
        }

        [ReactProp("captureTarget")]
        public void SetCaptureTarget(CaptureElement view, int captureTarget)
        {
            // No reason to handle this props valeu here since it's passed to the `capture` method.
        }

        [ReactProp("type")]
        public async void SetType(CaptureElement view, int type)
        {
            var camera = CameraManager.GetOrCreateCameraForView(view);
            await camera.UpdatePanelAsync((Windows.Devices.Enumeration.Panel)type);
        }

        [ReactProp("torchMode")]
        public void SetTorchMode(CaptureElement view, int torchMode)
        {
            var camera = CameraManager.GetOrCreateCameraForView(view);
            camera.TorchMode = torchMode;
        }

        [ReactProp("flashMode")]
        public void SetFlashMode(CaptureElement view, int flashMode)
        {
            var camera = CameraManager.GetOrCreateCameraForView(view);
            camera.FlashMode = flashMode;
        }

        [ReactProp("orientation")]
        public void SetOrientation(CaptureElement view, int orientation)
        {
            var camera = CameraManager.GetOrCreateCameraForView(view);
            camera.Orientation = orientation;
        }

        [ReactProp("barcodeScannerEnabled")]
        public void SetBarcodeScannerEnabled(CaptureElement view, bool enabled)
        {
            var camera = CameraManager.GetOrCreateCameraForView(view);
            var wasEnabled = camera.BarcodeScanningEnabled;
            camera.BarcodeScanningEnabled = enabled;
            if (enabled)
            {
                camera.BarcodeScanned += OnBarcodeScanned;
            }
            else if (wasEnabled)
            {
                camera.BarcodeScanned -= OnBarcodeScanned;
            }
        }

        [ReactProp("barCodeTypes")]
        public void SetBarcodeTypes(CaptureElement view, JArray barcodeTypes)
        {
            var camera = CameraManager.GetOrCreateCameraForView(view);
            var barcodeReader = camera.BarcodeReader;
            if (barcodeReader == null)
            {
                barcodeReader = new BarcodeReader();
                camera.BarcodeReader = barcodeReader;
            }

            barcodeReader.Options.PossibleFormats =
                barcodeTypes.Select(t => (BarcodeFormat)t.Value<int>()).ToList();
        }

        [ReactProp("keepAwake")]
        public void SetKeepAwake(CaptureElement view, bool keepAwake)
        {
            var camera = CameraManager.GetOrCreateCameraForView(view);
            camera.KeepAwake = keepAwake;
        }

        public override async void OnDropViewInstance(ThemedReactContext reactContext, CaptureElement view)
        {
            var viewTag = view.GetTag();
            var camera = CameraManager.GetCameraForView(viewTag);

            if (camera.BarcodeScanningEnabled)
            {
                camera.BarcodeScanned -= OnBarcodeScanned;
            }

            await camera.DisposeAsync().ConfigureAwait(false);

            CameraManager.DropCameraForView(view);
        }

        protected override async void OnAfterUpdateTransaction(CaptureElement view)
        {
            var camera = CameraManager.GetCameraForView(view.GetTag());
            if (!camera.IsInitialized)
            {
                await camera.InitializeAsync().ConfigureAwait(false);
            }
        }

        protected override CaptureElement CreateViewInstance(ThemedReactContext reactContext)
        {
            return new CaptureElement();
        }

        private void OnBarcodeScanned(Result result)
        {
            var resultPoints = new JArray();
            foreach (var point in result.ResultPoints)
            {
                resultPoints.Add(new JObject
                {
                    { "x", point.X },
                    { "y", point.Y },
                });
            }

            var eventData = new JObject
            {
                { "bounds", resultPoints },
                { "data", result.Text },
                { "type", result.BarcodeFormat.GetName() },
            };

            _reactContext.GetJavaScriptModule<RCTDeviceEventEmitter>()
                .emit("CameraBarCodeReadWindows", eventData);
        }
    }
}
