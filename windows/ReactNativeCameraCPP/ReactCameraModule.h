#pragma once

#include "pch.h"

#include <functional>

#include "NativeModules.h"
#include <winrt/Windows.Devices.Sensors.h>
#include <winrt/Windows.Devices.Enumeration.h>
#include <winrt/Windows.Media.Mediaproperties.h>
#include <winrt/Windows.UI.Core.h>
#include <winrt/Windows.Storage.Streams.h>

#include "JSValueTreeWriter.h"
#include "ReactCameraConstants.h"
#include "ReactCameraViewManager.h"

using namespace winrt::Microsoft::ReactNative;

namespace winrt {
    using namespace Windows::UI::Xaml;
    using namespace Windows::UI::Xaml::Media;
    using namespace Windows::Media::MediaProperties;
    using namespace Windows::UI::Xaml::Controls;
    using namespace Windows::Foundation;
    using namespace winrt::Windows::UI::Core;
    using namespace winrt::Windows::Storage::Streams;
} //namespace winrt
namespace winrt::ReactNativeCameraCPP {
    REACT_MODULE(RNCameraModule);
    struct RNCameraModule {
        const std::string Name = "RNCameraModule";

#pragma region Constants
        REACT_CONSTANT_PROVIDER(ConstantProvider)
            void ConstantProvider(ReactConstantProvider& provider) noexcept {
            provider.Add(L"Aspect", ReactCameraContants::GetAspectConstants());
            provider.Add(L"BarCodeType", ReactCameraContants::GetBarcodeConstants());
            provider.Add(L"AutoFocus", ReactCameraContants::GetAutoFocusConstants());
            provider.Add(L"WhiteBalance", ReactCameraContants::GetWhiteBalanceConstants());
            provider.Add(L"Type", ReactCameraContants::GetTypeConstants());
            provider.Add(L"VideoQuality", ReactCameraContants::GetCaptureQualityConstants());
            provider.Add(L"CaptureTarget", ReactCameraContants::GetCaptureTargetConstants());
            provider.Add(L"Orientation", ReactCameraContants::GetOrientationConstants());
            provider.Add(L"FlashMode", ReactCameraContants::GetFlashModeConstants());
            provider.Add(L"TorchMode", ReactCameraContants::GetTorchModeConstants());
        }

        REACT_METHOD(record)
            void record(
                std::map<std::wstring, winrt::Microsoft::ReactNative::JSValue>&& options,
                int viewTag,
                winrt::Microsoft::ReactNative::ReactPromise<JSValueObject>&& result) noexcept
        {
            try {
                winrt::ReactNativeCameraCPP::implementation::ReactCameraViewManager::RecordAsync(
                    options,
                    viewTag, result).get();  //block on IAsyncAction
            }
            catch (winrt::hresult_error const& ex)
            {
                result.Reject(ex.message().c_str());
            }
        }

        REACT_METHOD(takePicture)
            void takePicture(
                std::map<std::wstring, winrt::Microsoft::ReactNative::JSValue>&& options,
                int viewTag,
                winrt::Microsoft::ReactNative::ReactPromise<JSValueObject>&& result) noexcept
        {
            try {
                winrt::ReactNativeCameraCPP::implementation::ReactCameraViewManager::TakePictureAsync(
                    options,
                    viewTag, result).get();  //block on IAsyncAction
            }
            catch (winrt::hresult_error const& ex)
            {
                result.Reject(ex.message().c_str());
            }
        }

        REACT_METHOD(checkMediaCapturePermission)
            void checkMediaCapturePermission(winrt::Microsoft::ReactNative::ReactPromise<bool>&& result) noexcept
        {
            winrt::ReactNativeCameraCPP::implementation::ReactCameraViewManager::CheckMediaCapturePermissionAsync(result).get();  //block on IAsyncAction
        }

#pragma endregion

    public:
        RNCameraModule() = default;

        ~RNCameraModule() = default;

    };

} // namespace ReactNativeCameraCPP
