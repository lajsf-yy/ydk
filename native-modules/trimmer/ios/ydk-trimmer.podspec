require "json"

package = JSON.parse(File.read(File.join(__dir__, "../../../package.json")))

Pod::Spec.new do |s|
  
  s.name              = 'ydk-trimmer'
  s.platform          = :ios, "9.0"
  s.summary           = package['description']
  s.version           = package['version']
  s.homepage          = package['homepage']
  s.author            = { package['author']['name'] => package['author']['email'] }
  s.license           = package['license']
  s.source            = { :git => "http://192.168.30.4/js/ydk.git", :tag => s.version }
  
  s.subspec 'Core' do |ss|
    ss.source_files = 'Classes/*.{h,m}', 'Classes/Trimmer/*.{h,m}', 'Classes/BaseView/*.{h,m}'
    ss.public_header_files = 'Classes/*.h', 'Classes/Trimmer/*.h', 'Classes/BaseView/*.h'
  end

  s.framework  = "AVKit"
  s.dependency 'ydk-toolkit'

end
