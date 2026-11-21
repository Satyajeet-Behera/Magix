package com.example.data.assistant

import com.example.domain.contract.AccessibilityController
import com.example.domain.contract.ScreenPerceptionResult
import com.example.domain.contract.ScreenVisionEngine
import com.example.domain.contract.UIElementNode
import com.example.service.MagixAccessibilityService

class ScreenVisionEngineImpl(
    private val accessibilityController: AccessibilityController
) : ScreenVisionEngine {

    override val isVisionEnabled: Boolean
        get() = accessibilityController.isServiceEnabled.value

    override suspend fun analyzeCurrentScreen(): ScreenPerceptionResult? {
        val service = MagixAccessibilityService.get() ?: return null
        val root = service.rootInActiveWindow ?: return null
        val pkg = root.packageName?.toString() ?: "Unknown"

        val elements = mutableListOf<UIElementNode>()
        val foundNodes = service.findNodesByText("")
        foundNodes.take(25).forEach { node ->
            elements.add(
                UIElementNode(
                    id = node.viewIdResourceName ?: "node_${elements.size}",
                    text = node.text,
                    contentDescription = node.contentDescription,
                    bounds = node.boundsInScreen,
                    isClickable = node.isClickable
                )
            )
        }

        return ScreenPerceptionResult(
            appPackage = pkg,
            activityName = "ForegroundWindow",
            visibleElements = elements,
            timestamp = System.currentTimeMillis()
        )
    }
}
