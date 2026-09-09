package com.swiftbrowser.fast.secure.data.local.database

import androidx.room.TypeConverter
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.swiftbrowser.fast.secure.data.local.entity.DownloadStatus

object DatabaseMigrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS news_cache (
                    id INTEGER NOT NULL PRIMARY KEY,
                    title TEXT NOT NULL,
                    url TEXT NOT NULL,
                    source TEXT NOT NULL,
                    category TEXT NOT NULL,
                    published_at INTEGER NOT NULL,
                    cached_at INTEGER NOT NULL,
                    image_url TEXT NOT NULL DEFAULT ''
                )
                """.trimIndent()
            )
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS speed_dial (
                    id INTEGER NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL,
                    url TEXT NOT NULL,
                    icon_color INTEGER NOT NULL,
                    icon_label TEXT NOT NULL,
                    sort_order INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DELETE FROM speed_dial")
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
}

class DatabaseConverters {

    @TypeConverter
    fun fromDownloadStatus(status: DownloadStatus): String = status.name

    @TypeConverter
    fun toDownloadStatus(value: String): DownloadStatus = enumValueOf(value)
}
