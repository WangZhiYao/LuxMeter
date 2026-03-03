package com.paperloong.lux.ui.detect

import android.app.Application
import android.hardware.Sensor
import androidx.lifecycle.AndroidViewModel
import com.paperloong.lux.R
import com.paperloong.lux.constant.IlluminanceUnit
import com.paperloong.lux.data.DetectRecordRepository
import com.paperloong.lux.data.SettingRepository
import com.paperloong.lux.di.qualifier.IODispatcher
import com.paperloong.lux.ext.luxToFc
import com.paperloong.lux.ext.sensorEventFlow
import com.paperloong.lux.model.DetectRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

/**
 *
 *
 * @author WangZhiYao
 * @since 2024/4/23
 */
@HiltViewModel
class IlluminanceDetectViewModel @Inject constructor(
    private val application: Application,
    private val settingRepository: SettingRepository,
    private val detectRecordRepository: DetectRecordRepository,
    @param:IODispatcher private val dispatcher: CoroutineDispatcher
) : ContainerHost<IlluminanceDetectUiState, IlluminanceDetectSideEffect>,
    AndroidViewModel(application) {

    override val container: Container<IlluminanceDetectUiState, IlluminanceDetectSideEffect> =
        container(IlluminanceDetectUiState())

    private var currentJob: Job? = null
    private val detectRecordList: MutableList<DetectRecord> = mutableListOf()

    init {
        intent {
            settingRepository.getIlluminanceUnit()
                .onEach { detectRecordList.clear() }
                .collect { unit ->
                    reduce {
                        IlluminanceDetectUiState(unit = unit)
                    }
                }
        }
    }

    fun registerLightSensorEventListener() {
        currentJob = intent {
            sensorEventFlow(application, Sensor.TYPE_LIGHT)
                .map { sensorEvent ->
                    val value = sensorEvent.values[0]
                    DetectRecord(
                        value = when (state.unit) {
                            IlluminanceUnit.LUX -> value
                            IlluminanceUnit.FC -> value.luxToFc()
                        },
                        unit = state.unit
                    )
                }
                .flowOn(dispatcher)
                .onEach { detectRecord ->
                    detectRecordList.add(detectRecord)
                }
                .collect { detectRecord ->
                    val min = if (state.initializedZero) detectRecord.value else min(
                        state.min,
                        detectRecord.value
                    )
                    val avg = detectRecordList.map { it.value }.average().toFloat()
                    val max = max(state.max, detectRecord.value)
                    reduce {
                        state.copy(
                            min = min,
                            avg = avg,
                            max = max,
                            current = detectRecord.value,
                            time = detectRecord.createTime,
                            initializedZero = false
                        )
                    }
                }
        }
    }

    fun unregisterLightSensorEventListener() {
        currentJob?.let {
            it.cancel()
            currentJob = null
        }
    }

    fun setIlluminanceUnit(unit: IlluminanceUnit) {
        intent {
            settingRepository.setIlluminanceUnit(unit)
        }
    }

    fun refreshData() {
        intent {
            reduce {
                detectRecordList.clear()
                IlluminanceDetectUiState(unit = state.unit)
            }
        }
    }

    fun attemptAddRecord(record: DetectRecord) {
        intent {
            detectRecordRepository.insertDetectRecord(record)
                .catch {
                    postSideEffect(Snack(application.getString(R.string.error_add_record)))
                }
                .collect {
                    postSideEffect(Snack(application.getString(R.string.add_record_success)))
                }
        }
    }
}