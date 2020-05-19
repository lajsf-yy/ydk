import React from 'react'

import screens from './screens'
import { createAppContainer } from 'react-navigation'
import { createStackNavigator } from 'react-navigation-stack'
import { AppProvider } from '../../services'

const AppNavigator = createStackNavigator(screens)
const AppContainer = createAppContainer(AppNavigator)
export default class App extends React.Component {
  render() {
    return (
      <AppProvider providers={[]}>
        <AppContainer />
      </AppProvider>
    )
  }
}
