import { NativeModules } from 'react-native'

export interface LocationInfo {
  longitude: number
  latitude: number

  provinceName?: string
  cityName?: string
  regionName?: string
  addressName?: string
  name?: string
  cityCode?: string
  adCode?: string
}

export interface YdkLocation {
  getCurrentLocation(): Promise<LocationInfo>
}

const Location: YdkLocation = NativeModules.YdkLocationModule

export default Location
