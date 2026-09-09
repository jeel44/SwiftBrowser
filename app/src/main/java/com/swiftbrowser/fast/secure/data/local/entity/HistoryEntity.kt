package com.swiftbrowser.fast.secure.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for a single browsing history entry. The unique index on [url] + [visitedAt]
 * prevents exact duplicate entries while still allowing the same URL to appear multiple times
 * on different visits.
 */
@Entity(
    tableName = "history",
    indices = [Index(value = ["url", "visited_at"], unique = true)],
)
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "favicon_url")
    val faviconUrl: String? = null,

    @ColumnInfo(name = "visited_at")
    val visitedAt: Long = System.currentTimeMillis(),
)
