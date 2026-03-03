package com.paperloong.lux.model

import com.paperloong.lux.constant.IlluminanceUnit

/**
 *
 *
 * @author WangZhiYao
 * @since 2024/4/27
 */
data class DetectRecord(
    val id: Long = 0,
    val value: Float,
    val unit: IlluminanceUnit,
    val remark: String = "",
    val createTime: Long = System.currentTimeMillis()
)