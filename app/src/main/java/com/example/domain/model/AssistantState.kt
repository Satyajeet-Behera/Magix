package com.example.domain.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.MagixCyan
import com.example.ui.theme.MagixCyanGlow
import com.example.ui.theme.MagixGreenSuccess
import com.example.ui.theme.MagixPurpleExec
import com.example.ui.theme.MagixRedError
import com.example.ui.theme.MagixTextSecondary

enum class AssistantState(
    val title: String,
    val statusPhrase: String,
    val detail: String,
    val indicatorColor: Color
) {
    IDLE(
        title = "IDLE",
        statusPhrase = "Ready when you are",
        detail = "Standby mode · Ambient monitoring",
        indicatorColor = MagixCyan
    ),
    LISTENING(
        title = "LISTENING",
        statusPhrase = "Listening to input...",
        detail = "Audio buffer active · Capturing speech",
        indicatorColor = MagixCyanGlow
    ),
    THINKING(
        title = "THINKING",
        statusPhrase = "Processing directive...",
        detail = "Synthesizing contextual parameters",
        indicatorColor = MagixCyan
    ),
    SPEAKING(
        title = "SPEAKING",
        statusPhrase = "Responding...",
        detail = "Acoustic synthesis streaming",
        indicatorColor = MagixCyanGlow
    ),
    EXECUTING(
        title = "EXECUTING",
        statusPhrase = "Executing task...",
        detail = "Coordinating device subroutines",
        indicatorColor = MagixPurpleExec
    ),
    SUCCESS(
        title = "SUCCESS",
        statusPhrase = "Directive completed",
        detail = "Action verified and logged",
        indicatorColor = MagixGreenSuccess
    ),
    ERROR(
        title = "ERROR",
        statusPhrase = "Interruption encountered",
        detail = "Parameter bounds exceeded",
        indicatorColor = MagixRedError
    )
}
