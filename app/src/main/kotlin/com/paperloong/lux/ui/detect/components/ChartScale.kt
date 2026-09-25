package com.paperloong.lux.ui.detect.components

import kotlin.math.ceil
import kotlin.math.pow

/**
 * 走势图坐标刻度：量程向上取优雅值（1/2/2.5/5 × 10ⁿ），网格线数量落在 2~4 条。
 *
 * @author WangZhiYao
 * @since 2026/9/20
 */
internal data class ChartScale(
    val top: Float,
    val step: Float,
    val ticks: List<Float>
)

/**
 * 从小到大枚举优雅步长，取第一个「覆盖 rawTop 所需段数 ≤ 5」的步长；
 * 段数因此必然 ≥ 3（相邻候选步长至多 ×2，从 ≥6 跨落至 ≤5 只能落在 3~5），
 * 网格线（不含量程顶）即 2~4 条。rawTop 非有限（NaN/∞）或 < 1 时按 1 处理（防全 0 读数除零）。
 * rawTop > 2.5e7（超出枚举上限 5e6×5 段）会抛异常，物理上不可达（日光 ~120k lux）。
 */
internal fun niceChartScale(rawTop: Float): ChartScale {
    val safe = if (rawTop.isFinite()) rawTop.coerceAtLeast(1f) else 1f
    for (exponent in -3..6) {
        for (mantissa in floatArrayOf(1f, 2f, 2.5f, 5f)) {
            val step = mantissa * 10f.pow(exponent)
            val lines = ceil(safe / step)
            if (lines <= 5f) {
                val top = lines * step
                val ticks = buildList {
                    var tick = step
                    while (tick < top) {
                        add(tick)
                        tick += step
                    }
                }
                return ChartScale(top = top, step = step, ticks = ticks)
            }
        }
    }
    error("unreachable: rawTop=$rawTop")
}
