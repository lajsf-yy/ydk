
require "json"

package = JSON.parse(File.read(File.join(__dir__, "../../../package.json")))

Pod::Spec.new do |s|
  
  s.name              = 'ydk-share'
  s.platform          = :ios, "9.0"
  s.summary           = package['description']
  s.version           = package['version']
  s.homepage          = package['homepage']
  s.author            = { package['author']['name'] => package['author']['email'] }
  s.license           = package['license']
  s.source            = { :git => "http://192.168.30.4/js/ydk.git", :tag => s.version }
  
  s.subspec 'Core' do |ss|
    ss.source_files = 'Classes/YdkShare.{h,m}'
    ss.public_header_files = 'Classes/YdkShare.h'
  end
  
  s.subspec 'rn' do |ss|
    ss.source_files = 'Classes/react-native/*.{h,m}'
    ss.public_header_files = 'Classes/react-native/*.h'
    ss.dependency 'React'
    ss.dependency 'ydk-share/Core'
  end
  
  s.dependency 'ydk-core'
  s.dependency 'MOBFoundation', '3.2.8'
  s.dependency 'mob_sharesdk', '4.3.4'
  s.dependency 'mob_sharesdk/ShareSDKExtension'
  s.dependency 'mob_sharesdk/ShareSDKPlatforms/QQ'
  s.dependency 'mob_sharesdk/ShareSDKPlatforms/SinaWeibo'
  s.dependency 'mob_sharesdk/ShareSDKPlatforms/WeChatFull'
  
end
