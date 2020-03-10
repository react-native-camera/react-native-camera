#include "pch.h"
#include "ReactCameraView.h"
#include "ReactCameraViewManager.h"
#include "NativeModules.h"
#include "ReactCameraConstants.h"

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
} //namespace winrt

using namespace std::chrono;

namespace winrt::ReactNativeCameraCPP {

    /*static*/ winrt::com_ptr<ReactCameraView> ReactCameraView::Create() {
        auto view = winrt::make_self<ReactCameraView>();
        view->Initialize();
        return view;
    }

    ReactCameraView::~ReactCameraView()
    {
        m_unloadedEventToken.revoke();
    }

    void ReactCameraView::Initialize()
    {
        m_childElement = winrt::CaptureElement();
        Children().Append(m_childElement);
        // RNW does not support DropView yet, so we need to manually register to Unloaded event and remove self
        // from the static view list
        m_unloadedEventToken = Unloaded(winrt::auto_revoke, [ref = get_weak()](auto const&, auto const&) {
            if (auto self = ref.get()) {
                auto unloadedAction{ self->OnUnloaded() };
                unloadedAction.Completed([self](auto&& /*sender*/, AsyncStatus const /* args */) {
                    winrt::ReactNativeCameraCPP::implementation::ReactCameraViewManager::RemoveViewFromList(self);
                });
            }
        });
    }

    void ReactCameraView::UpdateProperties(IJSValueReader const& propertyMapReader)
    {
        const JSValueObject& propertyMap = JSValue::ReadObjectFrom(propertyMapReader);

        for (auto const& pair : propertyMap) {
            auto const& propertyName = pair.first;
            auto const& propertyValue = pair.second;
            if (!propertyValue.IsNull()) {
                if (propertyName == "torchMode") {
                    UpdateTorchMode(static_cast<int>(propertyValue.Double()));
                }
                if (propertyName == "flashMode") {
                    UpdateFlashMode(static_cast<int>(propertyValue.Double()));
                }
                else if (propertyName == "type") {
                    UpdateDeviceType(static_cast<int>(propertyValue.Double()));
                }
                else if (propertyName == "keepAwake") {
                    UpdateKeepAwake(propertyValue.Boolean());
                }
                else if (propertyName == "aspect") {
                    UpdateAspect(static_cast<int>(propertyValue.Double()));
                }
            }
        }
    }

    IAsyncAction ReactCameraView::UpdateFilePropertiesAsync(StorageFile storageFile, std::map<std::wstring, JSValue> const& options)
    {
        auto props = co_await storageFile.Properties().GetImagePropertiesAsync();
        auto searchTitle = options.find(L"title");
        if (searchTitle != options.end()) {
            const auto& titleValue = options.at(L"title");
            if (titleValue.Type() == JSValueType::String)
            {
                auto titleString = titleValue.String();
                props.Title(winrt::to_hstring(titleString));
            }
            else
            {
                throw winrt::hresult_invalid_argument();
            }
            co_await props.SavePropertiesAsync();
        }
    }
    
    // RNW has a bug where the numeric value is set as int in debug but double in release
    // ToDo: remove this function once bug https://github.com/microsoft/react-native-windows/issues/4225 is fixed.
    bool ReactCameraView::TryGetValueAsInt(std::map<std::wstring, JSValue> const& options, const std::wstring key, int &value)
    {
        bool found = false;
        auto search = options.find(key);
        if (search != options.end())
        {
            const auto& searchValue = options.at(key);
            const bool valueIsInt = (searchValue.Type() == JSValueType::Int64);
            const bool valueIsDouble = (searchValue.Type() == JSValueType::Double);
            if (valueIsInt || valueIsDouble)
            {
                found = true;
                if (valueIsInt)
                {
                    value = static_cast<int>(searchValue.Int64());
                }
                else
                {
                    value = static_cast<int>(searchValue.Double());
                }
            }
        }

        return found;
    }

    IAsyncAction ReactCameraView::TakePictureAsync(std::map<std::wstring, JSValue> const& options
        , ReactPromise<JSValueObject>& result)
    {
        if (!m_isInitialized)
        {
            result.Reject(L"Media device is not initialized.");
            return;
        }

        auto dispatcher = Dispatcher();
        co_await resume_foreground(dispatcher); // Jump to UI thread
        if (auto mediaCapture = m_childElement.Source())
        {
            auto encoding = winrt::ImageEncodingProperties().CreateJpeg();
            auto randomStream = winrt::InMemoryRandomAccessStream();
            co_await mediaCapture.CapturePhotoToStreamAsync(encoding, randomStream);
            int target;
            if (!TryGetValueAsInt(options, L"target", target))
            {
                result.Reject(L"target parameter not specified.");
                return;
            }
            if (target == ReactCameraContants::CameraCaptureTargetMemory)
            {
                // In memeory returns a base64 string for the captured image
                auto string = co_await GetBase64DataAsync(randomStream);
                JSValueObject jsObject;
                jsObject["data"] = winrt::to_string(string);
                result.Resolve(jsObject);
            }
            else
            {
                auto storageFile = co_await GetOutputStorageFileAsync(ReactCameraContants::MediaTypeImage, target);
                {
                    auto photoOrientation = m_rotationHelper.GetConvertedCameraCaptureOrientation();
                    auto decoder = co_await winrt::BitmapDecoder::CreateAsync(randomStream);
                    auto outputStream = co_await storageFile.OpenAsync(FileAccessMode::ReadWrite);
                    auto encoder = co_await winrt::BitmapEncoder::CreateForTranscodingAsync(outputStream, decoder);
                    auto bitmapTypedValue = winrt::BitmapTypedValue(winrt::box_value(photoOrientation), PropertyType::UInt16);
                    auto properties = winrt::BitmapPropertySet();
                    properties.Insert(hstring(L"System.Photo.Orientation"), bitmapTypedValue);
                    co_await encoder.BitmapProperties().SetPropertiesAsync(properties);
                    co_await encoder.FlushAsync();
                }

                co_await UpdateFilePropertiesAsync(storageFile, options);
                JSValueObject jsObject;
                jsObject["path"] = winrt::to_string(storageFile.Path());
                result.Resolve(jsObject);
            }
        }
        else
        {
            result.Reject(L"Media device is not initialized.");
        }

        co_await resume_background();
    }

    IAsyncAction ReactCameraView::RecordAsync(std::map<std::wstring, JSValue> const& options, ReactPromise<JSValueObject>& result)
    {
        if (!m_isInitialized)
        {
            result.Reject(L"Media device is not initialized.");
            return;
        }

        auto dispatcher = Dispatcher();
        co_await resume_foreground(dispatcher); // Jump to UI thread
        if (auto mediaCapture = m_childElement.Source())
        {
            int quality = static_cast<int>(VideoEncodingQuality::Auto);
            TryGetValueAsInt(options, L"quality", quality);
            auto encodingProfile = winrt::MediaEncodingProfile();
            auto encoding = encodingProfile.CreateMp4(static_cast<VideoEncodingQuality>(quality));

            auto searchAudio = options.find(L"audio");
            if (searchAudio != options.end())
            {
                const auto& audioValue = options.at(L"audio");
                mediaCapture.AudioDeviceController().Muted(static_cast<bool>(audioValue.Boolean()));
            }

            int totalSeconds = INT_MAX;
            TryGetValueAsInt(options, L"totalSeconds", totalSeconds);

            int target;
            if (!TryGetValueAsInt(options, L"target", target))
            {
                result.Reject(L"target parameter not specified.");
                return;
            }
            if (target == ReactCameraContants::CameraCaptureTargetMemory)
            {
                auto randomStream = winrt::InMemoryRandomAccessStream();
                m_mediaRecording = co_await mediaCapture.PrepareLowLagRecordToStreamAsync(
                    encoding, randomStream);
                co_await m_mediaRecording.StartAsync();
                co_await DelayStopRecording(totalSeconds);
                co_await WaitAndStopRecording();

                auto string = co_await GetBase64DataAsync(randomStream);
                JSValueObject jsObject;
                jsObject["data"] = winrt::to_string(string);
                result.Resolve(jsObject);
            }
            else
            {
                auto storageFile = co_await GetOutputStorageFileAsync(ReactCameraContants::MediaTypeVideo, target);
                m_mediaRecording = co_await mediaCapture.PrepareLowLagRecordToStorageFileAsync(
                    encoding, storageFile);
                co_await m_mediaRecording.StartAsync();
                co_await DelayStopRecording(totalSeconds);
                co_await WaitAndStopRecording();
                co_await UpdateFilePropertiesAsync(storageFile, options);

                JSValueObject jsObject;
                jsObject["path"] = winrt::to_string(storageFile.Path());
                result.Resolve(jsObject);
            }
        }
        else
        {
            result.Reject("No media capture device found");
        }
        co_await resume_background();
    }
    
    // async function to wait for specified seconds before signaling to stop recording
    IAsyncAction ReactCameraView::DelayStopRecording(int totalRecordingInSecs)
    {
        ResetEvent(m_signal.get());
        std::chrono::duration<int> secs(totalRecordingInSecs);
        co_await secs;
        SetEvent(m_signal.get());
    }

    IAsyncAction ReactCameraView::WaitAndStopRecording()
    {
        co_await resume_on_signal(m_signal.get());
        auto dispatcher = Dispatcher();
        co_await resume_foreground(dispatcher);
        co_await m_mediaRecording.StopAsync();
        co_await resume_background();
    }

    // Switch between front and back cameras, need to clean up and reinitialize the mediaCapture object
    fire_and_forget ReactCameraView::UpdateDeviceType(int type)
    {
        winrt::Windows::Devices::Enumeration::Panel newPanelType = static_cast<winrt::Windows::Devices::Enumeration::Panel>(type);
        if (m_panelType == newPanelType && m_isInitialized)
        {
            return;
        }

        m_panelType = newPanelType;
        if (m_isInitialized)
        {
            co_await CleanupMediaCaptureAsync();
        }
        co_await InitializeAsync();
    }

    // Request monitor to not turn off if keepAwake is true
    void ReactCameraView::UpdateKeepAwake(bool keepAwake)
    {
        if (m_keepAwake != keepAwake)
        {
            m_keepAwake = keepAwake;
            if (m_keepAwake)
            {
                if (m_displayRequest == nullptr)
                {
                    m_displayRequest = DisplayRequest();
                    m_displayRequest.RequestActive();
                }
            }
            else
            {
                m_displayRequest.RequestRelease();
            }
        }
    }

    void ReactCameraView::UpdateTorchMode(int torchMode)
    {
        m_torchMode = torchMode;
        if (auto mediaCapture = m_childElement.Source())
        {
            auto torchControl = mediaCapture.VideoDeviceController().TorchControl();
            if (torchControl.Supported())
            {
                torchControl.Enabled(torchMode == ReactCameraContants::CameraTorchModeOn);
            }
        }
    }

    void ReactCameraView::UpdateFlashMode(int flashMode)
    {
        m_flashMode = flashMode;
        if (auto mediaCapture = m_childElement.Source())
        {
            auto flashControl = mediaCapture.VideoDeviceController().FlashControl();
            if (flashControl.Supported())
            {
                flashControl.Enabled(flashMode == ReactCameraContants::CameraFlashModeOn);
                flashControl.Auto(flashMode == ReactCameraContants::CameraFlashModeAuto);
            }
        }
    }

    void ReactCameraView::UpdateAspect(int aspect)
    {
        switch (aspect)
        {
        case ReactCameraContants::CameraAspectFill:
            m_childElement.Stretch(Stretch::Uniform);
            break;
        case ReactCameraContants::CameraAspectFit:
            m_childElement.Stretch(Stretch::UniformToFill);
            break;
        case ReactCameraContants::CameraAspectStretch:
            m_childElement.Stretch(Stretch::Fill);
            break;
        default:
            m_childElement.Stretch(Stretch::None);
            break;
        }
    }

    // Intialization takes care few things below:
    // 1. Register rotation helper to update preview if rotation changes.
    // 2. Takes care connected standby scenarios to cleanup and reintialize when suspend/resume
    IAsyncAction ReactCameraView::InitializeAsync()
    {
        try {
            auto device = co_await FindCameraDeviceByPanelAsync();
            if (device != nullptr)
            {
                auto settings = winrt::Windows::Media::Capture::MediaCaptureInitializationSettings();
                settings.VideoDeviceId(device.Id());
                auto mediaCapture = winrt::Windows::Media::Capture::MediaCapture();
                co_await mediaCapture.InitializeAsync(settings);
                m_childElement.Source(mediaCapture);
                UpdateTorchMode(m_torchMode);
                UpdateFlashMode(m_flashMode);
                UpdateKeepAwake(m_keepAwake);
                co_await mediaCapture.StartPreviewAsync();
                m_rotationHelper = CameraRotationHelper(device.EnclosureLocation());
                m_rotationEventToken = m_rotationHelper.OrientationChanged([ref = get_weak()](const auto&, const bool updatePreview)
                {
                    if (auto self = ref.get()) {
                        self->OnOrientationChanged(updatePreview);
                    }
                });
                co_await UpdatePreviewOrientationAsync();

                m_applicationSuspendingEventToken = winrt::Application::Current().Suspending(winrt::auto_revoke, [ref = get_weak()](auto const&, auto const&) {
                    if (auto self = ref.get()) {
                        self->OnApplicationSuspending();
                    }
                });

                m_applicationResumingEventToken = winrt::Application::Current().Resuming(winrt::auto_revoke, [ref = get_weak()](auto const&, auto const&) {
                    if (auto self = ref.get()) {
                        self->OnApplicationResuming();
                    }
                });

                m_isInitialized = true;
            }
        }
        catch (winrt::hresult_error const&)
        {
            m_isInitialized = false;
        }
    }

    IAsyncAction ReactCameraView::CleanupMediaCaptureAsync()
    {
        if (m_isInitialized)
        {
            SetEvent(m_signal.get()); // In case recording is still going on
            if (auto mediaCapture = m_childElement.Source())
            {
                co_await mediaCapture.StopPreviewAsync();
                if (m_rotationHelper != nullptr)
                {
                    m_rotationHelper.OrientationChanged(m_rotationEventToken);
                    m_rotationHelper = nullptr;
                }
                m_childElement.Source(nullptr);
            }
            m_isInitialized = false;
        }
    }

    IAsyncOperation<winrt::DeviceInformation> ReactCameraView::FindCameraDeviceByPanelAsync()
    {
        // Get available devices for capturing pictures
        auto allVideoDevices = co_await winrt::DeviceInformation::FindAllAsync(winrt::DeviceClass::VideoCapture);
        for (auto cameraDeviceInfo : allVideoDevices)
        {
            if (cameraDeviceInfo.EnclosureLocation() != nullptr && cameraDeviceInfo.EnclosureLocation().Panel() == m_panelType)
            {
                co_return cameraDeviceInfo;
            }
        }
        // Nothing matched, just return the first
        if (allVideoDevices.Size() > 0)
        {
            co_return allVideoDevices.GetAt(0);
        }
        // We didn't find any devices, so return a null instance
        co_return nullptr;
    }
    // update preview if display orientation changes.
    void ReactCameraView::OnOrientationChanged(const bool updatePreview)
    {
        if (updatePreview)
        {
            UpdatePreviewOrientationAsync();
        }
    }

    void ReactCameraView::OnApplicationSuspending()
    {
        if (m_keepAwake)
        {
            m_displayRequest.RequestRelease();
        }
        CleanupMediaCaptureAsync();
    }

    IAsyncAction ReactCameraView::OnUnloaded()
    {
        co_await CleanupMediaCaptureAsync();
    }

    void ReactCameraView::OnApplicationResuming()
    {
        if (m_keepAwake)
        {
            m_displayRequest.RequestActive();
        }
        InitializeAsync();
    }

    // update preview considering current orientation
    IAsyncAction ReactCameraView::UpdatePreviewOrientationAsync()
    {
        if (m_isInitialized)
        {
            if (auto mediaCapture = m_childElement.Source())
            {
                const GUID RotationKey = { 0xC380465D, 0x2271, 0x428C, {0x9B, 0x83, 0xEC, 0xEA, 0x3B, 0x4A, 0x85, 0xC1} };
                auto props = mediaCapture.VideoDeviceController().GetMediaStreamProperties(MediaStreamType::VideoPreview);
                props.Properties().Insert(RotationKey, winrt::box_value(m_rotationHelper.GetCameraPreviewClockwiseDegrees()));
                co_await mediaCapture.SetEncodingPropertiesAsync(MediaStreamType::VideoPreview, props, nullptr);
            }
        }
    }

    IAsyncOperation<winrt::hstring> ReactCameraView::GetBase64DataAsync(winrt::Windows::Storage::Streams::IRandomAccessStream stream)
    {
        auto streamSize = static_cast<uint32_t>(stream.Size());
        auto inputStream = stream.GetInputStreamAt(0);
        auto dataReader = winrt::Windows::Storage::Streams::DataReader(inputStream);
        co_await dataReader.LoadAsync(streamSize);
        auto buffer = dataReader.ReadBuffer(streamSize);
        co_return winrt::Windows::Security::Cryptography::CryptographicBuffer::EncodeToBase64String(buffer);
    }

    IAsyncOperation<winrt::StorageFile> ReactCameraView::GetOutputStorageFileAsync(int type, int target)
    {
        auto ext = type == ReactCameraContants::MediaTypeImage ? ".jpg" : ".mp4";
        auto now = winrt::clock::now();
        auto ttnow = winrt::clock::to_time_t(now);
        struct tm time;
        _localtime64_s(&time, &ttnow);
        wchar_t buf[35];
        swprintf_s(buf, ARRAYSIZE(buf), L"%04d%02d%02d_%02d%02d%02d", 1900 + time.tm_year, 1 + time.tm_mon, time.tm_mday, time.tm_hour, time.tm_min, time.tm_sec);
        auto filename = winrt::to_hstring(buf) + winrt::to_hstring(ext);

        switch (target)
        {
        case ReactCameraContants::CameraCaptureTargetMemory:
        case ReactCameraContants::CameraCaptureTargetTemp:
            return winrt::ApplicationData::Current().TemporaryFolder().CreateFileAsync(filename);
        case ReactCameraContants::CameraCaptureTargetCameraRoll:
            return winrt::KnownFolders::CameraRoll().CreateFileAsync(filename);
        case ReactCameraContants::CameraCaptureTargetDisk:
            if (type == ReactCameraContants::MediaTypeImage)
            {
                return winrt::KnownFolders::PicturesLibrary().CreateFileAsync(filename);
            }
            else
            {
                return winrt::KnownFolders::VideosLibrary().CreateFileAsync(filename);
            }
        }
        return nullptr;
    }

    void ReactCameraView::SetContext(winrt::Microsoft::ReactNative::IReactContext const& reactContext)
    {
        m_reactContext = reactContext;
    }

} // namespace winrt::ReactNativeVideoCPP
