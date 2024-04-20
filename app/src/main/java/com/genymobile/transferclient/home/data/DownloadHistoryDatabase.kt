package com.genymobile.transferclient.home.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DownloadHistory::class], version = 1, exportSchema = false)
abstract class DownloadHistoryDatabase : RoomDatabase() {
    abstract fun downloadHistoryDao(): DownloadHistoryDao
}