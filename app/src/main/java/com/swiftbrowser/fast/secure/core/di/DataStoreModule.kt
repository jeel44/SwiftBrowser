package com.swiftbrowser.fast.secure.core.di

import android.content.Context
import com.swiftbrowser.fast.secure.data.datastore.BrowserPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing the DataStore-backed [BrowserPreferences] singleton.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideBrowserPreferences(
        @ApplicationContext context: Context,
    ): BrowserPreferences = BrowserPreferences(context)
}
