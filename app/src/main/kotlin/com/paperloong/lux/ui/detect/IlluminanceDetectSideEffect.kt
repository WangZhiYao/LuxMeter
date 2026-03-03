package com.paperloong.lux.ui.detect

/**
 *
 *
 * @author WangZhiYao
 * @since 2024/4/24
 */
sealed interface IlluminanceDetectSideEffect

data class Snack(val message: String) : IlluminanceDetectSideEffect
