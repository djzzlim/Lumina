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
@Database(entities = [LuminaInfo::class, Profile::class], version = 7, exportSchema = false)
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

        /**
         * Migration from version 3 to 4.
         * Adds the `disableJavascript` column to the `luminas` table.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE luminas ADD COLUMN disableJavascript INTEGER NOT NULL DEFAULT 0")
            }
        }

        /**
         * Migration from version 4 to 5.
         * Adds `pin` and `isDecoy` columns to the `profiles` table.
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE profiles ADD COLUMN pin TEXT")
                database.execSQL("ALTER TABLE profiles ADD COLUMN isDecoy INTEGER NOT NULL DEFAULT 0")
            }
        }

        /**
         * Migration from version 5 to 6.
         * Introduces the `vaults` table and updates `profiles` to reference a vault.
         */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. Create the vaults table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `vaults` (
                        `id` TEXT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `pin` TEXT NOT NULL, 
                        `isDecoy` INTEGER NOT NULL, 
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                // 2. Drop and recreate profiles table
                database.execSQL("DROP TABLE IF EXISTS `profiles`")
                database.execSQL("""
                    CREATE TABLE `profiles` (
                        `id` TEXT NOT NULL, 
                        `vaultId` TEXT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                // 3. Recreate luminas table
                database.execSQL("DROP TABLE IF EXISTS `luminas`")
                database.execSQL("""
                    CREATE TABLE `luminas` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `profileId` TEXT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `url` TEXT NOT NULL, 
                        `icon` TEXT NOT NULL, 
                        `color` INTEGER NOT NULL, 
                        `isWebRtcDisabled` INTEGER NOT NULL, 
                        `afpEnabled` INTEGER NOT NULL, 
                        `randomizeUserAgent` INTEGER NOT NULL, 
                        `spoofLocale` INTEGER NOT NULL, 
                        `spoofTimezone` INTEGER NOT NULL, 
                        `randomizeCanvas` INTEGER NOT NULL, 
                        `disableAudioContext` INTEGER NOT NULL, 
                        `disableWebGl` INTEGER NOT NULL, 
                        `randomizeScreen` INTEGER NOT NULL, 
                        `spoofHardware` INTEGER NOT NULL, 
                        `disablePayment` INTEGER NOT NULL, 
                        `disableJavascript` INTEGER NOT NULL,
                        FOREIGN KEY(`profileId`) REFERENCES `profiles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_luminas_profileId` ON `luminas` (`profileId`)")
            }
        }

        /**
         * Migration from version 6 to 7.
         * Removes Vaults and simplifies Profiles.
         */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS `vaults`")
                
                // Recreate profiles without vaultId
                database.execSQL("DROP TABLE IF EXISTS `profiles`")
                database.execSQL("""
                    CREATE TABLE `profiles` (
                        `id` TEXT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                // Recreate luminas pointing to simple profiles
                database.execSQL("DROP TABLE IF EXISTS `luminas`")
                database.execSQL("""
                    CREATE TABLE `luminas` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `profileId` TEXT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `url` TEXT NOT NULL, 
                        `icon` TEXT NOT NULL, 
                        `color` INTEGER NOT NULL, 
                        `isWebRtcDisabled` INTEGER NOT NULL, 
                        `afpEnabled` INTEGER NOT NULL, 
                        `randomizeUserAgent` INTEGER NOT NULL, 
                        `spoofLocale` INTEGER NOT NULL, 
                        `spoofTimezone` INTEGER NOT NULL, 
                        `randomizeCanvas` INTEGER NOT NULL, 
                        `disableAudioContext` INTEGER NOT NULL, 
                        `disableWebGl` INTEGER NOT NULL, 
                        `randomizeScreen` INTEGER NOT NULL, 
                        `spoofHardware` INTEGER NOT NULL, 
                        `disablePayment` INTEGER NOT NULL, 
                        `disableJavascript` INTEGER NOT NULL,
                        FOREIGN KEY(`profileId`) REFERENCES `profiles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_luminas_profileId` ON `luminas` (`profileId`)")
            }
        }
    }
}
