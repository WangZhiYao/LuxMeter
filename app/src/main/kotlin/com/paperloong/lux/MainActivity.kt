package com.paperloong.lux

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.paperloong.lux.ui.LuxMeterApp
import com.paperloong.lux.ui.theme.LuxMeterTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 *
 *
 * @author WangZhiYao
 * @since 2024/4/23
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LuxMeterTheme {
                LuxMeterApp()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    LuxMeterTheme {
        LuxMeterApp()
    }
}


