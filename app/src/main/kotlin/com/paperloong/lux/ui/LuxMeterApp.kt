package com.paperloong.lux.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.paperloong.lux.ui.detect.IlluminanceDetectScreen
import com.paperloong.lux.ui.record.DetectRecordScreen

/**
 *
 *
 * @author WangZhiYao
 * @since 2024/4/23
 */
@Composable
fun LuxMeterApp() {
    val navController = rememberNavController()
    LuxMeterNavHost(navController = navController)
}

@Composable
fun LuxMeterNavHost(navController: NavHostController) {
    val snackbarHostState = remember { SnackbarHostState() }
    NavHost(navController = navController, startDestination = Screen.IlluminanceDetect.route) {
        composable(route = Screen.IlluminanceDetect.route) {
            IlluminanceDetectScreen(
                snackbarHostState = snackbarHostState,
                navController = navController
            )
        }

        composable(route = Screen.DetectRecord.route) {
            DetectRecordScreen(snackbarHostState = snackbarHostState, navController = navController)
        }
    }
}