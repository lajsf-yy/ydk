
require "json"

package = JSON.parse(File.read(File.join(__dir__, "../../../package.json")))

Pod::Spec.new do |s|
    
    s.name              = 'libwebp'
    s.summary           = 'Library to encode and decode images in WebP format.'
    s.version           = '1.0.3'
    s.homepage          = 'https://developers.google.com/speed/webp/'
    s.author            = 'Google Inc.'
    s.license           = { :type => 'BSD', :file => 'COPYING' }
    s.source            = { :git => "https://chromium.googlesource.com/webm/libwebp", :tag => 'v1.0.3' }
    
    s.ios.deployment_target = '6.0'
    s.osx.deployment_target = '10.8'
    s.watchos.deployment_target = '2.0'
    s.tvos.deployment_target = '9.0'
    
    s.compiler_flags = '-D_THREAD_SAFE'
    s.requires_arc= false
    
    s.pod_target_xcconfig = { :USER_HEADER_SEARCH_PATHS => "$(inherited) ${PODS_ROOT}/libwebp/ ${PODS_TARGET_SRCROOT}/" }
    s.preserve_paths = 'src'
    s.default_subspecs= ['webp', 'demux', 'mux']
    s.prepare_command= "sed -i.bak \'s/<inttypes.h>/<stdint.h>/g\' \'./src/webp/types.h\'"
    
    s.subspec 'webp' do |sw|
        sw.source_files = 'src/webp/decode.h', 'src/webp/encode.h', 'src/webp/types.h', 'src/webp/mux_types.h', 'src/webp/format_constants.h', 'src/utils/*.{h,c}', 'src/dsp/*.{h,c}', 'src/dec/*.{h,c}', 'src/enc/*.{h,c}'
        sw.public_header_files = 'src/webp/decode.h', 'src/webp/encode.h', 'src/webp/types.h', 'src/webp/mux_types.h', 'src/webp/format_constants.h'
    end
    
    s.subspec 'demux' do |sd|
        sd.source_files = 'src/demux/*.{h,c}', 'src/webp/demux.h'
        sd.public_header_files = 'src/webp/demux.h'
        sd.dependency 'libwebp/webp'
    end
    
    s.subspec 'mux' do |sm|
        sm.source_files = 'src/mux/*.{h,c}', 'src/webp/mux.h'
        sm.public_header_files = "src/webp/mux.h"
        sm.dependency 'libwebp/demux'
    end
    
end
