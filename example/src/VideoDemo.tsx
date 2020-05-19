import React from 'react'
import { Text, Button, View } from 'react-native'
import { Video, VideoProgress } from '../../native-modules'

export default class VideoDemo extends React.Component {
  state = {
    playerTime: '',
    isPlaying: false,
  }
  private videoRef: Video
  render() {
    // let uri =
    // 'http://cdn-qa.yryz.com/lovelorn/video/android/20195/af742cda-de40-45d9-a954-62ffaa7c2382.mp4'
    let uri =
      'https://cdn-s.lajsf.com/nutrition-plan/video/default/201910/472412805160960.mp4?auth_key=1571111455962-97-0-a6febd827f28b6238923df1357b5c4fa'
    // let uri = 'https://cdn-test.lajsf.com/nutrition-plan/video/default/201909/461587190988800.mp4'
    return (
      <View style={{ flex: 1, alignItems: 'center' }}>
        <Text>{this.state.playerTime}</Text>
        <Button title="播放" onPress={this.handlerOnPress}></Button>
        <Button title="播放音频" onPress={this.handlerOnPress}></Button>
        <Video
          style={{ flex: 1, width: '100%', backgroundColor: 'red' }}
          ref={r => (this.videoRef = r)}
          source={{ uri }}
          onReadyToPlay={this.onReadyToPlay}
          onProgress={this.onVideoProgress}
          onLoad={this.onVideoLoad}
          onEnd={this.onPlayEnd}
          onError={this.onPlayError}
        />
      </View>
    )
  }

  handlerOnPress = () => {
    // let { isPlaying } = this.state
    // isPlaying ? this.videoRef.pause() : this.videoRef.play()
    // this.setState({ isPlaying: !isPlaying })
    this.videoRef.play()
  }

  onPlayEnd = () => {
    this.setState({ isPlaying: false })
  }

  onPlayError = error => {
    console.log('onPlayError ' + error)
    this.setState({ isPlaying: false })
  }

  onVideoLoad = (loading: boolean) => {
    console.log('onVideoLoad', loading)
  }

  onVideoProgress = (progress: VideoProgress) => {
    this.setState({
      playerTime: `总时长: ${Math.round(progress.duration)} S, 当前时长: ${Math.round(
        progress.progress,
      )} S.`,
    })
  }

  onReadyToPlay = () => {
    this.videoRef.play()
    this.setState({ isPlaying: true })
  }
}
