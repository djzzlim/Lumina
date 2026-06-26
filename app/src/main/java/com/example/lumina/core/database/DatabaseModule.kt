package com.example.lumina.core.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

/**
 * Hilt module that provides database-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides a singleton instance of [LuminaDatabase].
     *
     * @param context The application context.
     * @return The built [LuminaDatabase] instance.
     */
    @Provides
    @Singleton
    fun provideLuminaDatabase(@ApplicationContext context: Context): LuminaDatabase {
        // Get or create the secure encryption key
        val passphrase = SecurityUtils.getOrCreateDatabaseKey()
        val factory = SupportFactory(passphrase)

        return Room.databaseBuilder(
                context,
                LuminaDatabase::class.java,
                "lumina-database-v2" // Changed name to avoid "file is not a database" error with existing unencrypted DB
            )
            .openHelperFactory(factory) // Use SQLCipher for encryption
            .addMigrations(
                LuminaDatabase.MIGRATION_1_2, 
                LuminaDatabase.MIGRATION_2_3,
                LuminaDatabase.MIGRATION_3_4,
                LuminaDatabase.MIGRATION_4_5,
                LuminaDatabase.MIGRATION_5_6,
                LuminaDatabase.MIGRATION_6_7,
                LuminaDatabase.MIGRATION_7_8
            )
            .fallbackToDestructiveMigration(false)
            .build()
    }

    /**
     * Provides the [LuminaDao] from the database instance.
     *
     * @param database The [LuminaDatabase] instance.
     * @return The [LuminaDao] instance.
     */
    @Provides
    fun provideLuminaDao(database: LuminaDatabase): LuminaDao {
        return database.luminaDao()
    }

    /**
     * Provides the [ProfileDao] from the database instance.
     *
     * @param database The [LuminaDatabase] instance.
     * @return The [ProfileDao] instance.
     */
    @Provides
    fun provideProfileDao(database: LuminaDatabase): ProfileDao {
        return database.profileDao()
    }
}
