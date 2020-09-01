#include "pch.h"

#include "NativeModules.h"

#include "ReactCameraConstants.h"
#include "ReactCameraView.h"
#include "ReactCameraViewManager.h"

namespace winrt {
using namespace Windows::UI::Xaml;
using namespace Windows::UI::Xaml::Media;
using namespace Windows::UI::Xaml::Controls;
using namespace Windows::Devices::Enumeration;
using namespace Windows::Foundation;
using namespace Windows::Foundation::Collections;
using namespace Windows::Media::MediaProperties;
using namespace Windows::Storage;
using namespace Windows::Storage::Streams;
using namespace Windows::UI::Core;
using namespace Windows::Media::Core;
using namespace Windows::Media::Playback;
using namespace Windows::Media::Capture;
using namespace Windows::Graphics::Imaging;
using namespace Windows::Media::Devices;
using namespace Windows::System::Display;
using namespace Microsoft::ReactNative;
} // namespace winrt

using namespace std::chrono;

namespace winrt::ReactNativeCameraCPP {

/*static*/ winrt::com_ptr<ReactCameraView> ReactCameraView::Create() {
  auto view = winrt::make_self<ReactCameraView>();
  view->Initialize();
  return view;
}

ReactCameraView::~ReactCameraView() {
  m_unloadedEventToken.revoke();
}

void ReactCameraView::Initialize() {
  m_childElement = winrt::CaptureElement();

  Children().Append(m_childElement);

  // RNW does not support DropView yet, so we need to manually register to Unloaded event and remove self
  // from the static view list
  m_unloadedEventToken = Unloaded(winrt::auto_revoke, [ref = get_weak()](auto const &, auto const &) {
    if (auto self = ref.get()) {
      auto unloadedAction{self->OnUnloaded()};
      unloadedAction.Completed([self](auto && /*sender*/, AsyncStatus const /* args */) {
        winrt::ReactNativeCameraCPP::implementation::ReactCameraViewManager::RemoveViewFromList(self);
      });
    }
  });
}

void ReactCameraView::UpdateProperties(IJSValueReader const &propertyMapReader) noexcept {
  const JSValueObject &propertyMap = JSValue::ReadObjectFrom(propertyMapReader);

  for (auto const &pair : propertyMap) {
    auto const &propertyName = pair.first;
    auto const &propertyValue = pair.second;
    if (!propertyValue.IsNull()) {
      if (propertyName == "flashMode") {
        UpdateFlashMode(propertyValue.AsInt32());
      } else if (propertyName == "autoFocus") {
        UpdateAutoFocus(propertyValue.AsInt32());
      } else if (propertyName == "whiteBalance") {
        UpdateWhiteBalance(propertyValue.AsInt32());
      } else if (propertyName == "type") {
        UpdateDeviceType(propertyValue.AsInt32());
      } else if (propertyName == "cameraId") {
        UpdateDeviceId(propertyValue.AsString());
      } else if (propertyName == "keepAwake") {
        UpdateKeepAwake(propertyValue.AsBoolean());
      } else if (propertyName == "mirrorVideo") {
        UpdateMirrorVideo(propertyValue.AsBoolean());
      } else if (propertyName == "aspect") {
        UpdateAspect(propertyValue.AsInt32());
      } else if (propertyName == "defaultVideoQuality") {
        UpdateDefaultVideoQuality(propertyValue.AsInt32());
      } else if (propertyName == "barCodeScannerEnabled") {
        UpdateBarcodeScannerEnabled(propertyValue.AsBoolean());
      } else if (propertyName == "barCodeTypes") {
        UpdateBarcodeTypes(propertyValue.AsArray());
      } else if (propertyName == "barCodeReadIntervalMS") {
        UpdateBarcodeReadIntervalMS(propertyValue.AsInt32());
      }
    }
  }
}

IAsyncAction ReactCameraView::UpdateFilePropertiesAsync(StorageFile storageFile, JSValueObject const &options) {
  auto props = co_await storageFile.Properties().GetImagePropertiesAsync();
  auto searchTitle = options.find("title");
  if (searchTitle != options.end()) {
    const auto &titleValue = options.at("title");
    if (titleValue.Type() == JSValueType::String) {
      auto titleString = titleValue.AsString();
      props.Title(winrt::to_hstring(titleString));
    } else {
      throw winrt::hresult_invalid_argument();
    }
    co_await props.SavePropertiesAsync();
  }
}

IAsyncAction ReactCameraView::TakePictureAsync(
    JSValueObject const &options,
    ReactPromise<JSValueObject> const &result) noexcept {
  auto capturedPromise = result;
  auto capturedOptions = options.Copy();

  if (!m_isInitialized) {
    capturedPromise.Reject(L"Media device is not initialized.");
    co_return;
  }

  StopBarcodeScanner();

  auto dispatcher = Dispatcher();
  co_await resume_foreground(dispatcher);

  if (auto mediaCapture = m_childElement.Source()) {
    // Default with no options is to save the image to the temp folder and return the uri
    // This follows the expectations of RNCamera without requiring extra app capabilities

    // Devs who want to only capture to memory (and get a base64 encoded image) can specify:
    // 1. doNotSave = true and base64 = true OR
    // 2. target = "memory"

    // Devs who want to capture to the Camera Roll can specify:
    // 1. target = "cameraRoll"
    // Devs who want to capture to the Pictures Library can specify:
    // 1. target = "disk"
    // Both will require the Pictures Library app capability to succeed

    bool doNotSave;
    TryGetValueAsBool(capturedOptions, "doNotSave", doNotSave, false);

    int target;
    TryGetValueAsInt(
        capturedOptions,
        "target",
        target,
        doNotSave ? ReactCameraConstants::CameraCaptureTargetMemory : ReactCameraConstants::CameraCaptureTargetTemp);

    // Prepare encoder options
    auto encoderPropertySet = winrt::BitmapPropertySet();

    // JPEG image quality
    float quality;
    TryGetValueAsFloat(capturedOptions, "quality", quality, 1.0f);
    auto imageQualityValue = winrt::BitmapTypedValue(winrt::box_value(quality), PropertyType::Single);
    encoderPropertySet.Insert(hstring(L"ImageQuality"), imageQualityValue);

    // Capture the image and relevant EXIF metadata
    auto lowLagCapture = co_await mediaCapture.PrepareLowLagPhotoCaptureAsync(
        winrt::ImageEncodingProperties().CreateUncompressed(winrt::MediaPixelFormat::Bgra8));
    auto capturedPhoto = co_await lowLagCapture.CaptureAsync();

    auto softwareBitmap = capturedPhoto.Frame().SoftwareBitmap();
    auto capturedProperties = capturedPhoto.Frame().BitmapProperties();

    co_await lowLagCapture.FinishAsync();

    // writeExif allows callers to (not) save EXIF metadata
    // TODO: writeExif can also be passed an object to allow callers to specify additional EXIF
    // metadata, however we don't have an easy API for doing so at the moment
    bool writeExif;
    TryGetValueAsBool(capturedOptions, "writeExif", writeExif, true);

    // Add additional metadata to what the camera provided
    auto photoOrientation = m_rotationHelper.GetConvertedCameraCaptureOrientation();
    auto photoOrientationValue =
        winrt::BitmapTypedValue(winrt::box_value(photoOrientation), winrt::PropertyType::UInt16);
    capturedProperties.Insert(hstring(L"System.Photo.Orientation"), photoOrientationValue);

    // Get transform options

    auto targetWidth = softwareBitmap.PixelWidth();
    auto targetHeight = softwareBitmap.PixelHeight();

    auto bitmapTransform = winrt::BitmapTransform();

    int resizeWidth;
    if (TryGetValueAsInt(capturedOptions, "width", resizeWidth, 0) && resizeWidth > 0 &&
        resizeWidth != softwareBitmap.PixelWidth()) {
      targetWidth = resizeWidth;
      targetHeight = static_cast<int32_t>(
          (static_cast<double>(targetWidth) / static_cast<double>(softwareBitmap.PixelWidth())) *
          static_cast<double>(softwareBitmap.PixelHeight()));

      // Resize output
      bitmapTransform.ScaledWidth(targetWidth);
      bitmapTransform.ScaledHeight(targetHeight);
      bitmapTransform.InterpolationMode(BitmapInterpolationMode::Fant);
    }

    bool mirrorImage;
    if (TryGetValueAsBool(capturedOptions, "mirrorImage", mirrorImage, false) && mirrorImage) {
      bitmapTransform.Flip(winrt::BitmapFlip::Horizontal);
    }

    // Start creating result
    winrt::JSValueObject resultObject;
    resultObject["width"] = targetWidth;
    resultObject["height"] = targetHeight;

    // Return exif data in result
    bool exif;
    TryGetValueAsBool(capturedOptions, "exif", exif, false);

    if (exif) {
      resultObject["exif"] = GetExifObject(capturedProperties);
    }

    if (target == ReactCameraConstants::CameraCaptureTargetMemory) {
      // Get memory output stream
      auto outputStream = winrt::InMemoryRandomAccessStream();

      // Encode Jpeg to output stream
      auto encoder = co_await winrt::BitmapEncoder::CreateAsync(
          winrt::BitmapEncoder::JpegEncoderId(), outputStream, encoderPropertySet);
      encoder.SetSoftwareBitmap(softwareBitmap);

      // Copy transformation
      encoder.BitmapTransform().ScaledWidth(bitmapTransform.ScaledWidth());
      encoder.BitmapTransform().ScaledHeight(bitmapTransform.ScaledHeight());
      encoder.BitmapTransform().InterpolationMode(bitmapTransform.InterpolationMode());
      encoder.BitmapTransform().Flip(bitmapTransform.Flip());

      if (writeExif) {
        co_await encoder.BitmapProperties().SetPropertiesAsync(capturedProperties);
      }

      co_await encoder.FlushAsync();

      // Get base64-encoded data to return
      auto base64String = co_await GetBase64DataAsync(outputStream);

      // Resolve promise with base64 encoded image
      resultObject["base64"] = winrt::to_string(base64String);
      capturedPromise.Resolve(resultObject);
    } else {
      try {
        auto storageFile = co_await GetOutputStorageFileAsync(ReactCameraConstants::MediaTypeJPG, target);

        if (storageFile) {
          // Get file output stream
          auto outputStream = co_await storageFile.OpenAsync(FileAccessMode::ReadWrite);

          // Encode Jpeg to output stream
          auto encoder = co_await winrt::BitmapEncoder::CreateAsync(
              winrt::BitmapEncoder::JpegEncoderId(), outputStream, encoderPropertySet);
          encoder.SetSoftwareBitmap(softwareBitmap);

          // Copy transformation
          encoder.BitmapTransform().ScaledWidth(bitmapTransform.ScaledWidth());
          encoder.BitmapTransform().ScaledHeight(bitmapTransform.ScaledHeight());
          encoder.BitmapTransform().InterpolationMode(bitmapTransform.InterpolationMode());
          encoder.BitmapTransform().Flip(bitmapTransform.Flip());

          if (writeExif) {
            co_await encoder.BitmapProperties().SetPropertiesAsync(capturedProperties);
          }

          co_await encoder.FlushAsync();

          co_await UpdateFilePropertiesAsync(storageFile, capturedOptions);

          // Resolve promise with uri, and optionally the base64 encoded image
          resultObject["uri"] = winrt::to_string(storageFile.Path());

          bool includeBase64 = false;
          TryGetValueAsBool(capturedOptions, "base64", includeBase64, false);
          if (includeBase64) {
            auto base64String = co_await GetBase64DataAsync(outputStream);
            resultObject["base64"] = winrt::to_string(base64String);
          }

          capturedPromise.Resolve(resultObject);
        }
      } catch (...) {
        capturedPromise.Reject(L"Unable to capture to disk, check app capabilities.");
      }
    }
  } else {
    capturedPromise.Reject(L"Media device is not initialized.");
  }

  co_await resume_background();

  StartBarcodeScanner();
}

IAsyncAction ReactCameraView::RecordAsync(
    JSValueObject const &options,
    ReactPromise<JSValueObject> const &result) noexcept {
  auto capturedPromise = result;
  auto capturedOptions = options.Copy();

  if (!m_isInitialized) {
    capturedPromise.Reject(L"Media device is not initialized.");
    co_return;
  }

  StopBarcodeScanner();

  auto dispatcher = Dispatcher();
  co_await resume_foreground(dispatcher);

  if (auto mediaCapture = m_childElement.Source()) {
    int quality;
    TryGetValueAsInt(capturedOptions, "quality", quality, m_defaultVideoQuality);

    // Update the stream with the requested quality
    co_await UpdateMediaStreamPropertiesAsync(quality);

    // Create an encoding profile for the requested codec
    int mediaType;
    winrt::MediaEncodingProfile encodingProfile = nullptr;

    int videoCodec;
    TryGetValueAsInt(capturedOptions, "codec", videoCodec, ReactCameraConstants::CameraVideoCodecH264);

    switch (videoCodec) {
      case ReactCameraConstants::CameraVideoCodecHEVC:
        encodingProfile = winrt::MediaEncodingProfile::CreateHevc(static_cast<VideoEncodingQuality>(quality));
        mediaType = ReactCameraConstants::MediaTypeMP4;
        break;
      case ReactCameraConstants::CameraVideoCodecWMV:
        encodingProfile = winrt::MediaEncodingProfile::CreateWmv(static_cast<VideoEncodingQuality>(quality));
        mediaType = ReactCameraConstants::MediaTypeWMV;
        break;
      default:
        videoCodec = ReactCameraConstants::CameraVideoCodecH264;
        encodingProfile = winrt::MediaEncodingProfile::CreateMp4(static_cast<VideoEncodingQuality>(quality));
        mediaType = ReactCameraConstants::MediaTypeMP4;
        break;
    }

    int videoBitrate;
    if (TryGetValueAsInt(capturedOptions, "videoBitrate", videoBitrate, 0) && videoBitrate > 0) {
      encodingProfile.Video().Bitrate(videoBitrate);
    }

    bool muteAudio;
    TryGetValueAsBool(capturedOptions, "mute", muteAudio, false);
    mediaCapture.AudioDeviceController().Muted(muteAudio);

    float maxDurationInSeconds;
    TryGetValueAsFloat(capturedOptions, "maxDuration", maxDurationInSeconds, 0.0f);

    // Default with no options is to save the video to the temp folder and return the uri
    // This follows the expectations of RNCamera without requiring extra app capabilities

    // Devs who want to only capture to memory (and get a base64 encoded video) can specify:
    // 1. target = "memory"

    // Devs who want to capture to the Camera Roll can specify:
    // 1. target = "cameraRoll"
    // This will require the Pictures Library app capability to succeed

    // Devs who want to capture to the Videos Library can specify:
    // 1. target = "disk"
    // This will require the Videos Library app capability to succeed

    int target;
    TryGetValueAsInt(capturedOptions, "target", target, ReactCameraConstants::CameraCaptureTargetTemp);

    // Start creating result
    winrt::JSValueObject resultObject;
    resultObject["codec"] = videoCodec;

    m_isRecording = true;
    if (target == ReactCameraConstants::CameraCaptureTargetMemory) {
      auto randomStream = winrt::InMemoryRandomAccessStream();
      m_mediaRecording = co_await mediaCapture.PrepareLowLagRecordToStreamAsync(encodingProfile, randomStream);

      co_await m_mediaRecording.StartAsync();
      DelayStopRecording(maxDurationInSeconds);
      co_await WaitAndStopRecording();

      auto string = co_await GetBase64DataAsync(randomStream);
      resultObject["base64"] = winrt::to_string(string);
      capturedPromise.Resolve(resultObject);
    } else {
      try {
        auto storageFile = co_await GetOutputStorageFileAsync(mediaType, target);
        m_mediaRecording = co_await mediaCapture.PrepareLowLagRecordToStorageFileAsync(encodingProfile, storageFile);

        co_await m_mediaRecording.StartAsync();
        DelayStopRecording(maxDurationInSeconds);
        co_await WaitAndStopRecording();

        co_await UpdateFilePropertiesAsync(storageFile, capturedOptions);

        resultObject["uri"] = winrt::to_string(storageFile.Path());
        capturedPromise.Resolve(resultObject);
      } catch (...) {
        capturedPromise.Reject(L"Unable to capture to disk, check app capabilities.");
      }
    }
    m_isRecording = false;

  } else {
    capturedPromise.Reject("No media capture device found");
  }

  co_await resume_background();

  StartBarcodeScanner();
}

IAsyncAction ReactCameraView::StopRecordAsync() noexcept {
  if (!m_isInitialized) {
    co_return;
  }

  SetEvent(m_signal.get());
}

IAsyncAction ReactCameraView::IsRecordingAsync(
    winrt::Microsoft::ReactNative::ReactPromise<bool> const &result) noexcept {
  auto capturedPromise = result;

  if (!m_isInitialized) {
    capturedPromise.Reject(L"Media device is not initialized.");
    co_return;
  }

  capturedPromise.Resolve(m_isRecording);
}

IAsyncAction ReactCameraView::PausePreviewAsync() noexcept {
  if (!m_isInitialized) {
    co_return;
  }

  auto dispatcher = Dispatcher();
  co_await resume_foreground(dispatcher);

  if (auto mediaCapture = m_childElement.Source()) {
    if (m_isPreview) {
      co_await mediaCapture.StopPreviewAsync();
      m_isPreview = false;
    }
  }

  co_await resume_background();
}

IAsyncAction ReactCameraView::ResumePreviewAsync() noexcept {
  if (!m_isInitialized) {
    co_return;
  }

  auto dispatcher = Dispatcher();
  co_await resume_foreground(dispatcher);

  if (auto mediaCapture = m_childElement.Source()) {
    if (!m_isPreview) {
      co_await mediaCapture.StartPreviewAsync();
      m_isPreview = true;
    }
  }

  co_await resume_background();
}

// start a timer to end the recording after the specified time
void ReactCameraView::DelayStopRecording(float totalRecordingInSecs) {
  ResetEvent(m_signal.get());
  auto totalRecordingInMs = static_cast<int32_t>(1000 * totalRecordingInSecs);

  std::chrono::duration<int32_t, std::milli> secs(totalRecordingInMs <= 0 ? INT32_MAX : totalRecordingInMs);
  m_recordTimer = winrt::Windows::System::Threading::ThreadPoolTimer::CreateTimer(
      [this](const winrt::Windows::System::Threading::ThreadPoolTimer) noexcept { SetEvent(m_signal.get()); }, secs);
}

IAsyncAction ReactCameraView::WaitAndStopRecording() {
  co_await resume_on_signal(m_signal.get());

  if (m_recordTimer) {
    m_recordTimer.Cancel();
  }

  // Switch to UI thread to stop recording
  auto dispatcher = Dispatcher();
  co_await resume_foreground(dispatcher);

  co_await m_mediaRecording.StopAsync();

  // Reset stream to default
  co_await UpdateMediaStreamPropertiesAsync();

  co_await resume_background();
}

// Select a particular camera, need to clean up and reinitialize the mediaCapture object
fire_and_forget ReactCameraView::UpdateDeviceId(std::string cameraId) {
  if (m_cameraId == cameraId && m_isInitialized) {
    return;
  }

  m_cameraId = cameraId;
  if (m_isInitialized) {
    co_await CleanupMediaCaptureAsync();
  }
  co_await InitializeAsync();
}

// Switch between front and back cameras, need to clean up and reinitialize the mediaCapture object
fire_and_forget ReactCameraView::UpdateDeviceType(int type) {
  winrt::Windows::Devices::Enumeration::Panel newPanelType =
      static_cast<winrt::Windows::Devices::Enumeration::Panel>(type);
  if (m_panelType == newPanelType && m_isInitialized) {
    return;
  }

  m_panelType = newPanelType;
  if (m_isInitialized) {
    co_await CleanupMediaCaptureAsync();
  }
  co_await InitializeAsync();
}

// Request monitor to not turn off if keepAwake is true
void ReactCameraView::UpdateKeepAwake(bool keepAwake) {
  if (m_keepAwake != keepAwake) {
    m_keepAwake = keepAwake;
    if (m_keepAwake) {
      if (m_displayRequest == nullptr) {
        m_displayRequest = DisplayRequest();
        m_displayRequest.RequestActive();
      }
    } else {
      m_displayRequest.RequestRelease();
    }
  }
}

void ReactCameraView::UpdateFlashMode(int flashMode) {
  m_flashMode = flashMode;
  if (auto mediaCapture = m_childElement.Source()) {
    auto flashControl = mediaCapture.VideoDeviceController().FlashControl();
    if (flashControl.Supported()) {
      flashControl.Enabled(flashMode == ReactCameraConstants::CameraFlashModeOn);
      flashControl.Auto(flashMode == ReactCameraConstants::CameraFlashModeAuto);
    }
  }
}

void ReactCameraView::UpdateAutoFocus(int focusMode) {
  m_focusMode = focusMode;
  if (auto mediaCapture = m_childElement.Source()) {
    auto focusControl = mediaCapture.VideoDeviceController().FocusControl();
    if (focusControl.Supported()) {
      auto asyncOp = focusControl.SetPresetAsync(static_cast<winrt::FocusPreset>(focusMode));
    }
  }
}

void ReactCameraView::UpdateWhiteBalance(int whiteBalance) {
  m_whiteBalance = whiteBalance;
  if (auto mediaCapture = m_childElement.Source()) {
    auto whiteBalanceControl = mediaCapture.VideoDeviceController().WhiteBalanceControl();
    if (whiteBalanceControl.Supported()) {
      auto asyncOp = whiteBalanceControl.SetPresetAsync(static_cast<winrt::ColorTemperaturePreset>(whiteBalance));
    }
  }
}

void ReactCameraView::UpdateMirrorVideo(bool mirrorVideo) {
  m_mirrorVideo = mirrorVideo;
  m_childElement.FlowDirection(mirrorVideo ? winrt::FlowDirection::RightToLeft : winrt::FlowDirection::LeftToRight);
}

void ReactCameraView::UpdateAspect(int aspect) {
  switch (aspect) {
    case ReactCameraConstants::CameraAspectFill:
      m_childElement.Stretch(Stretch::Uniform);
      break;
    case ReactCameraConstants::CameraAspectFit:
      m_childElement.Stretch(Stretch::UniformToFill);
      break;
    case ReactCameraConstants::CameraAspectStretch:
      m_childElement.Stretch(Stretch::Fill);
      break;
    default:
      m_childElement.Stretch(Stretch::None);
      break;
  }
}

void ReactCameraView::UpdateDefaultVideoQuality(int videoQuality) {
  if (m_defaultVideoQuality != videoQuality) {
    m_defaultVideoQuality = videoQuality;
    auto asyncOp = UpdateMediaStreamPropertiesAsync();
  }
}

void ReactCameraView::UpdateBarcodeScannerEnabled(bool barcodeScannerEnabled) {
  m_barcodeScannerEnabled = barcodeScannerEnabled;
  if (m_barcodeScannerEnabled) {
    StartBarcodeScanner();
  } else {
    StopBarcodeScanner();
  }
}

void ReactCameraView::UpdateBarcodeTypes(winrt::JSValueArray const &barcodeTypes) {
  m_barcodeTypes.clear();

  for (size_t i = 0; i < barcodeTypes.size(); i++) {
    auto barCodeTypeStr = barcodeTypes[i].AsString();
    if (barCodeTypeStr == "AZTEC") {
      m_barcodeTypes.push_back(winrt::ZXing::BarcodeType::AZTEC);
    } else if (barCodeTypeStr == "CODABAR") {
      m_barcodeTypes.push_back(winrt::ZXing::BarcodeType::CODABAR);
    } else if (barCodeTypeStr == "CODE_39") {
      m_barcodeTypes.push_back(winrt::ZXing::BarcodeType::CODE_39);
    } else if (barCodeTypeStr == "CODE_93") {
      m_barcodeTypes.push_back(winrt::ZXing::BarcodeType::CODE_93);
    } else if (barCodeTypeStr == "CODE_128") {
      m_barcodeTypes.push_back(winrt::ZXing::BarcodeType::CODE_128);
    } else if (barCodeTypeStr == "DATA_MATRIX") {
      m_barcodeTypes.push_back(winrt::ZXing::BarcodeType::DATA_MATRIX);
    } else if (barCodeTypeStr == "EAN_8") {
      m_barcodeTypes.push_back(winrt::ZXing::BarcodeType::EAN_8);
    } else if (barCodeTypeStr == "EAN_13") {
      m_barcodeTypes.push_back(winrt::ZXing::BarcodeType::EAN_13);
    } else if (barCodeTypeStr == "ITF") {
      m_barcodeTypes.push_back(winrt::ZXing::BarcodeType::ITF);
    } else if (barCodeTypeStr == "MAXICODE") {
      m_barcodeTypes.push_back(winrt::ZXing::BarcodeType::MAXICODE);
    } else if (barCodeTypeStr == "PDF_417") {
      m_barcodeTypes.push_back(winrt::ZXing::BarcodeType::PDF_417);
    } else if (barCodeTypeStr == "QR_CODE") {
      m_barcodeTypes.push_back(winrt::ZXing::BarcodeType::QR_CODE);
    } else if (barCodeTypeStr == "RSS_14") {
      m_barcodeTypes.push_back(winrt::ZXing::BarcodeType::RSS_14);
    } else if (barCodeTypeStr == "RSS_EXPANDED") {
      m_barcodeTypes.push_back(winrt::ZXing::BarcodeType::RSS_EXPANDED);
    } else if (barCodeTypeStr == "UPC_A") {
      m_barcodeTypes.push_back(winrt::ZXing::BarcodeType::UPC_A);
    } else if (barCodeTypeStr == "UPC_E") {
      m_barcodeTypes.push_back(winrt::ZXing::BarcodeType::UPC_E);
    } else if (barCodeTypeStr == "UPC_EAN_EXTENSION") {
      m_barcodeTypes.push_back(winrt::ZXing::BarcodeType::UPC_EAN_EXTENSION);
    }
  }
}

void ReactCameraView::UpdateBarcodeReadIntervalMS(int barcodeReadIntervalMS) {
  m_barcodeReadIntervalMS = std::max<int>(ReactCameraConstants::BarcodeReadIntervalMinMS, barcodeReadIntervalMS);
}

// Intialization takes care few things below:
// 1. Register rotation helper to update preview if rotation changes.
// 2. Takes care connected standby scenarios to cleanup and reintialize when suspend/resume
IAsyncAction ReactCameraView::InitializeAsync() {
  try {
    auto device = co_await FindCameraDeviceAsync();
    if (device != nullptr) {
      auto settings = winrt::Windows::Media::Capture::MediaCaptureInitializationSettings();
      settings.VideoDeviceId(device.Id());

      auto mediaCapture = winrt::Windows::Media::Capture::MediaCapture();
      co_await mediaCapture.InitializeAsync(settings);

      m_availableVideoEncodingProperties =
          mediaCapture.VideoDeviceController().GetAvailableMediaStreamProperties(winrt::MediaStreamType::VideoPreview);

      m_childElement.Source(mediaCapture);

      co_await UpdateMediaStreamPropertiesAsync();

      UpdateFlashMode(m_flashMode);
      UpdateAutoFocus(m_focusMode);
      UpdateWhiteBalance(m_whiteBalance);
      UpdateKeepAwake(m_keepAwake);
      UpdateMirrorVideo(m_mirrorVideo);
      UpdateBarcodeScannerEnabled(m_barcodeScannerEnabled);

      co_await mediaCapture.StartPreviewAsync();
      m_isPreview = true;

      m_rotationHelper = CameraRotationHelper(device.EnclosureLocation());
      m_rotationEventToken =
          m_rotationHelper.OrientationChanged([ref = get_weak()](const auto &, const bool updatePreview) {
            if (auto self = ref.get()) {
              self->OnOrientationChanged(updatePreview);
            }
          });
      co_await UpdatePreviewOrientationAsync();

      m_applicationSuspendingEventToken =
          winrt::Application::Current().Suspending(winrt::auto_revoke, [ref = get_weak()](auto const &, auto const &) {
            if (auto self = ref.get()) {
              self->OnApplicationSuspending();
            }
          });

      m_applicationResumingEventToken =
          winrt::Application::Current().Resuming(winrt::auto_revoke, [ref = get_weak()](auto const &, auto const &) {
            if (auto self = ref.get()) {
              self->OnApplicationResuming();
            }
          });

      m_isInitialized = true;
    }
  } catch (winrt::hresult_error const &) {
    m_isInitialized = false;
  }
}

IAsyncAction ReactCameraView::UpdateMediaStreamPropertiesAsync() {
  co_await UpdateMediaStreamPropertiesAsync(m_defaultVideoQuality);
}

IAsyncAction ReactCameraView::UpdateMediaStreamPropertiesAsync(int videoQuality) {
  if (auto mediaCapture = m_childElement.Source()) {
    winrt::VideoEncodingProperties foundProperties = nullptr;
    uint32_t foundResolution = 0;
    uint32_t foundFrameRate = 0;

    winrt::VideoEncodingProperties bestProperties = nullptr;
    uint32_t bestResolution = 0;
    uint32_t bestFrameRate = 0;

    for (auto mediaEncodingProperties : m_availableVideoEncodingProperties) {
      if (auto videoEncodingProperties = mediaEncodingProperties.try_as<winrt::VideoEncodingProperties>()) {
        auto resolution = videoEncodingProperties.Width() * videoEncodingProperties.Height();
        auto frameRate = videoEncodingProperties.FrameRate().Denominator() > 0
            ? videoEncodingProperties.FrameRate().Numerator() / videoEncodingProperties.FrameRate().Denominator()
            : 0;

        // Save the best encoding for later, in case the target cannot be found
        if (bestProperties == nullptr || (resolution >= bestResolution && frameRate >= bestFrameRate)) {
          bestProperties = videoEncodingProperties;
          bestResolution = resolution;
          bestFrameRate = frameRate;
        }

        bool resolutionMatch = (videoQuality == ReactCameraConstants::CameraVideoQuality2160P &&
                                videoEncodingProperties.Width() == 3840 && videoEncodingProperties.Height() == 2160) ||
            (videoQuality == ReactCameraConstants::CameraVideoQuality1080P && videoEncodingProperties.Width() == 1920 &&
             videoEncodingProperties.Height() == 1080) ||
            (videoQuality == ReactCameraConstants::CameraVideoQuality720P && videoEncodingProperties.Width() == 1280 &&
             videoEncodingProperties.Height() == 720) ||
            (videoQuality == ReactCameraConstants::CameraVideoQualityWVGA && videoEncodingProperties.Width() == 800 &&
             videoEncodingProperties.Height() == 480) ||
            (videoQuality == ReactCameraConstants::CameraVideoQualityVGA && videoEncodingProperties.Width() == 640 &&
             videoEncodingProperties.Height() == 480);

        // Save this encoding if it:
        // 1. Has the correct resolution AND
        // 2. Has a better framerate
        if (resolutionMatch && frameRate >= foundFrameRate) {
          foundProperties = videoEncodingProperties;
          foundResolution = resolution;
          foundFrameRate = frameRate;
        }
      }
    }

    // Default to the best encoding if the target could not be found, or if auto was requested
    if (foundProperties == nullptr || videoQuality == ReactCameraConstants::CameraVideoQualityAuto) {
      foundProperties = bestProperties;
    }

    if (foundProperties) {
      co_await mediaCapture.VideoDeviceController().SetMediaStreamPropertiesAsync(
          winrt::MediaStreamType::VideoPreview, foundProperties);
    }
  }
  co_return;
}

IAsyncAction ReactCameraView::CleanupMediaCaptureAsync() {
  if (m_isInitialized) {
    SetEvent(m_signal.get()); // In case recording is still going on
    if (auto mediaCapture = m_childElement.Source()) {
      StopBarcodeScanner();

      if (m_isPreview) {
        co_await mediaCapture.StopPreviewAsync();
        m_isPreview = false;
      }

      if (m_rotationHelper != nullptr) {
        m_rotationHelper.OrientationChanged(m_rotationEventToken);
        m_rotationHelper = nullptr;
      }
      m_childElement.Source(nullptr);
    }
    m_isInitialized = false;
  }
}

IAsyncOperation<winrt::DeviceInformation> ReactCameraView::FindCameraDeviceAsync() {
  // Get available devices for capturing pictures
  auto allVideoDevices = co_await winrt::DeviceInformation::FindAllAsync(winrt::DeviceClass::VideoCapture);

  winrt::DeviceInformation targetDevice = nullptr;

  // Id specified, search by Id
  if (m_cameraId != nullptr && m_cameraId != "") {
    for (auto cameraDeviceInfo : allVideoDevices) {
      if (cameraDeviceInfo.IsEnabled() && m_cameraId == winrt::to_string(cameraDeviceInfo.Id())) {
        // Exact id match
        targetDevice = cameraDeviceInfo;
        break;
      }
    }
  }

  // Target not found by id, search by type
  if (targetDevice == nullptr) {
    for (auto cameraDeviceInfo : allVideoDevices) {
      if (cameraDeviceInfo.IsEnabled()) {
        if (cameraDeviceInfo.EnclosureLocation() != nullptr &&
            cameraDeviceInfo.EnclosureLocation().Panel() == m_panelType) {
          // Device matches the panel requested (front/back/etc), take it
          targetDevice = cameraDeviceInfo;
          break;
        } else if (
            cameraDeviceInfo.EnclosureLocation() == nullptr ||
            m_panelType == winrt::Windows::Devices::Enumeration::Panel::Unknown) {
          // Device has no panel info, save it but keep looking
          targetDevice = cameraDeviceInfo;
        }
      }
    }
  }

  // Target not found by id or by type, take the first enabled camera
  if (targetDevice == nullptr) {
    for (auto cameraDeviceInfo : allVideoDevices) {
      if (cameraDeviceInfo.IsEnabled()) {
        targetDevice = cameraDeviceInfo;
        break;
      }
    }
  }

  // Return whichever device we've found
  co_return targetDevice;
}

void ReactCameraView::StartBarcodeScanner() {
  if (m_barcodeScannerEnabled && !m_barcodeScanTimer) {
    m_barcodeScanTimer = winrt::Windows::System::Threading::ThreadPoolTimer::CreatePeriodicTimer(
        [ref = this->get_strong()](const winrt::Windows::System::Threading::ThreadPoolTimer) noexcept {
          auto asyncOp = ref->ScanForBarcodeAsync();
          asyncOp.wait_for(std::chrono::milliseconds(ReactCameraConstants::BarcodeReadTimeoutMS));
        },
        std::chrono::milliseconds(m_barcodeReadIntervalMS));
  }
}

void ReactCameraView::StopBarcodeScanner() {
  if (m_barcodeScanTimer) {
    m_barcodeScanTimer.Cancel();
    m_barcodeScanTimer = nullptr;
  }
}

winrt::Windows::Foundation::IAsyncAction ReactCameraView::ScanForBarcodeAsync() {
  if (!m_isInitialized || !m_barcodeScannerEnabled || !m_isPreview) {
    co_return;
  }

  StopBarcodeScanner();

  auto dispatcher = Dispatcher();
  co_await resume_foreground(dispatcher);

  try {
    if (auto mediaCapture = m_childElement.Source()) {
      // Capture the image
      auto lowLagCapture = co_await mediaCapture.PrepareLowLagPhotoCaptureAsync(
          winrt::ImageEncodingProperties().CreateUncompressed(winrt::MediaPixelFormat::Bgra8));
      auto capturedPhoto = co_await lowLagCapture.CaptureAsync();

      auto softwareBitmap = capturedPhoto.Frame().SoftwareBitmap();

      co_await lowLagCapture.FinishAsync();

      if (softwareBitmap) {
        // Try to read barcode
        winrt::array_view<winrt::ZXing::BarcodeType const> barcodeTypes{m_barcodeTypes};
        auto barcodeReader = barcodeTypes.size() > 0 ? winrt::ZXing::BarcodeReader(true, true, barcodeTypes)
                                                     : winrt::ZXing::BarcodeReader(true, true);
        auto barcodeResult = barcodeReader.Read(softwareBitmap, 0, 0);

        auto control = this->get_strong().try_as<winrt::FrameworkElement>();

        if (barcodeResult && m_reactContext && control) {
          m_reactContext.DispatchEvent(
              control,
              BarcodeReadEvent,
              [barcodeResult](winrt::Microsoft::ReactNative::IJSValueWriter const &eventDataWriter) noexcept {
                auto result = winrt::JSValueObject();
                result["data"] = winrt::to_string(barcodeResult.Text());
                result["type"] = winrt::to_string(barcodeResult.Format());
                result.WriteTo(eventDataWriter);
              });
        }
      }
    }
  } catch (...) {
      // We can't do anything since this is running in it's own thread,
      // there's no way to report the exception, and we want the code to cleanup here
  }

  co_await resume_background();

  StartBarcodeScanner();
}

// update preview if display orientation changes.
void ReactCameraView::OnOrientationChanged(const bool updatePreview) {
  if (updatePreview) {
    UpdatePreviewOrientationAsync();
  }
}

void ReactCameraView::OnApplicationSuspending() {
  if (m_keepAwake) {
    m_displayRequest.RequestRelease();
  }
  CleanupMediaCaptureAsync();
}

IAsyncAction ReactCameraView::OnUnloaded() {
  co_await CleanupMediaCaptureAsync();
}

void ReactCameraView::OnApplicationResuming() {
  if (m_keepAwake) {
    m_displayRequest.RequestActive();
  }
  InitializeAsync();
}

// update preview considering current orientation
IAsyncAction ReactCameraView::UpdatePreviewOrientationAsync() {
  if (m_isInitialized) {
    if (auto mediaCapture = m_childElement.Source()) {
      const GUID RotationKey = {0xC380465D, 0x2271, 0x428C, {0x9B, 0x83, 0xEC, 0xEA, 0x3B, 0x4A, 0x85, 0xC1}};
      auto props = mediaCapture.VideoDeviceController().GetMediaStreamProperties(MediaStreamType::VideoPreview);
      props.Properties().Insert(RotationKey, winrt::box_value(m_rotationHelper.GetCameraPreviewClockwiseDegrees()));
      co_await mediaCapture.SetEncodingPropertiesAsync(MediaStreamType::VideoPreview, props, nullptr);
    }
  }
}

IAsyncOperation<winrt::hstring> ReactCameraView::GetBase64DataAsync(
    winrt::Windows::Storage::Streams::IRandomAccessStream stream) {
  auto streamSize = static_cast<uint32_t>(stream.Size());
  auto inputStream = stream.GetInputStreamAt(0);
  auto dataReader = winrt::Windows::Storage::Streams::DataReader(inputStream);
  co_await dataReader.LoadAsync(streamSize);
  auto buffer = dataReader.ReadBuffer(streamSize);
  co_return winrt::Windows::Security::Cryptography::CryptographicBuffer::EncodeToBase64String(buffer);
}

IAsyncOperation<winrt::StorageFile> ReactCameraView::GetOutputStorageFileAsync(int type, int target) {
  std::string ext;
  switch (type) {
    case ReactCameraConstants::MediaTypeJPG:
      ext = ".jpg";
      break;
    case ReactCameraConstants::MediaTypeMP4:
      ext = ".mp4";
      break;
    case ReactCameraConstants::MediaTypeWMV:
      ext = ".wmv";
      break;
  }

  auto now = winrt::clock::now();
  auto ttnow = winrt::clock::to_time_t(now);
  struct tm time;
  _localtime64_s(&time, &ttnow);
  wchar_t buf[35];
  swprintf_s(
      buf,
      ARRAYSIZE(buf),
      L"%04d%02d%02d_%02d%02d%02d",
      1900 + time.tm_year,
      1 + time.tm_mon,
      time.tm_mday,
      time.tm_hour,
      time.tm_min,
      time.tm_sec);
  auto filename = winrt::to_hstring(buf) + winrt::to_hstring(ext);

  switch (target) {
    case ReactCameraConstants::CameraCaptureTargetMemory:
    case ReactCameraConstants::CameraCaptureTargetTemp:
      return winrt::ApplicationData::Current().TemporaryFolder().CreateFileAsync(filename);
    case ReactCameraConstants::CameraCaptureTargetCameraRoll:
      return winrt::KnownFolders::CameraRoll().CreateFileAsync(filename);
    case ReactCameraConstants::CameraCaptureTargetDisk:
      if (type == ReactCameraConstants::MediaTypeJPG) {
        auto result = winrt::KnownFolders::PicturesLibrary().CreateFileAsync(filename);
        return result;
      } else {
        return winrt::KnownFolders::VideosLibrary().CreateFileAsync(filename);
      }
  }

  return nullptr;
}

void ReactCameraView::SetContext(winrt::Microsoft::ReactNative::IReactContext const &reactContext) {
  m_reactContext = reactContext;
}

winrt::JSValueObject ReactCameraView::GetExifObject(winrt::BitmapPropertySet const &properties) noexcept {
  winrt::JSValueObject exifObject;

  for (auto it : properties) {
    auto key = winrt::to_string(it.Key());
    auto value = it.Value();

    switch (value.Type()) {
      case PropertyType::Boolean:
        exifObject[key] = JSValue(unbox_value<bool>(value.Value()));
        break;
      case PropertyType::Single:
        exifObject[key] = JSValue(unbox_value<float_t>(value.Value()));
        break;
      case PropertyType::Double:
        exifObject[key] = JSValue(unbox_value<double_t>(value.Value()));
        break;
      case PropertyType::Int16:
        exifObject[key] = JSValue(unbox_value<int16_t>(value.Value()));
        break;
      case PropertyType::Int32:
        exifObject[key] = JSValue(unbox_value<int32_t>(value.Value()));
        break;
      case PropertyType::Int64:
        exifObject[key] = JSValue(unbox_value<int64_t>(value.Value()));
        break;
      case PropertyType::UInt16:
        exifObject[key] = JSValue(unbox_value<uint16_t>(value.Value()));
        break;
      case PropertyType::UInt32:
        exifObject[key] = JSValue(unbox_value<uint32_t>(value.Value()));
        break;
      case PropertyType::UInt64:
        exifObject[key] = JSValue(unbox_value<uint64_t>(value.Value()));
        break;
      case PropertyType::String:
        exifObject[key] = JSValue(winrt::to_string(unbox_value<winrt::hstring>(value.Value())));
        break;
    }
  }

  return exifObject;
}

bool ReactCameraView::TryGetValueAsInt(
    winrt::JSValueObject const &options,
    const std::string key,
    int &value,
    const int defaultValue) noexcept {
  auto search = options.find(key);

  bool found = search != options.end();

  value = found ? options[key].AsInt32() : defaultValue;

  return found;
}

bool ReactCameraView::TryGetValueAsBool(
    winrt::JSValueObject const &options,
    const std::string key,
    bool &value,
    const bool defaultValue) noexcept {
  auto search = options.find(key);

  bool found = search != options.end();

  value = found ? options[key].AsBoolean() : defaultValue;

  return found;
}

bool ReactCameraView::TryGetValueAsFloat(
    winrt::JSValueObject const &options,
    const std::string key,
    float &value,
    const float defaultValue) noexcept {
  auto search = options.find(key);

  bool found = search != options.end();

  value = found ? options[key].AsSingle() : defaultValue;

  return found;
}

} // namespace winrt::ReactNativeCameraCPP
