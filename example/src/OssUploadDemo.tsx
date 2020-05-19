import React from 'react'
import { Text, Button, View } from 'react-native'
import { Network } from '../../native-modules'
export default class extends React.Component {
  state = { val: {}, localFilePath: '', imageUrl: '' }
  upload = async () => {
    Network.upload(this.state.localFilePath, progress => {
      console.log('upload progress:', progress.uploadBytes / progress.total)
    })
      .then(info => {
        this.setState({ imageUrl: info.url })
        console.log('upload info: ' + info.url)
      })
      .catch(e => console.log(e))
  }
  download = async () => {
    Network.download(
      'https://cdn-qa.yryz.com/lovelorn/image/ios/201904/BFDF8D75-CDF7-4213-B73A-CD31625778E9.jpg',
      progress => {
        console.log('upload progress:', progress.downloadBytes / progress.total)
      },
    )
      .then(info => {
        this.setState({ localFilePath: info.filePath })
        console.log('download info: ' + info.filePath)
      })
      .catch(e => console.log(e))
  }
  http = () => {
    Network.get('/mall/v1.0/pb/product-categorys/action/list-client')
      .then(data => {
        console.warn('data:', data)
      })
      .catch(error => {
        console.warn('error:', error)
      })
  }
  render() {
    return (
      <View>
        <Button title="upload" onPress={this.upload} />
        <Button title="download" onPress={this.download} />
        <Button title="http" onPress={this.http} />
        <Text>下载后本地地址: {this.state.localFilePath}</Text>
        <Text>上传后网络地址: {this.state.imageUrl}</Text>
      </View>
    )
  }
}
