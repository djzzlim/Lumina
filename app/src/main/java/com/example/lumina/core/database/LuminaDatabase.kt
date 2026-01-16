package com.example.lumina.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.lumina.core.data.LuminaInfo

@Database(entities = [LuminaInfo::class], version = 1, exportSchema = false)
abstract class LuminaDatabase : RoomDatabase() {
    abstract fun luminaDao(): LuminaDao
}
