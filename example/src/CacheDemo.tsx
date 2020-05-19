import React from 'react'

import { Button, View, Text } from 'react-native'
import { CacheModule } from '../../native-modules'
import { number } from 'prop-types'

export default class extends React.Component {
  state = { fileSize: number }

  render() {
    return (
      <View style={{ flex: 1 }}>
        <Button
          title="获取缓存大小"
          onPress={() => {
            CacheModule.getCacheSize().then(result => {
              console.warn(result)
              this.setState({
                fileSize: result.cacheSize,
              })
            })
          }}
        />
        <Button
          title="清楚缓存大小"
          onPress={() => {
            CacheModule.clearCache().then(() => {
              console.warn('清楚缓存完成')
              CacheModule.getCacheSize().then(result => {
                this.setState({
                  fileSize: result.cacheSize,
                })
              })
            })
          }}
        />
        <Text>{this.state.fileSize}</Text>
      </View>
    )
  }
}
