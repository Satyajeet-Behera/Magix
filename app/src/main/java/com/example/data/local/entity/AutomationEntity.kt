package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.AutomationRoutine

@Entity(tableName = "automations")
data class AutomationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val schedule: String,
    val isEnabled: Boolean,
    val actionsJoined: String, // comma or pipe separated
    val category: String,
    val lastExecutedTime: Long? = null
) {
    fun toDomain(): AutomationRoutine = AutomationRoutine(
        id = id,
        title = title,
        description = description,
        schedule = schedule,
        isEnabled = isEnabled,
        actionsSummary = if (actionsJoined.isBlank()) emptyList() else actionsJoined.split("||"),
        category = category,
        lastExecutedTime = lastExecutedTime
    )

    companion object {
        fun fromDomain(routine: AutomationRoutine): AutomationEntity = AutomationEntity(
            id = routine.id,
            title = routine.title,
            description = routine.description,
            schedule = routine.schedule,
            isEnabled = routine.isEnabled,
            actionsJoined = routine.actionsSummary.joinToString("||"),
            category = routine.category,
            lastExecutedTime = routine.lastExecutedTime
        )
    }
}
