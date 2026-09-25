package com.paperloong.lux.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.paperloong.lux.constant.IlluminanceUnit
import com.paperloong.lux.constant.KEY_ILLUMINANCE_UNIT
import com.paperloong.lux.constant.KEY_TARGET_MAX
import com.paperloong.lux.constant.KEY_TARGET_MIN
import com.paperloong.lux.di.qualifier.IODispatcher
import com.paperloong.lux.model.TargetIlluminanceRange
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

    fun getTargetRange(): Flow<TargetIlluminanceRange?> =
        dataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preferences ->
                val min = preferences[KEY_TARGET_MIN]
                val max = preferences[KEY_TARGET_MAX]
                if (min != null && max != null) {
                    TargetIlluminanceRange(min, max).takeIf { it.isValid }
                } else {
                    null
                }
            }
            .flowOn(dispatcher)

    suspend fun setTargetRange(range: TargetIlluminanceRange) {
        dataStore.edit { preferences ->
            preferences[KEY_TARGET_MIN] = range.minLux
            preferences[KEY_TARGET_MAX] = range.maxLux
        }
    }

    suspend fun clearTargetRange() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_TARGET_MIN)
            preferences.remove(KEY_TARGET_MAX)
        }
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