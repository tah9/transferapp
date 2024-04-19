package com.genymobile.transferclient.tools

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

fun Drawable.toBitmap(): ImageBitmap {
    return if (this is BitmapDrawable && bitmap != null) {
        bitmap as ImageBitmap
    } else {
        // 创建一个新的 Bitmap 并绘制 Drawable 到其中
        Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            .also { bitmap ->
                draw(Canvas(bitmap))
            } as ImageBitmap
    }
}

fun Activity.requestReadWritePermissions() {
    val permissionsToRequest = mutableListOf<String>()

    if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    if (permissionsToRequest.isNotEmpty()) {
        ActivityCompat.requestPermissions(
            this,
            permissionsToRequest.toTypedArray(),
            100
        )
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE),
                200
            )
        }
    }
}