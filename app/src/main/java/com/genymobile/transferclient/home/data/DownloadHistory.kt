package com.genymobile.transferclient.home.data

import java.util.Date

data class DownloadHistory(
    val id: Long = 0L,
    val fileName: String,
    val fileSize: Long,
    val downloadPath: String,
    val downloadTime: Date,
    val status: String
)