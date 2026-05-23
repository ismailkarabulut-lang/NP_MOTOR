package com.example.data.repository

import com.example.data.dao.TheiaDao
import com.example.data.model.LocalPattern
import com.example.data.model.TheiaLog
import com.example.data.model.TheiaVaultNote
import kotlinx.coroutines.flow.Flow

class TheiaRepository(private val theiaDao: TheiaDao) {

    val allNotes: Flow<List<TheiaVaultNote>> = theiaDao.getAllNotes()
    val allPatterns: Flow<List<LocalPattern>> = theiaDao.getAllPatterns()
    val allLogs: Flow<List<TheiaLog>> = theiaDao.getAllLogs()

    fun getNotesByCategory(category: String): Flow<List<TheiaVaultNote>> = theiaDao.getNotesByCategory(category)

    suspend fun insertNote(note: TheiaVaultNote) = theiaDao.insertNote(note)
    suspend fun deleteNote(note: TheiaVaultNote) = theiaDao.deleteNote(note)
    suspend fun deleteAllNotes() = theiaDao.deleteAllNotes()
    suspend fun searchNotes(query: String): List<TheiaVaultNote> = theiaDao.listSearchNotes("%$query%")

    suspend fun insertPattern(pattern: LocalPattern) = theiaDao.insertPattern(pattern)
    suspend fun deletePattern(pattern: LocalPattern) = theiaDao.deletePattern(pattern)
    suspend fun updatePatternStatus(patternId: String, isActive: Boolean) = theiaDao.updatePatternStatus(patternId, isActive)

    suspend fun addLog(log: TheiaLog) = theiaDao.addLog(log)
    suspend fun clearLogs() = theiaDao.clearLogs()
}
