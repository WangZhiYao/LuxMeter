package com.paperloong.lux.data.database.dao

import androidx.room.Delete
import androidx.room.Insert

/**
 *
 *
 * @author WangZhiYao
 * @since 2024/4/27
 */
interface IDao<T> {

    @Insert
    suspend fun insert(item: T): Long

    @Delete
    suspend fun delete(item: T): Int

}