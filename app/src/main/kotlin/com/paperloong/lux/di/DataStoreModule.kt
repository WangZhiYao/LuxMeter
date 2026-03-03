package com.paperloong.lux.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.paperloong.lux.ext.luxMeterDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 *
 *
 * @author WangZhiYao
 * @since 2024/4/26
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideLuxMeterDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> =
        appContext.luxMeterDataStore

}