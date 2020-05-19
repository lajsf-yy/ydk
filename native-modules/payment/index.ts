import { NativeModules } from 'react-native'

export type PayChannelType = '1' | '2' | '3'
export type PayExtParams = AlipayParams | WechatParams | ApplePaySKParams
export interface PayParams {
  payChannel: PayChannelType
  ext: PayExtParams
}
export interface AlipayParams {
  orderStr: string
}
export interface WechatParams {
  appid: string
  partnerid: string
  prepayid: string
  noncestr: string
  timestamp: string
  package: string
  sign: string
}
export interface ApplePaySKParams {
  productIdentifier: string
  id: string // 唯一标识
}

export interface ApplePaySKResult {
  receipt?: string
  productIdentifier?: string
  transactionIdentifier?: string
  id?: string
}

export type PaymentResult = ApplePaySKResult
export interface YdkPayment {
  pay(params: PayParams): Promise<PaymentResult>
}

export const PaymentPurchasedEventType = 'paymentPurchased'

const Payment: YdkPayment = NativeModules.YdkPaymentModule

export default Payment
