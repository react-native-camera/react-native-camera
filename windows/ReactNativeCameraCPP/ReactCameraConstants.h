#pragma once

#include <functional>

namespace winrt::ReactNativeCameraCPP
{
    class ReactCameraContants
    {
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
        static const int CameraOrientationPortraitUpsideDown = (int)winrt::Windows::Devices::Sensors::SimpleOrientation::Rotated180DegreesCounterclockwise;
        static const int CameraOrientationLandscapeLeft = (int)winrt::Windows::Devices::Sensors::SimpleOrientation::Rotated90DegreesCounterclockwise;
        static const int CameraOrientationLandscapeRight = (int)winrt::Windows::Devices::Sensors::SimpleOrientation::Rotated270DegreesCounterclockwise;
        static const int CameraTypeFront = (int)winrt::Windows::Devices::Enumeration::Panel::Front;
        static const int CameraTypeBack = (int)winrt::Windows::Devices::Enumeration::Panel::Back;
        static const int CameraFlashModeOff = 0;
        static const int CameraFlashModeOn = 1;
        static const int CameraFlashModeAuto = 2;
        static const int CameraTorchModeOff = 0;
        static const int CameraTorchModeOn = 1;
        static const int CameraTorchModeAuto = 2;
        static const int CameraAutoFocusOff = 0;
        static const int CameraAutoFocusOn = 1;
        static const int CameraWhiteBalanceAuto = 0;
        static const int CameraWhiteBalanceSunny = 1;
        static const int CameraWhiteBalanceCloudy = 2;
        static const int CameraWhiteBalanceShadow = 3;
        static const int CameraWhiteBalanceIncandescent = 4;
        static const int CameraWhiteBalanceFluorescent = 5;
        static const int CameraCaptureQualityHigh = (int)winrt::Windows::Media::MediaProperties::VideoEncodingQuality::HD1080p;
        static const int CameraCaptureQualityLow = (int)winrt::Windows::Media::MediaProperties::VideoEncodingQuality::HD720p;
        static const int CameraCaptureQuality1080p = (int)winrt::Windows::Media::MediaProperties::VideoEncodingQuality::HD1080p;
        static const int CameraCaptureQuality720p = (int)winrt::Windows::Media::MediaProperties::VideoEncodingQuality::HD720p;
        static const int MediaTypeImage = 1;
        static const int MediaTypeVideo = 2;

        static std::map<std::wstring, int> GetAspectConstants() noexcept {
            return std::map<std::wstring, int>
            {
                { L"stretch", CameraAspectStretch },
                { L"fit", CameraAspectFit },
                { L"fill", CameraAspectFill }
            };
        }

        static std::map<std::wstring, std::wstring> GetBarcodeConstants() noexcept {
            return std::map<std::wstring, std::wstring>
            {
                { L"UPC_E", L"32768" },
                { L"CODE_39", L"4" },
            };
        }

        static std::map<std::wstring, int> GetAutoFocusConstants() noexcept {
            return std::map<std::wstring, int>
            {
                { L"off", CameraAutoFocusOff },
                { L"on", CameraAutoFocusOn },
            };
        }

        static std::map<std::wstring, int> GetWhiteBalanceConstants() noexcept {
            return std::map<std::wstring, int>
            {
                { L"auto", CameraWhiteBalanceAuto },
                { L"sunny", CameraWhiteBalanceSunny },
                { L"cloudy", CameraWhiteBalanceCloudy },
                { L"shadow", CameraWhiteBalanceShadow },
                { L"incandescent", CameraWhiteBalanceIncandescent },
                { L"fluorescent", CameraWhiteBalanceFluorescent },
            };
        }

        static std::map<std::wstring, int> GetTypeConstants() noexcept {
            return std::map<std::wstring, int>
            {
                { L"front", CameraTypeFront },
                { L"back", CameraTypeBack },
            };
        }

        static std::map<std::wstring, int> GetCaptureQualityConstants() noexcept {
            return std::map<std::wstring, int>
            {
                { L"low", CameraCaptureQualityLow },
                { L"high", CameraCaptureQualityHigh },
                { L"720p", CameraCaptureQuality720p },
                { L"1080p", CameraCaptureQuality1080p },
            };
        }

        static std::map<std::wstring, int>   GetCaptureTargetConstants() noexcept {
            return std::map<std::wstring, int>
            {
                { L"memory", CameraCaptureTargetMemory },
                { L"disk", CameraCaptureTargetDisk },
                { L"cameraRoll", CameraCaptureTargetCameraRoll },
                { L"temp", CameraCaptureTargetTemp },
            };
        }

        static std::map<std::wstring, int>   GetOrientationConstants() noexcept {
            return std::map<std::wstring, int>
            {
                { L"auto", CameraOrientationAuto },
                { L"landscapeLeft", CameraOrientationLandscapeLeft },
                { L"landscapeRight", CameraOrientationLandscapeRight },
                { L"portrait", CameraOrientationPortrait },
                { L"portraitUpsideDown", CameraOrientationPortraitUpsideDown },
            };
        }

        static std::map<std::wstring, int>   GetFlashModeConstants() noexcept {
            return std::map<std::wstring, int>
            {
                { L"off", CameraFlashModeOff },
                { L"on", CameraFlashModeOn },
                { L"auto", CameraOrientationAuto },
            };
        }

        static std::map<std::wstring, int>   GetTorchModeConstants() noexcept {
            return std::map<std::wstring, int>
            {
                { L"off", CameraTorchModeOff },
                { L"on", CameraTorchModeOn },
                { L"auto", CameraTorchModeAuto },
            };
        }
    };
};