package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.ReminderItem

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val dueTimeMillis: Long,
    val formattedTime: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): ReminderItem = ReminderItem(
        id = id,
        title = title,
        dueTimeMillis = dueTimeMillis,
        formattedTime = formattedTime,
        isCompleted = isCompleted,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(item: ReminderItem): ReminderEntity = ReminderEntity(
            id = item.id,
            title = item.title,
            dueTimeMillis = item.dueTimeMillis,
            formattedTime = item.formattedTime,
            isCompleted = item.isCompleted,
            createdAt = item.createdAt
        )
    }
}
