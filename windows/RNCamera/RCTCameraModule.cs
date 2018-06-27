using Newtonsoft.Json.Linq;
using ReactNative.Bridge;
using System;
using System.Collections.Generic;
using System.IO;
using System.Threading;
using System.Threading.Tasks;
using Windows.Devices.Enumeration;
using Windows.Devices.Sensors;
using Windows.Foundation;
using Windows.Graphics.Imaging;
using Windows.Media.Capture;
using Windows.Media.MediaProperties;
using Windows.Storage;
using Windows.Storage.FileProperties;
using Windows.Storage.Streams;
using ZXing;
using static System.FormattableString;

namespace RNCamera
{
    class RCTCameraModule : ReactContextNativeModuleBase, ILifecycleEventListener
    {
        public const int CameraAspectFill = 0;
        public const int CameraAspectFit = 1;
        public const int CameraAspectStretch = 2;
        public const int CameraCaptureModeStill = 0;
        public const int CameraCaptureModeVideo = 1;
        public const int CameraCaptureTargetMemory = 0;
        public const int CameraCaptureTargetDisk = 1;
        public const int CameraCaptureTargetCameraRoll = 2;
        public const int CameraCaptureTargetTemp = 3;
        public const int CameraOrientationAuto = int.MaxValue;
        public const int CameraOrientationPortrait = (int)SimpleOrientation.NotRotated;
        public const int CameraOrientationPortraitUpsideDown = (int)SimpleOrientation.Rotated180DegreesCounterclockwise;
        public const int CameraOrientationLandscapeLeft = (int)SimpleOrientation.Rotated90DegreesCounterclockwise;
        public const int CameraOrientationLandscapeRight = (int)SimpleOrientation.Rotated270DegreesCounterclockwise;
        public const int CameraTypeFront = (int)Panel.Front;
        public const int CameraTypeBack = (int)Panel.Back;
        public const int CameraFlashModeOff = 0;
        public const int CameraFlashModeOn = 1;
        public const int CameraFlashModeAuto = 2;
        public const int CameraTorchModeOff = 0;
        public const int CameraTorchModeOn = 1;
        public const int CameraTorchModeAuto = 2;
        public const int CameraCaptureQualityHigh = (int)VideoEncodingQuality.HD1080p;
        public const int CameraCaptureQualityLow = (int)VideoEncodingQuality.HD720p;
        public const int CameraCaptureQuality1080p = (int)VideoEncodingQuality.HD1080p;
        public const int CameraCaptureQuality720p = (int)VideoEncodingQuality.HD720p;
        public const int MediaTypeImage = 1;
        public const int MediaTypeVideo = 2;

        private static readonly Guid RotationKey = new Guid("C380465D-2271-428C-9B83-ECEA3B4A85C1");

        private Task _recordingTask;
        private CancellationTokenSource _recordingCancellation;

        public RCTCameraModule(ReactContext reactContext)
            : base(reactContext)
        {
        }

        public override string Name
        {
            get
            {
                return "CameraModule";
            }
        }

        public override IReadOnlyDictionary<string, object> Constants
        {
            get
            {
                return new Dictionary<string, object>
                {
                    { "Aspect", GetAspectConstants() },
                    { "BarCodeType", GetBarcodeConstants() },
                    { "Type", GetTypeConstants() },
                    { "CaptureQuality", GetCaptureQualityConstants() },
                    { "CaptureMode", GetCaptureModeConstants() },
                    { "CaptureTarget", GetCaptureTargetConstants() },
                    { "Orientation", GetOrientationConstants() },
                    { "FlashMode", GetFlashModeConstants() },
                    { "TorchMode", GetTorchModeConstants() },
                };
            }
        }

        public CameraForViewManager CameraManager { get; } = new CameraForViewManager();

        [ReactMethod]
        public async void capture(JObject options, IPromise promise)
        {
            var viewTag = options.Value<int>("view");
            var cameraForView = CameraManager.GetCameraForView(viewTag);
            if (cameraForView == null)
            {
                promise.Reject("No camera found.");
                return;
            }

            var mode = options.Value<int>("mode");
            if (mode == CameraCaptureModeVideo)
            {
                if (_recordingTask != null)
                {
                    promise.Reject("Cannot run more than one capture operation.");
                    return;
                }

                _recordingCancellation = new CancellationTokenSource();
                _recordingTask = RecordAsync(cameraForView, options, promise, _recordingCancellation.Token);
                return;
            }
            else
            {
                await CapturePhotoAsync(cameraForView, options, promise).ConfigureAwait(false);
            }
        }

        [ReactMethod]
        public async void stopCapture(IPromise promise)
        {
            var recordingTask = _recordingTask;
            if (recordingTask != null)
            {
                _recordingCancellation.Cancel();
                await recordingTask;
                promise.Resolve("Finished recording.");
            }
            else
            {
                promise.Resolve("Not recording.");
            }
        }

        [ReactMethod]
        public void hasFlash(JObject options, IPromise promise)
        {
            var viewTag = options.Value<int>("view");
            var mediaCapture = CameraManager.GetCameraForView(viewTag).MediaCapture;
            if (mediaCapture == null)
            {
                promise.Reject("No camera found.");
                return;
            }

            var isSupported = mediaCapture.VideoDeviceController.FlashControl.Supported;
            promise.Resolve(isSupported);
        }

        public void OnSuspend()
        {
            var recordingTask = _recordingTask;
            if (recordingTask != null)
            {
                _recordingCancellation.Cancel();
                recordingTask.Wait();
            }
        }

        public void OnResume()
        {
        }

        public void OnDestroy()
        {
        }

        private async Task CapturePhotoAsync(CameraForView cameraForView, JObject options, IPromise promise)
        {
            var mediaCapture = cameraForView.MediaCapture;
            var encoding = ImageEncodingProperties.CreateJpeg();
            var target = options.Value<int>("target");
            using (var stream = new InMemoryRandomAccessStream())
            {
                await mediaCapture.CapturePhotoToStreamAsync(encoding, stream).AsTask().ConfigureAwait(false);
                if (target == CameraCaptureTargetMemory)
                {
                    var data = await GetBase64DataAsync(stream).ConfigureAwait(false);
                    promise.Resolve(new JObject
                    {
                        { "data", data },
                    });
                }
                else
                {
                    var storageFile = await GetOutputStorageFileAsync(MediaTypeImage, target).AsTask().ConfigureAwait(false);
                    var orientation = await cameraForView.GetCameraCaptureOrientationAsync().ConfigureAwait(false);
                    var photoOrientation = CameraRotationHelper.ConvertSimpleOrientationToPhotoOrientation(orientation);
                    await ReencodeAndSavePhotoAsync(stream, storageFile, photoOrientation).ConfigureAwait(false);
;                   await UpdateImagePropertiesAsync(storageFile, options).ConfigureAwait(false);
                    promise.Resolve(new JObject
                    {
                        { "path", storageFile.Path },
                    });
                }
            }
        }

        private async Task RecordAsync(CameraForView cameraForView, JObject options, IPromise promise, CancellationToken token)
        {
            var mediaCapture = cameraForView.MediaCapture;
            var taskCompletionSource = new TaskCompletionSource<bool>();
            using (var cancellationTokenSource = new CancellationTokenSource())
            using (token.Register(cancellationTokenSource.Cancel))
            using (cancellationTokenSource.Token.Register(async () => await StopRecordingAsync(mediaCapture, taskCompletionSource)))
            {
                var quality = (VideoEncodingQuality)options.Value<int>("quality");
                var encodingProfile = MediaEncodingProfile.CreateMp4(quality);

                mediaCapture.AudioDeviceController.Muted = options.Value<bool>("audio");

                var orientation = await cameraForView.GetCameraCaptureOrientationAsync().ConfigureAwait(false);
                encodingProfile.Video.Properties.Add(RotationKey, CameraRotationHelper.ConvertSimpleOrientationToClockwiseDegrees(orientation));

                var stream = default(InMemoryRandomAccessStream);

                try
                {
                    var target = options.Value<int>("target");
                    var outputFile = default(StorageFile);
                    if (target == CameraCaptureTargetMemory)
                    {
                        stream = new InMemoryRandomAccessStream();
                        await mediaCapture.StartRecordToStreamAsync(encodingProfile, stream).AsTask().ConfigureAwait(false);
                    }
                    else
                    {
                        outputFile = await GetOutputStorageFileAsync(MediaTypeVideo, target).AsTask().ConfigureAwait(false);
                        await mediaCapture.StartRecordToStorageFileAsync(encodingProfile, outputFile).AsTask().ConfigureAwait(false);
                    }

                    if (options.ContainsKey("totalSeconds"))
                    {
                        var totalSeconds = options.Value<double>("totalSeconds");
                        if (totalSeconds > 0)
                        {
                            cancellationTokenSource.CancelAfter(TimeSpan.FromSeconds(totalSeconds));
                        }
                    }

                    await taskCompletionSource.Task.ConfigureAwait(false);

                    if (target == CameraCaptureTargetMemory)
                    {
                        var data = await GetBase64DataAsync(stream).ConfigureAwait(false);
                        promise.Resolve(new JObject
                        {
                            { "data", data },
                        });
                    }
                    else
                    {
                        await UpdateVideoPropertiesAsync(outputFile, options).ConfigureAwait(false);
                        promise.Resolve(new JObject
                        {
                            { "path", outputFile.Path },
                        });
                    }
                }
                finally
                {
                    stream?.Dispose();
                }
            }

            _recordingCancellation.Dispose();
            _recordingTask = null;
        }

        private async Task StopRecordingAsync(MediaCapture mediaCapture, TaskCompletionSource<bool> taskCompletionSource)
        {
            await mediaCapture.StopRecordAsync().AsTask().ConfigureAwait(false);
            taskCompletionSource.SetResult(true);
        }

        private static async Task<string> GetBase64DataAsync(IRandomAccessStream stream)
        {
            var size = (int)stream.Size;
            var data = new byte[size];
            using (var readableStream = stream.AsStreamForRead())
            {
                await readableStream.ReadAsync(data, 0, size).ConfigureAwait(false);
                return Convert.ToBase64String(data);
            }
        }

        private static async Task UpdateVideoPropertiesAsync(StorageFile storageFile, JObject options)
        {
            var props = await storageFile.Properties.GetVideoPropertiesAsync().AsTask().ConfigureAwait(false);
            if (options.ContainsKey("title"))
            {
                props.Title = options.Value<string>("title");
            }

            if (options.ContainsKey("latitude"))
            {
                await props.SavePropertiesAsync(GeolocationHelper.GetLatitudeProperties(options.Value<double>("latitude"))).AsTask().ConfigureAwait(false);
            }

            if (options.ContainsKey("longitude"))
            {
                await props.SavePropertiesAsync(GeolocationHelper.GetLongitudeProperties(options.Value<double>("longitude"))).AsTask().ConfigureAwait(false);
            }
        }

        private static async Task UpdateImagePropertiesAsync(StorageFile storageFile, JObject options)
        {
            var props = await storageFile.Properties.GetImagePropertiesAsync().AsTask().ConfigureAwait(false);
            if (options.ContainsKey("title"))
            {
                props.Title = options.Value<string>("title");
            }

            if (options.ContainsKey("latitude"))
            {
                await props.SavePropertiesAsync(GeolocationHelper.GetLatitudeProperties(options.Value<double>("latitude"))).AsTask().ConfigureAwait(false);
            }

            if (options.ContainsKey("longitude"))
            {
                await props.SavePropertiesAsync(GeolocationHelper.GetLongitudeProperties(options.Value<double>("longitude"))).AsTask().ConfigureAwait(false);
            }
        }

        private static IAsyncOperation<StorageFile> GetOutputStorageFileAsync(int type, int target)
        {
            var ext = type == MediaTypeImage ? ".jpg" : ".mp4";
            var filename = DateTimeOffset.Now.ToString("yyyyMMdd_HHmmss") + ext;
            switch (target)
            {
                case CameraCaptureTargetMemory:
                case CameraCaptureTargetTemp:
                    return ApplicationData.Current.TemporaryFolder.CreateFileAsync(filename);
                case CameraCaptureTargetCameraRoll:
                    return KnownFolders.CameraRoll.CreateFileAsync(filename);
                case CameraCaptureTargetDisk:
                    if (type == MediaTypeImage)
                    {
                        return KnownFolders.PicturesLibrary.CreateFileAsync(filename);
                    }
                    else
                    {
                        return KnownFolders.VideosLibrary.CreateFileAsync(filename);
                    }
                default:
                    throw new InvalidOperationException(
                        Invariant($"Unknown capture target '{target}'."));
            }
        }

        private static async Task ReencodeAndSavePhotoAsync(IRandomAccessStream stream, StorageFile file, PhotoOrientation photoOrientation)
        {
            using (var inputStream = stream)
            {
                var decoder = await BitmapDecoder.CreateAsync(inputStream).AsTask().ConfigureAwait(false);

                using (var outputStream = await file.OpenAsync(FileAccessMode.ReadWrite).AsTask().ConfigureAwait(false))
                {
                    var encoder = await BitmapEncoder.CreateForTranscodingAsync(outputStream, decoder).AsTask().ConfigureAwait(false);

                    var properties = new BitmapPropertySet { { "System.Photo.Orientation", new BitmapTypedValue(photoOrientation, PropertyType.UInt16) } };

                    await encoder.BitmapProperties.SetPropertiesAsync(properties).AsTask().ConfigureAwait(false);
                    await encoder.FlushAsync().AsTask().ConfigureAwait(false);
                }
            }
        }

        private static IReadOnlyDictionary<string, object> GetAspectConstants()
        {
            return new Dictionary<string, object>
            {
                { "stretch", CameraAspectStretch },
                { "fit", CameraAspectFit },
                { "fill", CameraAspectFill },
            };
        }

        private static IReadOnlyDictionary<string, object> GetBarcodeConstants()
        {
            // TODO: code39mod43, itf14, etc.
            return new Dictionary<string, object>
            {
                { BarcodeFormat.UPC_E.GetName(), BarcodeFormat.UPC_E },
                { BarcodeFormat.CODE_39.GetName(), BarcodeFormat.CODE_39 },
                { BarcodeFormat.EAN_13.GetName(), BarcodeFormat.EAN_13 },
                { BarcodeFormat.EAN_8.GetName(), BarcodeFormat.EAN_8 },
                { BarcodeFormat.CODE_93.GetName(), BarcodeFormat.CODE_93 },
                { BarcodeFormat.CODE_128.GetName(), BarcodeFormat.CODE_128 },
                { BarcodeFormat.PDF_417.GetName(), BarcodeFormat.PDF_417 },
                { BarcodeFormat.QR_CODE.GetName(), BarcodeFormat.QR_CODE },
                { BarcodeFormat.AZTEC.GetName(), BarcodeFormat.AZTEC },
                { BarcodeFormat.ITF.GetName(), BarcodeFormat.ITF },
                { BarcodeFormat.DATA_MATRIX.GetName(), BarcodeFormat.DATA_MATRIX },
            };
        }

        private static IReadOnlyDictionary<string, object> GetTypeConstants()
        {
            return new Dictionary<string, object>
            {
                { "front", CameraTypeFront },
                { "back", CameraTypeBack },
            };
        }

        private static IReadOnlyDictionary<string, object> GetCaptureQualityConstants()
        {
            return new Dictionary<string, object>
            {
                { "low", CameraCaptureQualityLow },
                { "high", CameraCaptureQualityHigh },
                { "720p", CameraCaptureQuality720p },
                { "1080p", CameraCaptureQuality1080p },
            };
        }

        private static IReadOnlyDictionary<string, object> GetCaptureModeConstants()
        {
            return new Dictionary<string, object>
            {
                { "still", CameraCaptureModeStill },
                { "video", CameraCaptureModeVideo },
            };
        }

        private static IReadOnlyDictionary<string, object> GetCaptureTargetConstants()
        {
            return new Dictionary<string, object>
            {
                { "memory", CameraCaptureTargetMemory },
                { "disk", CameraCaptureTargetDisk },
                { "cameraRoll", CameraCaptureTargetCameraRoll },
                { "temp", CameraCaptureTargetTemp },
            };
        }

        private static IReadOnlyDictionary<string, object> GetOrientationConstants()
        {
            return new Dictionary<string, object>
            {
                { "auto", CameraOrientationAuto },
                { "landscapeLeft", CameraOrientationLandscapeLeft },
                { "landscapeRight", CameraOrientationLandscapeRight },
                { "portrait", CameraOrientationPortrait },
                { "portraitUpsideDown", CameraOrientationPortraitUpsideDown },
            };
        }

        private static IReadOnlyDictionary<string, object> GetFlashModeConstants()
        {
            return new Dictionary<string, object>
            {
                { "off", CameraFlashModeOff },
                { "on", CameraFlashModeOn },
                { "auto", CameraOrientationAuto },
            };
        }

        private static IReadOnlyDictionary<string, object> GetTorchModeConstants()
        {
            return new Dictionary<string, object>
            {
                { "off", CameraTorchModeOff },
                { "on", CameraTorchModeOn },
                { "auto", CameraTorchModeAuto },
            };
        }
    }
}
