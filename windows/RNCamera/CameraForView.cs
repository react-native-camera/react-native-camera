using ReactNative.Bridge;
using System;
using System.Linq;
using System.Reactive.Disposables;
using System.Reactive.Linq;
using System.Threading;
using System.Threading.Tasks;
using Windows.ApplicationModel.Core;
using Windows.Devices.Enumeration;
using Windows.Devices.Sensors;
using Windows.Graphics.Imaging;
using Windows.Media;
using Windows.Media.Capture;
using Windows.Media.MediaProperties;
using Windows.Storage.FileProperties;
using Windows.System.Display;
using Windows.UI.Core;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using ZXing;

namespace RNCamera
{
    class CameraForView : IAsyncDisposable, ILifecycleEventListener
    {
        private static readonly Guid RotationKey = new Guid("C380465D-2271-428C-9B83-ECEA3B4A85C1");

        private readonly object _initializationGate = new object();

        private Windows.Devices.Enumeration.Panel? _panel;
        private CameraRotationHelper _rotationHelper;

        private DisplayRequest _displayRequest;
        private bool _keepAwake;

        private bool _barcodeScanningEnabled;
        private SerialDisposable _barcodeScanningSubscription = new SerialDisposable();

        private int _flashMode;
        private int _torchMode;

        private bool _wasInitializedOnSuspend;
        private Task _initializationTask;

        public CameraForView(CaptureElement captureElement)
        {
            CaptureElement = captureElement;
        }

        public event Action<Result> BarcodeScanned;

        public CaptureElement CaptureElement { get; }

        public MediaCapture MediaCapture { get; set; }

        public bool IsInitialized { get; private set; }

        public BarcodeReader BarcodeReader { get; set; }

        public bool BarcodeScanningEnabled
        {
            get
            {
                return _barcodeScanningEnabled;
            }
            set
            {
                if (_barcodeScanningEnabled != value)
                {
                    var wasBarcodeScanningEnabled = _barcodeScanningEnabled;
                    _barcodeScanningEnabled = value;
                    if (_barcodeScanningEnabled && BarcodeReader == null)
                    {
                        BarcodeReader = new BarcodeReader();
                    }

                    if (_barcodeScanningEnabled && IsInitialized)
                    {
                        _barcodeScanningSubscription.Disposable =
                            GetBarcodeScanningObservable().Subscribe(result =>
                            {
                                BarcodeScanned?.Invoke(result);
                            });
                    }
                    else if (wasBarcodeScanningEnabled && IsInitialized)
                    {
                        _barcodeScanningSubscription.Disposable = Disposable.Empty;
                    }
                }
            }
        }

        public bool KeepAwake
        {
            get
            {
                return _keepAwake;
            }
            set
            {
                if (_keepAwake != value)
                {
                    _keepAwake = value;
                    if (_keepAwake)
                    {
                        if (_displayRequest == null)
                        {
                            _displayRequest = new DisplayRequest();
                        }

                        _displayRequest.RequestActive();
                    }
                    else if (_displayRequest != null)
                    {
                        _displayRequest.RequestRelease();
                    }
                }
            }
        }

        public int FlashMode
        {
            get
            {
                return _flashMode;
            }
            set
            {
                _flashMode = value;
                if (IsInitialized)
                {
                    MediaCapture.SetFlashMode(_flashMode);
                }
            }
        }

        public int TorchMode
        {
            get
            {
                return _torchMode;
            }
            set
            {
                _torchMode = value;
                if (IsInitialized)
                {
                    MediaCapture.SetTorchMode(_torchMode);
                }
            }
        }

        public int Orientation { get; set; }

        public async Task<SimpleOrientation> GetCameraCaptureOrientationAsync()
        {
            var result = new TaskCompletionSource<SimpleOrientation>();

            await CoreApplication.MainView.Dispatcher.RunAsync(CoreDispatcherPriority.Normal, () =>
            {
                var orientation = _rotationHelper.GetCameraCaptureOrientation();
                Task.Run(() => result.SetResult(orientation));
            }).AsTask().ConfigureAwait(false);

            return await result.Task.ConfigureAwait(false);
        }

        public Task InitializeAsync()
        {
            var initializationTask = default(Task);
            if (!IsInitialized && _initializationTask == null)
            {
                lock (_initializationGate)
                {
                    // If not initialized and no running initialization, start one
                    if (!IsInitialized && _initializationTask == null)
                    {
                        MediaCapture = new MediaCapture();
                        _initializationTask = InitializeMediaCaptureAsync();
                    }

                    // Set the current task or null if initialized
                    initializationTask = _initializationTask;
                }
            }

            return initializationTask ?? Task.CompletedTask;
        }

        public async Task UpdatePanelAsync(Windows.Devices.Enumeration.Panel panel)
        {
            if (panel == _panel)
            {
                return;
            }

            _panel = panel;
            if (IsInitialized)
            {
                await CleanupMediaCaptureAsync();
                await InitializeAsync().ConfigureAwait(false);
            }
        }

        public void OnSuspend()
        {
            if (KeepAwake)
            {
                _displayRequest.RequestRelease();
            }

            // Blocking to ensure cleanup before suspend
            _wasInitializedOnSuspend = IsInitialized;
            if (_wasInitializedOnSuspend)
            {
                CleanupMediaCaptureAsync().Wait();
            }
        }

        public async void OnResume()
        {
            if (KeepAwake)
            {
                _displayRequest.RequestActive();
            }

            if (_wasInitializedOnSuspend)
            {
                await InitializeAsync().ConfigureAwait(false);
            }
        }

        public void OnDestroy()
        {
            // Blocking to ensure cleanup before dispose
            DisposeAsync().Wait();
        }

        public async Task DisposeAsync()
        {
            if (IsInitialized)
            {
                await CleanupMediaCaptureAsync().ConfigureAwait(false);
            }

            if (KeepAwake)
            {
                _displayRequest.RequestRelease();
            }

            _barcodeScanningSubscription.Dispose();
        }

        private async Task CleanupMediaCaptureAsync()
        {
            // Wait for initialization to complete
            var initializationTask = default(Task);
            lock (_initializationGate)
            {
                initializationTask = _initializationTask;
            }

            // This blocking call should rarely occur
            if (initializationTask != null)
            {
                await initializationTask;
            }

            // Cancel current barcode scanning subscription
            _barcodeScanningSubscription.Disposable = Disposable.Empty;

            // Stop preview
            // TODO: uncomment when async dispose is supported
            // await MediaCapture.StopPreviewAsync().AsTask().ConfigureAwait(false);

            // Dispose media capture
            MediaCapture.Dispose();

            // Remove orientation subscription
            if (IsInitialized)
            {
                _rotationHelper.OrientationChanged -= OnOrientationChanged;
            }

            IsInitialized = false;

            // TODO: race condition on re-initializing MediaCapture?
            // E.g., set new panel while starting record
            MediaCapture = null;
        }

        private async Task InitializeMediaCaptureAsync()
        {
            // Do not use ConfigureAwait(false), subsequent calls must come from Dispatcher thread
            var devices = await DeviceInformation.FindAllAsync(DeviceClass.VideoCapture);
            var device = _panel.HasValue
                ? devices.FirstOrDefault(d => d.EnclosureLocation?.Panel == _panel)
                : devices.FirstOrDefault();

            // TODO: remove this hack, it defaults the camera to any camera if it cannot find one for a specific panel
            device = device ?? devices.FirstOrDefault();

            if (device == null)
            {
                throw new InvalidOperationException("Could not find camera device.");
            }

            _rotationHelper = new CameraRotationHelper(device.EnclosureLocation);
            _rotationHelper.OrientationChanged += OnOrientationChanged;

            // Initialize for panel
            var settings = new MediaCaptureInitializationSettings
            {
                VideoDeviceId = device.Id,
            };

            // Do not use ConfigureAwait(false), subsequent calls must come from Dispatcher thread
            await MediaCapture.InitializeAsync(settings);

            // Set flash modes
            MediaCapture.SetFlashMode(FlashMode);
            MediaCapture.SetTorchMode(TorchMode);

            // Set to capture element
            CaptureElement.Source = MediaCapture;
            // Mirror for front facing camera
            CaptureElement.FlowDirection = _panel == Windows.Devices.Enumeration.Panel.Front
                ? FlowDirection.RightToLeft
                : FlowDirection.LeftToRight;

            // Start preview
            // Do not `ConfigureAwait(false), orientation must be set on Dispatcher thread
            await MediaCapture.StartPreviewAsync();

            // Set preview rotation
            await UpdatePreviewOrientationAsync().ConfigureAwait(false);

            // Start barcode scanning
            if (BarcodeScanningEnabled)
            {
                _barcodeScanningSubscription.Disposable =
                    GetBarcodeScanningObservable().Subscribe(result =>
                    {
                        BarcodeScanned?.Invoke(result);
                    });
            }

            IsInitialized = true;

            lock (_initializationGate)
            {
                _initializationTask = null;
            }
        }

        private IObservable<Result> GetBarcodeScanningObservable()
        {
            return Observable.Create(
                new Func<IObserver<Result>, CancellationToken, Task>(DoBarcodeScanningAsync));
        }

        private async Task DoBarcodeScanningAsync(IObserver<Result> observer, CancellationToken token)
        {
            var previewProperties = (VideoEncodingProperties)MediaCapture.VideoDeviceController.GetMediaStreamProperties(MediaStreamType.VideoPreview);
            while (!token.IsCancellationRequested)
            {
                var videoFrame = new VideoFrame(BitmapPixelFormat.Bgra8, (int)previewProperties.Width, (int)previewProperties.Height);
                var previewFrame = await MediaCapture.GetPreviewFrameAsync(videoFrame).AsTask().ConfigureAwait(false);
                if (previewFrame.SoftwareBitmap != null)
                {
                    var result = BarcodeReader.Decode(previewFrame.SoftwareBitmap);
                    if (result != null)
                    {
                        observer.OnNext(result);
                    }
                }
            }
        }

        private async void OnOrientationChanged(object sender, bool updatePreview)
        {
            if (updatePreview)
            {
                await UpdatePreviewOrientationAsync().ConfigureAwait(false);
            }
        }

        private async Task UpdatePreviewOrientationAsync()
        {
            var rotation = _rotationHelper.GetCameraPreviewOrientation();
            var props = MediaCapture.VideoDeviceController.GetMediaStreamProperties(MediaStreamType.VideoPreview);
            props.Properties.Add(RotationKey, CameraRotationHelper.ConvertSimpleOrientationToClockwiseDegrees(rotation));
            await MediaCapture.SetEncodingPropertiesAsync(MediaStreamType.VideoPreview, props, null).AsTask().ConfigureAwait(false);
        }
    }
}
