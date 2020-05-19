import {
  NativeModules,
  NativeEventEmitter,
  EventSubscriptionVendor,
  EmitterSubscription,
} from 'react-native'
import { string } from 'prop-types'

export interface UploadInfo {
  url: string
  localFile: string
  width?: number
  height?: number
}
export interface DownloadInfo {
  url: string
  fileName: string
  filePath: string
}
export interface UploadProgress {
  total: number
  uploadBytes: number
}
export interface DownloadProgress {
  total: number
  downloadBytes: number
}

const NetworkNative: any = NativeModules.YdkNetworkModule
const NetworkNativeEmitter = new NativeEventEmitter(NetworkNative)

export const get = (url: string, parameters?: object): Promise<any> => {
  return NetworkNative.get(url, parameters).catch((ex: any) => {
    console.warn(url, parameters, ex.userInfo)
    throw ex
  })
}

export const post = (url: string, parameters?: object): Promise<any> => {
  return NetworkNative.post(url, parameters).catch((ex: any) => {
    console.warn(url, parameters, ex.userInfo)
    throw ex
  })
}

export const del = (url: string, parameters?: object): Promise<any> => {
  return NetworkNative.delete(url, parameters).catch((ex: any) => {
    console.warn(url, parameters, ex.userInfo)
    throw ex
  })
}

export const put = (url: string, parameters?: object, data?: any): Promise<any> => {
  return NetworkNative.put(url, parameters).catch((ex: any) => {
    console.warn(url, parameters, ex.userInfo)
    throw ex
  })
}

export const upload = (filePath: string, uploadProgress?: (progress: UploadProgress) => void) => {
  return _upload(filePath, null, uploadProgress)
}

export const uploadHeadImg = (
  filePath: string,
  uploadProgress?: (progress: UploadProgress) => void,
) => {
  return _upload(filePath, 'head', uploadProgress)
}

export const download = async (
  downloadUrl: string,
  downloadProgress?: (progress: DownloadProgress) => void,
) => {
  let subscription: EmitterSubscription
  if (downloadProgress) {
    subscription = NetworkNativeEmitter.addListener('downloadProcess', progress => {
      let { url, total, downloadBytes } = progress
      if (downloadUrl === url) {
        downloadProgress(progress)
      }
    })
  }
  let downloadInfo: DownloadInfo
  try {
    downloadInfo = await NetworkNative.download(downloadUrl)
  } catch (e) {
    throw e
  } finally {
    subscription && subscription.remove()
  }
  return downloadInfo
}

// fileType: head（头像），image（图片），audio（音频），video（视频）
const _upload = async (
  uploadFilePath: string,
  fileType: string,
  uploadProgress?: (progress: UploadProgress) => void,
) => {
  let subscription: EmitterSubscription
  if (uploadProgress) {
    subscription = NetworkNativeEmitter.addListener('uploadProcess', progress => {
      let { filePath, total, uploadBytes } = progress
      if (uploadFilePath === filePath) {
        uploadProgress(progress)
      }
    })
  }

  let uploadInfo: UploadInfo
  try {
    uploadInfo = await NetworkNative.upload(uploadFilePath, fileType)
  } catch (e) {
    throw e
  } finally {
    subscription && subscription.remove()
  }
  return uploadInfo
}

export default { upload, uploadHeadImg, download, get, post, del, put }
