import { NativeEventEmitter, NativeModules } from 'react-native'

export interface Options {
  /**
   * iOS Only
   * true 支持侧滑手势
   * false 不支持侧滑手势
   */
  popGesture?: boolean
}

export interface NavigationRouteStatics {
  registerComponents(components: string[]): Promise<any>
  /**
   * 导航页面push跳转
   * @param componentId 当前页面componentId
   * @param layout 目前页面结构
   */
  push(
    componentId: string,
    layout: {
      componentName: string
      componentId: string
      passProps: Record<string, any>
    },
  ): Promise<any>
  pop(componentId: string): Promise<any>
  popTo(componentId: string): Promise<any>

  /**
   * back to home page, always call after login success
   */
  popToRoot(componentId: string): Promise<any>
  showModal(
    componentId: string,
    layout: {
      componentName: string
      componentId: string
      passProps: Record<string, any>
    },
  ): Promise<any>
  dismissModal(componentId: string): Promise<any>
  setResult(targetComponentId: string, componentId: string, data: any): void
  popToRootAndSwitchTab(componentId: string, tabIndex: number): void
  /**
   * iOS Only
   * 更新侧滑手势等
   */
  mergeOptions(componentId: string, options: Options): void
}
// Route
const NavigationModule: NavigationRouteStatics = NativeModules.YdkNavigationModule

// Event
const eventEmitter: NativeEventEmitter = new NativeEventEmitter(
  NativeModules.YdkNavigationEventEmitter,
)
const registerComponentDidAppearListener = (
  callback: (event: { componentId: string; componentName: string }) => void,
) => {
  return eventEmitter.addListener('ComponentDidAppear', callback)
}
const registerSetResultListener = (
  callback: (event: { componentId: string; data: any }) => void,
) => {
  return eventEmitter.addListener('ComponentReceiveResult', callback)
}
const registerComponentDidDisappearListener = (
  callback: (event: { componentId: string; componentName: string }) => void,
) => {
  return eventEmitter.addListener('ComponentDidDisappear', callback)
}

export default {
  ...NavigationModule,
  registerComponentDidAppearListener,
  registerSetResultListener,
  registerComponentDidDisappearListener,
}
