package ydk.react

import android.app.Application
import android.graphics.Bitmap
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.facebook.cache.disk.DiskCacheConfig
import com.facebook.common.internal.Supplier
import java.io.File


object FrescoConfig {

    fun getConfig(instance: Application,maxCacheSize : Long = 20*1024*1024L,dirName:String = "imageCache") : ImagePipelineConfig {
        val diskCacheConfig = DiskCacheConfig.newBuilder(instance)
                .setMaxCacheSize(maxCacheSize)//最大缓存
                .setBaseDirectoryName(dirName)//子目录
                .setBaseDirectoryPathSupplier(object : Supplier<File> {
                    override fun get(): File {
                        return instance.cacheDir//还是推荐缓存到应用本身的缓存文件夹,这样卸载时能自动清除,其他清理软件也能扫描出来
                    }
                })
                .build()
        return ImagePipelineConfig.newBuilder(instance)
                .setMainDiskCacheConfig(diskCacheConfig)
                .setDownsampleEnabled(true)
                //Downsampling，要不要向下采样,它处理图片的速度比常规的裁剪scaling更快，
                // 并且同时支持PNG，JPG以及WEP格式的图片，非常强大,与ResizeOptions配合使用
                .setBitmapsConfig(Bitmap.Config.RGB_565)
                //如果不是重量级图片应用,就用这个省点内存吧.默认是RGB_888
                .build()
    }

}