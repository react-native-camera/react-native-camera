#pragma once
#include <pch.h>
#include "NativeModules.h"
#include "JSValueTreeWriter.h"
#include "CameraRotationHelper.h"
#include "ReactCameraConstants.h"

namespace winrt::ReactNativeCameraCPP {
    struct ReactCameraView : winrt::Windows::UI::Xaml::Controls::GridT <ReactCameraView> {
    public:
        ReactCameraView() = default;
        ~ReactCameraView();
        void SetContext(winrt::Microsoft::ReactNative::IReactContext const &reactContext);
        void Initialize();
        void UpdateProperties(winrt::Microsoft::ReactNative::IJSValueReader const &propertyMapReader);
        
        winrt::Windows::Foundation::IAsyncAction TakePictureAsync(std::map<std::wstring, winrt::Microsoft::ReactNative::JSValue> const& options,
            winrt::Microsoft::ReactNative::ReactPromise<winrt::Microsoft::ReactNative::JSValueObject> &result);
        winrt::Windows::Foundation::IAsyncAction RecordAsync(std::map<std::wstring, winrt::Microsoft::ReactNative::JSValue> const& options, winrt::Microsoft::ReactNative::ReactPromise<winrt::Microsoft::ReactNative::JSValueObject>& result);

    public:
        static winrt::com_ptr<ReactCameraView> Create();

    private:
        void UpdateKeepAwake(bool keepAwake);
        void UpdateTorchMode(int torchMode);
        void UpdateFlashMode(int flashMode);
        void UpdateAspect(int aspect);
        fire_and_forget UpdateDeviceType(int type);

        winrt::Windows::Foundation::IAsyncAction InitializeAsync();
        winrt::Windows::Foundation::IAsyncAction CleanupMediaCaptureAsync();
        winrt::Windows::Foundation::IAsyncOperation<winrt::Windows::Devices::Enumeration::DeviceInformation> FindCameraDeviceByPanelAsync();
        winrt::Windows::Foundation::IAsyncOperation<winrt::hstring> GetBase64DataAsync(winrt::Windows::Storage::Streams::IRandomAccessStream stream);
        winrt::Windows::Foundation::IAsyncOperation<winrt::Windows::Storage::StorageFile> GetOutputStorageFileAsync(int type, int target);
        winrt::Windows::Foundation::IAsyncAction DelayStopRecording(int totalRecordingInSecs);
        winrt::Windows::Foundation::IAsyncAction WaitAndStopRecording();
        winrt::Windows::Foundation::IAsyncAction UpdatePreviewOrientationAsync();
        winrt::Windows::Foundation::IAsyncAction UpdateFilePropertiesAsync(winrt::Windows::Storage::StorageFile storageFile,
            std::map<std::wstring, winrt::Microsoft::ReactNative::JSValue> const& options);

        void OnOrientationChanged(const bool updatePreview);
        void OnApplicationSuspending();
        void OnApplicationResuming();
        winrt::Windows::Foundation::IAsyncAction OnUnloaded();

        bool TryGetValueAsInt(std::map<std::wstring, winrt::Microsoft::ReactNative::JSValue> const& options, const std::wstring key, int& value);

        winrt::Microsoft::ReactNative::IReactContext m_reactContext{ nullptr };
        winrt::Windows::UI::Xaml::Controls::CaptureElement m_childElement;

        handle m_signal{ CreateEvent(nullptr, true, false, nullptr) };
        winrt::Windows::Media::Capture::LowLagMediaRecording m_mediaRecording{ nullptr};
        winrt::ReactNativeCameraCPP::CameraRotationHelper m_rotationHelper{nullptr};
        winrt::Windows::System::Display::DisplayRequest m_displayRequest{ nullptr };

        winrt::event_token m_rotationEventToken{};
        winrt::Windows::UI::Xaml::Application::Suspending_revoker m_applicationSuspendingEventToken;
        winrt::Windows::UI::Xaml::Application::Resuming_revoker m_applicationResumingEventToken;
        winrt::Windows::UI::Xaml::FrameworkElement::Unloaded_revoker m_unloadedEventToken;

        bool m_isInitialized{ false };
        bool m_keepAwake{ false };
        int m_torchMode{ ReactCameraContants::CameraTorchModeOff };
        int m_flashMode{ ReactCameraContants::CameraFlashModeOff };
        winrt::Windows::Devices::Enumeration::Panel m_panelType{ winrt::Windows::Devices::Enumeration::Panel::Unknown };
    };
} // namespace winrt::ReactNativeVideoCPP
