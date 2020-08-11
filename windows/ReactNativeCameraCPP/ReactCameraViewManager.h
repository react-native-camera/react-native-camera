#pragma once

#include "NativeModules.h"

#include "ReactCameraView.h"

namespace winrt::ReactNativeCameraCPP::implementation {

struct ReactCameraViewManager : winrt::implements<
                                    ReactCameraViewManager,
                                    winrt::Microsoft::ReactNative::IViewManager,
                                    winrt::Microsoft::ReactNative::IViewManagerWithReactContext,
                                    winrt::Microsoft::ReactNative::IViewManagerWithNativeProperties,
                                    winrt::Microsoft::ReactNative::IViewManagerWithExportedEventTypeConstants> {
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

  // IViewManagerWithExportedEventTypeConstants
  winrt::Microsoft::ReactNative::ConstantProviderDelegate ExportedCustomBubblingEventTypeConstants() noexcept;

  winrt::Microsoft::ReactNative::ConstantProviderDelegate ExportedCustomDirectEventTypeConstants() noexcept;

  static winrt::Windows::Foundation::IAsyncAction TakePictureAsync(
      winrt::Microsoft::ReactNative::JSValueObject const &options,
      int viewTag,
      winrt::Microsoft::ReactNative::ReactPromise<winrt::Microsoft::ReactNative::JSValueObject> const &result) noexcept;

  static winrt::Windows::Foundation::IAsyncAction RecordAsync(
      winrt::Microsoft::ReactNative::JSValueObject const &options,
      int viewTag,
      winrt::Microsoft::ReactNative::ReactPromise<winrt::Microsoft::ReactNative::JSValueObject> const &result) noexcept;

  static winrt::Windows::Foundation::IAsyncAction StopRecordAsync(int viewTag) noexcept;

  static winrt::Windows::Foundation::IAsyncAction IsRecordingAsync(
      int viewTag,
      winrt::Microsoft::ReactNative::ReactPromise<bool> const &result) noexcept;

  static winrt::Windows::Foundation::IAsyncAction PausePreviewAsync(int viewTag) noexcept;

  static winrt::Windows::Foundation::IAsyncAction ResumePreviewAsync(int viewTag) noexcept;

  static winrt::Windows::Foundation::IAsyncAction CheckMediaCapturePermissionAsync(
      winrt::Microsoft::ReactNative::ReactPromise<bool> const &result) noexcept;

  static winrt::Windows::Foundation::IAsyncAction GetCameraIdsAsync(
      winrt::Microsoft::ReactNative::ReactPromise<winrt::Microsoft::ReactNative::JSValueArray> const &result) noexcept;

  static void RemoveViewFromList(winrt::com_ptr<ReactNativeCameraCPP::ReactCameraView>);

 private:
  static winrt::Windows::Foundation::IAsyncOperation<int> FindCamera(int viewTag) noexcept;

  winrt::Microsoft::ReactNative::IReactContext m_reactContext{nullptr};
  static std::vector<winrt::com_ptr<ReactNativeCameraCPP::ReactCameraView>> m_cameraViewInstances;
};

} // namespace winrt::ReactNativeCameraCPP::implementation
