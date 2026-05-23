package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "theia_logs")
data class TheiaLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val prompt: String,
    val response: String,
    val latencyMs: Long,
    val matchedIntent: String,
    val isCacheHit: Boolean,
    val engineUsed: String, // "L2 Local (Whisper/Piper)" or "L3 Cloud (Gemini RAG)"
    val timestamp: Long = System.currentTimeMillis()
)
