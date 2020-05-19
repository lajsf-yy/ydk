require "json"

package = JSON.parse(File.read(File.join(__dir__, "../../../package.json")))

Pod::Spec.new do |s|
  
  s.name              = 'ydk-navigation'
  s.platform          = :ios, "9.0"
  s.summary           = package['description']
  s.version           = package['version']
  s.homepage          = package['homepage']
  s.author            = { package['author']['name'] => package['author']['email'] }
  s.license           = package['license']
  s.source            = { :git => "http://192.168.30.4/js/ydk.git", :tag => s.version }
  
  s.source_files = 'Classes/*.{h,m}', 'Classes/Bridge/*.{h,m}', 'Classes/Controllers/*.{h,m}', 'Classes/Helpers/*.{h,m}', 'Classes/Controllers/Managers/*.{h,m}', 'Classes/Controllers/Options/*.{h,m}', 'Classes/Controllers/Protocols/*.{h,m}'
  s.public_header_files = 'Classes/*.h', 'Classes/Bridge/*.h', 'Classes/Controllers/*.h', 'Classes/Helpers/*.h', 'Classes/Controllers/Managers/*.h', 'Classes/Controllers/Options/*.h', 'Classes/Controllers/Protocols/*.h'
  
  s.dependency 'ydk-core'
  s.dependency 'ydk-toolkit'
  s.dependency 'React'
  
end
