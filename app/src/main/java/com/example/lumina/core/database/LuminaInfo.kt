package com.example.lumina.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing configuration and metadata for a specific "Lumina" instance.
 *
 * Each Lumina entry is associated with a [Profile] and contains various privacy
 * and browser configuration settings.
 *
 * @property id Unique identifier for the lumina info, auto-generated.
 * @property profileId The ID of the [Profile] this entry belongs to.
 * @property name The display name of the entry.
 * @property url The URL associated with this entry.
 * @property icon The name or identifier of the icon to display.
 * @property color The color associated with this entry, stored as a Long.
 * @property isEphemeral Whether this session should be ephemeral (no data persisted).
 * @property isWebRtcDisabled Whether WebRTC should be disabled to prevent IP leaks.
 * @property afpEnabled Whether Advanced Fingerprinting Protection is enabled.
 * @property randomizeUserAgent Whether to use a randomized User-Agent.
 * @property spoofLocale Whether to spoof the browser locale.
 * @property spoofTimezone Whether to spoof the browser timezone.
 * @property randomizeCanvas Whether to randomize canvas fingerprinting results.
 * @property disableAudioContext Whether to disable AudioContext to prevent fingerprinting.
 * @property disableWebGl Whether to disable WebGL to prevent fingerprinting.
 * @property randomizeScreen Whether to randomize screen resolution reporting.
 * @property spoofHardware Whether to spoof hardware information (e.g., number of cores).
 * @property disablePayment Whether to disable the Payment Request API.
 */
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
