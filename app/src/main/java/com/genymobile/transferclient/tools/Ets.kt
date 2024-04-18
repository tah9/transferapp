package com.genymobile.transferclient.tools

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap

fun Drawable.toBitmap(): ImageBitmap {
    return if (this is BitmapDrawable && bitmap != null) {
        bitmap as ImageBitmap
    } else {
        // 创建一个新的 Bitmap 并绘制 Drawable 到其中
        Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888).also { bitmap ->
            draw(Canvas(bitmap))
        } as ImageBitmap
    }
}