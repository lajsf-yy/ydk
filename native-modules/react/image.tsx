import React from 'react'
import {
  requireNativeComponent,
  Image,
  StyleSheet,
  ImageProps,
  ImageResizeMode,
  ImageStyle,
  ImageURISource,
  Platform,
} from 'react-native'
const flattenStyle = StyleSheet.flatten
export interface YdkImageProps extends ImageProps {
  defaultSourceResizeMode?: ImageResizeMode
}
const resolveAssetSource = Image.resolveAssetSource

const YdkImageView = requireNativeComponent('YdkImageView')
const YdkImageAndroid: React.FC<YdkImageProps> = props => {
  let source = resolveAssetSource(props.source)
  const defaultSource = resolveAssetSource(props.defaultSource)
  const loadingIndicatorSource = resolveAssetSource(props.loadingIndicatorSource)
  let sources
  let style
  if (source && !source.uri && !Array.isArray(source)) {
    source = null
    sources = source
  }
  if (source && source.uri) {
    style = flattenStyle([styles.base, props.style])
    sources = [{ uri: source.uri }]
  }

  const { onLoadStart, onLoad, onLoadEnd, onError } = props
  const nativeProps = {
    style,
    shouldNotifyLoadEvents: !!(onLoadStart || onLoad || onLoadEnd || onError),
    src: sources,
    /* $FlowFixMe(>=0.78.0 site=react_native_android_fb) This issue was found
     * when making Flow check .android.js files. */
    // headers: (source as any).headers,
    defaultSrc: defaultSource ? defaultSource.uri : null,
    loadingIndicatorSrc: loadingIndicatorSource ? loadingIndicatorSource.uri : null,
  }

  return <YdkImageView {...props} {...nativeProps} />
}

const YdkImageIos: React.FC<YdkImageProps> = props => {
  const source =
    resolveAssetSource(props.source) ||
    ({
      uri: undefined,
      width: undefined,
      height: undefined,
    } as ImageURISource)

  const { width, height } = source
  // $FlowFixMe flattenStyle is not strong enough
  let style = flattenStyle([{ width, height }, styles.base, props.style]) || {}
  let sources = [source]
  const resizeMode = props.resizeMode || 'cover'
  const tintColor = style.tintColor
  return (
    <YdkImageView
      {...props}
      style={style}
      resizeMode={resizeMode}
      tintColor={tintColor}
      source={sources}
    />
  )
}

const styles = StyleSheet.create({
  base: {
    overflow: 'hidden',
  } as ImageStyle,
})
const YdkImage = Platform.OS == 'ios' ? YdkImageIos : YdkImageAndroid
export default YdkImage
