package com.swiftbrowser.fast.secure.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.swiftbrowser.fast.secure.data.local.entity.NewsEntity

@Dao
interface NewsDao {

    @Query("SELECT * FROM news_cache ORDER BY published_at DESC LIMIT 50")
    suspend fun getAll(): List<NewsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<NewsEntity>)

    @Query("DELETE FROM news_cache WHERE cached_at < :expiry")
    suspend fun deleteExpired(expiry: Long)

    @Query("DELETE FROM news_cache")
    suspend fun deleteAll()
}
