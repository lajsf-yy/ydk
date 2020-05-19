import { NativeModules } from 'react-native'

export interface YdkTrack {
  setEvent(eventName: string, eventData: object): Promise<void>
  startTrack(eventName: string): Promise<void>
  endTrack(eventName: string, eventData: object): Promise<void>
  identify(uid: string, eventData: object): Promise<void>
}
const Track: YdkTrack = NativeModules.YdkTrackModule
export default Track
