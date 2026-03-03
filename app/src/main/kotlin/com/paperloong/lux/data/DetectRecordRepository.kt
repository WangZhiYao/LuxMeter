package com.paperloong.lux.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
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

    fun observeDetectRecordList(): Flow<PagingData<DetectRecord>> =
        Pager(PAGING_CONFIG) {
            detectRecordDao.observeDetectRecordList()
        }
            .flow
            .map { pagingData ->
                pagingData.map { entity -> detectRecordMapper.mapToModel(entity) }
            }
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

        val PAGING_CONFIG: PagingConfig =
            PagingConfig(
                pageSize = 20,
                prefetchDistance = 3,
                enablePlaceholders = false
            )

    }
}