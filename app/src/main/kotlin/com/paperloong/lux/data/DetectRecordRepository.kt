package com.paperloong.lux.data

import com.paperloong.lux.data.database.dao.DetectRecordDao
import com.paperloong.lux.data.database.mapper.DetectRecordMapper
import com.paperloong.lux.di.qualifier.IODispatcher
import com.paperloong.lux.model.DetectRecord
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 *
 *
 * @author WangZhiYao
 * @since 2024/4/27
 */
class DetectRecordRepository @Inject constructor(
    private val detectRecordDao: DetectRecordDao,
    private val detectRecordMapper: DetectRecordMapper,
    @param:IODispatcher private val dispatcher: CoroutineDispatcher
) {

    fun insertDetectRecord(detectRecord: DetectRecord): Flow<DetectRecord> =
        flow {
            emit(detectRecordDao.insert(detectRecordMapper.mapToEntity(detectRecord)))
        }
            .map { id ->
                detectRecord.copy(id = id)
            }
            .flowOn(dispatcher)

    fun observeDetectRecordList(
        location: String? = null,
        search: String = ""
    ): Flow<List<DetectRecord>> =
        detectRecordDao.observeDetectRecordList(location, search)
            .map { list -> list.map { detectRecordMapper.mapToModel(it) } }
            .flowOn(dispatcher)

    suspend fun getDetectRecordList(
        location: String? = null,
        search: String = ""
    ): List<DetectRecord> =
        detectRecordDao.getDetectRecordList(location, search)
            .map { detectRecordMapper.mapToModel(it) }

    fun observeLocationList(): Flow<List<String>> =
        detectRecordDao.observeLocationList()
            .flowOn(dispatcher)

    fun observeRecentRecordList(limit: Int = RECENT_RECORD_LIMIT): Flow<List<DetectRecord>> =
        detectRecordDao.observeRecentRecordList(limit)
            .map { list -> list.map { detectRecordMapper.mapToModel(it) } }
            .flowOn(dispatcher)

    fun deleteDetectRecord(detectRecord: DetectRecord): Flow<Int> =
        flow {
            emit(detectRecordDao.delete(detectRecordMapper.mapToEntity(detectRecord)))
        }
            .flowOn(dispatcher)

    fun deleteAllRecord(): Flow<Int> =
        flow {
            emit(detectRecordDao.deleteAllRecord())
        }
            .flowOn(dispatcher)

    companion object {

        const val RECENT_RECORD_LIMIT = 2
    }
}
