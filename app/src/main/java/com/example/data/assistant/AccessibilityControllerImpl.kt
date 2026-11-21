package com.example.data.assistant

import com.example.domain.contract.AccessibilityController
import com.example.domain.contract.ScrollDirection
import com.example.domain.model.GlobalActionType
import com.example.domain.model.UiNodeInfo
import com.example.service.MagixAccessibilityService
import kotlinx.coroutines.flow.StateFlow

class AccessibilityControllerImpl : AccessibilityController {

    override val isServiceEnabled: StateFlow<Boolean> = MagixAccessibilityService.isServiceActive

    override suspend fun performGlobalAction(action: GlobalActionType): Boolean {
        val service = MagixAccessibilityService.get() ?: return false
        return service.executeGlobal(action)
    }

    override suspend fun findNodesByText(text: String): List<UiNodeInfo> {
        val service = MagixAccessibilityService.get() ?: return emptyList()
        return service.findNodesByText(text)
    }

    override suspend fun performClickOnNode(textOrId: String): Boolean {
        val service = MagixAccessibilityService.get() ?: return false
        return service.clickNodeByText(textOrId)
    }

    override suspend fun performTap(x: Float, y: Float): Boolean {
        val service = MagixAccessibilityService.get() ?: return false
        return service.dispatchTap(x, y)
    }

    override suspend fun performScroll(direction: ScrollDirection): Boolean {
        val service = MagixAccessibilityService.get() ?: return false
        return service.dispatchScroll(direction)
    }

    override suspend fun performTextEntry(text: String): Boolean {
        val service = MagixAccessibilityService.get() ?: return false
        return service.enterText(text)
    }

    override suspend fun captureScreenHierarchySummary(): String {
        val service = MagixAccessibilityService.get()
        return service?.extractHierarchySummary()
            ?: "Accessibility service is not active. Enable MAGIX Autonomous Interaction in Android Accessibility Settings to read screen content."
    }
}
