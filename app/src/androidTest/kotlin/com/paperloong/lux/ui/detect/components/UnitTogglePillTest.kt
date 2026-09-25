package com.paperloong.lux.ui.detect.components

import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.paperloong.lux.constant.IlluminanceUnit
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * 单位切换胶囊：渲染与点击回调。
 *
 * @author WangZhiYao
 * @since 2026/9/19
 */
class UnitTogglePillTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersAllUnits() {
        composeRule.setContent {
            UnitTogglePill(unit = IlluminanceUnit.LUX, onUnitClick = {})
        }
        composeRule.onNodeWithText("LUX").assertExists()
        composeRule.onNodeWithText("FC").assertExists()
        composeRule.onNodeWithText("LUX").assertIsSelected()
        composeRule.onNodeWithText("FC").assertIsNotSelected()
    }

    @Test
    fun clickUnit_invokesCallback() {
        val clicked = mutableListOf<IlluminanceUnit>()
        composeRule.setContent {
            UnitTogglePill(unit = IlluminanceUnit.LUX, onUnitClick = { clicked.add(it) })
        }
        composeRule.onNodeWithText("FC").performClick()
        composeRule.runOnIdle {
            assertEquals(listOf(IlluminanceUnit.FC), clicked)
        }
    }
}
