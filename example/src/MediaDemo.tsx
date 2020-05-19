import React from 'react'

import { Text, Button, View, Image } from 'react-native'
import { Album, AlbumType, Location, LocationInfo } from '../../native-modules'
export default class extends React.Component {
  state = { val: {} }
  recognitionQRCode = async () => {
    try {
      let val = await Album.picturePick({
        type: AlbumType.video,
        style: { numColumns: 4, showCamera: false },
        picture: { maxNum: 2, isCrop: false, cropScale: 1.0 },
      })
      this.setState({ val })
    } catch (err) {}
  }

  getLocation = () => {
    Location.getCurrentLocation()
      .then(val => {
        this.setState({ val })
      })
      .catch(e => {
        console.warn('e:', e)
      })
  }

  render() {
    return (
      <View>
        <Text>{JSON.stringify(this.state.val)}</Text>
        <Button title="recognitionQRCode" onPress={this.recognitionQRCode} />
        <Button title="getLocation" onPress={this.getLocation} />
      </View>
    )
  }
}
