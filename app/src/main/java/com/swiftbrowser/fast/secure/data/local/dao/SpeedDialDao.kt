package com.swiftbrowser.fast.secure.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.swiftbrowser.fast.secure.data.local.entity.SpeedDialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeedDialDao {

    @Query("SELECT * FROM speed_dial ORDER BY sort_order ASC")
    fun getAll(): Flow<List<SpeedDialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<SpeedDialEntity>)

    @Query("SELECT COUNT(*) FROM speed_dial")
    suspend fun count(): Int

    @Query("DELETE FROM speed_dial WHERE id = :id")
    suspend fun deleteById(id: Int)
}
