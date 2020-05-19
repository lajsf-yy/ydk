package ydk.navigation.react

import android.os.Bundle
import com.facebook.react.bridge.ReadableMap

data class PushOptionsData(val componentName:String, val componentId:String, val props:Bundle)

data class ModalOptionsData(val componentName:String, val props:ReadableMap)