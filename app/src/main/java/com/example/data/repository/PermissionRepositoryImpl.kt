package com.example.data.repository

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.domain.contract.PermissionRepository
import com.example.domain.model.CapabilityState
import com.example.domain.model.SystemCapability
import com.example.service.MagixAccessibilityService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PermissionRepositoryImpl(
    private val context: Context
) : PermissionRepository {

    override fun checkMicrophoneGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun checkNotificationsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    override fun checkAccessibilityGranted(): Boolean {
        return MagixAccessibilityService.isServiceActive.value
    }

    fun checkOverlayGranted(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun checkAssistantRoleHeld(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            roleManager?.isRoleHeld(RoleManager.ROLE_ASSISTANT) ?: false
        } else {
            false
        }
    }

    override fun observeCapabilities(): Flow<List<SystemCapability>> = flow {
        val micOk = checkMicrophoneGranted()
        val notifOk = checkNotificationsGranted()
        val accessOk = checkAccessibilityGranted()
        val overlayOk = checkOverlayGranted()
        val roleOk = checkAssistantRoleHeld()

        emit(
            listOf(
                SystemCapability(
                    id = "cap_mic",
                    title = "Microphone Telemetry",
                    description = "Acoustic input for voice directives and live speech.",
                    state = if (micOk) CapabilityState.CONNECTED else CapabilityState.NOT_CONNECTED,
                    category = "Sensory Inputs",
                    whatItDoes = "Captures vocal directives when you speak to MAGIX or activate the voice dock.",
                    whyNeeded = "Required for real-time speech recognition and hands-free conversational flow.",
                    howToEnable = "Grant 'Microphone' permission in Android App Settings.",
                    settingsAction = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                ),
                SystemCapability(
                    id = "cap_accessibility",
                    title = "Autonomous Interaction Engine",
                    description = "Perceives UI hierarchy and executes authorized taps, scrolls, and global actions.",
                    state = if (accessOk) CapabilityState.CONNECTED else CapabilityState.NOT_CONNECTED,
                    category = "Device Control",
                    whatItDoes = "Allows MAGIX to read on-screen elements, press buttons, scroll pages, and navigate on your behalf.",
                    whyNeeded = "Required for autonomous multi-step execution (e.g. searching YouTube, adjusting settings).",
                    howToEnable = "Open Accessibility Settings, find 'MAGIX Autonomous Interaction', and toggle switch On.",
                    settingsAction = Settings.ACTION_ACCESSIBILITY_SETTINGS
                ),
                SystemCapability(
                    id = "cap_overlay",
                    title = "Live Presence Overlay",
                    description = "Allows top-of-screen presence pill to remain visible above other apps.",
                    state = if (overlayOk) CapabilityState.CONNECTED else CapabilityState.NOT_CONNECTED,
                    category = "System Presence",
                    whatItDoes = "Displays the subtle top-of-screen MAGIX pill when you are outside the app.",
                    whyNeeded = "Lets you monitor task progress and trigger voice commands without leaving your current app.",
                    howToEnable = "Enable 'Display over other apps' in Android Special App Access settings.",
                    settingsAction = Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                ),
                SystemCapability(
                    id = "cap_notif",
                    title = "Proactive Notifications",
                    description = "Alerts for scheduled reminders and task completion summaries.",
                    state = if (notifOk) CapabilityState.CONNECTED else CapabilityState.NOT_CONNECTED,
                    category = "System Presence",
                    whatItDoes = "Dispatches push notifications when reminders are due or routines complete.",
                    whyNeeded = "Keeps you informed of autonomous background events.",
                    howToEnable = "Enable notifications in Android Settings for MAGIX.",
                    settingsAction = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                ),
                SystemCapability(
                    id = "cap_role",
                    title = "Default Assistant Role",
                    description = "Integrates with Android home-swipe/power-button assistant invocations.",
                    state = if (roleOk) CapabilityState.CONNECTED else CapabilityState.NOT_CONNECTED,
                    category = "System Integration",
                    whatItDoes = "Registers MAGIX as the system default Digital Assistant app.",
                    whyNeeded = "Allows invoking MAGIX via standard Android system gestures and voice buttons.",
                    howToEnable = "Select MAGIX under 'Default Assistant App' in Android Settings.",
                    settingsAction = Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS
                )
            )
        )
    }
}
