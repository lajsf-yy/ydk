require "json"

package = JSON.parse(File.read(File.join(__dir__, "../../../package.json")))

Pod::Spec.new do |s|
  
  s.name              = 'ydk-toolkit'
  s.platform          = :ios, "9.0"
  s.summary           = package['description']
  s.version           = package['version']
  #s.description      = package['description']
  s.homepage          = package['homepage']
  s.author            = { package['author']['name'] => package['author']['email'] }
  s.license           = package['license']
  s.source            = { :git => "http://192.168.30.4/js/ydk.git", :tag => s.version }

  s.source_files = 'Classes/**/*.{h,m}'
  s.public_header_files = 'Classes/*.h', 'Classes/**/*.h'

  s.framework  = "UIKit"

end
