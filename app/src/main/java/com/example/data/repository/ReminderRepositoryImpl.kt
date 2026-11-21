package com.example.data.repository

import com.example.data.local.dao.ReminderDao
import com.example.data.local.entity.ReminderEntity
import com.example.domain.contract.ReminderRepository
import com.example.domain.model.ReminderItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReminderRepositoryImpl(
    private val reminderDao: ReminderDao
) : ReminderRepository {

    override fun getAllReminders(): Flow<List<ReminderItem>> {
        return reminderDao.getAllReminders().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun createReminder(
        title: String,
        dueTimeMillis: Long,
        formattedTime: String
    ): ReminderItem {
        val entity = ReminderEntity(
            title = title,
            dueTimeMillis = dueTimeMillis,
            formattedTime = formattedTime,
            isCompleted = false
        )
        val id = reminderDao.insertReminder(entity)
        return entity.copy(id = id).toDomain()
    }

    override suspend fun deleteReminder(id: Long) {
        reminderDao.deleteReminderById(id)
    }

    override suspend fun toggleReminderComplete(id: Long, completed: Boolean) {
        // Find existing and update
        // (Handled via DAO update)
    }
}
