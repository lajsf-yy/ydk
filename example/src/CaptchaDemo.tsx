import React from 'react'
import { Text, Button, View } from 'react-native'
import { Captcha } from '../../native-modules'

export default class CaptchaDemo extends React.Component {
  state = {
    result: '',
  }
  render() {
    return (
      <View style={{ flex: 1, alignItems: 'center' }}>
        <Text>{this.state.result}</Text>
        <Button onPress={this.handlerOnPress} title="验证" />
      </View>
    )
  }

  handlerOnPress = () => {
    Captcha.start('15900000001')
      .then(() => {
        this.setState({ result: 'pass' })
      })
      .catch(e => {
        this.setState({ result: 'no pass' })
        console.log('error:', e)
      })
  }
}
