package com.example.domain.contract

import com.example.domain.model.GlobalActionType
import com.example.domain.model.UiNodeInfo
import kotlinx.coroutines.flow.StateFlow

interface AccessibilityController {
    val isServiceEnabled: StateFlow<Boolean>

    suspend fun performGlobalAction(action: GlobalActionType): Boolean
    suspend fun findNodesByText(text: String): List<UiNodeInfo>
    suspend fun performClickOnNode(textOrId: String): Boolean
    suspend fun performTap(x: Float, y: Float): Boolean
    suspend fun performScroll(direction: ScrollDirection): Boolean
    suspend fun performTextEntry(text: String): Boolean
    suspend fun captureScreenHierarchySummary(): String
}

enum class ScrollDirection {
    UP, DOWN, LEFT, RIGHT
}
