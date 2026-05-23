package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_patterns")
data class LocalPattern(
    @PrimaryKey val patternId: String,
    val patternType: String, // e.g., "time_based", "format_preference"
    val triggerConditions: String, // JSON or descriptive trigger, e.g., "09:00-12:00"
    val adaptation: String, // JSON or description, e.g., "short response, max 2 sentences"
    val confidence: Float = 0.85f,
    val lastApplied: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
