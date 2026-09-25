package com.paperloong.lux.ui.detect

import com.paperloong.lux.constant.IlluminanceUnit
import com.paperloong.lux.model.DetectRecord
import com.paperloong.lux.model.IlluminanceJudgment
import com.paperloong.lux.model.TargetIlluminanceRange

/**
 *
 *
 * @author WangZhiYao
 * @since 2024/4/23
 */
data class IlluminanceDetectUiState(
    val unit: IlluminanceUnit = IlluminanceUnit.LUX,
    /** 当前读数，内部恒为 lux 基准，显示时按 unit 换算。 */
    val current: Float = 0f,
    val time: Long = System.currentTimeMillis(),
    val min: Float? = null,
    val avg: Float? = null,
    val max: Float? = null,
    val trend: List<Float> = emptyList(),
    val sessionCount: Int = 0,
    val target: TargetIlluminanceRange? = null,
    val judgment: IlluminanceJudgment? = null,
    val recentRecords: List<DetectRecord> = emptyList(),
    val locationSuggestions: List<String> = emptyList()
)
