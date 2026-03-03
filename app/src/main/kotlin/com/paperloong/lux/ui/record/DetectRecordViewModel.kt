package com.paperloong.lux.ui.record

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.paperloong.lux.R
import com.paperloong.lux.data.DetectRecordRepository
import com.paperloong.lux.model.DetectRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

/**
 *
 *
 * @author WangZhiYao
 * @since 2024/4/27
 */
@HiltViewModel
class DetectRecordViewModel @Inject constructor(
    private val application: Application,
    private val detectRecordRepository: DetectRecordRepository
) : ContainerHost<DetectRecordUiState, DetectRecordSideEffect>, AndroidViewModel(application) {

    override val container: Container<DetectRecordUiState, DetectRecordSideEffect> =
        container(DetectRecordUiState())

    val detectRecordList: StateFlow<PagingData<DetectRecord>> =
        detectRecordRepository.observeDetectRecordList()
            .cachedIn(viewModelScope)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = PagingData.empty()
            )

    fun attemptRemoveRecord(detectRecord: DetectRecord) {
        intent {
            detectRecordRepository.deleteDetectRecord(detectRecord)
                .catch {
                    postSideEffect(Snack(application.getString(R.string.error_remove_record)))
                }
                .collect {

                }
        }
    }

    fun attemptRemoveAllRecord() {
        intent {
            detectRecordRepository.deleteAllRecord()
                .catch {
                    postSideEffect(Snack(application.getString(R.string.error_remove_all_record)))
                }
                .collect {

                }
        }
    }

}