package com.example.domain.contract

import com.example.domain.model.MemoryItem
import kotlinx.coroutines.flow.Flow

/**
 * Extension Contract: MemoryRepository
 * In Phase 1: Backed by local SQLite Room Database.
 * In Phase 2+: Extended with vector embeddings / semantic retrieval without changing the UI interface.
 */
interface MemoryRepository {
    fun getAllMemories(): Flow<List<MemoryItem>>
    fun getMemoriesByCategory(category: String): Flow<List<MemoryItem>>
    suspend fun insertMemory(item: MemoryItem): Long
    suspend fun deleteMemory(id: Long)
    suspend fun clearAllMemories()
}
