package com.paperloong.lux.ext

import android.content.Context
import android.hardware.SensorEvent
import android.hardware.SensorEventCallback
import android.hardware.SensorManager
import com.paperloong.lux.R
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

/**
 *
 *
 * @author WangZhiYao
 * @since 2024/4/26
 */
fun sensorEventFlow(context: Context, sensorType: Int) = callbackFlow {
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val sensor = sensorManager.getDefaultSensor(sensorType)

    if (sensor == null) {
        close(Throwable(context.getString(R.string.error_sensor_not_find)))
        return@callbackFlow
    }

    val sensorEventListener = object : SensorEventCallback() {
        override fun onSensorChanged(event: SensorEvent) {
            trySend(event)
        }
    }

    sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)

    awaitClose {
        sensorManager.unregisterListener(sensorEventListener)
    }
}