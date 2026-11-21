package com.example.domain.contract

import kotlinx.coroutines.flow.StateFlow

enum class HotwordState {
    DISABLED,
    STANDBY,
    DETECTED,
    UNAVAILABLE
}

interface HotwordEngine {
    val state: StateFlow<HotwordState>
    val invocationPhrase: String // "Hey, Magix!"

    fun startListening(): Boolean
    fun stopListening()
    fun simulateInvocation()
}
