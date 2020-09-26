#include "pch.h"

#include "ReactCameraConstants.h"
#include "ReactCameraViewManager.h"

#include <iomanip>

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
using namespace Microsoft::ReactNative;
} // namespace winrt

namespace winrt::ReactNativeCameraCPP::implementation {
// static vector to contain all currently active camera view instances
// This is a temporary workaround to map the view tag to view instance, since
// Method call from ReactCameraModule constains view tag as parameter and we need
// to forward the call to CameraView instance.
std::vector<winrt::com_ptr<ReactNativeCameraCPP::ReactCameraView>> ReactCameraViewManager::m_cameraViewInstances;

ReactCameraViewManager::ReactCameraViewManager() {}

// IViewManager
hstring ReactCameraViewManager::Name() noexcept {
  return L"RNCamera";
}

FrameworkElement ReactCameraViewManager::CreateView() noexcept {
  auto const &view = ReactNativeCameraCPP::ReactCameraView::Create();
  view->SetContext(m_reactContext);
  m_cameraViewInstances.emplace_back(view);

  return view.as<winrt::Grid>();
}

// IViewManagerWithReactContext
IReactContext ReactCameraViewManager::ReactContext() noexcept {
  return m_reactContext;
}

void ReactCameraViewManager::ReactContext(IReactContext reactContext) noexcept {
  m_reactContext = reactContext;
}

// IViewManagerWithNativeProperties
IMapView<hstring, ViewManagerPropertyType> ReactCameraViewManager::NativeProps() noexcept {
  auto nativeProps = winrt::single_threaded_map<hstring, ViewManagerPropertyType>();

  nativeProps.Insert(L"aspect", ViewManagerPropertyType::Number);
  nativeProps.Insert(L"type", ViewManagerPropertyType::Number);
  nativeProps.Insert(L"cameraId", ViewManagerPropertyType::String);
  nativeProps.Insert(L"autoFocus", ViewManagerPropertyType::Boolean);
  nativeProps.Insert(L"whiteBalance", ViewManagerPropertyType::Number);
  nativeProps.Insert(L"torchMode", ViewManagerPropertyType::Number);
  nativeProps.Insert(L"flashMode", ViewManagerPropertyType::Number);
  nativeProps.Insert(L"barCodeScannerEnabled", ViewManagerPropertyType::Boolean);
  nativeProps.Insert(L"barCodeTypes", ViewManagerPropertyType::Array);
  nativeProps.Insert(L"barCodeReadIntervalMS", ViewManagerPropertyType::Number);
  nativeProps.Insert(L"keepAwake", ViewManagerPropertyType::Boolean);
  nativeProps.Insert(L"mirrorVideo", ViewManagerPropertyType::Boolean);
  nativeProps.Insert(L"defaultVideoQuality", ViewManagerPropertyType::Number);

  return nativeProps.GetView();
}

void ReactCameraViewManager::UpdateProperties(
    FrameworkElement const &view,
    IJSValueReader const &propertyMapReader) noexcept {
  if (auto reactCameraView = view.try_as<ReactNativeCameraCPP::ReactCameraView>()) {
    reactCameraView->UpdateProperties(propertyMapReader);
  }
}

// IViewManagerWithExportedEventTypeConstants
ConstantProviderDelegate ReactCameraViewManager::ExportedCustomBubblingEventTypeConstants() noexcept {
  return nullptr;
}

ConstantProviderDelegate ReactCameraViewManager::ExportedCustomDirectEventTypeConstants() noexcept {
  return [](winrt::Microsoft::ReactNative::IJSValueWriter const &constantWriter) {
    constantWriter.WritePropertyName(BarcodeReadEvent);
    constantWriter.WriteObjectBegin();
    WriteProperty(constantWriter, L"registrationName", BarcodeReadEvent);
    constantWriter.WriteObjectEnd();
  };
}

void ReactCameraViewManager::RemoveViewFromList(winrt::com_ptr<ReactNativeCameraCPP::ReactCameraView> view) {
  auto it = std::find(m_cameraViewInstances.begin(), m_cameraViewInstances.end(), view);
  if (it != m_cameraViewInstances.end())
    m_cameraViewInstances.erase(it);
}

IAsyncAction ReactCameraViewManager::TakePictureAsync(
    JSValueObject const &options,
    int viewTag,
    ReactPromise<JSValueObject> const &result) noexcept {
  auto capturedPromise = result;
  auto capturedOptions = options.Copy();

  auto index = co_await FindCamera(viewTag);
  if (index != -1) {
    auto cameraView = m_cameraViewInstances.at(index);
    co_await cameraView->TakePictureAsync(capturedOptions, capturedPromise);
  } else {
    capturedPromise.Reject("No camera instance found.");
  }
}

IAsyncAction ReactCameraViewManager::RecordAsync(
    JSValueObject const &options,
    int viewTag,
    ReactPromise<JSValueObject> const &result) noexcept {
  auto capturedPromise = result;
  auto capturedOptions = options.Copy();

  auto index = co_await FindCamera(viewTag);
  if (index != -1) {
    auto cameraView = m_cameraViewInstances.at(index);
    co_await cameraView->RecordAsync(capturedOptions, capturedPromise);
  } else {
    capturedPromise.Reject("No camera instance found.");
  }
}

IAsyncAction ReactCameraViewManager::StopRecordAsync(int viewTag) noexcept {
  auto index = co_await FindCamera(viewTag);
  if (index != -1) {
    auto cameraView = m_cameraViewInstances.at(index);
    co_await cameraView->StopRecordAsync();
  }
}

IAsyncAction ReactCameraViewManager::IsRecordingAsync(
    int viewTag,
    winrt::Microsoft::ReactNative::ReactPromise<bool> const &result) noexcept {
  auto capturedPromise = result;

  auto index = co_await FindCamera(viewTag);
  if (index != -1) {
    auto cameraView = m_cameraViewInstances.at(index);
    co_await cameraView->IsRecordingAsync(capturedPromise);
  } else {
    capturedPromise.Reject("No camera instance found.");
  }
}

IAsyncAction ReactCameraViewManager::PausePreviewAsync(int viewTag) noexcept {
  auto index = co_await FindCamera(viewTag);
  if (index != -1) {
    auto cameraView = m_cameraViewInstances.at(index);
    co_await cameraView->PausePreviewAsync();
  }
}

IAsyncAction ReactCameraViewManager::ResumePreviewAsync(int viewTag) noexcept {
  auto index = co_await FindCamera(viewTag);
  if (index != -1) {
    auto cameraView = m_cameraViewInstances.at(index);
    co_await cameraView->ResumePreviewAsync();
  }
}

// Intialize MediaCapture and will bring up the constent dialog if this
// is the first time. RNC will display a progress indicator while waiting
// for user click. This method will resolve as no permission if user does
// not grant the permission or App does not have the appx capabilities for
// Microphone and WebCam specified in its Package.Appxmanifest.
IAsyncAction ReactCameraViewManager::CheckMediaCapturePermissionAsync(
    winrt::Microsoft::ReactNative::ReactPromise<bool> const &result) noexcept {
  auto capturedPromise = result;

  auto mediaCapture = winrt::Windows::Media::Capture::MediaCapture();
  bool hasPermission = true;
  try {
    co_await mediaCapture.InitializeAsync();
  } catch (winrt::hresult_error const &) {
    hasPermission = false;
  }

  capturedPromise.Resolve(hasPermission);
}

winrt::IAsyncAction ReactCameraViewManager::GetCameraIdsAsync(
    winrt::ReactPromise<winrt::JSValueArray> const &result) noexcept {
  auto capturedPromise = result;

  auto allVideoDevices = co_await winrt::DeviceInformation::FindAllAsync(winrt::DeviceClass::VideoCapture);

  winrt::JSValueArray resultArray;

  for (auto cameraDeviceInfo : allVideoDevices) {
    if (cameraDeviceInfo.IsEnabled()) {
      resultArray.push_back(winrt::JSValueObject{
          {"id", winrt::to_string(cameraDeviceInfo.Id())},
          {"name", winrt::to_string(cameraDeviceInfo.Name())},
          {"type",
           cameraDeviceInfo.EnclosureLocation() ? static_cast<int>(cameraDeviceInfo.EnclosureLocation().Panel())
                                                : ReactCameraConstants::CameraTypeUnknown},
      });
    }
  }

  capturedPromise.Resolve(resultArray);

  co_return;
}

IAsyncOperation<int> ReactCameraViewManager::FindCamera(int viewTag) noexcept {
  int index = 0;
  for (const auto &cameraView : m_cameraViewInstances) {
    auto element = cameraView.as<winrt::Grid>();
    auto dispatcher = element.Dispatcher();
    co_await resume_foreground(dispatcher);
    int currentTag = static_cast<int>(
        element.GetValue(winrt::FrameworkElement::TagProperty()).as<winrt::IPropertyValue>().GetInt64());
    if (currentTag == viewTag) {
      co_return index;
    }
    co_await resume_background();
    index++;
  }
  co_return -1;
}

} // namespace winrt::ReactNativeCameraCPP::implementation
