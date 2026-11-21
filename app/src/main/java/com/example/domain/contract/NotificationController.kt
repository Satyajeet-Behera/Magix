package com.example.domain.contract

import kotlinx.coroutines.flow.Flow

/**
 * Future Extension Contract: NotificationController
 * In Phase 1: Conceptual abstraction.
 * In Phase 2+: Connects to NotificationListenerService for proactive alerting and reply orchestration.
 */
interface NotificationController {
    val isNotificationListenerEnabled: Boolean

    suspend fun postAssistantStatusNotification(title: String, body: String)
    suspend fun dismissAssistantNotification()
    fun observeIncomingNotifications(): Flow<String>
}
