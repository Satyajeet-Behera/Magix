package com.example.domain.contract

import com.example.domain.model.AuditLogItem
import kotlinx.coroutines.flow.Flow

interface AuditLogger {
    val recentLogs: Flow<List<AuditLogItem>>
    suspend fun logAction(
        actionName: String,
        target: String,
        status: String,
        details: String,
        evidence: String = ""
    )
    suspend fun clearLogs()
}
