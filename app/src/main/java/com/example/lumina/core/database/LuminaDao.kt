package com.example.lumina.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LuminaDao {
    @Insert
    suspend fun insert(luminaInfo: LuminaInfo)

    @Update
    suspend fun update(luminaInfo: LuminaInfo)

    @Query("SELECT * FROM luminas WHERE profileId = :profileId")
    fun getAll(profileId: String): Flow<List<LuminaInfo>>

    @Query("SELECT * FROM luminas WHERE id = :id")
    fun getById(id: Long): Flow<LuminaInfo>

    @Query("DELETE FROM luminas WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}
