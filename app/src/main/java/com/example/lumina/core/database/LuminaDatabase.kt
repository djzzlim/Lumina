package com.example.lumina.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [LuminaInfo::class, Profile::class], version = 3, exportSchema = false)
abstract class LuminaDatabase : RoomDatabase() {
    abstract fun luminaDao(): LuminaDao
    abstract fun profileDao(): ProfileDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the profileId column as nullable initially
                database.execSQL("ALTER TABLE luminas ADD COLUMN profileId TEXT")
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `profiles` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, PRIMARY KEY(`id`))")
            }
        }
    }
}
