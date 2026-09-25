package com.paperloong.lux.ui.record

import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.paperloong.lux.R
import com.paperloong.lux.constant.IlluminanceUnit
import com.paperloong.lux.ext.dayOffsetFromToday
import com.paperloong.lux.ext.formatToShortDate
import com.paperloong.lux.ext.formatToShortTime
import com.paperloong.lux.model.DetectRecord
import com.paperloong.lux.ui.theme.LuxMeterTheme
import com.paperloong.lux.ui.widget.AppCommonDialog
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

/**
 *
 *
 * @author WangZhiYao
 * @since 2024/4/26
 */
@Composable
fun DetectRecordScreen(
    viewModel: DetectRecordViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
    navController: NavController
) {
    val context = LocalContext.current
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is Snack -> {
                snackbarHostState.showSnackbar(sideEffect.message)
            }

            is ShareCsv -> {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, sideEffect.uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(sendIntent, null))
            }
        }
    }

    val state by viewModel.collectAsState()
    var showRemoveAllDialog by remember { mutableStateOf(false) }

    DetectRecordContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onNavBackClick = { navController.navigateUp() },
        onSearchChange = viewModel::setSearch,
        onSelectLocation = viewModel::selectLocation,
        onExportClick = viewModel::exportRecords,
        onRemoveAllClick = { showRemoveAllDialog = true },
        onConfirmRemoveClick = viewModel::attemptRemoveRecord
    )

    if (showRemoveAllDialog) {
        ConfirmDialog(
            title = stringResource(id = R.string.dialog_title_tip),
            text = stringResource(id = R.string.dialog_text_remove_all_detect_record),
            onConfirmClick = {
                viewModel.attemptRemoveAllRecord()
                showRemoveAllDialog = false
            },
            onDismissRequest = { showRemoveAllDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectRecordContent(
    state: DetectRecordUiState,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onNavBackClick: () -> Unit = {},
    onSearchChange: (String) -> Unit = {},
    onSelectLocation: (String?) -> Unit = {},
    onExportClick: () -> Unit = {},
    onRemoveAllClick: () -> Unit = {},
    onConfirmRemoveClick: (DetectRecord) -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val todayText = stringResource(id = R.string.today)
    val yesterdayText = stringResource(id = R.string.yesterday)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.record_list)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onExportClick) {
                        Icon(
                            imageVector = Icons.Rounded.UploadFile,
                            contentDescription = stringResource(id = R.string.export)
                        )
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = stringResource(id = R.string.remove_all_record)
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(text = stringResource(id = R.string.remove_all_record)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Delete,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onRemoveAllClick()
                                }
                            )
                        }
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
        ) {
            OutlinedTextField(
                value = state.search,
                onValueChange = onSearchChange,
                placeholder = {
                    Text(text = stringResource(id = R.string.search_hint))
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.selectedLocation == null,
                    onClick = { onSelectLocation(null) },
                    label = {
                        Text(
                            text = if (state.selectedLocation == null && state.search.isBlank()) {
                                stringResource(
                                    id = R.string.filter_all_with_count,
                                    state.records.size
                                )
                            } else {
                                stringResource(id = R.string.filter_all)
                            },
                        )
                    }
                )
                state.locations.forEach { location ->
                    FilterChip(
                        selected = state.selectedLocation == location,
                        onClick = { onSelectLocation(location) },
                        label = {
                            Text(text = location)
                        }
                    )
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val groups = state.records
                    .groupBy { it.createTime.dayOffsetFromToday() }
                    .toList()
                groups.forEach { (dayOffset, records) ->
                    item(key = "header-$dayOffset-${records.firstOrNull()?.id}") {
                        Text(
                            text = when (dayOffset) {
                                0 -> todayText
                                1 -> yesterdayText
                                else -> records.firstOrNull()
                                    ?.createTime?.formatToShortDate().orEmpty()
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp, top = 6.dp, bottom = 4.dp)
                        )
                    }
                    items(records, key = { it.id }) { record ->
                        DetectRecordItem(
                            detectRecord = record,
                            modifier = Modifier.animateItem(),
                            onConfirmClick = onConfirmRemoveClick
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectRecordItem(
    detectRecord: DetectRecord,
    modifier: Modifier,
    onConfirmClick: (DetectRecord) -> Unit
) {
    val scope = rememberCoroutineScope()
    var confirmDialogState by remember { mutableStateOf(false) }
    val swipeToDismissBoxState = rememberSwipeToDismissBoxState()
    SwipeToDismissBox(
        state = swipeToDismissBoxState,
        backgroundContent = {
            DetectRecordItemBackground(
                dismissState = swipeToDismissBoxState,
                modifier = modifier
            )
        },
        enableDismissFromStartToEnd = false,
        onDismiss = {
            confirmDialogState = true
        }
    ) {
        DetectRecordItemContent(
            detectRecord = detectRecord,
            modifier = modifier
        )
    }

    if (confirmDialogState) {
        ConfirmDialog(
            title = stringResource(id = R.string.dialog_title_tip),
            text = stringResource(id = R.string.dialog_text_remove_current_detect_record),
            onConfirmClick = {
                onConfirmClick(detectRecord)
            },
            onDismissRequest = {
                confirmDialogState = false
                scope.launch {
                    swipeToDismissBoxState.reset()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectRecordItemBackground(dismissState: SwipeToDismissBoxState, modifier: Modifier) {
    val scale by animateFloatAsState(
        targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) 0.75f else 1f,
        label = ""
    )
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.errorContainer),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                Icons.Outlined.Delete,
                contentDescription = null,
                modifier = Modifier
                    .scale(scale)
                    .padding(end = 16.dp)
            )
        }
    }
}

@Composable
fun DetectRecordItemContent(detectRecord: DetectRecord, modifier: Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (detectRecord.location.isNotBlank()) {
                    Text(
                        text = detectRecord.location,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 9.dp, vertical = 3.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                } else {
                    Box(modifier = Modifier.weight(1f))
                }
                Text(
                    text = detectRecord.createTime.formatToShortTime(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Text(
                    text = detectRecord.unit.format(detectRecord.value),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = " " + detectRecord.unit.name.lowercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            if (detectRecord.remark.isNotBlank()) {
                Text(
                    text = detectRecord.remark,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    onConfirmClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    AppCommonDialog(
        onDismissRequest = onDismissRequest,
        confirm = stringResource(id = R.string.confirm),
        onConfirmClick = onConfirmClick,
        title = title,
        text = text,
        dismiss = stringResource(id = R.string.cancel),
        onDismissClick = onDismissRequest
    )
}

@Preview
@Composable
fun DetectRecordScreenPreview() {
    LuxMeterTheme {
        DetectRecordContent(
            state = DetectRecordUiState(
                records = listOf(
                    DetectRecord(
                        id = 1,
                        value = 123.0f,
                        unit = IlluminanceUnit.LUX,
                        remark = "备注",
                        location = "窗台"
                    ),
                    DetectRecord(id = 2, value = 45689.0f, unit = IlluminanceUnit.LUX)
                ),
                locations = listOf("窗台", "灯下 20cm")
            )
        )
    }
}
