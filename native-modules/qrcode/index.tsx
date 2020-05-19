import React from 'react'
import { requireNativeComponent, NativeModules, ViewProperties } from 'react-native'

const NativeScannerView = requireNativeComponent('YdkScannerView')

export interface ScannerConfig {
  boxColor: string // 四遍框颜色
  angleColor: string // 四个角颜色
  lineColor: string // 扫描线颜色
  width: number // 扫描框宽
  height: number // 扫描框高
  topOffset: number // 扫描框与上边的距离
}

export const DefaultScannerConfig = {
  boxColor: '#FFF',
  angleColor: '#FFF',
  lineColor: '#FFF',
  width: 300,
  height: 400,
  topOffset: 100,
}

export interface Event<T> {
  nativeEvent: T
}

export interface ScannerProps extends ViewProperties {
  config?: ScannerConfig
  onResult?: (e: Event<ScannerResult>) => void
  onError?: (e: Error) => void
}
export interface ScannerResult {
  codeInfo: string
}

export class ScannerView extends React.Component<ScannerProps> {
  public setFlashLight = () => {
    NativeModules.UIManager.dispatchViewManagerCommand(
      NativeModules.findNodeHandle(this),
      NativeModules.UIManager.YdkScannerView.Commands.setFlashLight,
      [],
    )
  }
  render() {
    let config = { ...DefaultScannerConfig, ...this.props.config }
    return (
      <NativeScannerView
        config={config}
        onResult={this.props.onResult}
        onError={this.props.onError}
        {...this.props}
      />
    )
  }
}
export default ScannerView

export interface CreateQRCodeConfig {
  content: string
  iconUrl?: string
  width: number
  height: number
}

export interface YdkScanner {
  /**
   * 原生页面扫描二维码
   */
  openScan(): Promise<string>
  /**
   * 原生生成二维码
   * @param content 文本内容
   * @param iconUrl icon地址
   */
  createQRCode(reateQRCode: CreateQRCodeConfig): Promise<string>

  /**
   * 解析二维码
   * @param path 二维码本地地址
   */
  decodeQRCode(path: string): Promise<string>
}

const Scanner: YdkScanner = NativeModules.YdkScannerModule

export { Scanner }
