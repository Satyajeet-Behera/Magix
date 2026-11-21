package com.example.data.assistant

import com.example.domain.contract.MagixPresenceController
import com.example.domain.contract.PresenceMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MagixPresenceControllerImpl(
    initialOverlayPermitted: Boolean = false
) : MagixPresenceController {

    private val _presenceMode = MutableStateFlow(PresenceMode.COMPACT)
    override val presenceMode: StateFlow<PresenceMode> = _presenceMode.asStateFlow()

    private var _isOverlayPermitted: Boolean = initialOverlayPermitted
    override val isOverlayPermitted: Boolean
        get() = _isOverlayPermitted

    override fun expandPresence() {
        _presenceMode.value = PresenceMode.EXPANDED
    }

    override fun collapsePresence() {
        _presenceMode.value = PresenceMode.COMPACT
    }

    override fun triggerNotificationFallback(title: String, message: String) {
        _presenceMode.value = PresenceMode.NOTIFICATION_FALLBACK
    }

    override fun setOverlayPermitted(permitted: Boolean) {
        _isOverlayPermitted = permitted
    }
}
