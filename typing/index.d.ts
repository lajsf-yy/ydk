declare module '*.ttf'
declare module 'blueimp-md5'
declare module 'react-native-vector-icons/AntDesign'
type Func<T, P = void> = (t: T) => P
type AsyncFunc<T, P = {}> = (t: T) => Promise<P>
type Action<T> = (t: T) => void
type AsyncAction<T> = (t: T) => Promise<void>
