package com.example.domain.model

enum class CapabilityState {
    CONNECTED,
    NOT_CONNECTED,
    COMING_SOON
}

data class SystemCapability(
    val id: String,
    val title: String,
    val description: String,
    val state: CapabilityState,
    val category: String,
    val whatItDoes: String = "",
    val whyNeeded: String = "",
    val howToEnable: String = "",
    val settingsAction: String? = null,
    val isDeferredPhase: Boolean = false
)
