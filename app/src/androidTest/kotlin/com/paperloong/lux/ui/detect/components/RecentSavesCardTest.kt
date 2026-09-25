package com.paperloong.lux.ui.detect.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.paperloong.lux.constant.IlluminanceUnit
import com.paperloong.lux.model.DetectRecord
import org.junit.Rule
import org.junit.Test

/**
 * 最近保存卡：空态与记录行渲染。
 *
 * @author WangZhiYao
 * @since 2026/9/19
 */
class RecentSavesCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyRecords_showsEmptyText() {
        composeRule.setContent {
            RecentSavesCard(
                records = emptyList(),
                valueTextOf = { "" },
                timeTextOf = { "" },
                titleText = "最近保存",
                viewAllText = "查看全部 ›",
                emptyText = "暂无保存记录",
                onViewAllClick = {}
            )
        }
        composeRule.onNodeWithText("暂无保存记录").assertExists()
        composeRule.onNodeWithText("查看全部 ›").assertExists()
    }

    @Test
    fun withRecords_showsRecordRow_notEmptyText() {
        val record = DetectRecord(
            value = 328f,
            unit = IlluminanceUnit.LUX,
            location = "客厅",
            createTime = 0L
        )
        composeRule.setContent {
            RecentSavesCard(
                records = listOf(record),
                valueTextOf = { "328" },
                timeTextOf = { "14:32" },
                titleText = "最近保存",
                viewAllText = "查看全部 ›",
                emptyText = "暂无保存记录",
                onViewAllClick = {}
            )
        }
        composeRule.onNodeWithText("328").assertExists()
        composeRule.onNodeWithText("客厅").assertExists()
        composeRule.onNodeWithText("14:32").assertExists()
        composeRule.onNodeWithText("暂无保存记录").assertDoesNotExist()
    }
}
