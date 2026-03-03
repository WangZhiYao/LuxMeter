package com.paperloong.lux.di

import com.paperloong.lux.di.qualifier.IODispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 *
 *
 * @author WangZhiYao
 * @since 2024/4/24
 */
@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @Provides
    @IODispatcher
    fun providerIODispatcher(): CoroutineDispatcher = Dispatchers.IO

}