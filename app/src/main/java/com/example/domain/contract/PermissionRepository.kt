package com.example.domain.contract

import com.example.domain.model.SystemCapability
import kotlinx.coroutines.flow.Flow

/**
 * Future Extension Contract: PermissionRepository
 * In Phase 1: Observes actual runtime permissions (Mic, Notification, Accessibility)
 * and safely reports capability status without pretending.
 */
interface PermissionRepository {
    fun observeCapabilities(): Flow<List<SystemCapability>>
    fun checkMicrophoneGranted(): Boolean
    fun checkNotificationsGranted(): Boolean
    fun checkAccessibilityGranted(): Boolean
}
