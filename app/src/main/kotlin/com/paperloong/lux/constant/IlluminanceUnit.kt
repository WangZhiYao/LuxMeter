package com.paperloong.lux.constant

import java.text.DecimalFormat

/**
 *
 *
 * @author WangZhiYao
 * @since 2024/4/24
 */
enum class IlluminanceUnit(private val df: DecimalFormat) {

    LUX(DecimalFormat("0")),

    FC(DecimalFormat("0.00"));

    fun format(value: Float): String {
        return df.format(value)
    }
}