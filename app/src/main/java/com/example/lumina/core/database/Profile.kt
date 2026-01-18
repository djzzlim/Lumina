package com.example.lumina.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity representing a user profile.
 *
 * Each profile allows for separate collections of [LuminaInfo] entries.
 *
 * @property id Unique identifier for the profile, defaults to a random UUID string.
 * @property name The display name of the profile.
 */
@Entity(tableName = "profiles")
data class Profile(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String
)
