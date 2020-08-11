#pragma once

#include "CameraRotationHelper.g.h"

// This is a Cpp/WinRT implementation of the Camera Rotation Helper class(C#) on MSDN:
// https://docs.microsoft.com/en-us/windows/uwp/audio-video-camera/handle-device-orientation-with-mediacapture#camerarotationhelper-full-code-listing

namespace winrt::ReactNativeCameraCPP::implementation {
struct CameraRotationHelper : CameraRotationHelperT<CameraRotationHelper> {
 public:
  CameraRotationHelper(winrt::Windows::Devices::Enumeration::EnclosureLocation location);
  winrt::event_token OrientationChanged(Windows::Foundation::EventHandler<bool> const &handler);
  void OrientationChanged(winrt::event_token const &token) noexcept;

  winrt::Windows::Devices::Sensors::SimpleOrientation GetCameraCaptureOrientation();
  winrt::Windows::Storage::FileProperties::PhotoOrientation GetConvertedCameraCaptureOrientation();
  winrt::Windows::Devices::Sensors::SimpleOrientation GetCameraPreviewOrientation();
  int GetCameraPreviewClockwiseDegrees();

 private:
  bool IsEnclosureLocationExternal(winrt::Windows::Devices::Enumeration::EnclosureLocation enclosureLocation) {
    return (
        enclosureLocation == nullptr ||
        enclosureLocation.Panel() == winrt::Windows::Devices::Enumeration::Panel::Unknown);
  }
  bool ShouldMirrorPreview();
  winrt::Windows::Devices::Sensors::SimpleOrientation GetCameraOrientationRelativeToNativeOrientation();

  winrt::Windows::Devices::Sensors::SimpleOrientation SubtractOrientations(
      winrt::Windows::Devices::Sensors::SimpleOrientation a,
      winrt::Windows::Devices::Sensors::SimpleOrientation b);
  winrt::Windows::Devices::Sensors::SimpleOrientation MirrorOrientation(
      winrt::Windows::Devices::Sensors::SimpleOrientation orientation);
  winrt::Windows::Devices::Sensors::SimpleOrientation AddOrientations(
      winrt::Windows::Devices::Sensors::SimpleOrientation a,
      winrt::Windows::Devices::Sensors::SimpleOrientation b);

  winrt::Windows::Devices::Sensors::SimpleOrientation ConvertDisplayOrientationToSimpleOrientation(
      Windows::Graphics::Display::DisplayOrientations orientation);

  static winrt::Windows::Storage::FileProperties::PhotoOrientation ConvertSimpleOrientationToPhotoOrientation(
      winrt::Windows::Devices::Sensors::SimpleOrientation orientation);
  static int ConvertSimpleOrientationToClockwiseDegrees(
      winrt::Windows::Devices::Sensors::SimpleOrientation orientation);
  static winrt::Windows::Devices::Sensors::SimpleOrientation ConvertClockwiseDegreesToSimpleOrientation(
      int orientation);

  void SimpleOrientationSensor_OrientationChanged(IInspectable const &sender, IInspectable const &args);
  void DisplayInformation_OrientationChanged(IInspectable const &sender, IInspectable const &args);

  winrt::Windows::Devices::Sensors::SimpleOrientationSensor::OrientationChanged_revoker
      m_sensorOrientationChanged_revoker{};
  winrt::Windows::Graphics::Display::DisplayInformation::OrientationChanged_revoker
      m_displayOrientationChanged_revoker{};

  winrt::Windows::Devices::Enumeration::EnclosureLocation m_cameraEnclosureLocation{nullptr};
  winrt::event<Windows::Foundation::EventHandler<bool>> m_orientationChangedEvent;
  winrt::Windows::Devices::Sensors::SimpleOrientationSensor m_orientationSensor{
      winrt::Windows::Devices::Sensors::SimpleOrientationSensor::GetDefault()};
  winrt::Windows::Graphics::Display::DisplayInformation m_displayInformation{
      winrt::Windows::Graphics::Display::DisplayInformation::GetForCurrentView()};
};
} // namespace winrt::ReactNativeCameraCPP::implementation

namespace winrt::ReactNativeCameraCPP::factory_implementation {
struct CameraRotationHelper : CameraRotationHelperT<CameraRotationHelper, implementation::CameraRotationHelper> {};
} // namespace winrt::ReactNativeCameraCPP::factory_implementation
