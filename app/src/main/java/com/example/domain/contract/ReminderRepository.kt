package com.example.domain.contract

import com.example.domain.model.ReminderItem
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getAllReminders(): Flow<List<ReminderItem>>
    suspend fun createReminder(title: String, dueTimeMillis: Long, formattedTime: String): ReminderItem
    suspend fun deleteReminder(id: Long)
    suspend fun toggleReminderComplete(id: Long, completed: Boolean)
}
