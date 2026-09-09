package com.swiftbrowser.fast.secure.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "news_cache")
data class NewsEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "source")
    val source: String,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "published_at")
    val publishedAt: Long,

    @ColumnInfo(name = "cached_at")
    val cachedAt: Long,

    @ColumnInfo(name = "image_url")
    val imageUrl: String = "",
)
