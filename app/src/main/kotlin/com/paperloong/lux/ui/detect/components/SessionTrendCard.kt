package com.paperloong.lux.ui.detect.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paperloong.lux.R
import com.paperloong.lux.model.TargetIlluminanceRange
import kotlin.math.roundToInt

/**
 * 会话走势卡：带刻度网格的面积折线图 + 目标区间横带 + 按住/横拖查看数值 + 重新开始按钮。
 *
 * @author WangZhiYao
 * @since 2026/9/19
 */
@Composable
fun SessionTrendCard(
    trend: List<Float>,
    target: TargetIlluminanceRange?,
    topLabelText: String,
    hintText: String,
    displayValueOf: (Float) -> Float,
    valueTextOf: (Float) -> String,
    onRestartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(16.dp))
            // 顶部 3dp + 重新开始 48dp 触控节点内居中的 10dp 偏移 ≈ 原 13dp 视觉间距
            .padding(start = 16.dp, end = 16.dp, top = 3.dp, bottom = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = topLabelText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = hintText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
            Text(
                text = stringResource(id = R.string.restart_session),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable(role = Role.Button, onClick = onRestartClick)
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            )
        }
        SessionTrendChart(
            trend = trend,
            target = target,
            displayValueOf = displayValueOf,
            valueTextOf = valueTextOf,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .height(140.dp)
        )
    }
}

/** 按索引线性插值；两侧长度不齐时以 to 侧自身值兜底（新增点直接出现）。 */
private fun lerpByIndex(from: List<Float>, to: List<Float>, fraction: Float): List<Float> =
    to.mapIndexed { index, value ->
        from.getOrNull(index)?.let { it + (value - it) * fraction } ?: value
    }

@Composable
private fun SessionTrendChart(
    trend: List<Float>,
    target: TargetIlluminanceRange?,
    displayValueOf: (Float) -> Float,
    valueTextOf: (Float) -> String,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val bandColor = lineColor.copy(alpha = 0.10f)
    val edgeColor = lineColor.copy(alpha = 0.50f)
    val edgeNumberColor = lineColor.copy(alpha = 0.90f)
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val axisLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val dotCoreColor = MaterialTheme.colorScheme.surface
    val crosshairColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val bubbleColor = MaterialTheme.colorScheme.onSurface
    val bubbleTextColor = MaterialTheme.colorScheme.surface
    val textMeasurer = rememberTextMeasurer()
    // 按住/横拖时显示准线与数值气泡；用 rememberUpdatedState 避免新数据到达时打断手势
    val currentTrend by rememberUpdatedState(trend)
    var scrubIndex by remember { mutableStateOf<Int?>(null) }

    // 全部换算到显示单位域后再算量程与刻度，lux/fc 切换即整体换算
    val display = remember(trend, displayValueOf) { trend.map(displayValueOf) }
    val displayTarget = remember(target, displayValueOf) {
        target?.let { displayValueOf(it.minLux) to displayValueOf(it.maxLux) }
    }
    val scale = remember(display, displayTarget) {
        niceChartScale(maxOf(display.maxOrNull() ?: 0f, displayTarget?.second ?: 0f))
    }

    // 文本测量在组合作用域完成
    val tickStyle = TextStyle(fontSize = 9.sp)
    val tickLayouts = remember(scale) {
        scale.ticks.map { textMeasurer.measure(valueTextOf(it), tickStyle) }
    }

    // 刻度槽宽随最宽刻度文字伸缩，避免大数值被左缘裁切
    val tickAreaWidthDp = with(LocalDensity.current) {
        (tickLayouts.maxOfOrNull { it.size.width } ?: 0).toDp() + 8.dp
    }
    val edgeLayouts = remember(displayTarget) {
        displayTarget?.let { (minValue, maxValue) ->
            textMeasurer.measure(valueTextOf(maxValue), tickStyle) to
                    textMeasurer.measure(valueTextOf(minValue), tickStyle)
        }
    }

    // 量程动画：max 跳变引起整图缩放时平滑过渡
    val topAnim = remember { Animatable(scale.top) }
    LaunchedEffect(scale.top) {
        if (topAnim.value != scale.top) {
            topAnim.animateTo(scale.top, tween(durationMillis = 250))
        }
    }
    // 新点 morph：按索引 lerp 上一序列；窗口滑出（>60 点左移）表现为整体向左流动
    var morphFrom by remember { mutableStateOf(display) }
    var morphTo by remember { mutableStateOf(display) }
    val morph = remember { Animatable(1f) }
    LaunchedEffect(display) {
        if (display.isEmpty()) {
            morphFrom = display
            morphTo = display
            morph.snapTo(1f)
            return@LaunchedEffect
        }
        // 上一次动画未播完：从当前插值位置无缝续播
        morphFrom = if (morphTo.isNotEmpty() && morph.value < 1f) {
            lerpByIndex(morphFrom, morphTo, morph.value)
        } else {
            morphTo
        }
        morphTo = display
        morph.snapTo(0f)
        morph.animateTo(1f, tween(durationMillis = 200))
    }

    val bubbleLayout = scrubIndex?.let { index ->
        currentTrend.getOrNull(index)?.let { lux ->
            textMeasurer.measure(
                valueTextOf(displayValueOf(lux)),
                TextStyle(fontSize = 11.sp, color = bubbleTextColor)
            )
        }
    }

    fun indexAt(x: Float, width: Float): Int =
        if (currentTrend.size <= 1) 0
        else (((x / width) * (currentTrend.size - 1)).roundToInt()).coerceIn(
            0,
            currentTrend.size - 1
        )

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        if (currentTrend.isNotEmpty()) {
                            scrubIndex = indexAt(offset.x, size.width.toFloat())
                            tryAwaitRelease()
                            scrubIndex = null
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        if (currentTrend.isNotEmpty()) scrubIndex =
                            indexAt(offset.x, size.width.toFloat())
                    },
                    onHorizontalDrag = { change, _ ->
                        if (currentTrend.isNotEmpty()) {
                            scrubIndex = indexAt(change.position.x, size.width.toFloat())
                        }
                    },
                    onDragEnd = { scrubIndex = null },
                    onDragCancel = { scrubIndex = null }
                )
            }
    ) {
        // 无数据时不画
        if (display.isEmpty()) return@Canvas
        val values = lerpByIndex(morphFrom, display, morph.value)
        val top = topAnim.value
        val tickAreaWidth = tickAreaWidthDp.toPx()
        val rightInset = 6.dp.toPx()
        val plotLeft = tickAreaWidth
        val plotRight = size.width - rightInset
        val plotWidth = plotRight - plotLeft

        fun xOf(index: Int): Float =
            if (values.size <= 1) plotLeft else plotLeft + plotWidth * index / (values.size - 1)

        fun yOf(value: Float): Float =
            size.height * (1f - (value / top).coerceIn(0f, 1f))

        // 网格线与左侧刻度
        tickLayouts.forEachIndexed { index, layout ->
            val y = yOf(scale.ticks[index])
            drawLine(
                color = gridColor,
                start = Offset(plotLeft, y),
                end = Offset(plotRight, y),
                strokeWidth = 1.dp.toPx()
            )
            drawText(
                textLayoutResult = layout,
                color = axisLabelColor,
                topLeft = Offset(
                    x = plotLeft - 4.dp.toPx() - layout.size.width,
                    y = y - layout.size.height / 2f
                )
            )
        }
        // 目标带：淡填充 + 虚线边界 + 左端纯数值
        if (displayTarget != null) {
            val (minValue, maxValue) = displayTarget
            val yTop = yOf(maxValue)
            val yBottom = yOf(minValue)
            drawRect(
                color = bandColor,
                topLeft = Offset(plotLeft, yTop),
                size = Size(plotWidth, (yBottom - yTop).coerceAtLeast(0f))
            )
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 6.dp.toPx()))
            drawLine(
                color = edgeColor,
                start = Offset(plotLeft, yTop),
                end = Offset(plotRight, yTop),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = dashEffect
            )
            drawLine(
                color = edgeColor,
                start = Offset(plotLeft, yBottom),
                end = Offset(plotRight, yBottom),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = dashEffect
            )
            edgeLayouts?.let { (topLayout, bottomLayout) ->
                drawText(
                    textLayoutResult = topLayout,
                    color = edgeNumberColor,
                    topLeft = Offset(
                        x = plotLeft + 4.dp.toPx(),
                        y = (yTop - topLayout.size.height - 2.dp.toPx()).coerceAtLeast(0f)
                    )
                )
                drawText(
                    textLayoutResult = bottomLayout,
                    color = edgeNumberColor,
                    topLeft = Offset(
                        x = plotLeft + 4.dp.toPx(),
                        y = (yBottom + 3.dp.toPx()).coerceAtMost(size.height - bottomLayout.size.height)
                    )
                )
            }
        }
        // 折线 + 贴合折线形状的面积渐变（从数据最高点向下渐隐）
        val points = values.mapIndexed { index, value -> Offset(xOf(index), yOf(value)) }
        val dataTopY = points.minOf { it.y }
        val linePath = Path().apply {
            points.forEachIndexed { index, point ->
                if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
            }
        }
        val areaPath = Path().apply {
            addPath(linePath)
            lineTo(points.last().x, size.height)
            lineTo(points.first().x, size.height)
            close()
        }
        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.24f), Color.Transparent),
                startY = dataTopY,
                endY = size.height
            )
        )
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(
                width = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        // 末端点：surface 芯 + primary 环，右缘留白防裁切
        points.last().let { point ->
            drawCircle(color = dotCoreColor, radius = 4.dp.toPx(), center = point)
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = point,
                style = Stroke(width = 2.dp.toPx())
            )
        }
        // 按住/横拖：竖直准线 + 高亮点 + 数值气泡（点靠上时气泡翻到下方）
        val index = scrubIndex ?: return@Canvas
        val layout = bubbleLayout ?: return@Canvas
        val point = points.getOrNull(index) ?: return@Canvas
        drawLine(
            color = crosshairColor,
            start = Offset(point.x, 0f),
            end = Offset(point.x, size.height),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 6.dp.toPx()))
        )
        drawCircle(color = dotCoreColor, radius = 5.dp.toPx(), center = point)
        drawCircle(
            color = lineColor,
            radius = 5.dp.toPx(),
            center = point,
            style = Stroke(width = 2.5.dp.toPx())
        )
        val bubbleWidth = layout.size.width + 20.dp.toPx()
        val bubbleHeight = 22.dp.toPx()
        val bubbleX = (point.x - bubbleWidth / 2).coerceIn(
            plotLeft,
            (plotRight - bubbleWidth).coerceAtLeast(plotLeft)
        )
        val bubbleAboveY = point.y - 10.dp.toPx() - bubbleHeight
        val bubbleY = if (bubbleAboveY >= 0f) {
            bubbleAboveY
        } else {
            (point.y + 10.dp.toPx()).coerceAtMost(size.height - bubbleHeight)
        }
        drawRoundRect(
            color = bubbleColor,
            topLeft = Offset(bubbleX, bubbleY),
            size = Size(bubbleWidth, bubbleHeight),
            cornerRadius = CornerRadius(11.dp.toPx())
        )
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(
                bubbleX + 10.dp.toPx(),
                bubbleY + (bubbleHeight - layout.size.height) / 2f
            )
        )
    }
}
