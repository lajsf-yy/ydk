import { NativeModules } from 'react-native'

export interface YdkCaptcha {
  start(phone: string): Promise<string>
  stop(): void
}
const Captcha: YdkCaptcha = NativeModules.YdkCaptchaModule
export default Captcha
