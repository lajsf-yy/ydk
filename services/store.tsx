import React, { useEffect } from 'react'
import AsyncStorage from '@react-native-community/async-storage'
let persistKeys: string[] = []
let persistStore = new Map<string, any>()

let hasInstall = false
export interface StoreProps<T> {
  storeState: [T, Func<T>]
}
interface WithStore<T> {
  <P>(Component: React.ComponentType<P & StoreProps<T>>): React.ComponentType<P>
  getState?(): T
  setState?: Func<T>
  addListener?(listener: Func<T>): void
  removeListener?(listener: Func<T>): void
}

export function withStore<T extends object>(persistKey: string, initValue: T): WithStore<T> {
  const listeners = new Set<Func<T>>()
  const addListener = (listener: Func<T>) => listeners.add(listener)
  const removeListener = (listener: Func<T>) => listeners.delete(listener)
  const setState: Func<T> = (value: T) => {
    persistStore.set(persistKey, value)
    AsyncStorage.setItem(persistKey, JSON.stringify(value))
    listeners.forEach(setValue => {
      try {
        setValue && setValue(value)
      } catch (ex) {
        console.log(persistKey, ex)
      }
    })
    // console.warn('share state change', value);
  }
  persistKeys.push(persistKey)
  persistStore.set(persistKey, initValue)

  function StoreComponent<P>(Component: React.ComponentType<P & StoreProps<T>>) {
    return class WithStoreComponent extends React.Component<P> {
      constructor(props: P) {
        super(props)
        this.setState = this.setState.bind(this)
      }
      componentDidMount() {
        addListener(this.setState)
      }
      componentWillUnmount() {
        removeListener(this.setState)
      }
      render() {
        return <Component {...this.props} storeState={[persistStore.get(persistKey), setState]} />
      }
    }
  }
  const hoc: WithStore<T> = StoreComponent
  hoc.setState = setState
  hoc.addListener = addListener
  hoc.removeListener = removeListener
  hoc.getState = () => persistStore.get(persistKey)
  return hoc
}
export async function install() {
  if (hasInstall) return
  let values = await AsyncStorage.multiGet(persistKeys)
  for (let [key, value] of values) {
    try {
      if (value) persistStore.set(key, JSON.parse(value))
    } catch (ex) {
      console.error(ex)
    }
  }
  hasInstall = true
}
export const StoreProvider: React.FC<{ onStoreLoaded: () => void }> = props => {
  let [hasLoad, setLoad] = React.useState(hasInstall)
  useEffect(() => {
    if (hasLoad) return
    install().then(() => setLoad(true))
  }, [hasLoad])

  if (!hasLoad) return null
  return <React.Fragment>{props.children}</React.Fragment>
}
