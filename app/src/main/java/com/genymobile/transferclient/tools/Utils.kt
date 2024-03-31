package com.genymobile.transferclient.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Base64
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream

object Utils {
    private const val TAG = "Utils"
    fun drawableToBase64(context: Context, drawable: Drawable, compressQuality: Int = 1): String? {
        // 指定一个较小的 Bitmap 尺寸
        val targetWidth = 100
        val targetHeight = 100

        // 创建一个较小尺寸的 Bitmap
        val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)

        // 将 Drawable 绘制到 Bitmap 上
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, targetWidth, targetHeight) // 设置绘制范围为目标尺寸
        drawable.draw(canvas)

        // 压缩 Bitmap
        val compressedBitmap = compressBitmap(bitmap, compressQuality)

        // 将 Bitmap 转换为 Base64 字符串
        val outputStream = ByteArrayOutputStream()
        compressedBitmap?.compress(Bitmap.CompressFormat.PNG, compressQuality, outputStream) // 根据需要选择压缩格式和质量
        val byteArray: ByteArray = outputStream.toByteArray()

        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun compressBitmap(bitmap: Bitmap, compressQuality: Int): Bitmap? {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        options.inSampleSize = calculateInSampleSize(options, 1, 1) // 例如，设置目标宽高为 100x100
        options.inJustDecodeBounds = false
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, compressQuality, outputStream)
        val byteArray: ByteArray = outputStream.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, options)
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }
    fun decodeBase64ToBitmap(base64String: String): ImageBitmap? {
        return try {
            val imageByteArray = Base64.decode(base64String, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            Log.d(TAG, "decodeBase64ToBitmap: $e")
            null
        }
    }

}
// 调用示例
// val base64String = drawableToBase64(context, R.drawable.your_drawable)
