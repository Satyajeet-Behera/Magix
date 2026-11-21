package com.example.data.repository

import com.example.data.local.dao.MemoryDao
import com.example.data.local.entity.MemoryEntity
import com.example.domain.contract.MemoryRepository
import com.example.domain.model.MemoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MemoryRepositoryImpl(
    private val memoryDao: MemoryDao
) : MemoryRepository {

    override fun getAllMemories(): Flow<List<MemoryItem>> {
        return memoryDao.getAllMemories().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getMemoriesByCategory(category: String): Flow<List<MemoryItem>> {
        return memoryDao.getMemoriesByCategory(category).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun insertMemory(item: MemoryItem): Long {
        return memoryDao.insertMemory(MemoryEntity.fromDomain(item))
    }

    override suspend fun deleteMemory(id: Long) {
        memoryDao.deleteMemoryById(id)
    }

    override suspend fun clearAllMemories() {
        memoryDao.clearAllMemories()
    }
}
