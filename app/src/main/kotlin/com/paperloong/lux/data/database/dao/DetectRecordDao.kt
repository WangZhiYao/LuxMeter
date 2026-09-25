package com.paperloong.lux.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.paperloong.lux.data.database.entity.DetectRecordEntity
import kotlinx.coroutines.flow.Flow

/**
 *
 *
 * @author WangZhiYao
 * @since 2024/4/27
 */
@Dao
interface DetectRecordDao : IDao<DetectRecordEntity> {

    @Query(
        """
        SELECT * FROM detect_record
        WHERE (:location IS NULL OR location = :location)
          AND (:search = '' OR remark LIKE '%' || :search || '%' OR location LIKE '%' || :search || '%')
        ORDER BY create_time DESC
        """
    )
    fun observeDetectRecordList(location: String?, search: String): Flow<List<DetectRecordEntity>>

    @Query(
        """
        SELECT * FROM detect_record
        WHERE (:location IS NULL OR location = :location)
          AND (:search = '' OR remark LIKE '%' || :search || '%' OR location LIKE '%' || :search || '%')
        ORDER BY create_time DESC
        """
    )
    suspend fun getDetectRecordList(location: String?, search: String): List<DetectRecordEntity>

    @Query(
        """
        SELECT location FROM detect_record
        WHERE location != ''
        GROUP BY location
        ORDER BY MAX(create_time) DESC
        """
    )
    fun observeLocationList(): Flow<List<String>>

    @Query("SELECT * FROM detect_record ORDER BY create_time DESC LIMIT :limit")
    fun observeRecentRecordList(limit: Int): Flow<List<DetectRecordEntity>>

    @Query("DELETE FROM detect_record")
    suspend fun deleteAllRecord(): Int
}
