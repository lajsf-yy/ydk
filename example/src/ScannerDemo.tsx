import React from 'react'
import { Text, Button, View, Dimensions } from 'react-native'
import { ScannerView } from '../../native-modules'

export default class ScannerDemo extends React.Component {
  state = {
    playerTime: '',
    isPlaying: false,
  }
  render() {
    return (
      <View style={{ flex: 1 }}>
        <ScannerView
          style={{ flex: 1 }}
          onResult={e => console.warn(Object.keys(e.nativeEvent.codeInfo))}
          onError={e => console.warn('Error: ' + e)}
        />
      </View>
    )
  }
}
