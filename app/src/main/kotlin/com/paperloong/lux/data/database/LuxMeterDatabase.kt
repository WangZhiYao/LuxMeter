package com.paperloong.lux.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.paperloong.lux.data.database.dao.DetectRecordDao
import com.paperloong.lux.data.database.entity.DetectRecordEntity

/**
 *
 *
 * @author WangZhiYao
 * @since 2024/4/27
 */
@Database(
    entities = [DetectRecordEntity::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
abstract class LuxMeterDatabase : RoomDatabase() {

    abstract fun detectRecordDao(): DetectRecordDao
}
