package com.example.data.repository

import com.example.data.local.dao.AutomationDao
import com.example.data.local.entity.AutomationEntity
import com.example.domain.contract.AutomationRepository
import com.example.domain.model.AutomationRoutine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AutomationRepositoryImpl(
    private val automationDao: AutomationDao
) : AutomationRepository {

    override fun getAllRoutines(): Flow<List<AutomationRoutine>> {
        return automationDao.getAllAutomations().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun toggleRoutine(id: String, isEnabled: Boolean) {
        automationDao.updateEnabled(id, isEnabled)
    }

    override suspend fun insertRoutine(routine: AutomationRoutine) {
        automationDao.insertAutomation(AutomationEntity.fromDomain(routine))
    }

    override suspend fun updateLastExecuted(id: String, timestamp: Long) {
        automationDao.updateLastExecuted(id, timestamp)
    }

    override suspend fun deleteRoutine(id: String) {
        automationDao.deleteAutomation(id)
    }

    override suspend fun clearAllRoutines() {
        automationDao.clearAllAutomations()
    }
}
