package com.example.domain.model

data class ReminderItem(
    val id: Long = 0,
    val title: String,
    val dueTimeMillis: Long,
    val formattedTime: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
