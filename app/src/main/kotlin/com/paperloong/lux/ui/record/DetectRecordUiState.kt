package com.paperloong.lux.ui.record

import com.paperloong.lux.model.DetectRecord

/**
 *
 *
 * @author WangZhiYao
 * @since 2024/4/27
 */
data class DetectRecordUiState(
    val records: List<DetectRecord> = emptyList(),
    val locations: List<String> = emptyList(),
    val selectedLocation: String? = null,
    val search: String = ""
)
