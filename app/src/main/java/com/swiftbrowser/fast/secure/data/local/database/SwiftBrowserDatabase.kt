package com.swiftbrowser.fast.secure.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.swiftbrowser.fast.secure.core.utils.Constants
import com.swiftbrowser.fast.secure.data.local.dao.BookmarkDao
import com.swiftbrowser.fast.secure.data.local.dao.DownloadDao
import com.swiftbrowser.fast.secure.data.local.dao.HistoryDao
import com.swiftbrowser.fast.secure.data.local.dao.NewsDao
import com.swiftbrowser.fast.secure.data.local.dao.SpeedDialDao
import com.swiftbrowser.fast.secure.data.local.entity.BookmarkEntity
import com.swiftbrowser.fast.secure.data.local.entity.DownloadEntity
import com.swiftbrowser.fast.secure.data.local.entity.HistoryEntity
import com.swiftbrowser.fast.secure.data.local.entity.NewsEntity
import com.swiftbrowser.fast.secure.data.local.entity.SpeedDialEntity

@Database(
    entities = [
        BookmarkEntity::class,
        HistoryEntity::class,
        DownloadEntity::class,
        NewsEntity::class,
        SpeedDialEntity::class,
    ],
    version = Constants.DATABASE_VERSION,
    exportSchema = false,
)
@TypeConverters(DatabaseConverters::class)
abstract class SwiftBrowserDatabase : RoomDatabase() {

    abstract fun bookmarkDao(): BookmarkDao
    abstract fun historyDao(): HistoryDao
    abstract fun downloadDao(): DownloadDao
    abstract fun newsDao(): NewsDao
    abstract fun speedDialDao(): SpeedDialDao
}
