package com.example.lumina.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "luminas",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profileId"])]
)
data class LuminaInfo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: String,
    val name: String,
    val url: String,
    val icon: String, // Storing icon name as string
    val color: Long,   // Storing color as Long
    val isEphemeral: Boolean = false,
    val isWebRtcDisabled: Boolean = true,
    val afpEnabled: Boolean = true,
    val randomizeUserAgent: Boolean = true,
    val spoofLocale: Boolean = true,
    val spoofTimezone: Boolean = true,
    val randomizeCanvas: Boolean = true,
    val disableAudioContext: Boolean = true,
    val disableWebGl: Boolean = true,
    val randomizeScreen: Boolean = true,
    val spoofHardware: Boolean = true,
    val disablePayment: Boolean = true
)
