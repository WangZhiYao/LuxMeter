package com.paperloong.lux.model

/**
 * 读数相对目标区间的判定结果。
 *
 * @author WangZhiYao
 * @since 2026/9/19
 */
sealed interface IlluminanceJudgment {

    /** 偏暗，[gap] 为距下限还差多少 lux。 */
    data class TooLow(val gap: Float) : IlluminanceJudgment

    data object InRange : IlluminanceJudgment

    /** 过强，[gap] 为超出上限多少 lux。 */
    data class TooHigh(val gap: Float) : IlluminanceJudgment
}

/** 未设目标返回 null；否则按区间判定。 */
fun judgeIlluminance(valueLux: Float, target: TargetIlluminanceRange?): IlluminanceJudgment? =
    when {
        target == null -> null
        valueLux < target.minLux -> IlluminanceJudgment.TooLow(target.minLux - valueLux)
        valueLux > target.maxLux -> IlluminanceJudgment.TooHigh(valueLux - target.maxLux)
        else -> IlluminanceJudgment.InRange
    }
