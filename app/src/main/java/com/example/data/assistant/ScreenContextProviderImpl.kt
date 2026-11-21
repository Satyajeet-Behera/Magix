package com.example.data.assistant

import com.example.domain.contract.AccessibilityController
import com.example.domain.contract.ScreenContextProvider
import com.example.service.MagixAccessibilityService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ScreenContextProviderImpl(
    private val accessibilityController: AccessibilityController
) : ScreenContextProvider {

    override val isCapturing: Boolean
        get() = accessibilityController.isServiceEnabled.value

    override suspend fun captureCurrentScreenSnapshot(): ByteArray? {
        // Screenshots are dispatched via AccessibilityService.takeScreenshot where supported
        return null
    }

    override suspend fun readAccessibilityHierarchy(): String? {
        return accessibilityController.captureScreenHierarchySummary()
    }

    override fun observeForegroundApp(): Flow<String> = flow {
        val root = MagixAccessibilityService.get()?.rootInActiveWindow
        val pkg = root?.packageName?.toString() ?: "com.example"
        emit(pkg)
    }
}
