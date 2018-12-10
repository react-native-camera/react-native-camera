using ReactNative.UIManager;
using System.Collections.Generic;
using Windows.UI.Xaml.Controls;

namespace RNCamera
{
    class CameraForViewManager
    {
        private readonly object _gate = new object();

        private readonly IDictionary<int, CameraForView> _cameras =
            new Dictionary<int, CameraForView>();

        public CameraForView GetCameraForView(int viewTag)
        {
            CameraForView result;
            if (!_cameras.TryGetValue(viewTag, out result))
            {
                return null;
            }

            return result;
        }

        public CameraForView GetOrCreateCameraForView(CaptureElement view)
        {
            var viewTag = view.GetTag();
            var reactContext = view.GetReactContext();
            CameraForView result;
            if (!_cameras.TryGetValue(viewTag, out result))
            {
                result = new CameraForView(view);
                _cameras.Add(viewTag, result);
                reactContext.AddLifecycleEventListener(result);
            }

            return result;
        }

        public void DropCameraForView(CaptureElement view)
        {
            var viewTag = view.GetTag();
            var camera = GetCameraForView(viewTag);
            if (camera != null)
            {
                _cameras.Remove(viewTag);
                var reactContext = view.GetReactContext();
                reactContext.RemoveLifecycleEventListener(camera);
            }
        }
    }
}
