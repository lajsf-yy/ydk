import { NativeModules } from 'react-native'
export type SharePlatform = 'qq' | 'qZone' | 'weChat' | 'weChatMoment' | 'sinaWeibo'
export type ShareContentType = 'auto' | 'image' | 'audio' | 'video'
export interface ShareContent {
  type?: ShareContentType
  title?: string
  content?: string
  url?: string
  imgUrl?: string

  // 小程序参数
  path?: string
  thumbImage?: string
  hdThumbImage?: string
  miniProgramType?: 0 | 1 | 2
}
export interface AuthorizeLoginInfo {
  token: string
  userId: string
  userName: string
  userIcon: string
  userGender: string
}
export interface YdkShare {
  share(platform: SharePlatform, shareContent: ShareContent): Promise<void>
  authorizeLogin(platform: SharePlatform): Promise<AuthorizeLoginInfo>
  authorize(platform: SharePlatform): Promise<{ code: string }>
  getInstallPlatforms(): Promise<SharePlatform[]>
}
const Share: YdkShare = NativeModules.YdkShareModule
export default Share
