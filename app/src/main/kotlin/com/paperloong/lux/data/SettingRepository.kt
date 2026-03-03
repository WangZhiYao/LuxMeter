package com.paperloong.lux.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.paperloong.lux.constant.IlluminanceUnit
import com.paperloong.lux.constant.KEY_ILLUMINANCE_UNIT
import com.paperloong.lux.di.qualifier.IODispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 *
 *
 * @author WangZhiYao
 * @since 2024/4/26
 */
class SettingRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @param:IODispatcher private val dispatcher: CoroutineDispatcher
) {

    fun getIlluminanceUnit(): Flow<IlluminanceUnit> =
        get(KEY_ILLUMINANCE_UNIT, IlluminanceUnit.LUX.name)
            .map { name -> enumValueOf<IlluminanceUnit>(name) }
            .flowOn(dispatcher)

    suspend fun setIlluminanceUnit(unit: IlluminanceUnit) {
        set(KEY_ILLUMINANCE_UNIT, unit.name)
    }

    private fun <T> get(key: Preferences.Key<T>, default: T): Flow<T> =
        dataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preferences ->
                preferences[key] ?: default
            }

    private suspend fun <T> set(key: Preferences.Key<T>, value: T) {
        dataStore.edit { preferences -> preferences[key] = value }
    }
}