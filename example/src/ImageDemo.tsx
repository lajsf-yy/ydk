import React from 'react'
// import { Text, Image } from 'react-native';
import Image from '../../native-modules/react/image'
import { ScrollView } from 'react-native'
let placeholder = [require('./assets/type_logo-l.png')]
export default class extends React.Component {
  renderImages = () => {
    let arr = []
    for (let i = 0; i < 10; i++) {
      arr.push(
        <Image
          key={i.toString()}
          source={{
            uri:
              'http://cdn-qa.yryz.com/lovelorn/image/android/20193/e2ab5d2d-974b-4784-8a20-fc58a98d5452.png',
          }}
          // defaultSource={require('./assets/type_logo-l.png')}
          defaultSource={placeholder[0]}
          resizeMode="cover"
          style={{ height: 300, width: 412, backgroundColor: '#f1f1f1' }}
        />,
      )
    }
    return arr
  }
  render() {
    return <ScrollView>{this.renderImages()}</ScrollView>
  }
}
