package com.example.data.dao

import androidx.room.*
import com.example.data.model.LocalPattern
import com.example.data.model.TheiaLog
import com.example.data.model.TheiaVaultNote
import kotlinx.coroutines.flow.Flow

@Dao
interface TheiaDao {

    // --- Notes (TheiaVault) ---
    @Query("SELECT * FROM theia_vault_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<TheiaVaultNote>>

    @Query("SELECT * FROM theia_vault_notes WHERE category = :category ORDER BY timestamp DESC")
    fun getNotesByCategory(category: String): Flow<List<TheiaVaultNote>>

    @Query("SELECT * FROM theia_vault_notes WHERE title LIKE :searchQuery OR content LIKE :searchQuery")
    suspend fun listSearchNotes(searchQuery: String): List<TheiaVaultNote>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: TheiaVaultNote)

    @Delete
    suspend fun deleteNote(note: TheiaVaultNote)

    @Query("DELETE FROM theia_vault_notes")
    suspend fun deleteAllNotes()

    // --- Patterns (ADHD Adaptive Profiles) ---
    @Query("SELECT * FROM local_patterns ORDER BY confidence DESC")
    fun getAllPatterns(): Flow<List<LocalPattern>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPattern(pattern: LocalPattern)

    @Delete
    suspend fun deletePattern(pattern: LocalPattern)

    @Query("UPDATE local_patterns SET isActive = :isActive WHERE patternId = :patternId")
    suspend fun updatePatternStatus(patternId: String, isActive: Boolean)

    // --- System Activity Logs ---
    @Query("SELECT * FROM theia_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<TheiaLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addLog(log: TheiaLog)

    @Query("DELETE FROM theia_logs")
    suspend fun clearLogs()
}
