import React from 'react'
import {
  requireNativeComponent,
  ViewProperties,
  NativeModules,
  findNodeHandle,
  Platform,
} from 'react-native'

export interface VideoPlayerSource {
  uri: string // 视频播放链接地址
  videoGravity?: 'aspect' | 'aspectFill' | 'resize'
}

export interface Error {
  code: number
  domain: string
}
export interface Event<T> {
  nativeEvent: T
}

export interface VideoPlayerProps extends ViewProperties {
  source?: VideoPlayerSource

  onReadyToPlay?: () => void
  onLoad?: (loading: boolean) => void
  onEnd?: () => void
  onError?: (e: Error) => void
  onProgress?: (progress: VideoProgress) => void
}
export interface VideoProgress {
  progress: number
  duration: number
}

const NativeVideoPlayerView = requireNativeComponent('YdkVideoPlayView')

export default class VideoPlayView extends React.Component<VideoPlayerProps> {
  private _loading = false
  public play = () => {
    this._sendToNativeCommand('start')
  }

  public pause = () => {
    this._sendToNativeCommand('pause')
  }

  public seekToTime = (time: number) => {
    this._sendToNativeCommand('seekToTime', [time])
  }

  private _sendToNativeCommand(commandName: string, params?: [any]) {
    if (Platform.OS === 'ios') {
      if (params) {
        NativeModules['YdkVideoPlayView'][commandName](findNodeHandle(this._videoRef), ...params)
      } else {
        NativeModules['YdkVideoPlayView'][commandName](findNodeHandle(this._videoRef))
      }
    } else {
      NativeModules.UIManager.dispatchViewManagerCommand(
        findNodeHandle(this._videoRef),
        NativeModules.UIManager.YdkVideoPlayView.Commands[commandName],
        params ? params : [],
      )
    }
  }

  onReadyToPlay = (e: Event<void>) => {
    this.props.onReadyToPlay && this.props.onReadyToPlay()
  }

  onVideoLoad = (e: Event<void>) => {
    this._loading = true
    this.props.onLoad && this.props.onLoad(true)
  }

  onVideoLoadEnd = (e: Event<void>) => {
    this._loading = false
    this.props.onLoad && this.props.onLoad(false)
  }

  onPlayEnd = (e: Event<void>) => {
    this.props.onEnd && this.props.onEnd()
  }

  onPlayError = (e: Event<Error>) => {
    this.props.onError && this.props.onError(e.nativeEvent)
  }

  onVideoProgress = (e: Event<VideoProgress>) => {
    this.props.onProgress && this.props.onProgress(e.nativeEvent)
    if (this._loading && this.props.onLoad) this.props.onLoad(false)
  }

  private _videoRef: React.Component
  render() {
    return (
      <NativeVideoPlayerView
        {...this.props}
        ref={r => (this._videoRef = r)}
        onReadyToPlay={this.onReadyToPlay}
        onVideoLoad={this.onVideoLoad}
        onVideoLoadEnd={this.onVideoLoadEnd}
        onPlayEnd={this.onPlayEnd}
        onPlayError={this.onPlayError}
        onVideoProgress={this.onVideoProgress}
      />
    )
  }
}
