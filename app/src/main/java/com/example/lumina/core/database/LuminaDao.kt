package com.example.lumina.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the `luminas` table.
 */
@Dao
interface LuminaDao {
    /**
     * Inserts a new [LuminaInfo] into the database.
     *
     * @param luminaInfo The lumina info to insert.
     */
    @Insert
    suspend fun insert(luminaInfo: LuminaInfo)

    /**
     * Updates an existing [LuminaInfo] in the database.
     *
     * @param luminaInfo The lumina info to update.
     */
    @Update
    suspend fun update(luminaInfo: LuminaInfo)

    /**
     * Retrieves all [LuminaInfo] entries associated with a specific profile.
     *
     * @param profileId The ID of the profile.
     * @return A flow of a list of lumina info entries.
     */
    @Query("SELECT * FROM luminas WHERE profileId = :profileId")
    fun getAll(profileId: String): Flow<List<LuminaInfo>>

    /**
     * Retrieves a specific [LuminaInfo] by its ID.
     *
     * @param id The ID of the lumina info entry.
     * @return A flow of the lumina info entry.
     */
    @Query("SELECT * FROM luminas WHERE id = :id")
    fun getById(id: Long): Flow<LuminaInfo>

    /**
     * Deletes multiple [LuminaInfo] entries by their IDs.
     *
     * @param ids The list of IDs of the entries to delete.
     */
    @Query("DELETE FROM luminas WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}
