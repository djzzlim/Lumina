package com.example.lumina.core

import com.example.lumina.core.data.LuminaInfo
import com.example.lumina.core.database.LuminaDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LuminaRepository @Inject constructor(private val luminaDao: LuminaDao) {

    fun getAllLuminas(): Flow<List<LuminaInfo>> = luminaDao.getAll()

    fun getLuminaById(id: Long): Flow<LuminaInfo> = luminaDao.getById(id)

    suspend fun insertLumina(luminaInfo: LuminaInfo) {
        luminaDao.insert(luminaInfo)
    }

    suspend fun deleteLuminasByIds(ids: List<Long>) {
        luminaDao.deleteByIds(ids)
    }
}
