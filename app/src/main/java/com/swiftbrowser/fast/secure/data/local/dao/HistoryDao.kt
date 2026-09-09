package com.swiftbrowser.fast.secure.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.swiftbrowser.fast.secure.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the `history` table.
 */
@Dao
interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(history: HistoryEntity): Long

    @Query("SELECT * FROM history ORDER BY visited_at DESC")
    fun getAll(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history WHERE title LIKE '%' || :query || '%' OR url LIKE '%' || :query || '%' ORDER BY visited_at DESC LIMIT 50")
    fun search(query: String): Flow<List<HistoryEntity>>

    /** Returns the [limit] most recently visited URLs — used for autocomplete in the address bar. */
    @Query("SELECT * FROM history ORDER BY visited_at DESC LIMIT :limit")
    suspend fun getRecent(limit: Int = 20): List<HistoryEntity>

    @Query("DELETE FROM history")
    suspend fun deleteAll()

    /** Deletes history entries older than [timestamp] (unix millis). */
    @Query("DELETE FROM history WHERE visited_at < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteById(id: Long)
}
