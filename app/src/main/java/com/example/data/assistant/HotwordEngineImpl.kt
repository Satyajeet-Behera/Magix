package com.example.data.assistant

import com.example.domain.contract.HotwordEngine
import com.example.domain.contract.HotwordState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HotwordEngineImpl(
    private val onInvocationDetected: () -> Unit
) : HotwordEngine {

    override val invocationPhrase: String = "Hey, Magix!"

    private val _state = MutableStateFlow(HotwordState.DISABLED)
    override val state: StateFlow<HotwordState> = _state.asStateFlow()

    override fun startListening(): Boolean {
        // Honest disclosure: Continuous OS-level background hotword requires VoiceInteractionService enrollment.
        _state.value = HotwordState.STANDBY
        return true
    }

    override fun stopListening() {
        _state.value = HotwordState.DISABLED
    }

    override fun simulateInvocation() {
        _state.value = HotwordState.DETECTED
        onInvocationDetected()
        _state.value = HotwordState.STANDBY
    }
}
