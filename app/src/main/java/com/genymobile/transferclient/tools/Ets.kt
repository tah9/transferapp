package com.genymobile.transferclient.tools

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
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

@SuppressLint("Range")
fun Uri.getUriInfo(context: Context): Pair<Long, String> {
    val TAG = "getUriInfo"
    var size: Long = -1
    var name: String? = null
    val projection =
        arrayOf(MediaStore.Files.FileColumns.DISPLAY_NAME, MediaStore.Files.FileColumns.SIZE)

    // 查询指定Uri的数据
    val cursor: Cursor? = context.contentResolver.query(this, projection, null, null, null)
    if (cursor != null) {
        try {
            // 如果查询结果中有数据，则移动游标到第一条记录
            if (cursor.moveToFirst()) {
                for (columnName in cursor.columnNames) {
                    Log.d(TAG, "getUriInfo: ${columnName}")
                }

                // 获取文件大小所在的列索引
                val columnIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                // 获取文件大小
                size = cursor.getLong(columnIndex)

                name =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME))

            }
        } finally {
            cursor.close()
        }
    }

    return Pair(size, name!!)
}