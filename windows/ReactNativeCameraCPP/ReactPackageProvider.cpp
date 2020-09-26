#include "pch.h"
#include "ReactPackageProvider.h"
#if __has_include("ReactPackageProvider.g.cpp")
#include "ReactPackageProvider.g.cpp"
#endif

#include "ReactCameraModule.h"
#include "ReactCameraViewManager.h"

using namespace winrt::Microsoft::ReactNative;

namespace winrt::ReactNativeCameraCPP::implementation {

void ReactPackageProvider::CreatePackage(IReactPackageBuilder const &packageBuilder) noexcept {
  AddAttributedModules(packageBuilder);
  packageBuilder.AddViewManager(L"ReactCameraViewManager", []() { return winrt::make<ReactCameraViewManager>(); });
}

} // namespace winrt::ReactNativeCameraCPP::implementation
