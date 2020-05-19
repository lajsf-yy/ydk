require "json"

package = JSON.parse(File.read(File.join(__dir__, "../../../package.json")))

Pod::Spec.new do |s|
  
  s.name              = 'ydk-mqtt'
  s.platform          = :ios, "9.0"
  s.summary           = package['description']
  s.version           = package['version']
  #s.description      = package['description']
  s.homepage          = package['homepage']
  s.author            = { package['author']['name'] => package['author']['email'] }
  s.license           = { :type => 'GPL' }
  s.source            = { :path => '.' }
  
  s.subspec 'Core' do |ss|
    ss.source_files = 'Classes/*.{h,m}'
    ss.public_header_files = 'Classes/*.h'
  end
  
  s.subspec 'rn' do |ss|
    ss.source_files = 'Classes/react-native/*.{h,m}'
    ss.public_header_files = 'Classes/react-native/*.h'
    ss.dependency 'React'
    ss.dependency 'ydk-mqtt/Core'
  end
  s.dependency 'ydk-network'
  s.dependency 'ydk-core'
  s.dependency 'MQTT'
  
end
