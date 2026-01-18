package com.example.lumina.core

import com.example.lumina.core.database.LuminaDao
import com.example.lumina.core.database.LuminaInfo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository class that manages [LuminaInfo] data operations.
 *
 * This class provides a centralized point for accessing and modifying lumina information,
 * acting as an abstraction layer over the [LuminaDao].
 *
 * @property luminaDao The Data Access Object for lumina info operations.
 */
@Singleton
class LuminaRepository @Inject constructor(private val luminaDao: LuminaDao) {

    /**
     * Returns a [Flow] of all [LuminaInfo] entries associated with a specific profile.
     *
     * @param profileId The ID of the profile for which to retrieve lumina info.
     * @return A flow emitting a list of lumina info entries.
     */
    fun getAllLuminas(profileId: String): Flow<List<LuminaInfo>> = luminaDao.getAll(profileId)

    /**
     * Retrieves a specific [LuminaInfo] entry by its ID.
     *
     * @param id The ID of the lumina info entry to retrieve.
     * @return A flow emitting the requested lumina info.
     */
    fun getLuminaById(id: Long): Flow<LuminaInfo> = luminaDao.getById(id)

    /**
     * Inserts a new [LuminaInfo] entry into the database.
     *
     * @param luminaInfo The lumina info to insert.
     */
    suspend fun insertLumina(luminaInfo: LuminaInfo) {
        luminaDao.insert(luminaInfo)
    }

    /**
     * Deletes [LuminaInfo] entries with the specified IDs.
     *
     * @param ids The list of IDs of the entries to delete.
     */
    suspend fun deleteLuminasByIds(ids: List<Long>) {
        luminaDao.deleteByIds(ids)
    }
}
