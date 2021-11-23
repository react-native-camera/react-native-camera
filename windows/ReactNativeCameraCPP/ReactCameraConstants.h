#pragma once

#include <functional>

#include <winrt/Windows.Media.Devices.h>
#include <winrt/Windows.Media.MediaProperties.h>

#include "JSValue.h"

#define BarcodeReadEvent L"onBarCodeRead"
#define CameraReadyEvent L"onCameraReady"

namespace winrt::ReactNativeCameraCPP {
class ReactCameraConstants {
 public:
  static const int CameraAspectFill = 0;
  static const int CameraAspectFit = 1;
  static const int CameraAspectStretch = 2;
  static const int CameraCaptureTargetMemory = 0;
  static const int CameraCaptureTargetDisk = 1;
  static const int CameraCaptureTargetCameraRoll = 2;
  static const int CameraCaptureTargetTemp = 3;
  static const int CameraOrientationAuto = UINT_MAX;
  static const int CameraOrientationPortrait = (int)winrt::Windows::Devices::Sensors::SimpleOrientation::NotRotated;
  static const int CameraOrientationPortraitUpsideDown =
      (int)winrt::Windows::Devices::Sensors::SimpleOrientation::Rotated180DegreesCounterclockwise;
  static const int CameraOrientationLandscapeLeft =
      (int)winrt::Windows::Devices::Sensors::SimpleOrientation::Rotated90DegreesCounterclockwise;
  static const int CameraOrientationLandscapeRight =
      (int)winrt::Windows::Devices::Sensors::SimpleOrientation::Rotated270DegreesCounterclockwise;
  static const int CameraTypeUnknown = (int)winrt::Windows::Devices::Enumeration::Panel::Unknown;
  static const int CameraTypeFront = (int)winrt::Windows::Devices::Enumeration::Panel::Front;
  static const int CameraTypeBack = (int)winrt::Windows::Devices::Enumeration::Panel::Back;
  static const int CameraTypeTop = (int)winrt::Windows::Devices::Enumeration::Panel::Top;
  static const int CameraTypeBottom = (int)winrt::Windows::Devices::Enumeration::Panel::Bottom;
  static const int CameraTypeLeft = (int)winrt::Windows::Devices::Enumeration::Panel::Left;
  static const int CameraTypeRight = (int)winrt::Windows::Devices::Enumeration::Panel::Right;
  static const int CameraFlashModeOff = 0;
  static const int CameraFlashModeOn = 1;
  static const int CameraFlashModeAuto = 2;
  static const int CameraAutoFocusOff = (int)winrt::Windows::Media::Devices::FocusPreset::Manual;
  static const int CameraAutoFocusOn = (int)winrt::Windows::Media::Devices::FocusPreset::Auto;
  static const int CameraWhiteBalanceAuto = (int)winrt::Windows::Media::Devices::ColorTemperaturePreset::Auto;
  static const int CameraWhiteBalanceSunny = (int)winrt::Windows::Media::Devices::ColorTemperaturePreset::Daylight;
  static const int CameraWhiteBalanceCloudy = (int)winrt::Windows::Media::Devices::ColorTemperaturePreset::Cloudy;
  static const int CameraWhiteBalanceShadow = (int)winrt::Windows::Media::Devices::ColorTemperaturePreset::Candlelight;
  static const int CameraWhiteBalanceIncandescent =
      (int)winrt::Windows::Media::Devices::ColorTemperaturePreset::Tungsten;
  static const int CameraWhiteBalanceFluorescent =
      (int)winrt::Windows::Media::Devices::ColorTemperaturePreset::Fluorescent;
  static const int CameraVideoQualityAuto = (int)winrt::Windows::Media::MediaProperties::VideoEncodingQuality::Auto;
  static const int CameraVideoQuality2160P =
      (int)winrt::Windows::Media::MediaProperties::VideoEncodingQuality::Uhd2160p;
  static const int CameraVideoQuality1080P = (int)winrt::Windows::Media::MediaProperties::VideoEncodingQuality::HD1080p;
  static const int CameraVideoQuality720P = (int)winrt::Windows::Media::MediaProperties::VideoEncodingQuality::HD720p;
  static const int CameraVideoQualityWVGA = (int)winrt::Windows::Media::MediaProperties::VideoEncodingQuality::Wvga;
  static const int CameraVideoQualityVGA = (int)winrt::Windows::Media::MediaProperties::VideoEncodingQuality::Vga;
  static const int CameraVideoCodecH264 = 0;
  static const int CameraVideoCodecHEVC = 1;
  static const int CameraVideoCodecWMV = 2;
  static const int MediaTypeJPG = 1;
  static const int MediaTypeMP4 = 2;
  static const int MediaTypeWMV = 3;

  static const int BarcodeReadIntervalMinMS = 200;
  static const int BarcodeReadIntervalMS = 500;
  static const int BarcodeReadTimeoutMS = 5000;

  static winrt::Microsoft::ReactNative::JSValueObject GetAspectConstants() noexcept {
    return winrt::Microsoft::ReactNative::JSValueObject{
        {"stretch", CameraAspectStretch}, {"fit", CameraAspectFit}, {"fill", CameraAspectFill}};
  }

  static winrt::Microsoft::ReactNative::JSValue GetBarcodeConstants() noexcept {
    return winrt::Microsoft::ReactNative::JSValueObject{
        {"aztec", "AZTEC"}, // winrt::ZXing::BarcodeType::AZTEC
        {"codabar", "CODABAR"}, // winrt::ZXing::BarcodeType::CODABAR
        {"code39", "CODE_39"}, // winrt::ZXing::BarcodeType::CODE_39
        {"code93", "CODE_93"}, // winrt::ZXing::BarcodeType::CODE_93
        {"code128", "CODE_128"}, // winrt::ZXing::BarcodeType::CODE_128
        {"datamatrix", "DATA_MATRIX"}, // winrt::ZXing::BarcodeType::DATA_MATRIX
        {"ean8", "EAN_8"}, // winrt::ZXing::BarcodeType::EAN_8
        {"ean13", "EAN_13"}, // winrt::ZXing::BarcodeType::EAN_13
        {"interleaved2of5", "ITF"}, // winrt::ZXing::BarcodeType::ITF
        {"maxicode", "MAXICODE"}, // winrt::ZXing::BarcodeType::MAXICODE
        {"pdf417", "PDF_417"}, // winrt::ZXing::BarcodeType::PDF_417
        {"qr", "QR_CODE"}, // winrt::ZXing::BarcodeType::QR_CODE
        {"rss14", "RSS_14"}, // winrt::ZXing::BarcodeType::RSS_14
        {"rssexpanded", "RSS_EXPANDED"}, // winrt::ZXing::BarcodeType::RSS_EXPANDED
        {"upc_a", "UPC_A"}, // winrt::ZXing::BarcodeType::UPC_A
        {"upc_e", "UPC_E"}, // winrt::ZXing::BarcodeType::UPC_E
        {"upc_ean", "UPC_EAN_EXTENSION"}, // winrt::ZXing::BarcodeType::UPC_EAN_EXTENSION
    };
  }

  static winrt::Microsoft::ReactNative::JSValueObject GetFaceDetectionConstants() noexcept {
    return winrt::Microsoft::ReactNative::JSValueObject{
        {"Mode", winrt::Microsoft::ReactNative::JSValue::EmptyObject.Copy()},
        {"Landmarks", winrt::Microsoft::ReactNative::JSValue::EmptyObject.Copy()},
        {"Classifications", winrt::Microsoft::ReactNative::JSValue::EmptyObject.Copy()},
    };
  }

  static winrt::Microsoft::ReactNative::JSValueObject GetAutoFocusConstants() noexcept {
    return winrt::Microsoft::ReactNative::JSValueObject{
        {"off", CameraAutoFocusOff},
        {"on", CameraAutoFocusOn},
    };
  }

  static winrt::Microsoft::ReactNative::JSValueObject GetWhiteBalanceConstants() noexcept {
    return winrt::Microsoft::ReactNative::JSValueObject{
        {"auto", CameraWhiteBalanceAuto},
        {"sunny", CameraWhiteBalanceSunny},
        {"cloudy", CameraWhiteBalanceCloudy},
        {"shadow", CameraWhiteBalanceShadow},
        {"incandescent", CameraWhiteBalanceIncandescent},
        {"fluorescent", CameraWhiteBalanceFluorescent},
    };
  }

  static winrt::Microsoft::ReactNative::JSValueObject GetTypeConstants() noexcept {
    return winrt::Microsoft::ReactNative::JSValueObject{
        {"unknown", CameraTypeUnknown},
        {"front", CameraTypeFront},
        {"back", CameraTypeBack},
        {"top", CameraTypeTop},
        {"bottom", CameraTypeBottom},
        {"left", CameraTypeLeft},
        {"right", CameraTypeRight},
    };
  }

  static winrt::Microsoft::ReactNative::JSValueObject GetCaptureQualityConstants() noexcept {
    return winrt::Microsoft::ReactNative::JSValueObject{
        {"auto", CameraVideoQualityAuto},
        {"2160p", CameraVideoQuality2160P},
        {"1080p", CameraVideoQuality1080P},
        {"720p", CameraVideoQuality720P},
        {"480p", CameraVideoQualityWVGA},
        {"4:3", CameraVideoQualityVGA},
    };
  }

  static winrt::Microsoft::ReactNative::JSValueObject GetCaptureCodecConstants() noexcept {
    return winrt::Microsoft::ReactNative::JSValueObject{
        {"H264", CameraVideoCodecH264},
        {"HEVC", CameraVideoCodecHEVC},
        {"WMV", CameraVideoCodecWMV},
    };
  }

  static winrt::Microsoft::ReactNative::JSValueObject GetCaptureTargetConstants() noexcept {
    return winrt::Microsoft::ReactNative::JSValueObject{
        {"memory", CameraCaptureTargetMemory},
        {"disk", CameraCaptureTargetDisk},
        {"cameraRoll", CameraCaptureTargetCameraRoll},
        {"temp", CameraCaptureTargetTemp},
    };
  }

  static winrt::Microsoft::ReactNative::JSValueObject GetOrientationConstants() noexcept {
    return winrt::Microsoft::ReactNative::JSValueObject{
        {"auto", CameraOrientationAuto},
        {"landscapeLeft", CameraOrientationLandscapeLeft},
        {"landscapeRight", CameraOrientationLandscapeRight},
        {"portrait", CameraOrientationPortrait},
        {"portraitUpsideDown", CameraOrientationPortraitUpsideDown},
    };
  }

  static winrt::Microsoft::ReactNative::JSValueObject GetFlashModeConstants() noexcept {
    return winrt::Microsoft::ReactNative::JSValueObject{
        {"off", CameraFlashModeOff},
        {"on", CameraFlashModeOn},
        {"auto", CameraOrientationAuto},
    };
  }
};
}; // namespace winrt::ReactNativeCameraCPP
