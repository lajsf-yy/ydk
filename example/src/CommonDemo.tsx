import React from 'react'
import { Text, Button, View } from 'react-native'
export default class extends React.Component {
  state = { val: {} }
  getDeviceInfo = async () => {
    try {
      // console.warn(Common)
      // let val = await Common.getDeviceInfo()
      // this.setState({ val })
    } catch (err) {
      console.error(err)
    }
  }
  render() {
    return (
      <View>
        <Text>{JSON.stringify(this.state.val)}</Text>
        <Button title="getDeviceInfo" onPress={this.getDeviceInfo} />
      </View>
    )
  }
}
