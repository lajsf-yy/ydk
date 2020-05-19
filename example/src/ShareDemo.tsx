import React from 'react'
import { Button, View } from 'react-native'
import { Share } from '../../native-modules'
import { SharePlatform, ShareContent } from 'native-modules/share'

export default class extends React.Component {
  state = { val: {}, localFilePath: '', imageUrl: '' }
  share = (platform: SharePlatform) => {
    let shareContent: ShareContent = {
      type: 'auto',
      title: 'title',
      content: 'content',
      // url: 'https://www.baidu.com/',
      imgUrl:
        'https://cdn-qa.yryz.com/lovelorn/html/android/20191/416612c2-164b-41c6-81b6-437a08779f03.jpg?x-oss-process=image/resize,m_lfit,limit_0,h_0,w_400',
    }
    Share.share(platform, shareContent)
      .then(() => {
        console.log('分享成功')
      })
      .catch(() => {
        console.log('分享失败')
      })
  }

  getPlatform = () => {
    Share.getInstallPlatforms().then(platforms => {
      console.log('platforms:', platforms)
    })
  }

  render() {
    return (
      <View>
        <Button title="sina" onPress={() => this.share('sinaWeibo')} />
        <Button title="qq" onPress={() => this.share('qq')} />
        <Button title="wechat" onPress={() => this.share('weChat')} />
        <Button title="获取平台" onPress={() => this.getPlatform()} />
      </View>
    )
  }
}
