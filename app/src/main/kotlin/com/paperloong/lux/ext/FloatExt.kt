package com.paperloong.lux.ext

import java.math.BigDecimal

/**
 *
 *
 * @author WangZhiYao
 * @since 2024/4/26
 */
fun Float.luxToFc(): Float {
    val value = BigDecimal(this.toString())
    val conversionFactor = BigDecimal("0.09290304")
    val result = value * conversionFactor
    return result.toFloat()
}