package com.paperloong.lux.ui.record

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.paperloong.lux.R
import com.paperloong.lux.constant.IlluminanceUnit
import com.paperloong.lux.ext.formatToDateString
import com.paperloong.lux.model.DetectRecord
import com.paperloong.lux.ui.theme.LuxMeterTheme
import com.paperloong.lux.ui.widget.AppCommonDialog
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
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
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is Snack -> {
                snackbarHostState.showSnackbar(sideEffect.message)
            }
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    DetectRecordContent(
        viewModel.detectRecordList.collectAsLazyPagingItems(),
        Modifier,
        snackbarHostState,
        onConfirmClick = { detectRecord -> viewModel.attemptRemoveRecord(detectRecord) },
        onNavBackClick = { navController.navigateUp() },
        onRemoveAllRecordClick = {
            showDeleteDialog = true
        }
    )

    if (showDeleteDialog) {
        ConfirmDialog(
            title = stringResource(id = R.string.dialog_title_tip),
            text = stringResource(id = R.string.dialog_text_remove_all_detect_record),
            onConfirmClick = {
                viewModel.attemptRemoveAllRecord()
                showDeleteDialog = false
            },
            onDismissRequest = { showDeleteDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DetectRecordContent(
    detectRecordList: LazyPagingItems<DetectRecord>,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onConfirmClick: (DetectRecord) -> Unit = {},
    onNavBackClick: () -> Unit = {},
    onRemoveAllRecordClick: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.record)) },
                navigationIcon = {
                    IconButton(onClick = { onNavBackClick() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRemoveAllRecordClick) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = stringResource(id = R.string.remove_all_record)
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxHeight(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(
                count = detectRecordList.itemCount,
                key = detectRecordList.itemKey { item -> item.id }
            ) { index ->
                val item = detectRecordList[index] ?: return@items
                DetectRecordItem(
                    detectRecord = item,
                    modifier = modifier.animateItem(),
                    onConfirmClick = onConfirmClick
                )
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
        modifier = modifier
            .fillMaxWidth()
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.errorContainer),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                Icons.Outlined.Delete,
                contentDescription = "Localized description",
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
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(modifier = modifier.fillMaxWidth()) {
                Text(
                    text = detectRecord.unit.format(detectRecord.value),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "${detectRecord.unit}",
                    modifier = Modifier.align(Alignment.TopEnd),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            if (detectRecord.remark.isNotBlank()) {
                Text(
                    text = detectRecord.remark,
                    modifier = Modifier.padding(vertical = 8.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Text(
                text = detectRecord.createTime.formatToDateString(),
                modifier = Modifier.align(Alignment.End),
                style = MaterialTheme.typography.labelMedium
            )
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
            flowOf(
                PagingData.from(
                    listOf(
                        DetectRecord(1, 123.0f, IlluminanceUnit.LUX, "123"),
                        DetectRecord(2, 45689.0f, IlluminanceUnit.LUX)
                    )
                )
            ).collectAsLazyPagingItems()
        )
    }
}