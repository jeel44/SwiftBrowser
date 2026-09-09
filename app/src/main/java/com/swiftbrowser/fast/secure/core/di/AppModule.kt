package com.swiftbrowser.fast.secure.core.di

import android.content.Context
import com.swiftbrowser.fast.secure.core.utils.NetworkUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for application-scoped bindings that don't fit into more specific modules
 * (i.e. not database-specific and not network-specific).
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideNetworkUtils(
        @ApplicationContext context: Context,
    ): NetworkUtils = NetworkUtils(context)
}
