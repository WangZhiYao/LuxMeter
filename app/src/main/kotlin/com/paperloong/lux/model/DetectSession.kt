package com.paperloong.lux.model

/**
 * 一次测量会话：统计（min/avg/max）基于全部读数，
 * 走势序列只保留最近 [chartCapacity] 个点用于绘图。
 *
 * @author WangZhiYao
 * @since 2026/9/19
 */
class DetectSession(
    private val chartCapacity: Int = DEFAULT_CHART_CAPACITY
) {

    private var countInternal = 0
    private var sum = 0.0
    private var minInternal = Float.MAX_VALUE
    private var maxInternal = -Float.MAX_VALUE
    private val trendInternal = mutableListOf<Float>()

    val count: Int get() = countInternal
    val min: Float? get() = countInternal.takeIf { it > 0 }?.let { minInternal }
    val avg: Float? get() = countInternal.takeIf { it > 0 }?.let { (sum / it).toFloat() }
    val max: Float? get() = countInternal.takeIf { it > 0 }?.let { maxInternal }
    val trend: List<Float> get() = trendInternal.toList()

    fun add(value: Float) {
        countInternal++
        sum += value
        if (value < minInternal) minInternal = value
        if (value > maxInternal) maxInternal = value
        trendInternal.add(value)
        if (trendInternal.size > chartCapacity) {
            trendInternal.removeAt(0)
        }
    }

    fun reset() {
        countInternal = 0
        sum = 0.0
        minInternal = Float.MAX_VALUE
        maxInternal = -Float.MAX_VALUE
        trendInternal.clear()
    }

    companion object {

        const val DEFAULT_CHART_CAPACITY = 60
    }
}
