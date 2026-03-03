package com.paperloong.lux.ui

import androidx.navigation.NamedNavArgument

/**
 *
 *
 * @author WangZhiYao
 * @since 2024/4/23
 */
sealed class Screen(
    val route: String,
    val navArguments: List<NamedNavArgument> = emptyList()
) {

    data object IlluminanceDetect : Screen("detect")

    data object DetectRecord : Screen("record")

}