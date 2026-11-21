package com.example.domain.contract

import kotlinx.coroutines.flow.StateFlow

enum class PresenceMode {
    COMPACT,
    EXPANDED,
    NOTIFICATION_FALLBACK,
    HIDDEN
}

interface MagixPresenceController {
    val presenceMode: StateFlow<PresenceMode>
    val isOverlayPermitted: Boolean

    fun expandPresence()
    fun collapsePresence()
    fun triggerNotificationFallback(title: String, message: String)
    fun setOverlayPermitted(permitted: Boolean)
}
