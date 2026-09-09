package com.swiftbrowser.fast.secure.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.swiftbrowser.fast.secure.data.local.entity.DownloadEntity
import com.swiftbrowser.fast.secure.data.local.entity.DownloadStatus
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the `downloads` table.
 */
@Dao
interface DownloadDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(download: DownloadEntity): Long

    @Update
    suspend fun update(download: DownloadEntity)

    @Delete
    suspend fun delete(download: DownloadEntity)

    @Query("SELECT * FROM downloads ORDER BY created_at DESC")
    fun getAll(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = :status ORDER BY created_at DESC")
    fun getByStatus(status: DownloadStatus): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE system_download_id = :systemId LIMIT 1")
    suspend fun getBySystemId(systemId: Long): DownloadEntity?

    @Query("UPDATE downloads SET status = :status WHERE system_download_id = :systemId")
    suspend fun updateStatus(systemId: Long, status: DownloadStatus)

    @Query("DELETE FROM downloads")
    suspend fun deleteAll()
}
