package com.paperloong.lux.ext

import com.paperloong.lux.constant.IlluminanceUnit
import com.paperloong.lux.model.DetectRecord

/**
 *
 *
 * @author WangZhiYao
 * @since 2026/9/19
 */
fun DetectRecord.displayValue(displayUnit: IlluminanceUnit): Float =
    when {
        unit == displayUnit -> value
        displayUnit == IlluminanceUnit.FC -> value.luxToFc()
        else -> value.fcToLux()
    }
