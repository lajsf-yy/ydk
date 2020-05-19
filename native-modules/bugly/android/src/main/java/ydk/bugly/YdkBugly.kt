package ydk.bugly

import android.app.Application
import com.tencent.bugly.Bugly
import ydk.annotations.YdkModule

@YdkModule
class YdkBugly(var application: Application,var buglyConfig: BuglyConfig) {
    init {
        Bugly.init(application,buglyConfig.appId,BuildConfig.DEBUG)
    }

}