import React from 'react'
import { Text, Button, View } from 'react-native'
import { Audio, AudioPlayerProgress } from '../../native-modules'

export default class AudioDemo extends React.Component {
  state = {
    playerTime: '',
    isPlaying: false,
  }
  audioRef = React.createRef<Audio>()
  render() {
    let url =
      'https://yryz-resources-mo.oss-cn-hangzhou.aliyuncs.com/audio/opus/254C8020-37B1-44D0-8A9D-86089F00CD87_iOS.mp3'
    return (
      <View style={{ flex: 1, alignItems: 'center' }}>
        <Text>{this.state.playerTime}</Text>
        <Button onPress={this.handlerOnPress} title="播放" />
        <Audio
          ref={this.audioRef}
          onProgress={this.onAudioProgress}
          onPlayStatusChange={this.onPlayStatusChange}
          url={url}
        />
      </View>
    )
  }
  onAudioProgress = (progress: AudioPlayerProgress) => {
    this.setState({
      playerTime: `总时长: ${Math.round(progress.duration)} S, 当前时长: ${Math.round(
        progress.progress,
      )} S.`,
    })
  }

  onPlayStatusChange = (isPlaying: boolean) => {
    this.setState({ isPlaying })
    console.log('播放状态：', isPlaying ? '正在播放' : '停止播放')
  }

  handlerOnPress = () => {
    let { isPlaying } = this.state
    isPlaying ? this.audioRef.current.pause() : this.audioRef.current.play()
  }
}
