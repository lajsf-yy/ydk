require "json"

package = JSON.parse(File.read(File.join(__dir__, "../../../package.json")))

Pod::Spec.new do |s|
  
  s.name              = 'ydk-network'
  s.platform          = :ios, "9.0"
  s.summary           = package['description']
  s.version           = package['version']
  s.homepage          = package['homepage']
  s.author            = { package['author']['name'] => package['author']['email'] }
  s.license           = package['license']
  s.source            = { :git => "http://192.168.30.4/js/ydk.git", :tag => s.version }
  
  s.subspec 'Core' do |ss|
    ss.source_files = 'Classes/*.{h,m}', 'Classes/entity/*.{h,m}', 'Classes/service/*.{h,m}', 'Classes/service/serializer/*.{h,m}'
    ss.public_header_files = 'Classes/*.h', 'Classes/entity/*.h', 'Classes/service/*.h', 'Classes/service/serializer/*.h'
  end
  
  s.subspec 'rn' do |ss|
    ss.source_files = 'Classes/react-native/*.{h,m}'
    ss.public_header_files = 'Classes/react-native/*.h'
    ss.dependency 'React'
    ss.dependency 'ydk-network/Core'
  end
  
  s.dependency 'ydk-core'
  s.dependency 'ydk-toolkit'
  s.dependency 'AFNetworking', '~> 3.2.1'
  s.dependency 'ReactiveObjC', '~> 3.1.0'
  s.dependency 'AliyunOSSiOS', '~> 2.6.3'
  s.dependency 'JSONKit', '1.4'
  
end
