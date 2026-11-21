package com.example.data.repository

import com.example.data.local.dao.AuditLogDao
import com.example.data.local.entity.AuditLogEntity
import com.example.domain.contract.AuditLogger
import com.example.domain.model.AuditLogItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuditLoggerImpl(
    private val auditLogDao: AuditLogDao
) : AuditLogger {

    override val recentLogs: Flow<List<AuditLogItem>> =
        auditLogDao.getRecentAuditLogs().map { list -> list.map { it.toDomain() } }

    override suspend fun logAction(
        actionName: String,
        target: String,
        status: String,
        details: String,
        evidence: String
    ) {
        val redactedTarget = com.example.domain.model.SecurityPolicy.redactSensitiveData(target)
        val redactedDetails = com.example.domain.model.SecurityPolicy.redactSensitiveData(details)
        val redactedEvidence = com.example.domain.model.SecurityPolicy.redactSensitiveData(evidence)

        val entity = AuditLogEntity(
            actionName = actionName,
            target = redactedTarget,
            status = status,
            details = redactedDetails,
            evidence = redactedEvidence
        )
        auditLogDao.insertLog(entity)
    }

    override suspend fun clearLogs() {
        auditLogDao.clearAuditLogs()
    }
}
