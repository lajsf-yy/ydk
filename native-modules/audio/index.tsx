import React from 'react'
import { NativeModules, NativeEventEmitter, EventSubscriptionVendor, Image } from 'react-native'

interface AudioPlayer extends EventSubscriptionVendor {
  play(options: { url: string; tagId: number; from?: string }): void
  pause(options: { tagId: number }): void
  resume(options: { tagId: number }): void
  stop(options: { tagId: number }): void
  // 跳转指定时间
  seekToTime(options: { time: number; tagId: number }): void
}

export interface AudioProgress {
  progress: number // 当前时间
  duration: number // 总时长
  playableDuration: number // 可播时长
  tagId: number
}
export const AudioPlayer: AudioPlayer = NativeModules.YdkAudioPlayerModule
export const AudioPlayerEmitter = new NativeEventEmitter(AudioPlayer)
export interface AudioProps {
  autoPlay?: boolean
  onProgress?: (progress: AudioProgress) => void
  onChange: (isPlaying: boolean) => void
  onEnd?: () => void
  onLoad?: (loading: boolean) => void
  onError?: (data: any) => void
  onReadyToPlay?: (data: { tagId: number }) => void
  url: string | number
}
let audioMap: Map<number, Audio> = new Map()
AudioPlayerEmitter.addListener('OnAudioLoad', (data: { tagId: number }) => {
  let audio = audioMap.get(data.tagId)
  audio && audio.props.onLoad && audio.props.onLoad(true)
})

AudioPlayerEmitter.addListener('OnAudioLoadEnd', (data: { tagId: number }) => {
  let audio = audioMap.get(data.tagId)
  audio && audio.props.onLoad && audio.props.onLoad(false)
})

AudioPlayerEmitter.addListener('OnAudioError', (data: { tagId: number }) => {
  let audio = audioMap.get(data.tagId)
  audio && audio.props.onError && audio.props.onError(data)
})

AudioPlayerEmitter.addListener('OnAudioProgress', (progress: AudioProgress) => {
  let audio = audioMap.get(progress.tagId)
  if (audio) {
    audio.progress = progress
    audio.props.onProgress && audio.props.onProgress(progress)
  }
})

AudioPlayerEmitter.addListener('OnAudioEnd', (options: { tagId: number }) => {
  let audio = audioMap.get(options.tagId)
  if (audio) {
    audio.isPlaying = false
    audio.progress.progress = 0
    audio.props.onChange(false)
    audio.props.onEnd && audio.props.onEnd()
  }
})
AudioPlayerEmitter.addListener('OnPlaybackStalled', (options: { tagId: number }) => {
  let audio = audioMap.get(options.tagId)
  audio && audio.onPlaybackStalled()
})
AudioPlayerEmitter.addListener('OnReadyToPlay', (options: { tagId: number }) => {
  let audio = audioMap.get(options.tagId)
  audio && audio.props.onReadyToPlay && audio.props.onReadyToPlay(options)
})

export default class Audio extends React.Component<AudioProps> {
  tagId = new Date().getTime()
  isPlaying = false
  progress: AudioProgress = {
    progress: 0,
    duration: 0,
    playableDuration: 0,
    tagId: this.tagId,
  }
  private hasPlay = false

  onPlaybackStalled = () => {
    this.hasPlay = false
    this.isPlaying = false
    this.props.onChange && this.props.onChange(this.isPlaying)
  }
  // 跳转指定时间
  seekToTime = (time: number) => {
    this.progress.progress = time
    this.play()
  }
  pause = () => {
    if (this.isPlaying) {
      AudioPlayer.pause({ tagId: this.tagId })
      this.isPlaying = false
      this.props.onChange(this.isPlaying)
    }
  }

  play = async () => {
    let { url } = this.props
    if (!url) return
    if (typeof url === 'number') {
      const source = Image.resolveAssetSource(url)
      url = source.uri || ''
      if (url && url.match(/^\//)) {
        url = `file://${url}`
      }
    }
    let time = Math.round(this.progress.progress)

    // 如果正在播放，则直接跳转到指定时间
    if (this.isPlaying) {
      AudioPlayer.seekToTime({ time, tagId: this.tagId })
    } else {
      //如果没有播放过,则开始播放，并跳到指定时间
      if (!this.hasPlay) {
        await AudioPlayer.play({ url, tagId: this.tagId })
        if (time > 0) {
          setTimeout(() => {
            AudioPlayer.seekToTime({ time, tagId: this.tagId })
          }, 100)
        }
      } else {
        //暂定播放
        AudioPlayer.seekToTime({ time, tagId: this.tagId })
        AudioPlayer.resume({ tagId: this.tagId })
      }
      this.hasPlay = true
      this.isPlaying = true
      this.props.onChange(this.isPlaying)
    }
  }

  componentDidMount() {
    audioMap.set(this.tagId, this)
    this.props.autoPlay && this.play()
  }
  componentWillUnmount() {
    this.isPlaying && AudioPlayer.stop({ tagId: this.tagId })
    audioMap.delete(this.tagId)
  }
  componentDidUpdate(prevProps: AudioProps) {
    if (this.props.url && prevProps.url !== this.props.url) {
      this.hasPlay = false
      this.isPlaying = false
      this.props.autoPlay && this.play()
    }
  }
  shouldComponentUpdate(nextProps: AudioProps) {
    return nextProps.url !== this.props.url
  }
  render() {
    return <React.Fragment></React.Fragment>
  }
}
