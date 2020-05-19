require "json"
package = JSON.parse(File.read(File.join(__dir__, "../../../package.json")))
Pod::Spec.new do |s|
  s.name             = 'ydk-bugly'
	s.platform    = :ios, "9.0"  
 	s.summary          = package['description']
  s.version          = package['version']
  #s.description      = package['description']
  s.homepage         = package['homepage']
	s.author           = { package['author']['name'] => package['author']['email'] }
  s.license = { :type => 'GPL' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.public_header_files = 'Classes/**/*.h'
  s.dependency 'ydk-core'
	s.dependency 'Bugly'
end
