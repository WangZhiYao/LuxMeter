package com.paperloong.lux.data.database.mapper

import com.paperloong.lux.data.database.entity.DetectRecordEntity
import com.paperloong.lux.model.DetectRecord
import javax.inject.Inject

/**
 *
 *
 * @author WangZhiYao
 * @since 2024/4/29
 */
class DetectRecordMapper @Inject constructor() : IEntityMapper<DetectRecordEntity, DetectRecord> {

    override fun mapToEntity(model: DetectRecord): DetectRecordEntity =
        model.run { DetectRecordEntity(id, value, unit, remark, createTime) }

    override fun mapToModel(entity: DetectRecordEntity): DetectRecord =
        entity.run { DetectRecord(id, value, unit, remark, createTime) }

}
