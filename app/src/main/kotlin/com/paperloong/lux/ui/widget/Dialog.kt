package com.paperloong.lux.ui.widget

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paperloong.lux.R
import com.paperloong.lux.ui.theme.LuxMeterTheme

/**
 *
 *
 * @author WangZhiYao
 * @since 2024/4/28
 */
@Composable
fun AppCommonDialog(
    onDismissRequest: () -> Unit,
    confirm: String,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    title: String? = null,
    text: String? = null,
    dismiss: String? = null,
    onDismissClick: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirmClick) {
                Text(text = confirm)
            }
        },
        modifier = modifier,
        dismissButton = {
            if (!dismiss.isNullOrBlank()) {
                TextButton(onClick = onDismissClick) {
                    Text(text = dismiss)
                }
            }
        },
        icon = icon,
        title = {
            if (!title.isNullOrBlank()) {
                Text(text = title)
            }
        },
        text = {
            if (!text.isNullOrBlank()) {
                Text(text = text)
            }
        }
    )
}

@Preview
@Composable
fun AppCommonDialogPreview() {
    LuxMeterTheme {
        AppCommonDialog(
            onDismissRequest = {},
            modifier = Modifier,
            icon = null,
            title = "提示",
            text = "提示消息",
            confirm = stringResource(id = R.string.confirm),
            onConfirmClick = {},
            dismiss = null,
            onDismissClick = {}
        )
    }
}