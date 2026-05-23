package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "theia_vault_notes")
data class TheiaVaultNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val category: String = "general" // e.g., "daily_notes", "frequent_notes", "quick_note"
)
