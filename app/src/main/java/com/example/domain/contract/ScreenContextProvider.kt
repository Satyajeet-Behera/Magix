package com.example.domain.contract

import kotlinx.coroutines.flow.Flow

/**
 * Future Extension Contract: ScreenContextProvider
 * In Phase 1: Conceptual abstraction for screen perception.
 * In Phase 2+: Connects to MediaProjection / Accessibility Node Tree / Gemini 2.0 Screen Vision.
 */
interface ScreenContextProvider {
    val isCapturing: Boolean

    suspend fun captureCurrentScreenSnapshot(): ByteArray?
    suspend fun readAccessibilityHierarchy(): String?
    fun observeForegroundApp(): Flow<String>
}
