package com.paperloong.lux.ui.detect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.keepScreenOn
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.paperloong.lux.R
import com.paperloong.lux.constant.IlluminanceUnit
import com.paperloong.lux.ext.dayOffsetFromToday
import com.paperloong.lux.ext.displayValue
import com.paperloong.lux.ext.formatToDateString
import com.paperloong.lux.ext.formatToShortDate
import com.paperloong.lux.ext.formatToShortTime
import com.paperloong.lux.ext.luxToFc
import com.paperloong.lux.model.IlluminanceJudgment
import com.paperloong.lux.ui.Screen
import com.paperloong.lux.ui.detect.components.HeroReadingCard
import com.paperloong.lux.ui.detect.components.RecentSavesCard
import com.paperloong.lux.ui.detect.components.SaveRecordDialog
import com.paperloong.lux.ui.detect.components.SessionTrendCard
import com.paperloong.lux.ui.detect.components.StatsBar
import com.paperloong.lux.ui.detect.components.TargetRangeDialog
import com.paperloong.lux.ui.detect.components.UnitTogglePill
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

/**
 *
 *
 * @author WangZhiYao
 * @since 2024/4/23
 */
@Composable
fun IlluminanceDetectScreen(
    viewModel: IlluminanceDetectViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
    navController: NavController
) {
    LifecycleResumeEffect(key1 = viewModel, lifecycleOwner = LocalLifecycleOwner.current) {
        viewModel.registerLightSensorEventListener()
        onPauseOrDispose {
            viewModel.unregisterLightSensorEventListener()
        }
    }

    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is Snack -> {
                snackbarHostState.showSnackbar(sideEffect.message)
            }
        }
    }

    var showSaveDialog by remember { mutableStateOf(false) }
    var showTargetDialog by remember { mutableStateOf(false) }
    var saveSnapshot by remember { mutableStateOf<Triple<Float, IlluminanceUnit, Long>?>(null) }

    IlluminanceDetectContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onUnitClick = viewModel::setIlluminanceUnit,
        onRecordClick = { navController.navigate(Screen.DetectRecord.route) },
        onJudgmentClick = { showTargetDialog = true },
        onRestartSessionClick = viewModel::restartSession,
        onViewAllClick = { navController.navigate(Screen.DetectRecord.route) },
        onSaveClick = {
            // 冻结弹窗数值快照，避免弹窗内数值随传感器跳动、存下非所见值
            saveSnapshot = Triple(displayValueOf(state.current, state.unit), state.unit, state.time)
            showSaveDialog = true
        }
    )

    if (showSaveDialog) {
        val snapshot = saveSnapshot
        if (snapshot != null) {
            SaveRecordDialog(
                valueText = snapshot.second.format(snapshot.first),
                unitLabel = snapshot.second.name,
                timeText = snapshot.third.formatToDateString(),
                locationSuggestions = state.locationSuggestions,
                onConfirmClick = { location, remark ->
                    showSaveDialog = false
                    viewModel.attemptAddRecord(
                        value = snapshot.first,
                        unit = snapshot.second,
                        time = snapshot.third,
                        location = location,
                        remark = remark
                    )
                },
                onDismissRequest = { showSaveDialog = false }
            )
        }
    }

    if (showTargetDialog) {
        TargetRangeDialog(
            initial = state.target,
            currentLux = state.current,
            onConfirmClick = { min, max ->
                showTargetDialog = false
                viewModel.setTargetRange(min, max)
            },
            onClearClick = {
                showTargetDialog = false
                viewModel.clearTargetRange()
            },
            onDismissRequest = { showTargetDialog = false }
        )
    }
}

private fun displayValueOf(lux: Float, unit: IlluminanceUnit): Float =
    if (unit == IlluminanceUnit.FC) lux.luxToFc() else lux

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IlluminanceDetectContent(
    state: IlluminanceDetectUiState,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onUnitClick: (IlluminanceUnit) -> Unit = {},
    onRecordClick: () -> Unit = {},
    onJudgmentClick: () -> Unit = {},
    onRestartSessionClick: () -> Unit = {},
    onViewAllClick: () -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .keepScreenOn(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.app_name)
                    )
                },
                actions = {
                    IconButton(onClick = onRecordClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ListAlt,
                            contentDescription = stringResource(id = R.string.record)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val unitLabel = state.unit.name.lowercase()
            val statusText = when (state.judgment) {
                null -> stringResource(id = R.string.set_target)
                IlluminanceJudgment.InRange -> stringResource(id = R.string.judgment_ok)
                is IlluminanceJudgment.TooLow -> stringResource(id = R.string.judgment_low)
                is IlluminanceJudgment.TooHigh -> stringResource(id = R.string.judgment_high)
            }
            val targetText = state.target?.let {
                stringResource(
                    id = R.string.target_range_format,
                    state.unit.format(displayValueOf(it.minLux, state.unit)),
                    state.unit.format(displayValueOf(it.maxLux, state.unit)),
                    unitLabel
                )
            }
            UnitTogglePill(
                unit = state.unit,
                onUnitClick = onUnitClick
            )
            HeroReadingCard(
                valueText = state.unit.format(displayValueOf(state.current, state.unit)),
                unitLabel = state.unit.name,
                judgment = state.judgment,
                statusText = statusText,
                targetText = targetText,
                onJudgmentClick = onJudgmentClick
            )
            StatsBar(
                minText = state.min?.let { state.unit.format(displayValueOf(it, state.unit)) }
                    ?: "–",
                avgText = state.avg?.let { state.unit.format(displayValueOf(it, state.unit)) }
                    ?: "–",
                maxText = state.max?.let { state.unit.format(displayValueOf(it, state.unit)) }
                    ?: "–"
            )
            // 换算 lambda 以 unit 为 key 记忆，避免每次重组生成新实例导致图内动画重启
            val toDisplayValue =
                remember(state.unit) { { lux: Float -> displayValueOf(lux, state.unit) } }
            val toValueText = remember(state.unit) { { value: Float -> state.unit.format(value) } }
            SessionTrendCard(
                trend = state.trend,
                target = state.target,
                topLabelText = stringResource(id = R.string.session_trend),
                hintText = stringResource(id = R.string.trend_scrub_hint),
                displayValueOf = toDisplayValue,
                valueTextOf = toValueText,
                onRestartClick = onRestartSessionClick
            )
            val yesterdayText = stringResource(id = R.string.yesterday)
            RecentSavesCard(
                records = state.recentRecords,
                valueTextOf = { record ->
                    state.unit.format(record.displayValue(state.unit))
                },
                timeTextOf = { record ->
                    when (record.createTime.dayOffsetFromToday()) {
                        0 -> record.createTime.formatToShortTime()
                        1 -> yesterdayText + " " + record.createTime.formatToShortTime()
                        else -> record.createTime.formatToShortDate() + " " + record.createTime.formatToShortTime()
                    }
                },
                titleText = stringResource(id = R.string.recent_saves),
                viewAllText = stringResource(id = R.string.view_all),
                emptyText = stringResource(id = R.string.recent_saves_empty),
                onViewAllClick = onViewAllClick
            )
            Button(
                onClick = onSaveClick,
                enabled = state.sessionCount > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 10.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "＋ " + stringResource(id = R.string.save_record),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
