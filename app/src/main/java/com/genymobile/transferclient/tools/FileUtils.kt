package com.genymobile.transferclient.tools

import android.content.Context
import android.util.Log
import com.genymobile.transferclient.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object FileUtils{
    @JvmStatic
    fun copyAssetsFileToAdbPath(context: Context,fileName:String,targetName:String){
        try {
            // 获取AssetManager实例
            val assetManager = context.assets

            // 打开assets目录下的class.dex文件
            val inputStream = assetManager.open(fileName)

//            val inputStream = context.resources.openRawResource(R.raw.finish)

            // 创建目标目录（如果不存在）
            val targetDirectory = File(context.getFilesDir().absolutePath)
//            val targetDirectory = File("/data/local/tmp")
            if (!targetDirectory.exists()) {
                targetDirectory.mkdirs()
            }

            // 创建目标文件的完整路径
            val targetFile = File(targetDirectory, targetName)

            if (targetFile.exists()){
                Log.i("copyFileToTarget", "删除旧文件")
                targetFile.delete()
            }

            // 创建输出流，用于将文件写入设备文件系统
            val outputStream = FileOutputStream(targetFile)

            // 读取输入流并写入输出流
            val buffer = ByteArray(1024)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
            }

            // 关闭流
            outputStream.flush()
            outputStream.close()
            inputStream.close()

            // 完成后打印确认信息（可选）
            Log.i("copyFileToTarget", "has been copied to " + targetFile.absolutePath)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}