import React from 'react'
import { StoreProvider } from './store'
interface Props {
  providers: React.ComponentType[]
  onload?: () => void
}
const AppProvider: React.FC<Props> = props => {
  let { providers = [], children } = props
  providers = [...providers]
  const createProvider = () => {
    for (let i = providers.length - 1; i >= 0; i--) {
      let Provider = providers[i]
      children = <Provider>{children}</Provider>
    }
    return <React.Fragment>{children}</React.Fragment>
  }

  return <StoreProvider onStoreLoaded={props.onload}>{createProvider()}</StoreProvider>
}
export default AppProvider
