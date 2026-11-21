package com.example.domain.model

data class MemoryItem(
    val id: Long = 0,
    val key: String,
    val value: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis()
)
