import React from 'react'
import { NavigationScreenOptions, NavigationScreenProps } from 'react-navigation'
import { View } from 'react-native'
export default class extends React.Component<NavigationScreenProps<{ url: string }>> {
  static navigationOptions: NavigationScreenOptions = {
    // headerStyle: { backgroundColor: 'white' },
    // headerTransparent: true
  }
  render() {
    return (
      <View style={{ flex: 1 }}>
        {/* <FilePreview url={this.props.navigation.state.params.url} style={{ flex: 1 }} /> */}
      </View>
    )
  }
}
