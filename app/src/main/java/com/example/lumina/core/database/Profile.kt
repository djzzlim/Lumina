package com.example.lumina.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "profiles")
data class Profile(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String
)
