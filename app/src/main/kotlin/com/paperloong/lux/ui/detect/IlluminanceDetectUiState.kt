package com.paperloong.lux.ui.detect

import com.paperloong.lux.constant.IlluminanceUnit

/**
 *
 *
 * @author WangZhiYao
 * @since 2024/4/23
 */
data class IlluminanceDetectUiState(
    val min: Float = 0f,
    val avg: Float = 0f,
    val max: Float = 0f,
    val current: Float = 0f,
    val unit: IlluminanceUnit = IlluminanceUnit.LUX,
    val time: Long = System.currentTimeMillis(),
    val initializedZero: Boolean = true
)
