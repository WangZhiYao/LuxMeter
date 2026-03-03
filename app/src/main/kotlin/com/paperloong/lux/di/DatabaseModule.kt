package com.paperloong.lux.di

import android.content.Context
import androidx.room.Room
import com.paperloong.lux.data.database.LuxMeterDatabase
import com.paperloong.lux.data.database.dao.DetectRecordDao
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
 * @since 2024/4/27
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "lux_meter.db"

    @Provides
    @Singleton
    fun provideLuxMeterDatabase(@ApplicationContext appContext: Context): LuxMeterDatabase =
        Room.databaseBuilder(appContext, LuxMeterDatabase::class.java, DATABASE_NAME)
            .build()

    @Provides
    @Singleton
    fun provideDetectRecordDao(database: LuxMeterDatabase): DetectRecordDao =
        database.detectRecordDao()
}