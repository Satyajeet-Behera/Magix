package com.example.domain.contract

/**
 * Future Extension Contract: DeviceController
 * In Phase 1: Conceptual abstraction. Does NOT claim or pretend to execute OS taps/clicks.
 * In Phase 2+: Connects to Android AccessibilityService, Shizuku, or System companion APIs.
 */
interface DeviceController {
    val isAccessibilityActive: Boolean
    
    suspend fun openApplication(packageName: String): Boolean
    suspend fun performTap(x: Float, y: Float): Boolean
    suspend fun performScroll(startX: Float, startY: Float, endX: Float, endY: Float): Boolean
    suspend fun inputText(text: String): Boolean
    suspend fun pressBack(): Boolean
    suspend fun pressHome(): Boolean
}
