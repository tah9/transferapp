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
    fun drawableToBase64(context: Context, drawable: Drawable): String? {
        // 获取 Drawable 的宽高
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight

        // 创建一个与 Drawable 大小相匹配的 Bitmap
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // 将 Drawable 绘制到 Bitmap 上
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        // 将 Bitmap 转换为 Base64 字符串
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream) // 根据需要选择压缩格式和质量
        val byteArray: ByteArray = outputStream.toByteArray()

        return Base64.encodeToString(byteArray, Base64.DEFAULT)
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
