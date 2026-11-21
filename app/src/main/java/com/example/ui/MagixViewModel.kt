package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.assistant.AccessibilityControllerImpl
import com.example.data.assistant.AgentEngineImpl
import com.example.data.assistant.AndroidSpeechRecognitionEngine
import com.example.data.assistant.AndroidVoiceEngine
import com.example.data.assistant.GeminiEngineImpl
import com.example.data.assistant.GreetingEngineImpl
import com.example.data.assistant.HotwordEngineImpl
import com.example.data.assistant.MagixPresenceControllerImpl
import com.example.data.assistant.RealAssistantEngine
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
import com.example.domain.contract.GeminiEngine
import com.example.domain.contract.GreetingEngine
import com.example.domain.contract.HotwordEngine
import com.example.domain.contract.MagixPresenceController
import com.example.domain.contract.MemoryRepository
import com.example.domain.contract.PermissionRepository
import com.example.domain.contract.ReminderRepository
import com.example.domain.contract.SpeechRecognitionEngine
import com.example.domain.contract.VoiceEngine
import com.example.domain.model.ActionResult
import com.example.domain.model.AssistantState
import com.example.domain.model.AuditLogItem
import com.example.domain.model.AutomationRoutine
import com.example.domain.model.ChatMessage
import com.example.domain.model.ChatSender
import com.example.domain.model.MagixVoiceProfile
import com.example.domain.model.MemoryItem
import com.example.domain.model.ReminderItem
import com.example.domain.model.SystemCapability
import com.example.domain.model.ThemeMode
import com.example.domain.model.UserSettings
import com.example.domain.model.VoiceSpeed
import com.example.domain.tool.ToolRegistry
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class MagixViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MagixDatabase.getInstance(application)
    private val preferencesManager = MagixPreferencesManager(application)

    // Core Repositories & Controllers
    val memoryRepository: MemoryRepository = MemoryRepositoryImpl(database.memoryDao())
    val automationRepository: AutomationRepository = AutomationRepositoryImpl(database.automationDao())
    val reminderRepository: ReminderRepository = ReminderRepositoryImpl(database.reminderDao())
    val auditLogger: AuditLogger = AuditLoggerImpl(database.auditLogDao())
    val permissionRepository: PermissionRepository = PermissionRepositoryImpl(application)
    val accessibilityController: AccessibilityController = AccessibilityControllerImpl()

    // Tool Registry
    val toolRegistry: ToolRegistry = ToolRegistry(
        context = application,
        accessibilityController = accessibilityController,
        memoryRepository = memoryRepository,
        automationRepository = automationRepository,
        reminderRepository = reminderRepository,
        auditLogger = auditLogger
    )

    // Shared App Engine and Session Manager
    val appEngine = com.example.data.assistant.MagixAppEngine.get(application)
    val sessionManager = appEngine.sessionManager

    val geminiEngine: GeminiEngine = appEngine.geminiEngine
    val agentEngine: AgentEngine = appEngine.agentEngine
    val voiceEngine: VoiceEngine = appEngine.voiceEngine
    val speechRecognitionEngine: SpeechRecognitionEngine = appEngine.speechRecognitionEngine
    val greetingEngine: GreetingEngine = appEngine.greetingEngine
    val assistantEngine: AssistantEngine = appEngine.assistantEngine

    // Task Progress Indicator
    val activeTaskProgress: StateFlow<String?> = sessionManager.taskProgress

    val hotwordEngine: HotwordEngine = HotwordEngineImpl {
        handleInvocationDetected()
    }

    val presenceController: MagixPresenceController = MagixPresenceControllerImpl()

    // Live Presence Card Pop-up Expansion State
    private val _isLivePresenceExpanded = MutableStateFlow(false)
    val isLivePresenceExpanded: StateFlow<Boolean> = _isLivePresenceExpanded.asStateFlow()

    fun setLivePresenceExpanded(expanded: Boolean) {
        _isLivePresenceExpanded.value = expanded
    }

    val isMicrophonePermissionGranted: Boolean
        get() = permissionRepository.checkMicrophoneGranted()

    val isSpeechRecognitionAvailable: Boolean
        get() = speechRecognitionEngine.isRecognitionAvailable()

    val speechError: StateFlow<String?> = speechRecognitionEngine.speechError

    // Central State Exposures
    val assistantState: StateFlow<AssistantState> = sessionManager.sessionState
    val userSettings: StateFlow<UserSettings> = preferencesManager.settingsFlow
    val pendingConfirmation: StateFlow<ActionResult?> = agentEngine.pendingConfirmation
    val isGeminiOnline: StateFlow<Boolean> = geminiEngine.isOnline
    val isListening: StateFlow<Boolean> = sessionManager.isListening
    val isSpeaking: StateFlow<Boolean> = sessionManager.isSpeaking
    val recognizedSpeechText: StateFlow<String> = sessionManager.activeTranscript
    val speechRmsLevel: StateFlow<Float> = speechRecognitionEngine.rmsLevel

    private val _micPermissionRequested = MutableStateFlow(false)
    val micPermissionRequested: StateFlow<Boolean> = _micPermissionRequested.asStateFlow()

    fun requestMicrophonePermission() {
        _micPermissionRequested.value = true
    }

    fun resetMicPermissionRequest() {
        _micPermissionRequested.value = false
    }

    private val _speechIntentRequested = MutableStateFlow(false)
    val speechIntentRequested: StateFlow<Boolean> = _speechIntentRequested.asStateFlow()

    fun requestSpeechIntent() {
        _speechIntentRequested.value = true
    }

    fun resetSpeechIntentRequest() {
        _speechIntentRequested.value = false
    }
    val isAccessibilityActive: StateFlow<Boolean> = accessibilityController.isServiceEnabled

    // Local Data Flows from Room
    val memories: StateFlow<List<MemoryItem>> = memoryRepository.getAllMemories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val automations: StateFlow<List<AutomationRoutine>> = automationRepository.getAllRoutines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<ReminderItem>> = reminderRepository.getAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogItem>> = auditLogger.recentLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val capabilities: StateFlow<List<SystemCapability>> = permissionRepository.observeCapabilities()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chat History
    val chatMessages: StateFlow<List<ChatMessage>> = sessionManager.chatMessages

    init {
        // Sync initial voice profile & speed
        val initialVoice = MagixVoiceProfile.findByIdOrDefault(preferencesManager.settingsFlow.value.selectedVoiceId)
        viewModelScope.launch {
            voiceEngine.setVoiceProfile(initialVoice)
            voiceEngine.setSpeed(preferencesManager.settingsFlow.value.voiceSpeed)
        }
    }

    // --- Voice Invocation Experience ("Hey, Magix!") ---

    fun handleInvocationDetected() {
        _isLivePresenceExpanded.value = true
        sessionManager.startSession(isVoiceInvocation = true)
    }

    // --- Emergency Stop & Cancellation ---

    fun emergencyHalt() {
        sessionManager.emergencyHalt()
        presenceController.collapsePresence()
    }

    fun interruptAssistant() {
        emergencyHalt()
    }

    // --- Confirmation Policy ---

    fun confirmPendingAction(confirmed: Boolean) {
        agentEngine.confirmPendingAction(confirmed)
    }

    // --- Voice & Speech Toggling & Live Presence Pop-up ---

    fun speakToMagixAtHomeScreen() {
        _isLivePresenceExpanded.value = true
        if (!isMicrophonePermissionGranted) {
            requestMicrophonePermission()
            return
        }
        triggerMicListening()
    }

    fun triggerMicToggle() {
        _isLivePresenceExpanded.value = true
        if (sessionManager.isListening.value) {
            sessionManager.emergencyHalt()
        } else {
            triggerMicListening()
        }
    }

    fun triggerMicListening() {
        _isLivePresenceExpanded.value = true
        if (!isMicrophonePermissionGranted) {
            requestMicrophonePermission()
            return
        }
        sessionManager.listenForDirective()
    }

    fun onMicPermissionGranted() {
        _isLivePresenceExpanded.value = true
        triggerMicListening()
    }

    fun onMicPermissionDenied() {
        _isLivePresenceExpanded.value = true
        assistantEngine.setSystemState(AssistantState.IDLE)
    }

    fun submitDirectiveDirectly(text: String) {
        _isLivePresenceExpanded.value = true
        sessionManager.processDirective(text)
    }

    fun invokeHeyMagixExperience() {
        _isLivePresenceExpanded.value = true
        if (!isMicrophonePermissionGranted) {
            requestMicrophonePermission()
            return
        }
        sessionManager.startSession(isVoiceInvocation = true)
    }

    // --- Directive Dispatch & Execution ---

    fun sendUserMessage(text: String) {
        val clean = text.trim()
        if (clean.isBlank()) return

        // Interruption check
        if (clean.equals("magix, stop", ignoreCase = true) || clean.equals("stop", ignoreCase = true)) {
            emergencyHalt()
            return
        }

        // Invocation check
        if (clean.equals("hey magix", ignoreCase = true) || clean.equals("hey, magix!", ignoreCase = true)) {
            handleInvocationDetected()
            return
        }

        sessionManager.processDirective(clean)
    }

    fun setDirectAssistantState(state: AssistantState) {
        sessionManager.setSystemState(state)
    }

    fun clearChat() {
        sessionManager.clearChat()
    }

    // --- Automations Actions ---

    fun createAutomation(
        title: String,
        description: String,
        schedule: String,
        category: String,
        actions: List<String>
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) return
        val routine = AutomationRoutine(
            id = "auto_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}",
            title = cleanTitle,
            description = description.trim().ifBlank { "Custom user routine" },
            schedule = schedule.trim().ifBlank { "On Demand" },
            isEnabled = true,
            actionsSummary = if (actions.isEmpty()) listOf(cleanTitle) else actions,
            category = category.trim().ifBlank { "Custom" },
            lastExecutedTime = null
        )
        viewModelScope.launch {
            automationRepository.insertRoutine(routine)
            assistantEngine.setSystemState(AssistantState.SUCCESS)
            delay(800)
            assistantEngine.setSystemState(AssistantState.IDLE)
        }
    }

    fun deleteAutomation(id: String) {
        viewModelScope.launch {
            automationRepository.deleteRoutine(id)
        }
    }

    fun clearAllAutomations() {
        viewModelScope.launch {
            automationRepository.clearAllRoutines()
        }
    }

    fun toggleAutomation(id: String, isEnabled: Boolean) {
        viewModelScope.launch {
            automationRepository.toggleRoutine(id, isEnabled)
        }
    }

    fun executeRoutineNow(routine: AutomationRoutine) {
        sendUserMessage("Execute routine: ${routine.title}")
    }

    // --- Reminders Actions ---

    fun addReminder(title: String, timeDesc: String) {
        viewModelScope.launch {
            val due = System.currentTimeMillis() + 3600000L * 2
            reminderRepository.createReminder(title, due, timeDesc)
            assistantEngine.setSystemState(AssistantState.SUCCESS)
            delay(800)
            assistantEngine.setSystemState(AssistantState.IDLE)
        }
    }

    fun deleteReminder(id: Long) {
        viewModelScope.launch {
            reminderRepository.deleteReminder(id)
        }
    }

    // --- Memory Actions ---

    fun addMemory(key: String, value: String, category: String) {
        val cleanKey = key.trim()
        val cleanVal = value.trim()
        val cleanCat = category.trim().ifBlank { "Preferences" }
        if (cleanKey.isBlank() || cleanVal.isBlank()) return

        viewModelScope.launch {
            memoryRepository.insertMemory(
                MemoryItem(
                    key = cleanKey,
                    value = cleanVal,
                    category = cleanCat,
                    timestamp = System.currentTimeMillis()
                )
            )
            assistantEngine.setSystemState(AssistantState.SUCCESS)
            delay(800)
            assistantEngine.setSystemState(AssistantState.IDLE)
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            memoryRepository.deleteMemory(id)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            memoryRepository.clearAllMemories()
        }
    }

    fun clearAuditLogs() {
        viewModelScope.launch {
            auditLogger.clearLogs()
        }
    }

    // --- Settings & Preferences Actions ---

    fun updateDisplayName(name: String) {
        preferencesManager.updateDisplayName(name)
    }

    fun updateFormOfAddress(form: String) {
        preferencesManager.updateFormOfAddress(form)
    }

    fun selectVoice(profile: MagixVoiceProfile) {
        preferencesManager.updateSelectedVoice(profile.internalId)
        viewModelScope.launch {
            voiceEngine.setVoiceProfile(profile)
        }
    }

    fun setVoiceSpeed(speed: VoiceSpeed) {
        preferencesManager.updateVoiceSpeed(speed.speed)
        viewModelScope.launch {
            voiceEngine.setSpeed(speed.speed)
        }
    }

    fun auditionVoice(profile: MagixVoiceProfile) {
        viewModelScope.launch {
            voiceEngine.auditionVoice(profile)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        preferencesManager.updateThemeMode(mode)
    }

    fun toggleLivePresence(enabled: Boolean) {
        preferencesManager.updateLivePresenceEnabled(enabled)
    }
}
