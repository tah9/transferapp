package com.genymobile.transferclient.home.data

import androidx.compose.runtime.MutableState
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "download_history")
data class DownloadHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val fileName: String,
    val fileSize: Long,
    val downloadPath: String,
    val downloadTime: Long,
    var status: String,
    var progress: Float = 1.0f//下载进度
)