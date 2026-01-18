package com.example.lumina.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the `profiles` table.
 */
@Dao
interface ProfileDao {
    /**
     * Retrieves all [Profile] entries from the database.
     *
     * @return A flow of a list of all profiles.
     */
    @Query("SELECT * FROM profiles")
    fun getProfiles(): Flow<List<Profile>>

    /**
     * Inserts a new [Profile] or updates an existing one if the ID already exists.
     *
     * @param profile The profile to insert or update.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: Profile)

    /**
     * Deletes a specific [Profile] from the database.
     *
     * @param profile The profile to delete.
     */
    @Delete
    suspend fun delete(profile: Profile)
}
