package ydk.bugly

import ydk.annotations.YdkConfigNode


@YdkConfigNode(name="bugly")
data class BuglyConfig (val appId:String,val appKey:String)