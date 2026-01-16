package com.example.lumina.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.lumina.core.data.LuminaInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface LuminaDao {
    @Insert
    suspend fun insert(luminaInfo: LuminaInfo)

    @Update
    suspend fun update(luminaInfo: LuminaInfo)

    @Query("SELECT * FROM luminas")
    fun getAll(): Flow<List<LuminaInfo>>

    @Query("DELETE FROM luminas WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}
