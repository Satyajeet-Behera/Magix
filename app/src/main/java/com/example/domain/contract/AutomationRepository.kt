package com.example.domain.contract

import com.example.domain.model.AutomationRoutine
import kotlinx.coroutines.flow.Flow

/**
 * Extension Contract: AutomationRepository
 * Backed by local SQLite Room Database with custom user schedules and actions.
 */
interface AutomationRepository {
    fun getAllRoutines(): Flow<List<AutomationRoutine>>
    suspend fun toggleRoutine(id: String, isEnabled: Boolean)
    suspend fun insertRoutine(routine: AutomationRoutine)
    suspend fun updateLastExecuted(id: String, timestamp: Long)
    suspend fun deleteRoutine(id: String)
    suspend fun clearAllRoutines()
}
