#include "pch.h"
#include "CameraRotationHelper.h"
#include "CameraRotationHelper.g.cpp"

namespace winrt {
    using namespace Windows::Devices::Enumeration;
    using namespace Windows::Devices::Sensors;
    using namespace Windows::Storage::FileProperties;
    using namespace Windows::Graphics::Display;
} //namespace winrt

namespace winrt::ReactNativeCameraCPP::implementation
{
    CameraRotationHelper::CameraRotationHelper(EnclosureLocation location)
    {
        m_cameraEnclosureLocation = location;

        if (!IsEnclosureLocationExternal(m_cameraEnclosureLocation) && m_orientationSensor != nullptr)
        {
            m_sensorOrientationChanged_revoker = m_orientationSensor.OrientationChanged(winrt::auto_revoke, [ref = get_weak()](auto const& sender, auto const& args) 
            {
                if (auto self = ref.get()) {
                    self->SimpleOrientationSensor_OrientationChanged(sender, args);
                }
            });
        }
        m_displayOrientationChanged_revoker = m_displayInformation.OrientationChanged(winrt::auto_revoke, [ref = get_weak()](auto const& sender, auto const& args)
        {
            if (auto self = ref.get()) {
                self->DisplayInformation_OrientationChanged(sender, args);
            }
        });
    }

    PhotoOrientation CameraRotationHelper::GetConvertedCameraCaptureOrientation()
    {
        auto orientation = GetCameraCaptureOrientation();
        return ConvertSimpleOrientationToPhotoOrientation(orientation);
    }

    SimpleOrientation CameraRotationHelper::GetCameraCaptureOrientation()
    {
        if (IsEnclosureLocationExternal(m_cameraEnclosureLocation))
        {
            // Cameras that are not attached to the device do not rotate along with it, so apply no rotation
            return SimpleOrientation::NotRotated;
        }

        // Get the device orientation offset by the camera hardware offset
        auto deviceOrientation = m_orientationSensor ? m_orientationSensor.GetCurrentOrientation() : SimpleOrientation::NotRotated;
        auto result = SubtractOrientations(deviceOrientation, GetCameraOrientationRelativeToNativeOrientation());

        // If the preview is being mirrored for a front-facing camera, then the rotation should be inverted
        if (ShouldMirrorPreview())
        {
            result = MirrorOrientation(result);
        }
        return result;
    }

    SimpleOrientation CameraRotationHelper::GetCameraPreviewOrientation()
    {
        if (IsEnclosureLocationExternal(m_cameraEnclosureLocation))
        {
            // Cameras that are not attached to the device do not rotate along with it, so apply no rotation
            return SimpleOrientation::NotRotated;
        }

        // Get the app display rotation offset by the camera hardware offset
        auto result = ConvertDisplayOrientationToSimpleOrientation(m_displayInformation.CurrentOrientation());
        result = SubtractOrientations(result, GetCameraOrientationRelativeToNativeOrientation());

        // If the preview is being mirrored for a front-facing camera, then the rotation should be inverted
        if (ShouldMirrorPreview())
        {
            result = MirrorOrientation(result);
        }
        return result;
    }

    int CameraRotationHelper::GetCameraPreviewClockwiseDegrees()
    {
        auto rotation = GetCameraPreviewOrientation();
        return ConvertSimpleOrientationToClockwiseDegrees(rotation);
    }

    PhotoOrientation CameraRotationHelper::ConvertSimpleOrientationToPhotoOrientation(SimpleOrientation orientation)
    {
        switch (orientation)
        {
            case SimpleOrientation::Rotated90DegreesCounterclockwise:
                return PhotoOrientation::Rotate90;
            case SimpleOrientation::Rotated180DegreesCounterclockwise:
                return PhotoOrientation::Rotate180;
            case SimpleOrientation::Rotated270DegreesCounterclockwise:
                return PhotoOrientation::Rotate270;
            case SimpleOrientation::NotRotated:
            default:
                return PhotoOrientation::Normal;
        }
    }

    int CameraRotationHelper::ConvertSimpleOrientationToClockwiseDegrees(SimpleOrientation orientation)
    {
        switch (orientation)
        {
            case SimpleOrientation::Rotated90DegreesCounterclockwise:
                return 270;
            case SimpleOrientation::Rotated180DegreesCounterclockwise:
                return 180;
            case SimpleOrientation::Rotated270DegreesCounterclockwise:
                return 90;
            case SimpleOrientation::NotRotated:
            default:
                return 0;
        }
    }

    SimpleOrientation CameraRotationHelper::ConvertDisplayOrientationToSimpleOrientation(DisplayOrientations orientation)
    {
        SimpleOrientation result;
        switch (orientation)
        {
        case DisplayOrientations::Landscape:
            result = SimpleOrientation::NotRotated;
            break;
        case DisplayOrientations::PortraitFlipped:
            result = SimpleOrientation::Rotated90DegreesCounterclockwise;
            break;
        case DisplayOrientations::LandscapeFlipped:
            result = SimpleOrientation::Rotated180DegreesCounterclockwise;
            break;
        case DisplayOrientations::Portrait:
        default:
            result = SimpleOrientation::Rotated270DegreesCounterclockwise;
            break;
        }

        // Above assumes landscape; offset is needed if native orientation is portrait
        if (m_displayInformation.NativeOrientation() == DisplayOrientations::Portrait)
        {
            result = AddOrientations(result, SimpleOrientation::Rotated90DegreesCounterclockwise);
        }

        return result;
    }

    SimpleOrientation CameraRotationHelper::SubtractOrientations(SimpleOrientation a, SimpleOrientation b)
    {
        auto aRot = ConvertSimpleOrientationToClockwiseDegrees(a);
        auto bRot = ConvertSimpleOrientationToClockwiseDegrees(b);
        // Add 360 to ensure the modulus operator does not operate on a negative
        auto result = (360 + (aRot - bRot)) % 360;
        return ConvertClockwiseDegreesToSimpleOrientation(result);
    }

    SimpleOrientation CameraRotationHelper::MirrorOrientation(SimpleOrientation orientation)
    {
        // This only affects the 90 and 270 degree cases, because rotating 0 and 180 degrees is the same clockwise and counter-clockwise
        switch (orientation)
        {
        case SimpleOrientation::Rotated90DegreesCounterclockwise:
            return SimpleOrientation::Rotated270DegreesCounterclockwise;
        case SimpleOrientation::Rotated270DegreesCounterclockwise:
            return SimpleOrientation::Rotated90DegreesCounterclockwise;
        }
        return orientation;
    }

    SimpleOrientation CameraRotationHelper::AddOrientations(SimpleOrientation a, SimpleOrientation b)
    {
        auto aRot = ConvertSimpleOrientationToClockwiseDegrees(a);
        auto bRot = ConvertSimpleOrientationToClockwiseDegrees(b);
        auto result = (aRot + bRot) % 360;
        return ConvertClockwiseDegreesToSimpleOrientation(result);
    }

    SimpleOrientation CameraRotationHelper::ConvertClockwiseDegreesToSimpleOrientation(int orientation)
    {
        switch (orientation)
        {
        case 270:
            return SimpleOrientation::Rotated90DegreesCounterclockwise;
        case 180:
            return SimpleOrientation::Rotated180DegreesCounterclockwise;
        case 90:
            return SimpleOrientation::Rotated270DegreesCounterclockwise;
        case 0:
        default:
            return SimpleOrientation::NotRotated;
        }
    }

    void CameraRotationHelper::SimpleOrientationSensor_OrientationChanged(IInspectable const&, IInspectable const&)
    {
        if (m_orientationSensor.GetCurrentOrientation() != SimpleOrientation::Faceup && m_orientationSensor.GetCurrentOrientation() != SimpleOrientation::Facedown)
        {
            // Only raise the OrientationChanged event if the device is not parallel to the ground. This allows users to take pictures of documents (FaceUp)
            // or the ceiling (FaceDown) in portrait or landscape, by first holding the device in the desired orientation, and then pointing the camera
            // either up or down, at the desired subject.
            //Note: This assumes that the camera is either facing the same way as the screen, or the opposite way. For devices with cameras mounted
            //      on other panels, this logic should be adjusted.
            m_orientationChangedEvent(*this, false);
        }
    }

    void CameraRotationHelper::DisplayInformation_OrientationChanged(IInspectable const&, IInspectable const&)
    {
        m_orientationChangedEvent(*this, true);
    }

    bool CameraRotationHelper::ShouldMirrorPreview()
    {
        // It is recommended that applications mirror the preview for front-facing cameras, as it gives users a more natural experience, since it behaves more like a mirror
        return (m_cameraEnclosureLocation.Panel() == Panel::Front);
    }

    SimpleOrientation CameraRotationHelper::GetCameraOrientationRelativeToNativeOrientation()
    {
        // Get the rotation angle of the camera enclosure as it is mounted in the device hardware
        auto enclosureAngle = ConvertClockwiseDegreesToSimpleOrientation(static_cast<int>(m_cameraEnclosureLocation.RotationAngleInDegreesClockwise()));

        // Account for the fact that, on portrait-first devices, the built in camera sensor is read at a 90 degree offset to the native orientation
        if (m_displayInformation.NativeOrientation() == DisplayOrientations::Portrait && 
            !IsEnclosureLocationExternal(m_cameraEnclosureLocation))
        {
            enclosureAngle = AddOrientations(SimpleOrientation::Rotated90DegreesCounterclockwise, enclosureAngle);
        }

        return enclosureAngle;
    }
    
    winrt::event_token CameraRotationHelper::OrientationChanged(Windows::Foundation::EventHandler<bool> const& handler)
    {
        return m_orientationChangedEvent.add(handler);
    }

    void CameraRotationHelper::OrientationChanged(winrt::event_token const& token) noexcept
    {
        m_orientationChangedEvent.remove(token);
    }
}
