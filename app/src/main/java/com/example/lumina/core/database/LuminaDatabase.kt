package com.example.lumina.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * The Room database for this app.
 *
 * It contains [LuminaInfo] and [Profile] entities.
 */
@Database(entities = [LuminaInfo::class, Profile::class], version = 3, exportSchema = false)
abstract class LuminaDatabase : RoomDatabase() {
    /**
     * Gets the DAO for lumina-related operations.
     */
    abstract fun luminaDao(): LuminaDao

    /**
     * Gets the DAO for profile-related operations.
     */
    abstract fun profileDao(): ProfileDao

    companion object {
        /**
         * Migration from version 1 to 2.
         * Adds the `profileId` column to the `luminas` table.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the profileId column as nullable initially
                database.execSQL("ALTER TABLE luminas ADD COLUMN profileId TEXT")
            }
        }

        /**
         * Migration from version 2 to 3.
         * Creates the `profiles` table.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `profiles` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, PRIMARY KEY(`id`))")
            }
        }
    }
}
