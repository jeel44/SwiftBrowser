package com.swiftbrowser.fast.secure.core.di

import android.content.Context
import androidx.room.Room
import com.swiftbrowser.fast.secure.core.utils.Constants
import com.swiftbrowser.fast.secure.data.local.dao.BookmarkDao
import com.swiftbrowser.fast.secure.data.local.dao.DownloadDao
import com.swiftbrowser.fast.secure.data.local.dao.HistoryDao
import com.swiftbrowser.fast.secure.data.local.dao.NewsDao
import com.swiftbrowser.fast.secure.data.local.dao.SpeedDialDao
import com.swiftbrowser.fast.secure.data.local.database.DatabaseMigrations
import com.swiftbrowser.fast.secure.data.local.database.SwiftBrowserDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): SwiftBrowserDatabase = Room.databaseBuilder(
        context,
        SwiftBrowserDatabase::class.java,
        Constants.DATABASE_NAME,
    )
        .addMigrations(*DatabaseMigrations.ALL)
        .build()

    @Provides @Singleton
    fun provideBookmarkDao(db: SwiftBrowserDatabase): BookmarkDao = db.bookmarkDao()

    @Provides @Singleton
    fun provideHistoryDao(db: SwiftBrowserDatabase): HistoryDao = db.historyDao()

    @Provides @Singleton
    fun provideDownloadDao(db: SwiftBrowserDatabase): DownloadDao = db.downloadDao()

    @Provides @Singleton
    fun provideNewsDao(db: SwiftBrowserDatabase): NewsDao = db.newsDao()

    @Provides @Singleton
    fun provideSpeedDialDao(db: SwiftBrowserDatabase): SpeedDialDao = db.speedDialDao()
}
