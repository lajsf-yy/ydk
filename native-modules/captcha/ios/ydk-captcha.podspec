require "json"

package = JSON.parse(File.read(File.join(__dir__, "../../../package.json")))

Pod::Spec.new do |s|
  
  s.name              = 'ydk-captcha'
  s.platform          = :ios, "9.0"
  s.summary           = package['description']
  s.version           = package['version']
  s.homepage          = package['homepage']
  s.author            = { package['author']['name'] => package['author']['email'] }
  s.license           = package['license']
  s.source            = { :git => "http://192.168.30.4/js/ydk.git", :tag => s.version }
  
  s.subspec 'Core' do |ss|
    ss.source_files = 'Classes/*.{h,m}'
    ss.public_header_files = 'Classes/*.h'
  end
  
  s.subspec 'rn' do |ss|
    ss.source_files = 'Classes/react-native/*.{h,m}'
    ss.public_header_files = 'Classes/react-native/*.h'
    ss.dependency 'React'
    ss.dependency 'ydk-captcha/Core'
  end
  
  s.resource = 'Classes/vendor/GT3Captcha.bundle'
  s.vendored_frameworks = 'Classes/vendor/GT3Captcha.framework'
  s.dependency 'ydk-core'
  s.dependency 'ydk-network'
  s.dependency 'ReactiveObjC', '~> 3.1.0'
  
end