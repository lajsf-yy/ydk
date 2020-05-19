import React, { Component } from 'react'
import { Platform, StyleSheet, Text, View, Button, ScrollView } from 'react-native'
import { NavigationScreenProps, NavigationScreenOptions } from 'react-navigation'
import screens from './screens'
import { Scanner } from '../../native-modules'

//https://cdn-mo.yryz.com/lovelorn/common/20190108/demo.xlsx
//https://cdn-mo.yryz.com/lovelorn/common/20190108/demo.pdf
export default class HomeScreen extends React.Component<NavigationScreenProps> {
  async componentDidMount() {
    // let pdf = await FSManager.copyAssetsIos("demo", "pdf")
    // console.warn('pdf', pdf)
    // let url = await HttpManager.uploadFile({ objectKey: 'lovelorn/common/20190108/demo.pdf', uploadFile: pdf })
    // console.warn('pdf url', url)
    // let xlsx = await FSManager.copyAssetsIos("demo", "xlsx")
    // console.warn('xlsx', xlsx)
    // let xurl = await HttpManager.uploadFile({ objectKey: 'lovelorn/common/20190108/demo.xlsx', uploadFile: xlsx })
    // console.warn('xlsx url', xurl)
  }
  static navigationOptions: NavigationScreenOptions = {
    // headerStyle: { backgroundColor: 'white' },
    // headerTransparent: true
  }
  uploadPdf = () => {}
  goto = (screen: string) => () => {
    this.props.navigation.push(screen)
  }
  render() {
    let Item: React.FC<{ screen: string }> = props => (
      <View style={styles.button}>
        <Button title={props.screen} onPress={this.goto(props.screen)} />
      </View>
    )
    return (
      <ScrollView style={styles.container}>
        {Object.keys(screens)
          .filter(s => s !== 'FilePreviewDemo')
          .map(s => (
            <Item key={s} screen={s} />
          ))}
        <View style={styles.button}>
          <Button
            title="浏览pdf"
            onPress={() =>
              this.props.navigation.push('FilePreviewDemo', {
                url: 'https://cdn-mo.yryz.com/lovelorn/common/20190108/demo.pdf',
              })
            }
          />
        </View>
        <View style={styles.button}>
          <Button
            title="浏览xlsx"
            onPress={() =>
              this.props.navigation.push('FilePreviewDemo', {
                url: 'https://cdn-mo.yryz.com/lovelorn/common/20190108/demo.xlsx',
              })
            }
          />
        </View>
        <View style={styles.button}>
          <Button title="network" onPress={() => this.props.navigation.push('OssUploadDemo')} />
        </View>
        <View style={styles.button}>
          <Button title="share" onPress={() => this.props.navigation.push('ShareDemo')} />
        </View>
        <View style={styles.button}>
          <Button title="MediaDemo" onPress={() => this.props.navigation.push('MediaDemo')} />
        </View>
        <View style={styles.button}>
          <Button title="CaptchaDemo" onPress={() => this.props.navigation.push('CaptchaDemo')} />
        </View>
        <View style={styles.button}>
          <Button title="VideoDemo" onPress={() => this.props.navigation.push('VideoDemo')} />
        </View>
        <View style={styles.button}>
          <Button title="ScannerDemo" onPress={() => this.props.navigation.push('ScannerDemo')} />
        </View>
        <View style={styles.button}>
          <Button
            title="ScannerDemo2"
            onPress={() => {
              Scanner.openScan()
                .then(result => {
                  console.log('openScan result : ' + result)
                })
                .catch(error => {})
            }}
          />
        </View>
      </ScrollView>
    )
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,

    backgroundColor: '#F5FCFF',
  },
  button: {
    margin: 5,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
})
