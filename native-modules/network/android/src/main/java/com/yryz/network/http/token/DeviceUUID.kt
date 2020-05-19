package com.yryz.network.http.token

import android.content.Context
import android.os.Environment
import ydk.core.utils.FileUtils
import java.util.*
import java.io.*


object DeviceUUID {

    const val SerialFileName = ".serialInfo"

    var deviceId = ""

    fun loadDeviceId():String {
        return deviceId
    }

    fun init(context: Context) {
        var serialFile = File(context.getDir("devId", Context.MODE_PRIVATE), SerialFileName)
//        var targetExt = File(context.getExternalFilesDir("nutritionPlan"), SerialFileName)
        var targetExt = File(Environment.getExternalStorageDirectory(), SerialFileName)

        if(serialFile.exists()) {
            deviceId = loadContent(serialFile)
            if(!targetExt.exists()) {
                tryBackUpDeviceId(serialFile.absolutePath, targetExt.absolutePath)
            }
            return
        }

        if(targetExt.exists()) {
            deviceId = loadContent(targetExt)
            tryBackUpDeviceId(targetExt.absolutePath, serialFile.absolutePath)
            return
        }

        deviceId = UUID.randomUUID().toString()
        Thread {
            var target = FileUtils.create(serialFile.absolutePath)
            try {
                val fw = FileWriter(target)
                fw.write(deviceId)
                fw.flush()
                fw.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            tryBackUpDeviceId(serialFile.absolutePath, targetExt.absolutePath)
        }.start()
    }

    fun tryBackUpDeviceId(from:String, to:String) {
        Thread {
            try {
                FileUtils.copy(from, to)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun loadContent(file:File):String{
        var content = ""
        try {
            val instream = FileInputStream(file)
            val buffreader = BufferedReader(InputStreamReader(instream, "UTF-8"))

            var line:String? = ""
            while (true) {
                line = buffreader.readLine()
                if (null == line) {
                    break
                }
                content += line!! + "\n"
            }
            buffreader.close()
            instream.close()
        } catch (e: java.io.FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return content.trim()

    }

}