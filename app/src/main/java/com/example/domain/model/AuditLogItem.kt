package com.example.domain.model

data class AuditLogItem(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val actionName: String,
    val target: String,
    val status: String, // "SUCCESS", "FAILURE", "CANCELLED", "CONFIRMATION_PENDING"
    val details: String,
    val evidence: String = ""
)
