package com.paperloong.lux.ui.detect.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.paperloong.lux.R
import com.paperloong.lux.model.TargetIlluminanceRange

/**
 * 目标光照区间设置弹窗：下限/上限 + 区间预览条。
 *
 * @author WangZhiYao
 * @since 2026/9/19
 */
@Composable
fun TargetRangeDialog(
    initial: TargetIlluminanceRange?,
    currentLux: Float,
    onConfirmClick: (minLux: Float, maxLux: Float) -> Unit,
    onClearClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    var minText by remember { mutableStateOf(initial?.minLux?.toInt()?.toString() ?: "") }
    var maxText by remember { mutableStateOf(initial?.maxLux?.toInt()?.toString() ?: "") }
    val min = minText.toFloatOrNull()
    val max = maxText.toFloatOrNull()
    val valid = min != null && max != null && TargetIlluminanceRange(min, max).isValid

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = { if (valid) onConfirmClick(min, max) },
                enabled = valid
            ) {
                Text(text = stringResource(id = R.string.confirm))
            }
        },
        dismissButton = {
            Row {
                if (initial != null) {
                    TextButton(onClick = onClearClick) {
                        Text(
                            text = stringResource(id = R.string.clear_target),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                TextButton(onClick = onDismissRequest) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }
        },
        title = {
            Text(text = stringResource(id = R.string.target_range))
        },
        text = {
            Column {
                Text(
                    text = stringResource(id = R.string.target_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    OutlinedTextField(
                        value = minText,
                        onValueChange = { minText = it.filter { ch -> ch.isDigit() } },
                        label = { Text(text = stringResource(id = R.string.target_min)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxText,
                        onValueChange = { maxText = it.filter { ch -> ch.isDigit() } },
                        label = { Text(text = stringResource(id = R.string.target_max)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainer,
                            RoundedCornerShape(14.dp)
                        )
                        .padding(12.dp)
                ) {
                    // coerceAtLeast(1f)：全 0 输入时避免除 0 产生 NaN 坐标
                    val scaleMax = maxOf(max ?: 20000f, currentLux, initial?.maxLux ?: 0f)
                        .coerceAtLeast(1f) * 1.1f
                    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    val segmentColor = MaterialTheme.colorScheme.primary
                    val dotColor = MaterialTheme.colorScheme.onSurface
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    ) {
                        fun xOf(value: Float): Float =
                            size.width * (value / scaleMax).coerceIn(0f, 1f)

                        drawLine(
                            color = trackColor,
                            start = Offset(0f, size.height / 2),
                            end = Offset(size.width, size.height / 2),
                            strokeWidth = size.height,
                            cap = StrokeCap.Round
                        )
                        if (min != null && max != null) {
                            drawLine(
                                color = segmentColor,
                                start = Offset(xOf(min), size.height / 2),
                                end = Offset(xOf(max), size.height / 2),
                                strokeWidth = size.height,
                                cap = StrokeCap.Butt
                            )
                        }
                        drawCircle(
                            color = dotColor,
                            radius = size.height * 0.9f,
                            center = Offset(xOf(currentLux), size.height / 2)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Text(
                            text = "0",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = scaleMax.toInt().toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End
                        )
                    }
                }
                Text(
                    text = stringResource(id = R.string.target_store_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    )
}
