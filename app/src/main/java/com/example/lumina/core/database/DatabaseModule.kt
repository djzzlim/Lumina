package com.example.lumina.core.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

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

    @Provides
    fun provideLuminaDao(database: LuminaDatabase): LuminaDao {
        return database.luminaDao()
    }

    @Provides
    fun provideProfileDao(database: LuminaDatabase): ProfileDao {
        return database.profileDao()
    }
}
