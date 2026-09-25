package com.paperloong.lux.ui.record

import android.net.Uri

/**
 *
 *
 * @author WangZhiYao
 * @since 2024/4/27
 */
sealed interface DetectRecordSideEffect

data class Snack(val message: String) : DetectRecordSideEffect

data class ShareCsv(val uri: Uri) : DetectRecordSideEffect
