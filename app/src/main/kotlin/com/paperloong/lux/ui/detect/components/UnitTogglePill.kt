package com.paperloong.lux.ui.detect.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.paperloong.lux.constant.IlluminanceUnit

/**
 * 单位切换胶囊：整行两段均分，选中段 onSurface 填充。
 *
 * @author WangZhiYao
 * @since 2026/9/19
 */
@Composable
fun UnitTogglePill(
    unit: IlluminanceUnit,
    onUnitClick: (IlluminanceUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
            .padding(4.dp)
            .selectableGroup()
    ) {
        IlluminanceUnit.entries.forEach { entry ->
            val selected = entry == unit
            Text(
                text = entry.name,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (selected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                        RoundedCornerShape(50)
                    )
                    .selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = { onUnitClick(entry) })
                    .padding(vertical = 10.dp)
            )
        }
    }
}
