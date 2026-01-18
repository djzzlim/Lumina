package com.example.lumina.core.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
        return Room.databaseBuilder(
                context,
                LuminaDatabase::class.java,
                "lumina-database"
            ).addMigrations(LuminaDatabase.MIGRATION_1_2, LuminaDatabase.MIGRATION_2_3)
            .fallbackToDestructiveMigration(false).build()
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
