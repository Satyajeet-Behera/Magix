package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.AuditLogItem

@Entity(tableName = "action_audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val actionName: String,
    val target: String,
    val status: String,
    val details: String,
    val evidence: String = ""
) {
    fun toDomain(): AuditLogItem = AuditLogItem(
        id = id,
        timestamp = timestamp,
        actionName = actionName,
        target = target,
        status = status,
        details = details,
        evidence = evidence
    )

    companion object {
        fun fromDomain(item: AuditLogItem): AuditLogEntity = AuditLogEntity(
            id = item.id,
            timestamp = item.timestamp,
            actionName = item.actionName,
            target = item.target,
            status = item.status,
            details = item.details,
            evidence = item.evidence
        )
    }
}
