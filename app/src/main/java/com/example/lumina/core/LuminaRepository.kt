package com.example.lumina.core

import com.example.lumina.core.database.LuminaDao
import com.example.lumina.core.database.LuminaInfo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LuminaRepository @Inject constructor(private val luminaDao: LuminaDao) {

    fun getAllLuminas(profileId: String): Flow<List<LuminaInfo>> = luminaDao.getAll(profileId)

    fun getLuminaById(id: Long): Flow<LuminaInfo> = luminaDao.getById(id)

    suspend fun insertLumina(luminaInfo: LuminaInfo) {
        luminaDao.insert(luminaInfo)
    }

    suspend fun deleteLuminasByIds(ids: List<Long>) {
        luminaDao.deleteByIds(ids)
    }
}
