#pragma once

#include <functional>
#include <sstream>
#include <system_error>

#include "JSValue.h"
#include "NativeModules.h"

#include "ReactCameraConstants.h"
#include "ReactCameraViewManager.h"

using namespace winrt::Microsoft::ReactNative;

#ifdef RNW61
#define JSVALUEOBJECTPARAMETER
#else
#define JSVALUEOBJECTPARAMETER const &
#endif

namespace winrt {
using namespace Windows::UI::Xaml;
using namespace Windows::UI::Xaml::Media;
using namespace Windows::Media::MediaProperties;
using namespace Windows::UI::Xaml::Controls;
using namespace Windows::Foundation;
using namespace winrt::Windows::UI::Core;
using namespace winrt::Windows::Storage::Streams;
} // namespace winrt
namespace winrt::ReactNativeCameraCPP {
REACT_MODULE(RNCameraModule);
struct RNCameraModule {
  const std::string Name = "RNCameraModule";

#pragma region Constants

  REACT_CONSTANT_PROVIDER(ConstantProvider)
  void ConstantProvider(ReactConstantProvider &provider) noexcept {
    provider.Add(L"Aspect", ReactCameraConstants::GetAspectConstants());
    provider.Add(L"BarCodeType", ReactCameraConstants::GetBarcodeConstants());
    provider.Add(L"FaceDetection", ReactCameraConstants::GetFaceDetectionConstants());
    provider.Add(L"AutoFocus", ReactCameraConstants::GetAutoFocusConstants());
    provider.Add(L"WhiteBalance", ReactCameraConstants::GetWhiteBalanceConstants());
    provider.Add(L"Type", ReactCameraConstants::GetTypeConstants());
    provider.Add(L"VideoQuality", ReactCameraConstants::GetCaptureQualityConstants());
    provider.Add(L"VideoCodec", ReactCameraConstants::GetCaptureCodecConstants());
    provider.Add(L"CaptureTarget", ReactCameraConstants::GetCaptureTargetConstants());
    provider.Add(L"Orientation", ReactCameraConstants::GetOrientationConstants());
    provider.Add(L"FlashMode", ReactCameraConstants::GetFlashModeConstants());
  }

#pragma endregion

#pragma region Methods

  template <class TPromiseResult>
  winrt::AsyncActionCompletedHandler MakeAsyncActionCompletedHandler(
      winrt::Microsoft::ReactNative::ReactPromise<TPromiseResult> const &promise) {
    return [promise](winrt::IAsyncAction action, winrt::AsyncStatus status) {
      if (status == winrt::AsyncStatus::Error) {
        std::stringstream errorCode;
        errorCode << "0x" << std::hex << action.ErrorCode() << std::endl;

        auto error = winrt::Microsoft::ReactNative::ReactError();
        error.Message = "HRESULT " + errorCode.str() + ": " + std::system_category().message(action.ErrorCode());
        promise.Reject(error);
      }
    };
  }

  REACT_METHOD(record)
  void record(
      winrt::Microsoft::ReactNative::JSValueObject JSVALUEOBJECTPARAMETER options,
      int viewTag,
      winrt::Microsoft::ReactNative::ReactPromise<winrt::Microsoft::ReactNative::JSValueObject> const
          &result) noexcept {
    auto asyncOp =
        winrt::ReactNativeCameraCPP::implementation::ReactCameraViewManager::RecordAsync(options, viewTag, result);
    asyncOp.Completed(MakeAsyncActionCompletedHandler(result));
  }

  REACT_METHOD(stopRecording)
  void stopRecording(int viewTag) noexcept {
    auto asyncOp = winrt::ReactNativeCameraCPP::implementation::ReactCameraViewManager::StopRecordAsync(viewTag);
  }

  REACT_METHOD(isRecording)
  void isRecording(int viewTag, winrt::Microsoft::ReactNative::ReactPromise<bool> const &result) noexcept {
    auto asyncOp =
        winrt::ReactNativeCameraCPP::implementation::ReactCameraViewManager::IsRecordingAsync(viewTag, result);
    asyncOp.Completed(MakeAsyncActionCompletedHandler(result));
  }

  REACT_METHOD(pausePreview)
  void pausePreview(int viewTag) noexcept {
    auto asyncOp = winrt::ReactNativeCameraCPP::implementation::ReactCameraViewManager::PausePreviewAsync(viewTag);
  }

  REACT_METHOD(resumePreview)
  void resumePreview(int viewTag) noexcept {
    auto asyncOp = winrt::ReactNativeCameraCPP::implementation::ReactCameraViewManager::ResumePreviewAsync(viewTag);
  }

  REACT_METHOD(takePicture)
  void takePicture(
      winrt::Microsoft::ReactNative::JSValueObject JSVALUEOBJECTPARAMETER options,
      int viewTag,
      winrt::Microsoft::ReactNative::ReactPromise<winrt::Microsoft::ReactNative::JSValueObject> const
          &result) noexcept {
    auto asyncOp =
        winrt::ReactNativeCameraCPP::implementation::ReactCameraViewManager::TakePictureAsync(options, viewTag, result);
    asyncOp.Completed(MakeAsyncActionCompletedHandler(result));
  }

  REACT_METHOD(checkMediaCapturePermission)
  void checkMediaCapturePermission(winrt::Microsoft::ReactNative::ReactPromise<bool> const &result) noexcept {
    auto asyncOp =
        winrt::ReactNativeCameraCPP::implementation::ReactCameraViewManager::CheckMediaCapturePermissionAsync(result);
    asyncOp.Completed(MakeAsyncActionCompletedHandler(result));
  }

  REACT_METHOD(getCameraIds)
  void getCameraIds(
      winrt::Microsoft::ReactNative::ReactPromise<winrt::Microsoft::ReactNative::JSValueArray> const &result) noexcept {
    auto asyncOp = winrt::ReactNativeCameraCPP::implementation::ReactCameraViewManager::GetCameraIdsAsync(result);
    asyncOp.Completed(MakeAsyncActionCompletedHandler(result));
  }

#pragma endregion

 public:
  RNCameraModule() = default;

  ~RNCameraModule() = default;
};

} // namespace winrt::ReactNativeCameraCPP
