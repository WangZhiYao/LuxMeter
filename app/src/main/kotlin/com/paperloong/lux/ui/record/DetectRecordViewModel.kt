package com.paperloong.lux.ui.record

import android.app.Application
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import com.paperloong.lux.R
import com.paperloong.lux.data.CsvExporter
import com.paperloong.lux.data.DetectRecordRepository
import com.paperloong.lux.model.DetectRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
) : OrbitContainerHost<DetectRecordUiState, DetectRecordUiState, DetectRecordSideEffect>,
    AndroidViewModel(application) {

    override val container: OrbitContainer<DetectRecordUiState, DetectRecordUiState, DetectRecordSideEffect> =
        orbitContainer(DetectRecordUiState())

    private var recordJob: Job? = null

    init {
        intent {
            detectRecordRepository.observeLocationList()
                .collect { locations ->
                    reduce { state.copy(locations = locations) }
                }
        }
        observeRecords()
    }

    fun setSearch(query: String) {
        intent {
            reduce { state.copy(search = query) }
            observeRecords()
        }
    }

    fun selectLocation(location: String?) {
        intent {
            reduce { state.copy(selectedLocation = location) }
            observeRecords()
        }
    }

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

    fun exportRecords() {
        intent {
            val records = detectRecordRepository.getDetectRecordList(
                location = state.selectedLocation,
                search = state.search
            )
            if (records.isEmpty()) {
                postSideEffect(Snack(application.getString(R.string.error_export_empty)))
                return@intent
            }
            val csv = CsvExporter.export(records)
            val result = runCatching {
                val dir = File(application.cacheDir, EXPORT_DIR).apply { mkdirs() }
                val name = "luxmeter-records-" +
                        SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date()) + ".csv"
                val file = File(dir, name)
                file.writeText(csv, Charsets.UTF_8)
                FileProvider.getUriForFile(
                    application,
                    application.packageName + ".fileprovider",
                    file
                )
            }
            result.fold(
                onSuccess = { uri: Uri ->
                    postSideEffect(ShareCsv(uri))
                    postSideEffect(
                        Snack(
                            application.getString(
                                R.string.export_success,
                                records.size
                            )
                        )
                    )
                },
                onFailure = {
                    postSideEffect(Snack(application.getString(R.string.error_export)))
                }
            )
        }
    }

    private fun observeRecords() {
        recordJob?.cancel()
        recordJob = intent {
            detectRecordRepository.observeDetectRecordList(
                location = state.selectedLocation,
                search = state.search
            )
                .collect { list ->
                    reduce { state.copy(records = list) }
                }
        }
    }

    companion object {

        private const val EXPORT_DIR = "exports"
    }
}
