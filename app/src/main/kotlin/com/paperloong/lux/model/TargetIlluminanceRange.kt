package com.paperloong.lux.model

/**
 * 目标光照区间，以 lux 为基准存储。
 *
 * @author WangZhiYao
 * @since 2026/9/19
 */
data class TargetIlluminanceRange(
    val minLux: Float,
    val maxLux: Float
) {

    val isValid: Boolean
        get() = minLux > 0f && maxLux > minLux

    companion object {

        /** 非法区间返回 null，合法返回实例。 */
        fun of(minLux: Float, maxLux: Float): TargetIlluminanceRange? =
            TargetIlluminanceRange(minLux, maxLux).takeIf { it.isValid }
    }
}
