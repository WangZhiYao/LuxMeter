package com.paperloong.lux

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.paperloong.lux.data.database.LuxMeterDatabase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 *
 *
 * @author WangZhiYao
 * @since 2026/9/19
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        LuxMeterDatabase::class.java
    )

    @Test
    fun migrate1To2_keepsDataAndDefaultsLocation() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL(
            "INSERT INTO detect_record (value, unit, remark, create_time) VALUES (100.0, 'LUX', '旧备注', 123)"
        )
        db.close()

        // auto migration 由 helper 根据 database class 自动携带，无需显式传入
        db = helper.runMigrationsAndValidate(TEST_DB, 2, true)
        db.query("SELECT value, remark, location FROM detect_record").use { cursor ->
            cursor.moveToFirst()
            assertEquals(100.0, cursor.getDouble(0), 0.01)
            assertEquals("旧备注", cursor.getString(1))
            assertEquals("", cursor.getString(2))
        }
        db.close()
    }

    companion object {

        private const val TEST_DB = "migration-test.db"
    }
}
