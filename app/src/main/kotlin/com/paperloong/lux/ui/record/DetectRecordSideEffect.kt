package com.paperloong.lux.ui.record

/**
 *
 *
 * @author WangZhiYao
 * @since 2024/4/27
 */
sealed interface DetectRecordSideEffect

data class Snack(val message: String) : DetectRecordSideEffect