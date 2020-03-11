#pragma once
#include "pch.h"

namespace winrt::ReactNativeCameraCPP::implementation {

    struct ReactCameraViewManager
        : winrt::implements<
        ReactCameraViewManager,
        winrt::Microsoft::ReactNative::IViewManager,
        winrt::Microsoft::ReactNative::IViewManagerWithReactContext,
        winrt::Microsoft::ReactNative::IViewManagerWithNativeProperties> {
    public:
        ReactCameraViewManager();

        // IViewManager
        winrt::hstring Name() noexcept;
        winrt::Windows::UI::Xaml::FrameworkElement CreateView() noexcept;

        // IViewManagerWithReactContext
        winrt::Microsoft::ReactNative::IReactContext ReactContext() noexcept;
        void ReactContext(winrt::Microsoft::ReactNative::IReactContext reactContext) noexcept;

        // IViewManagerWithNativeProperties
        winrt::Windows::Foundation::Collections::
            IMapView<winrt::hstring, winrt::Microsoft::ReactNative::ViewManagerPropertyType>
            NativeProps() noexcept;

        void UpdateProperties(
            winrt::Windows::UI::Xaml::FrameworkElement const &view,
            winrt::Microsoft::ReactNative::IJSValueReader const &propertyMapReader) noexcept;

        static winrt::Windows::Foundation::IAsyncAction  TakePictureAsync(
            std::map<std::wstring, winrt::Microsoft::ReactNative::JSValue> const& options,
            int viewTag,
            winrt::Microsoft::ReactNative::ReactPromise<winrt::Microsoft::ReactNative::JSValueObject> &result);

        static winrt::Windows::Foundation::IAsyncAction  RecordAsync(
            std::map<std::wstring, winrt::Microsoft::ReactNative::JSValue> const& options,
            int viewTag,
            winrt::Microsoft::ReactNative::ReactPromise<winrt::Microsoft::ReactNative::JSValueObject> &result);

        static winrt::Windows::Foundation::IAsyncAction  CheckMediaCapturePermissionAsync(
            winrt::Microsoft::ReactNative::ReactPromise<bool>& result);

        static void RemoveViewFromList(winrt::com_ptr<ReactNativeCameraCPP::ReactCameraView>);

    private:

        static winrt::Windows::Foundation::IAsyncOperation<int> FindCamera(int viewTag);

        winrt::Microsoft::ReactNative::IReactContext m_reactContext{ nullptr };
        static std::vector<winrt::com_ptr<ReactNativeCameraCPP::ReactCameraView>> m_cameraViewInstances;
    };

} // namespace winrt::ReactNativeCameraCPP::implementation
