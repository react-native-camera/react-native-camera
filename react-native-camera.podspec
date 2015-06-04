Pod::Spec.new do |s|

  s.name         = "react-native-camera"
  s.version      = "0.2.9"
  s.summary      = "A Camera component for React Native. Also reads barcodes."
  s.homepage     = "https://github.com/lwansbrough/react-native-camera"
  s.license      = { :type => "MIT", :file => "LICENSE" }
  s.author       = { "Lochlan Wansbrough" => "lochie@live.com" }
  s.platform     = :ios, "7.0"
  s.source       = { :git => "https://github.com/lwansbrough/react-native-camera.git", :tag => "v0.2.8" }
  s.source_files = "*.{h,m}"
  s.requires_arc = true
  s.dependency 'React'
end
