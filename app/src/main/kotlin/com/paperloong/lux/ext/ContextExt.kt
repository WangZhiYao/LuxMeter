package com.paperloong.lux.ext

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 *
 *
 * @author WangZhiYao
 * @since 2024/4/26
 */
val Context.luxMeterDataStore: DataStore<Preferences> by preferencesDataStore(name = "pref_lux_meter")