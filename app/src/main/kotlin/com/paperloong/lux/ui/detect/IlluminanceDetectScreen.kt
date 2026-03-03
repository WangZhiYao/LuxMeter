package com.paperloong.lux.ui.detect

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.keepScreenOn
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.paperloong.lux.R
import com.paperloong.lux.constant.IlluminanceUnit
import com.paperloong.lux.ext.formatToDateString
import com.paperloong.lux.model.DetectRecord
import com.paperloong.lux.ui.Screen
import com.paperloong.lux.ui.theme.LuxMeterTheme
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

    var showAddRecordDialog by remember { mutableStateOf(false) }

    IlluminanceDetectContent(
        state,
        Modifier,
        snackbarHostState,
        onRefreshClick = { viewModel.refreshData() },
        onRecordClick = { navController.navigate(Screen.DetectRecord.route) },
        onUnitClick = { unit -> viewModel.setIlluminanceUnit(unit) },
        onAddRecordClick = { showAddRecordDialog = true }
    )

    if (showAddRecordDialog) {
        AddRecordDialog(
            state = state,
            onConfirmClick = { detectRecord ->
                showAddRecordDialog = false
                viewModel.attemptAddRecord(detectRecord)
            },
            onDismissRequest = { showAddRecordDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IlluminanceDetectContent(
    state: IlluminanceDetectUiState,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onRefreshClick: () -> Unit = {},
    onRecordClick: () -> Unit = {},
    onUnitClick: (IlluminanceUnit) -> Unit = {},
    onAddRecordClick: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier.fillMaxSize().keepScreenOn(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                modifier = modifier,
                actions = {
                    IconButton(onClick = onRefreshClick) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = stringResource(id = R.string.refresh_record)
                        )
                    }
                    IconButton(onClick = onRecordClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_rounded_assignment_24dp),
                            contentDescription = "Localized description"
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = stringResource(id = R.string.record)) },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = ""
                    )
                },
                onClick = onAddRecordClick,
                modifier = modifier
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        ConstraintLayout(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val (valueCard, illuminance, unitGroup) = createRefs()

            ValueCardCombination(
                state.unit.format(state.min),
                state.unit.format(state.avg),
                state.unit.format(state.max),
                modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .constrainAs(valueCard) {
                        top.linkTo(parent.top, margin = 8.dp)
                    }
            )

            Illuminance(
                state.unit.format(state.current),
                modifier.constrainAs(illuminance) {
                    top.linkTo(valueCard.bottom, margin = 144.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            )

            IlluminanceUnitGroup(
                onUnitClick,
                state.unit,
                modifier.constrainAs(unitGroup) {
                    top.linkTo(illuminance.bottom, margin = 64.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            )
        }
    }
}

@Composable
fun ValueCardCombination(
    min: String,
    avg: String,
    max: String,
    modifier: Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ValueCard(
            title = stringResource(id = R.string.min),
            value = min,
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
        )
        ValueCard(
            title = stringResource(id = R.string.avg),
            value = avg,
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
        )
        ValueCard(
            title = stringResource(id = R.string.max),
            value = max,
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
        )
    }
}

@Composable
fun ValueCard(title: String, value: String, modifier: Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                textAlign = TextAlign.Center,
                maxLines = 1,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = value,
                modifier = Modifier.padding(top = 4.dp),
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun Illuminance(value: String, modifier: Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            maxLines = 1,
            style = MaterialTheme.typography.headlineLarge
        )
    }
}

@Composable
fun IlluminanceUnitGroup(
    onUnitClick: (IlluminanceUnit) -> Unit,
    unit: IlluminanceUnit?,
    modifier: Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val selectedColor = MaterialTheme.colorScheme.primaryContainer

        FilledTonalButton(
            onClick = { onUnitClick(IlluminanceUnit.LUX) },
            modifier = Modifier
                .width(128.dp)
                .height(48.dp),
            border = if (unit == IlluminanceUnit.LUX) BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primaryContainer
            ) else null
        ) {
            Text(
                text = IlluminanceUnit.LUX.name,
                color = if (unit == IlluminanceUnit.LUX) selectedColor else Color.Unspecified,
                style = MaterialTheme.typography.labelLarge
            )
        }
        FilledTonalButton(
            onClick = { onUnitClick(IlluminanceUnit.FC) },
            modifier = Modifier
                .width(128.dp)
                .height(48.dp),
            border = if (unit == IlluminanceUnit.FC) BorderStroke(
                1.dp,
                selectedColor
            ) else null
        ) {
            Text(
                text = IlluminanceUnit.FC.name,
                color = if (unit == IlluminanceUnit.FC) selectedColor else Color.Unspecified,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun AddRecordDialog(
    state: IlluminanceDetectUiState,
    onConfirmClick: (DetectRecord) -> Unit,
    onDismissRequest: () -> Unit = {}
) {
    val detectRecord by remember {
        mutableStateOf(state.run {
            DetectRecord(
                value = current,
                unit = unit,
                createTime = time
            )
        })
    }
    var remark by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                onConfirmClick(detectRecord.copy(remark = remark))
            }) {
                Text(text = stringResource(id = R.string.confirm))
            }
        },
        title = {
            Text(text = stringResource(id = R.string.dialog_title_add_record))
        },
        text = {
            Column {
                Text(
                    text = stringResource(
                        id = R.string.dialog_text_current_value,
                        detectRecord.unit.format(detectRecord.value)
                    )
                )
                Text(
                    text = stringResource(
                        id = R.string.dialog_text_current_unit,
                        detectRecord.unit
                    ),
                    modifier = Modifier.padding(vertical = 6.dp)
                )
                Text(
                    text = stringResource(
                        id = R.string.dialog_text_current_time,
                        detectRecord.createTime.formatToDateString()
                    )
                )
                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    modifier = Modifier.padding(top = 6.dp),
                    label = { Text(text = stringResource(id = R.string.remark)) }
                )
            }
        }
    )
}

@Preview
@Composable
fun LuxMeterDetectScreenPreview() {
    LuxMeterTheme {
        val state = IlluminanceDetectUiState(
            100f,
            200f,
            300f,
            300f,
            IlluminanceUnit.LUX
        )
        IlluminanceDetectContent(state)
    }
}
