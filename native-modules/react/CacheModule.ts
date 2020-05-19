import { NativeModules } from 'react-native';

const YdkCacheModule = NativeModules.YdkCacheModule;
export default class CacheModule {
  static getCacheSize(): Promise<{ cacheSize: number }> {
    return YdkCacheModule.getCacheSize();
  }

  static clearCache(): Promise<void> {
    return YdkCacheModule.clearCache();
  }
}
