package com.paperloong.lux.ui.detect.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paperloong.lux.model.IlluminanceJudgment

/**
 * 主屏 hero 读数卡：状态 chip + 大数值 + 目标行。
 *
 * @author WangZhiYao
 * @since 2026/9/19
 */
@Composable
fun HeroReadingCard(
    valueText: String,
    unitLabel: String,
    judgment: IlluminanceJudgment?,
    statusText: String,
    targetText: String?,
    onJudgmentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container = when (judgment) {
        null -> MaterialTheme.colorScheme.surfaceContainerHigh
        IlluminanceJudgment.InRange -> MaterialTheme.colorScheme.primaryContainer
        is IlluminanceJudgment.TooLow -> MaterialTheme.colorScheme.tertiaryContainer
        is IlluminanceJudgment.TooHigh -> MaterialTheme.colorScheme.errorContainer
    }
    val content = when (judgment) {
        null -> MaterialTheme.colorScheme.onSurface
        IlluminanceJudgment.InRange -> MaterialTheme.colorScheme.onPrimaryContainer
        is IlluminanceJudgment.TooLow -> MaterialTheme.colorScheme.onTertiaryContainer
        is IlluminanceJudgment.TooHigh -> MaterialTheme.colorScheme.onErrorContainer
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(container, RoundedCornerShape(18.dp))
            .clickable(onClick = onJudgmentClick, role = Role.Button)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (judgment == null) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelLarge,
                color = content,
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 16.dp, vertical = 5.dp)
            )
        } else {
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelMedium,
                color = container,
                modifier = Modifier
                    .background(content, RoundedCornerShape(50))
                    .padding(horizontal = 16.dp, vertical = 5.dp)
            )
        }
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.padding(top = 13.dp)
        ) {
            BasicText(
                text = valueText,
                style = MaterialTheme.typography.displayLarge.copy(
                    color = content,
                    lineHeight = TextUnit.Unspecified
                ),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 28.sp,
                    maxFontSize = 76.sp,
                    stepSize = 4.sp
                )
            )
            Text(
                text = unitLabel,
                style = MaterialTheme.typography.labelLarge,
                color = content.copy(alpha = 0.75f),
                modifier = Modifier.padding(start = 10.dp, bottom = 10.dp)
            )
        }
        if (!targetText.isNullOrBlank()) {
            Text(
                text = targetText,
                style = MaterialTheme.typography.bodySmall,
                color = content.copy(alpha = 0.72f),
                modifier = Modifier.padding(top = 9.dp)
            )
        }
    }
}
