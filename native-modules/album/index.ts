import { NativeModules } from 'react-native'

export enum AlbumType {
  all = 0,
  image = 1,
  video = 2,
}

export interface AlbumConfigStyle {
  numColumns: number
  showCamera: boolean
}

export interface AlbumConfigPicture {
  maxNum: number
  isCrop: boolean
  cropScale: number
}

export interface AlbumConfig {
  type: AlbumType
  style?: AlbumConfigStyle
  picture?: AlbumConfigPicture
}

export interface VideoInfo {
  filePath: string // 视频路径
  thumbnailPath: string // 视频缩略图路径
  duration: number // 视频时长（秒）
  size: number // 视频大小（KB）
}

export interface AlbumPickResult {
  type: AlbumType
  images?: string[]
  videos?: VideoInfo[]
}

export const DefaultAlbumConfigStyle: AlbumConfigStyle = { numColumns: 4, showCamera: true }

export const DefaultAlbumConfigPicture: AlbumConfigPicture = {
  maxNum: 9,
  isCrop: false,
  cropScale: 1.0,
}

export interface YdkAlbum {
  picturePick(config: AlbumConfig): Promise<AlbumPickResult>
  photoTakenWithRecord(type: AlbumType): Promise<AlbumPickResult>
}

const Album: YdkAlbum = NativeModules.YdkAlbumModule

export default Album
