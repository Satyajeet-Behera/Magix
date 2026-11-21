package com.example.domain.model

enum class ChatSender {
    USER,
    MAGIX,
    SYSTEM
}

data class ChatMessage(
    val id: String,
    val sender: ChatSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val stateSnapshot: AssistantState = AssistantState.IDLE,
    val isDevMock: Boolean = true,
    val audioDurationSeconds: Float? = null
)
