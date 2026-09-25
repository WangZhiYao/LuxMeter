package com.paperloong.lux.ui.detect

import android.app.Application
import android.hardware.Sensor
import androidx.lifecycle.AndroidViewModel
import com.paperloong.lux.R
import com.paperloong.lux.constant.IlluminanceUnit
import com.paperloong.lux.data.DetectRecordRepository
import com.paperloong.lux.data.SettingRepository
import com.paperloong.lux.di.qualifier.IODispatcher
import com.paperloong.lux.ext.sensorEventFlow
import com.paperloong.lux.model.DetectRecord
import com.paperloong.lux.model.DetectSession
import com.paperloong.lux.model.TargetIlluminanceRange
import com.paperloong.lux.model.judgeIlluminance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

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
) : OrbitContainerHost<IlluminanceDetectUiState, IlluminanceDetectUiState, IlluminanceDetectSideEffect>,
    AndroidViewModel(application) {

    override val container: OrbitContainer<IlluminanceDetectUiState, IlluminanceDetectUiState, IlluminanceDetectSideEffect> =
        orbitContainer(IlluminanceDetectUiState())

    private var currentJob: Job? = null
    private val session = DetectSession()

    init {
        intent {
            settingRepository.getIlluminanceUnit()
                .collect { unit ->
                    // 切换单位不再清空会话统计（内部恒为 lux）
                    reduce { state.copy(unit = unit) }
                }
        }
        intent {
            settingRepository.getTargetRange()
                .collect { target ->
                    reduce {
                        state.copy(
                            target = target,
                            judgment = judgeIlluminance(state.current, target)
                        )
                    }
                }
        }
        intent {
            detectRecordRepository.observeRecentRecordList()
                .collect { list ->
                    reduce { state.copy(recentRecords = list) }
                }
        }
        intent {
            detectRecordRepository.observeLocationList()
                .collect { list ->
                    reduce { state.copy(locationSuggestions = list) }
                }
        }
    }

    fun registerLightSensorEventListener() {
        currentJob = intent {
            sensorEventFlow(application, Sensor.TYPE_LIGHT)
                .map { sensorEvent -> sensorEvent.values[0] }
                .flowOn(dispatcher)
                .catch {
                    postSideEffect(Snack(application.getString(R.string.error_sensor_not_find)))
                }
                .collect { lux ->
                    session.add(lux)
                    reduce {
                        state.copy(
                            current = lux,
                            time = System.currentTimeMillis(),
                            min = session.min,
                            avg = session.avg,
                            max = session.max,
                            trend = session.trend,
                            sessionCount = session.count,
                            judgment = judgeIlluminance(lux, state.target)
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

    /** 重新开始会话：清空统计与走势。 */
    fun restartSession() {
        intent {
            session.reset()
            reduce {
                state.copy(
                    min = null,
                    avg = null,
                    max = null,
                    trend = emptyList(),
                    sessionCount = 0
                )
            }
        }
    }

    fun setTargetRange(min: Float, max: Float) {
        val range = TargetIlluminanceRange.of(min, max) ?: return
        intent {
            settingRepository.setTargetRange(range)
        }
    }

    fun clearTargetRange() {
        intent {
            settingRepository.clearTargetRange()
        }
    }

    fun attemptAddRecord(
        value: Float,
        unit: IlluminanceUnit,
        time: Long,
        location: String,
        remark: String
    ) {
        intent {
            val record = DetectRecord(
                value = value,
                unit = unit,
                remark = remark,
                location = location.trim(),
                createTime = time
            )
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
