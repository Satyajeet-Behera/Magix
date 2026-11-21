package com.example.data.assistant

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.example.BuildConfig
import com.example.data.local.MagixDatabase
import com.example.data.local.preferences.MagixPreferencesManager
import com.example.data.repository.AuditLoggerImpl
import com.example.data.repository.AutomationRepositoryImpl
import com.example.data.repository.MemoryRepositoryImpl
import com.example.data.repository.PermissionRepositoryImpl
import com.example.data.repository.ReminderRepositoryImpl
import com.example.domain.contract.AccessibilityController
import com.example.domain.contract.AgentEngine
import com.example.domain.contract.AssistantEngine
import com.example.domain.contract.AuditLogger
import com.example.domain.contract.AutomationRepository
import com.example.domain.contract.GreetingEngine
import com.example.domain.contract.HotwordEngine
import com.example.domain.contract.MemoryRepository
import com.example.domain.contract.PermissionRepository
import com.example.domain.contract.ReminderRepository
import com.example.domain.contract.SpeechRecognitionEngine
import com.example.domain.contract.VoiceEngine
import com.example.domain.model.ActionResult
import com.example.domain.model.AssistantState
import com.example.domain.model.ChatMessage
import com.example.domain.model.MagixVoiceProfile
import com.example.domain.model.UserSettings
import com.example.domain.tool.ToolRegistry
import com.example.service.MagixOverlayService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Central engine hub providing unified state and engine orchestration
 * between the Main UI Activity, WindowManager Overlays, and Background Services.
 */
class MagixAppEngine private constructor(val application: Application) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    val database = MagixDatabase.getInstance(application)
    val preferencesManager = MagixPreferencesManager(application)

    // Repositories
    val memoryRepository: MemoryRepository = MemoryRepositoryImpl(database.memoryDao())
    val automationRepository: AutomationRepository = AutomationRepositoryImpl(database.automationDao())
    val reminderRepository: ReminderRepository = ReminderRepositoryImpl(database.reminderDao())
    val auditLogger: AuditLogger = AuditLoggerImpl(database.auditLogDao())
    val permissionRepository: PermissionRepository = PermissionRepositoryImpl(application)
    val accessibilityController: AccessibilityController = AccessibilityControllerImpl()

    // Tools & Brain
    val toolRegistry: ToolRegistry = ToolRegistry(
        context = application,
        accessibilityController = accessibilityController,
        memoryRepository = memoryRepository,
        automationRepository = automationRepository,
        reminderRepository = reminderRepository,
        auditLogger = auditLogger
    )

    val geminiEngine = GeminiEngineImpl(toolRegistry)
    val agentEngine: AgentEngine = AgentEngineImpl(
        geminiEngine = geminiEngine,
        toolRegistry = toolRegistry,
        accessibilityController = accessibilityController,
        auditLogger = auditLogger
    )

    // Voice & Audio
    val voiceEngine: VoiceEngine = AndroidVoiceEngine(application)
    val speechRecognitionEngine: SpeechRecognitionEngine = AndroidSpeechRecognitionEngine(application)
    val greetingEngine: GreetingEngine = GreetingEngineImpl()

    // Unified Voice-First Session Manager
    val sessionManager: MagixSessionManager = MagixSessionManager(
        context = application,
        agentEngine = agentEngine,
        geminiEngine = geminiEngine,
        voiceEngine = voiceEngine,
        speechRecognitionEngine = speechRecognitionEngine,
        greetingEngine = greetingEngine,
        auditLogger = auditLogger,
        getUserSettings = { preferencesManager.settingsFlow.value }
    )

    // State flows
    private val _activeTaskProgress = MutableStateFlow<String?>(null)
    val activeTaskProgress: StateFlow<String?> = sessionManager.taskProgress

    val assistantEngine: AssistantEngine = RealAssistantEngine(agentEngine) { progress ->
        _activeTaskProgress.value = progress
    }

    val userSettings: StateFlow<UserSettings> = preferencesManager.settingsFlow
        .stateIn(scope, SharingStarted.Eagerly, preferencesManager.settingsFlow.value)

    val pendingConfirmation: StateFlow<ActionResult?> = agentEngine.pendingConfirmation

    val isGeminiOnline: StateFlow<Boolean> = geminiEngine.isOnline

    val hotwordEngine: HotwordEngine = HotwordEngineImpl {
        handleInvocationDetected()
    }

    private val onInvocationListeners = mutableListOf<() -> Unit>()

    fun registerInvocationListener(listener: () -> Unit) {
        onInvocationListeners.add(listener)
    }

    fun unregisterInvocationListener(listener: () -> Unit) {
        onInvocationListeners.remove(listener)
    }

    private fun handleInvocationDetected() {
        sessionManager.startSession(isVoiceInvocation = true)
        onInvocationListeners.forEach { it.invoke() }
    }

    fun invokeJarvisGreeting(onGreetingSpoken: () -> Unit) {
        sessionManager.startSession(isVoiceInvocation = true)
        onGreetingSpoken()
    }

    fun triggerMicListening(onSpoken: ((String) -> Unit)? = null) {
        sessionManager.listenForDirective()
    }

    fun stopMicListening() {
        speechRecognitionEngine.stopListening()
        sessionManager.setSystemState(AssistantState.IDLE)
    }

    fun sendUserMessage(text: String, onComplete: ((String) -> Unit)? = null) {
        val clean = text.trim()
        if (clean.isBlank()) return

        if (clean.equals("magix, stop", ignoreCase = true) || clean.equals("stop", ignoreCase = true)) {
            emergencyHalt()
            return
        }

        if (clean.equals("hey magix", ignoreCase = true) || clean.equals("hey, magix", ignoreCase = true)) {
            handleInvocationDetected()
            return
        }

        sessionManager.processDirective(clean)
    }

    fun emergencyHalt() {
        sessionManager.emergencyHalt()
    }

    fun canDrawOverlays(): Boolean {
        return Settings.canDrawOverlays(application)
    }

    fun startOverlayService() {
        if (canDrawOverlays()) {
            val intent = Intent(application, MagixOverlayService::class.java).apply {
                action = MagixOverlayService.ACTION_SHOW_OVERLAY
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    application.startForegroundService(intent)
                } else {
                    application.startService(intent)
                }
            } catch (e: Exception) {
                application.startService(intent)
            }
        }
    }

    fun stopOverlayService() {
        val intent = Intent(application, MagixOverlayService::class.java).apply {
            action = MagixOverlayService.ACTION_HIDE_OVERLAY
        }
        application.stopService(intent)
    }

    companion object {
        @Volatile
        private var INSTANCE: MagixAppEngine? = null

        fun get(application: Application): MagixAppEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MagixAppEngine(application).also { INSTANCE = it }
            }
        }
    }
}
