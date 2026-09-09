package com.swiftbrowser.fast.secure.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "speed_dial")
data class SpeedDialEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "icon_color")
    val iconColor: Long,

    @ColumnInfo(name = "icon_label")
    val iconLabel: String,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,
)
