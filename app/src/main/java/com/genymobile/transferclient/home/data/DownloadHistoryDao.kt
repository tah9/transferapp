package com.genymobile.transferclient.home.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DownloadHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(downloadHistory: DownloadHistory)

    @Query("DELETE FROM download_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE download_history SET fileName = :fileName, fileSize = :fileSize, downloadTime = :downloadTime, status = :status WHERE id = :id")
    suspend fun update(
        id: Long,
        fileName: String,
        fileSize: Long,
        downloadTime: Long,
        status: String
    )

    @Query("SELECT * FROM download_history ORDER BY downloadTime DESC")
    fun getAllHistories(): List<DownloadHistory>
}